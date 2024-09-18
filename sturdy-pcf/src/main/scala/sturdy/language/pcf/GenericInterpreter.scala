package sturdy.language.pcf

import sturdy.data.MayJoin
import sturdy.effect.{EffectList, EffectStack}
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.fix
import sturdy.util.{IntLabel, Labeled}
import sturdy.values.Finite
import sturdy.values.booleans.BooleanBranching
import sturdy.values.closures.ClosureOps
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.{EqOps, OrderingOps}

import scala.collection.immutable.List

enum PCFFailure extends FailureKind:
  case UnboundVariable
  case TypeError
given Finite[PCFFailure] with {}

import PCFFailure.*

trait GenericInterpreter[V, Env, Addr, J[_] <: MayJoin[_]]:

  // value operations
  val intOps: IntegerOps[Int, V]
  val eqOps: EqOps[V, V]
  val orderingOps: OrderingOps[V, V]
  val branchOps: BooleanBranching[V, V]
  val closureOps: ClosureOps[String, Exp, Env, V, V]

  // effect operations
  val failure: Failure
  val environment: ClosableEnvironment[String, Addr, Env, J]
  val store: Store[Addr, V, J]
  val input: UserInput[V]

  private val effectStack: EffectStack = new EffectStack(EffectList(failure, environment, store, input))
  given EffectStack = effectStack

  def newAddr(e: Exp) : Addr

  // joins
  implicit def jv: J[V]

  // fixpoint
  enum FixIn:
    case Enter(e: Exp)
    
  given finiteFixIn: Finite[FixIn] with {}

  type Fixed = FixIn => V
  val fixpoint: EffectStack ?=> fix.Fixpoint[FixIn, V]
  
  private lazy val fixed = {
    fixpoint {
      case FixIn.Enter(f) => eval_open(f)
    }
  }

  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  // interpreter

  private var toplevelDefs: Map[String, Exp] = Map()

  def eval_open(e: Exp)(using Fixed): V = e match
    case Exp.Var(name) =>
      environment.lookup(name).flatMap(store.read).getOrElse(
        toplevelDefs.get(name).map(eval).getOrElse(
          failure(UnboundVariable, name)
        )
      )
    case Exp.Num(n) => 
      intOps.integerLit(n)
    case Exp.BinOpApp(op, e1, e2) =>
      val v1 = eval(e1)
      val v2 = eval(e2)
      op match
        case BinOp.Add => intOps.add(v1, v2)
        case BinOp.Sub => intOps.sub(v1, v2)
        case BinOp.Mul => intOps.mul(v1, v2)
        case BinOp.Eq => eqOps.equ(v1, v2)
        case BinOp.Gt => orderingOps.gt(v1, v2)
    case Exp.Read =>
      input.read()
    case Exp.If(cond, thn, els) =>
      val c = eval(cond)
      branchOps.boolBranch(c, eval(thn), eval(els))

    case Exp.Lam(x, body) =>
      val env = environment.closeEnvironment
      closureOps.closureValue(x, body, env)

    case l@Exp.App(fun, arg) =>
      val cl = eval(fun)
      closureOps.invokeClosure(cl) {
        case (x, body, env) => environment.scoped {
          val a = eval(arg)
          environment.loadClosedEnvironment(env)
          val addr = newAddr(l)
          environment.bind(x, addr)
          store.write(addr, a)
          enter(body)
        }
      }
    case l@Exp.Rec(f, body) =>
      body match
        case Exp.Lam(x, body_r) => environment.scoped {
          val addr = newAddr(l)
          environment.bind(f, addr)
          store.write(addr, closureOps.closureValue(x, body_r, environment.closeEnvironment))
          eval(body)
        }
        case _ => failure(TypeError, "")

  def eval(e: Exp)(using rec: Fixed): V = eval_open(e)
  def enter(f: Exp)(using rec: Fixed): V = rec(FixIn.Enter(f))

  def evalProgram(p: Program): V = external {
    p.definitions.get("main") match
      case None => throw new IllegalArgumentException(s"Program without main expression")
      case Some(exp) =>
        toplevelDefs = p.definitions
        eval(exp)
  }
