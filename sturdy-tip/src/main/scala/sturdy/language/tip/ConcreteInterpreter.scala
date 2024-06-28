package sturdy.language.tip

import sturdy.data.{NoJoin, noJoin, unit}
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.print.CPrint
import sturdy.effect.store.CStore
import sturdy.effect.userinput.{CUserInput, ImplicitlyNamedUserInput}
import sturdy.fix
import sturdy.fix.{Combinator, Contextual}
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
  override type VRef = Reference[Addr]
  override type VFun = Function
  override type VRecord = Map[Field, Value]

  override def topInt(using Instance): VInt = throw new UnsupportedOperationException
  override def topReference(using Instance): VRef = throw new UnsupportedOperationException
  override def topFun(using Instance): VFun = throw new UnsupportedOperationException
  override def topRecord: VRecord = throw new UnsupportedOperationException
  override def topBool(using Instance): Boolean = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using inst: Instance): Boolean = v match
    case Value.BoolValue(b) => b
    case _ => inst.failure(TipFailure.RuntimeTypeError, s"Expected Boolean but got $v")

  override def asInt(v: Value)(using inst: Instance): Int = v match
    case Value.IntValue(i) => i
    case _ => inst.failure(TipFailure.RuntimeTypeError, s"Expected Int but got $v")

  given Structural[VRecord] with {}
  given Structural[Addr] with {}

  override type Addr = (AllocationSite,Int)
  type Environment = Map[String, Value]
  type Store = Map[Addr, Value]

  class Instance(nextInput: () => Value) extends GenericInstance:

    def newInstance: Instance = new Instance(nextInput)
    override def jv: NoJoin[Value] = implicitly

    override val failure: ConcreteFailure = new ConcreteFailure
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val boolOps: BooleanOps[Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly
    override val instanceOfOps: InstanceOfOps[ConcreteInterpreter.Value, TypeAnno] = new InstanceOfOps:
      override def isInstanceOf(v: Value, ta: TypeAnno): Value = (ta, v) match
        case (TypeAnno.Int, Value.IntValue(_)) => v
        case (TypeAnno.Bool, Value.BoolValue(_)) => v
        case (TypeAnno.Unknown, _) => v
        case _ => failure.fail(TipFailure.RuntimeTypeError, s"$v not instance of $ta")

    // C = {0, true}
    // ta = Int
    // C' = {conc.isInstanceOf(c, ta) | c in C} = {0, fail}
    // alpha(C') = MaybeFailing(IntType)
    
    // alpha(C) = Top
    // abs.isInstanceOf(alpha(C), ta) = MaybeFailing(IntType)
    
    // forall C, ta. 
    //    alpha({conc.isInstanceOf(c, ta) | c in C}) <= 
    //      abs.isInstanceOf(alpha(C), ta)
    
    // RuntimeTypeError <= TypeError
    
    override val callFrame: ConcreteCallFrame[String, String, Value, Exp.Call] = new ConcreteCallFrame("$main", Iterable.empty)
    override val store: CStore[Addr, Value] = new CStore(Map.empty)
    override val alloc: CAllocatorIntIncrement[AllocationSite] = new CAllocatorIntIncrement
    override val print: CPrint[Value] = new CPrint
    override val input: CUserInput[Value] = new CUserInput(nextInput) with ImplicitlyNamedUserInput[Value]

    override val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut[Value]] {
      override protected def contextInsensitive: Contextual[Ctx, FixIn, FixOut[Value]] ?=> Combinator[FixIn, FixOut[Value]] =
        (f: FixIn => FixOut[Value]) => (in: FixIn) =>
          try {
            f(in)
          } catch {
            case e: StackOverflowError => failure.fail(TipFailure.StackOverflow, e.toString)
            case e => throw e
          }
    }

  def apply(nextInput: () => Value): Instance =
    new Instance(nextInput)
