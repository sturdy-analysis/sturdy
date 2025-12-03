package sturdy.language.tip_xdai.core

import sturdy.data.MayJoin.NoJoin
import sturdy.data.{MayJoin, noJoin}
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.{DecidableCallFrame, MutableCallFrame}
import sturdy.effect.failure.{DivergingKind, Failure, FailureKind, assert}
import sturdy.effect.print.Print
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.effect.{Effect, EffectList, EffectStack}
import sturdy.fix
import sturdy.language.tip_xdai.core.{Assert, Assign, Assignable, Block, Call, Eq, Error, Exp, FixIn, FixOut, Function, If, Input, Output, Program, Stm, TipFailure, Var, While}
import sturdy.util.{Label, given}
import sturdy.values.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps

import scala.collection.immutable.ArraySeq

trait AllocationSite

trait TipFailure extends FailureKind
case object UnboundVariable extends TipFailure
case object UserError extends TipFailure
case object TypeError extends TipFailure
case object VariableReferencesNotSupported extends TipFailure
case object StackOverflow extends TipFailure with DivergingKind

given Finite[TipFailure] with {}

enum FixIn:
  case Eval(e: Exp)
  case Run(s: Stm)
  case EnterFunction(f: Function)

  override def toString: String = this match
    case Eval(e) => s"eval $e"
    case Run(s) => s"run $s"
    case EnterFunction(fun) => s"enter ${fun.name}"

enum FixOut[V]:
  case Eval(v: V)
  case Run()
  case ExitFunction(ret: V)

given finiteFixIn: Finite[FixIn] with {}
//given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

given Ordering[FixIn] = {
  case (FixIn.Eval(e1), FixIn.Eval(e2)) => Ordering[Label].compare(e1.label, e2.label)
  case (FixIn.Run(s1), FixIn.Run(s2)) => Ordering[Label].compare(s1.label, s2.label)
  case (FixIn.EnterFunction(f1), FixIn.EnterFunction(f2)) => Ordering[Function].compare(f1,f2)
}

given CombineFixOut[V, W <: Widening](using w: Combine[V, W]): Combine[FixOut[V], W] with
  override def apply(out1: FixOut[V], out2: FixOut[V]): MaybeChanged[FixOut[V]] = (out1, out2) match
    case (FixOut.Eval(v1), FixOut.Eval(v2)) => Combine[V, W](v1, v2).map(FixOut.Eval.apply)
    case (FixOut.Run(), FixOut.Run()) => Unchanged(FixOut.Run())
    case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => Combine[V, W](v1, v2).map(FixOut.ExitFunction.apply)
    case _ => throw new IllegalArgumentException(s"Cannot combine outputs of different kind, $out1 and $out2")

/**
 * The generic interpreter for the Tip language (https://github.com/cs-au-dk/TIP).
 *
 * The generic interpreter captures the core semantics of the language and
 * is used to derive abstract interpreters and as well as the concrete interpreter.
 * This is useful for sharing code between different abstract interpreters,
 * but also simplifies the soundness proof as no reasoning about the generic interpreter
 * is necessary (https://doi.org/10.1145/3236767).
 * @param V The type of values
 * @param J Abstracts over if the interpreter joins or not.
 *           - The concrete interpreter defines `J` as [[NoJoin]], meaning the interpreter does not join.
 *           - The abstract interpreters defines `J` to be [[WithJoin]], meaning the interpreter does join.
 */
