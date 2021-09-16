package sturdy.language.tip

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

  override def topInt(using Instance): VInt = throw new UnsupportedOperationException
  override def topReference(using Instance): VRef = throw new UnsupportedOperationException
  override def topFun(using Instance): VFun = throw new UnsupportedOperationException
  override def topRecord(using Instance): VRecord = throw new UnsupportedOperationException

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

  class Instance(effects: Effects)
    extends GenericInstance with GenericInterpreter(effects):

    final def vintOps: IntOps[VInt] = implicitly
    final def vcompareOps: CompareOps[VInt, VBool] = implicitly
    final def vintEqOps: EqOps[VInt, VBool] = implicitly
    final def vrefEqOps: EqOps[VRef, VBool] = implicitly
    final def vfunEqOps: EqOps[VFun, VBool] = implicitly
    final def vrecEqOps: EqOps[VRecord, VBool] = implicitly
    final def vfunOps: FunctionOps[Function, Value, Value, VFun] = implicitly
    final def vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final def vrecOps: RecordOps[String, Value, VRecord] = implicitly

    override val phi: GenericPhi[Value] = fix.identity[FixIn, FixOut[Value]]

  def apply(initEnvironment: Environment, initStore: Store, nextInput: () => Value): Instance =
    val effects = new Effects(initEnvironment, initStore, nextInput)
    new Instance(effects)
