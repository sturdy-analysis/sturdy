package sturdy.language.tip

import sturdy.data.{NoJoin, noJoin, unit}
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.print.CPrint
import sturdy.effect.store.CStore
import sturdy.effect.userinput.CUserInput
import sturdy.fix
import sturdy.language.tip.Interpreter
import sturdy.language.tip.Function
import sturdy.language.tip.*
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}

object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]

  override type VBool = Boolean
  override type VInt = Int
  override type VRef = Option[Addr]
  override type VFun = Function
  override type VRecord = Map[Field, Value]

  override def topInt: VInt = throw new UnsupportedOperationException
  override def topReference(using Instance): VRef = throw new UnsupportedOperationException
  override def topFun(using Instance): VFun = throw new UnsupportedOperationException
  override def topRecord: VRecord = throw new UnsupportedOperationException
  override def topBool: Boolean = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using failure: Failure): Boolean = v match
    case Value.IntValue(i) => i != 0
    case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")
  override def boolean(b: Boolean): Value = Value.IntValue(if (b) 1 else 0)

  given Structural[VRecord] with {}

  override type Addr = Int
  type Environment = Map[String, Value]
  type Store = Map[Int, Value]

  class Instance(initEnvironment: Environment, initStore: Store, nextInput: () => Value) extends GenericInstance:

    def newInstance: Instance = new Instance(initEnvironment, initStore, nextInput)
    override def jv: NoJoin[Value] = implicitly

    override val failure: ConcreteFailure = new ConcreteFailure
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val referenceOps: ReferenceOps[WasmReference, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: ConcreteCallFrame[Unit, String, Value] = new ConcreteCallFrame((), initEnvironment)
    override val store: CStore[Addr, Value] = new CStore(initStore)
    override val alloc: CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
    override val print: CPrint[Value] = new CPrint
    override val input: CUserInput[Value] = new CUserInput(nextInput)

    override val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut[Value]]

  def apply(initEnvironment: Environment, initStore: Store, nextInput: () => Value): Instance =
    new Instance(initEnvironment, initStore, nextInput)
