package sturdy.language.tip

import sturdy.effect.noJoin
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.callframe.CCallFrame
import sturdy.effect.failure.{Failure, CFailure}
import sturdy.effect.print.CPrint
import sturdy.effect.store.CStore
import sturdy.effect.userinput.CUserInput
import sturdy.fix
import sturdy.language.tip.Interpreter
import sturdy.language.tip.Function
import sturdy.language.tip.GenericInterpreter.*
import sturdy.values.booleans.{_, given}
import sturdy.values.ints.{_, given}
import sturdy.values.functions.{_, given}
import sturdy.values.records.{_, given}
import sturdy.values.references.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.{_, given}

object ConcreteInterpreter extends Interpreter:
  override type VBool = Boolean
  override type VInt = Int
  override type VRef = Option[Addr]
  override type VFun = Function
  override type VRecord = Map[String, Value]

  override def topInt(using Interpreter): VInt = throw new UnsupportedOperationException
  override def topReference(using Interpreter): VRef = throw new UnsupportedOperationException
  override def topFun(using Interpreter): VFun = throw new UnsupportedOperationException
  override def topRecord(using Interpreter): VRecord = throw new UnsupportedOperationException

  override def asBoolean(v: Value): Boolean = v match
    case Value.IntValue(i) => i != 0
    case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
  override def boolean(b: Boolean): Value = Value.IntValue(if (b) 1 else 0)

  given Structural[VRecord] with {}

  override type Addr = Int
  type Environment = Map[String, Int]
  type Store = Map[Int, Value]

  class Effects(initEnvironment: Environment, initStore: Store, nextInput: () => Value)
    extends CBoolBranching[Value]
      with CCallFrame[Unit, String, Int]((), initEnvironment)
      with CStore[Int, Value](initStore)
      with CAllocationIntIncrement[AllocationSite]
      with CPrint[Value]
      with CUserInput[Value](nextInput)
      with CFailure

  def apply(initEnvironment: Environment, initStore: Store, nextInput: () => Value): Instance =
    val effects = new Effects(initEnvironment, initStore, nextInput)
    given Failure = effects
    new Instance(effects)

  class Instance(effects: Effects)(using Failure)
    extends Interpreter with GenericInterpreter(effects):

    final val vintOps: IntOps[VInt] = implicitly
    final val vcompareOps: CompareOps[VInt, VBool] = implicitly
    final val vintEqOps: EqOps[VInt, VBool] = implicitly
    final val vrefEqOps: EqOps[VRef, VBool] = implicitly
    final val vfunEqOps: EqOps[VFun, VBool] = implicitly
    final val vrecEqOps: EqOps[VRecord, VBool] = implicitly
    final val vfunOps: FunctionOps[Function, Value, Value, VFun] = implicitly
    final val vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final val vrecOps: RecordOps[String, Value, VRecord] = implicitly

    override val phi: GenericPhi[Value] = fix.identity[FixIn, FixOut[Value]]
