package sturdy.language.tip_xdai

import com.sun.jdi.BooleanValue
import sturdy.Executor
import sturdy.data.{MayJoin, NoJoin, noJoin}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.{Allocator, CAllocatorIntIncrement}
import sturdy.effect.callframe.{ConcreteCallFrame, DecidableCallFrame, MutableCallFrame}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.print.{CPrint, Print}
import sturdy.effect.store.{CStore, Store}
import sturdy.effect.userinput.{CUserInput, UserInput}
import sturdy.fix.{Combinator, ConcreteFixpoint, Contextual, Fixpoint}
import sturdy.language.tip_xdai.core.{AllocationSite, Call, CoreGenericInterpreter, FixIn, FixOut, FunValue, Function, Value}
import sturdy.language.tip_xdai.references.concrete.ConcreteAddr
import sturdy.values.booleans.BooleanBranching
import sturdy.values.functions.{FunctionOps, *, given}
import sturdy.values.ordering.EqOps
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{concreteRecordOps, *, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{*, given}
import sturdy.language.tip_xdai.record.concrete.ConcreteInterpreter as RecordConcreteInterp
import sturdy.language.tip_xdai.arithmetic.concrete.{IntValue, ConcreteInterpreter as ArithmeticConcreteInterp}
import sturdy.language.tip_xdai.references.concrete.ConcreteInterpreter as ReferencesConcreteInterp

case class ConcreteInterpreter(nextInput: () => Value) extends CoreGenericInterpreter[Value, NoJoin]
  with ArithmeticConcreteInterp
  with RecordConcreteInterp
  with ReferencesConcreteInterp:

  private def unliftFun(v: Value): FunValue = v match
    case f: FunValue => f
    case _ => throw IllegalStateException(s"Expected concrete function, but got $v")
  
  override def jv: NoJoin[Value] = implicitly

  override lazy val failure: ConcreteFailure = new ConcreteFailure

  override val eqOps: EqOps[Value, Value] = new EqOps[Value, Value]:
    override def equ(v1: Value, v2: Value): Value = if (v1 == v2) IntValue(1) else IntValue(0)
    override def neq(v1: Value, v2: Value): Value = if (v1 != v2) IntValue(1) else IntValue(0)

  override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = new FunctionOps[Function, Seq[Value], Value, Value]:
    def funValue(fun: Function): Value = FunValue(fun)
    def invokeFun(fun: Value, a: Seq[Value])(invoke: (Function, Seq[Value]) => Value): Value = invoke(unliftFun(fun).f, a)
  
  override val branchOps: BooleanBranching[Value, Unit] = new BooleanBranching[Value, Unit]:
    override def boolBranch(v: Value, thn: => Unit, els: => Unit): Unit = v match
      case IntValue(0) => thn
      case IntValue(_) => els
      //case BoolValue(b) => if (b) thn else els

  // References & Records
  override val store: CStore[ConcreteAddr, Value] = new CStore(Map.empty)
  override val alloc: CAllocatorIntIncrement[AllocationSite] = new CAllocatorIntIncrement
  
  override val callFrame: ConcreteCallFrame[String, String, Value, Call] = new ConcreteCallFrame("$main", Iterable.empty)
  override val print: CPrint[Value] = new CPrint
  override val input: CUserInput[Value] = new CUserInput(nextInput)

  override val fixpoint = new ConcreteFixpoint[FixIn, FixOut[Value]] {
    override protected def contextInsensitive: Contextual[Ctx, FixIn, FixOut[Value]] ?=> Combinator[FixIn, FixOut[Value]] =
      (f: FixIn => FixOut[Value]) => (in: FixIn) =>
        try {
          f(in)
        } catch {
          case e: StackOverflowError => failure.stackOverflow(e)
          case e => throw e
        }
  }