trait CoreGenericInterpreter[V, J[_] <: MayJoin[_]]:

  // fixpoint
  val fixpoint: EffectStack ?=> fix.Fixpoint[FixIn, FixOut[V]]
  type Fixed = FixIn => FixOut[V]

  implicit def jv: J[V]

  // value components
  val eqOps: EqOps[V, V]; import eqOps.*
  val functionOps: FunctionOps[Function, Seq[V], V, V]; import functionOps.*

  implicit val branchOps: BooleanBranching[V, Unit]; import branchOps.*

  // effect components
  val callFrame: DecidableCallFrame[String, String, V, Call] with MutableCallFrame[String, String, V, Call, NoJoin]
  val print: Print[V]
  val input: UserInput[V]
  implicit lazy val failure: Failure

  // Factory method for effect stacks
  def newEffectStack(effects: => Effect,
                     inEffects: PartialFunction[Any, Effect],
                     outEffects: PartialFunction[Any, Effect]): EffectStack =
    new EffectStack(effects, inEffects, outEffects)

  def allEffects: Set[Effect] = Set(callFrame, print, input, failure)
  def inEffect(fixIn: Any): Set[Effect] = fixIn match
    case _: FixIn.Run | _: FixIn.EnterFunction => Set(callFrame, print, failure) // store
    case _: FixIn.Eval => Set(callFrame, input, failure) // store, alloc
    case _ => allEffects
  def outEffect(fixIn: Any): Set[Effect] = fixIn match
    case _: FixIn.Run | _: FixIn.EnterFunction => Set(callFrame, print, failure) // store
    case _: FixIn.Eval => Set(callFrame, failure) // alloc
    case _ => allEffects

  val effectStack: EffectStack = newEffectStack(EffectList(allEffects.toSeq:_*) , { fixIn =>
    EffectList(inEffect(fixIn).toSeq:_*)
  }, { fixIn =>
    EffectList(outEffect(fixIn).toSeq:_*)
  })

  given EffectStack = effectStack

  // analysis state
  protected var functions: Map[String, Function] = Map()
  def getFunctions: Iterable[Function] = functions.values

  def eval_open(e: Exp)(using Fixed): V = e match
    case Input() => input.read()
    case Var(x) => functions.get(x) match
      case Some(fun) => funValue(fun)
      case None => callFrame.getLocalByName(x).getOrElse(failure(UnboundVariable, x))
    case Eq(e1, e2) => equ(eval(e1), eval(e2))
    case site@Call(fun, args) =>
      invokeFun(eval(fun), args.map(eval(_)))(call(site))
//      val addr = callFrame.getLocalByName(x).getOrElse(failure(UnboundVariable, x))
//      unmanagedRefValue(addr)

  def run_open(s: Stm)(using Fixed): Unit = s match
    case Assign(lhs: Assignable, e: Exp) =>
      val v = eval(e)
      assign(lhs, v)
    case If(cond: Exp, thn: Stm, els: Option[Stm]) =>
      boolBranch(eval(cond), run(thn), els.map(run(_)).getOrElse(()))
    case While(cond, body) =>
      boolBranch(eval(cond), {run(body); run(s)}, {})
    case Block(body) =>
      body.foreach(run(_))
    case Output(e) =>
      print(eval(e))
    case a@Assert(e) =>
      assert(eval(e), a)
    case Error(e) =>
      failure(UserError, eval(e).toString)

  def assign(lhs: Assignable, v: V)(using Fixed): Unit = lhs match
    case AVar(x) =>
      callFrame.setLocalByName(x, v).getOrElse(failure(UnboundVariable, x))

  def call(site: Call)(fun: Function, args: Seq[V])(using Fixed): V =
    val locals: Iterable[(String, Option[V])] =
      fun.params.zip(args.map(Some.apply)) ++
      fun.locals.map(x => (x, None))
    callFrame.withNew(fun.name, locals, site) {
      enterFunction(fun)
    }

  inline def eval(e: Exp)(using rec: Fixed): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  inline def run(s: Stm)(using rec: Fixed): Unit = rec(FixIn.Run(s)) match {case FixOut.Run() => (); case _ => throw new IllegalStateException()}
  private def enterFunction(fun: Function)(using rec: Fixed): V = rec(FixIn.EnterFunction(fun)) match {case FixOut.ExitFunction(v) => v; case _ => throw new IllegalStateException() }

  private lazy val fixed = {
    fixpoint {
      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
      case FixIn.Run(s) => run_open(s); FixOut.Run()
      case FixIn.EnterFunction(f) => FixOut.ExitFunction({run(f.body); eval(f.ret)})
    }
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  def execute(p: Program): V = external {
    functions = p.funs.map(f => f.name -> f).toMap
    val main = functions("main")
    val args = main.params.map(_ => Input())
    eval(Call(Var("main"), args))
  }
