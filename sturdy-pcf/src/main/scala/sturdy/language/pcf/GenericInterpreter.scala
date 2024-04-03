package sturdy.language.pcf

import sturdy.data.MayJoin
import sturdy.effect.{Effect, EffectStack}
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.environment.CyclicEnvironment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.userinput.UserInput
import sturdy.fix
import sturdy.language.pcf.PCFFailure
import sturdy.util.{IntLabel, Labeled}
import sturdy.values.Finite
import sturdy.values.booleans.BooleanBranching
import sturdy.values.closures.ClosureOps
import sturdy.values.integer.IntegerOps
import sturdy.values.relational.{EqOps, OrderingOps}

import scala.collection.immutable.List
import PCFFailure.*

case object UnboundVariable extends FailureKind
case object TypeError extends FailureKind

enum PCFFailure extends FailureKind:
  case UnboundVariable
  case UserError
  case TypeError
given Finite[PCFFailure] with {}


trait GenericInterpreter[V, Env, J[_] <: MayJoin[_]]:

  // value operations
  val intOps: IntegerOps[Int, V]; import intOps.*
  val eqOps: EqOps[V, V]; import eqOps.*
  val orderingOps: OrderingOps[V, V]; import orderingOps.*
  val branchOps: BooleanBranching[V, V]; import branchOps.*
  val closureOps: ClosureOps[String, Exp, Env, V, V]; import closureOps.*

  // effect operations
  val failure: Failure
  val environment: CyclicEnvironment[String, V, J] with ClosableEnvironment[String, V, Env, J]
  val input: UserInput[V]

  val effectStack: EffectStack = new EffectStack(List(failure, environment, input))
  given EffectStack = effectStack

  // joins
  implicit def jv: J[V]

  // fixpoint
  type Fixed = Exp => V
  val fixpoint: EffectStack ?=> fix.Fixpoint[Exp, V]

  private lazy val fixed = fixpoint(eval_open)

//  private lazy val fixed = {
//    fixpoint {
//      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
//      case FixIn.Run(s) => run_open(s); FixOut.Run()
//      case FixIn.EnterFunction(f) => FixOut.ExitFunction({
//        run(f.body); eval(f.ret)
//      })
//    }
//  }
//
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  // interpreter

  private var toplevelDefs: Map[String, Exp] = Map()

  def eval_open(e: Exp)(using Fixed): Fixed ?=> V = e match
    case Exp.Var(name) =>
      environment.lookup(name).getOrElse(
        toplevelDefs.get(name).map(eval).getOrElse(
          failure(UnboundVariable, name)
        )
      )
    case Exp.Num(n) => intOps.integerLit(n)
    case Exp.BinOpApp(op, e1, e2) =>
      val v1 = eval(e1)
      val v2 = eval(e2)
      op match
        case BinOp.Add => intOps.add(v1, v2)
        case BinOp.Sub => intOps.sub(v1, v2)
        case BinOp.Mul => intOps.mul(v1, v2)
        case BinOp.Eq => eqOps.equ(v1, v2)
        case BinOp.Gt => orderingOps.gt(v1, v2)
    case Exp.Read => input.read()
    case Exp.If(cond, thn, els) =>
      val c = eval(cond)
      branchOps.boolBranch(c, eval(thn), eval(els))
    case Exp.Lam(x, body) =>
      val env = environment.closeEnvironment
      closureOps.closureValue(x, body, env)
    case Exp.App(fun, arg) =>
      val cl = eval(fun)
      closureOps.invokeClosure(cl) {
        case (x, body, env) => environment.scoped {
          val a = eval(arg)
          environment.loadClosedEnvironment(env)
          environment.bind(x, a)
          eval(body)
        }
      }
    case Exp.Rec(f, body) =>
      lazy val rec: V = {
        environment.bindLazy(f, rec)
        eval(body)
      }
      rec

  def eval(e: Exp)(using rec: Fixed): V =
    println(s"Hi???")
    rec(e)

  def evalProgram(p: Program): V = external {
    p.definitions.get("main") match
      case None => throw new IllegalArgumentException(s"Program without main expression")
      case Some(exp) =>
        toplevelDefs = p.definitions
        eval(exp)
  }
