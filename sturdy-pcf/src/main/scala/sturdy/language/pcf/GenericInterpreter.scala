package sturdy.language.pcf

import sturdy.data.MayJoin
import sturdy.effect.{AnalysisState, EffectStack}
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.userinput.UserInput
import sturdy.fix
import sturdy.util.{IntLabel, Labeled}
import sturdy.values.booleans.BooleanBranching
import sturdy.values.closures.ClosureOps
import sturdy.values.integer.IntegerOps
import sturdy.values.relational.{EqOps, OrderingOps}

import scala.collection.immutable.List

case object UnboundVariable extends FailureKind

trait GenericInterpreter[V, Env, J[_] <: MayJoin[_]]:

  // value operations
  val intOps: IntegerOps[Int, V]
  val eqOps: EqOps[V, V]
  val orderingOps: OrderingOps[V, V]
  val branchOps: BooleanBranching[V, V]
  val closureOps: ClosureOps[String, V, Exp, Env, V, V]

  // effect operations
  val fail: Failure
  val environment: ClosableEnvironment[String, V, Env, J]
  val input: UserInput[V]

  val effectStack: EffectStack = new EffectStack(List(fail, environment, input))
  given EffectStack = effectStack

  // joins
  implicit def jv: J[V]

  // fixpoint
  type State = environment.State
  private implicit val analysisState: AnalysisState[Exp, State, State, State] = new AnalysisState {
    override def getInState(dom: Exp): State = environment.getState
    override def setInState(in: State): Unit = environment.setState(in)
    override def getOutState(dom: Exp): State = environment.getState
    override def setOutState(out: State): Unit = environment.setState(out)
    override def getAllState: State = environment.getState
    override def setAllState(all: State): Unit = environment.setState(all)
  }
  
  type Fixed = Exp => V
  val fixpoint: (AnalysisState[Exp, State, State, State], EffectStack) ?=> fix.Fixpoint[Exp, V]

  private lazy val fixed = fixpoint(eval_open)
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)


  // interpreter

  private var toplevelDefs: Map[String, Exp] = Map()

  def eval_open(e: Exp)(using Fixed): V = e match
    case Exp.Var(name) =>
      environment.lookup(name).getOrElse(
        toplevelDefs.get(name).map(eval).getOrElse(
          fail(UnboundVariable, name)
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
      closureOps.closureValue(List(x), body, env)
    case Exp.App(fun, arg) =>
      val cl = eval(fun)
      closureOps.invokeClosure(cl) {
        case (List(), rec@Exp.Rec(f, body), env) => environment.scoped {
          environment.loadClosedEnvironment(env)
          environment.bind(f, cl)
          eval(rec.label @: Exp.App(body, arg))
        }
        case (List(x), body, env) => environment.scoped {
          environment.loadClosedEnvironment(env)
          val a = eval(arg)
          environment.bind(x, a)
          eval(body)
        }
        case c => throw MatchError(c)
      }
    case Exp.Rec(f, body) =>
      closureOps.closureValue(List(), Exp.Rec(f, body), environment.closeEnvironment)

  def eval(e: Exp)(using rec: Fixed): V = rec(e)

  def evalProgram(p: Program): V = external {
    p.definitions.get("main") match
      case None => throw new IllegalArgumentException(s"Program without main expression")
      case Some(exp) =>
        toplevelDefs = p.definitions
        eval(exp)
  }
