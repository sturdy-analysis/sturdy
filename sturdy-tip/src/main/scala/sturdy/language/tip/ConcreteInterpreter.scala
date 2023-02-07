package sturdy.language.tip

import sturdy.data.{NoJoin, unit, noJoin}
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.print.CPrint
import sturdy.effect.store.CStore
import sturdy.effect.userinput.CUserInput
import sturdy.fix
import sturdy.language.tip.Function
import sturdy.language.tip.*
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{*, given}
import sturdy.util.Label

object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]

  override type VBool = Boolean
  override type VInt = Int
  override type VRef = Option[Addr]
  override type VFun = Function
  override type VRecord = Map[Field, Value]

  override def topInt(using Instance): VInt = throw new UnsupportedOperationException
  override def topReference(using Instance): VRef = throw new UnsupportedOperationException
  override def topFun(using Instance): VFun = throw new UnsupportedOperationException
  override def topRecord: VRecord = throw new UnsupportedOperationException
  override def topBool: Boolean = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using inst: Instance): Boolean = v match
    case Value.BoolValue(b) => b
    case Value.IntValue(i) => i != 0
    case _ => inst.failure(TipFailure.TypeError, s"Expected Boolean but got $this")

  override def asInt(v: Value)(using inst: Instance): Int = v match
    case Value.BoolValue(b) => if b then 1 else 0
    case Value.IntValue(i) => i
    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  given Structural[VRecord] with {}

  override type Addr = Int
  type Environment = Map[String, Value]
  type Store = Map[Int, Value]

  class Instance(nextInput: () => Value) extends GenericInstance:

    def newInstance: Instance = new Instance(nextInput)
    override def jv: NoJoin[Value] = implicitly

    override val failure: ConcreteFailure = new ConcreteFailure
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: ConcreteCallFrame[String, String, Value] = new ConcreteCallFrame("$main", Iterable.empty)
    override val store: CStore[Addr, Value] = new CStore(Map.empty)
    override val alloc: CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
    override val print: CPrint[Value] = new CPrint
    override val input: CUserInput[Value] = new CUserInput(nextInput)

    override val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut[Value]]

  def apply(nextInput: () => Value): Instance =
    new Instance(nextInput)
