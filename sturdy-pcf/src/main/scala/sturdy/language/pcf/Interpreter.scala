package sturdy.language.pcf

import sturdy.data.MayJoin
import sturdy.effect.failure.Failure
import sturdy.values.{Combine, MaybeChanged, PartialOrder, Widen, Widening}
import sturdy.values.booleans.BooleanBranching
import sturdy.values.booleans.LiftedBooleanBranching
import sturdy.values.closures.ClosureOps
import sturdy.values.closures.LiftedClosureOps
import sturdy.values.integer.IntegerOps
import sturdy.values.integer.LiftedIntegerOps
import sturdy.values.ordering.EqOps
import sturdy.values.ordering.LiftedOrderingOps
import sturdy.values.ordering.OrderingOps

trait Interpreter:

  type J[A] <: MayJoin[A]

  type VInt
  type VClosure
  type VBoolean
  type Env

  def boolean(b: VBoolean): Value
  def asBoolean(v: Value)(using Failure): VBoolean

  import PCFFailure.*
  enum Value:
    case Int(i: VInt)
    case Closure(closure: VClosure)

    def asInt(using failure: Failure): VInt = this match
      case Int(i) => i
      case _ => failure(TypeError, s"Expected Int but got $this")
    def asClosure(using Failure): VClosure = this match
      case Closure(closure) => closure
      case _ => Failure(TypeError, s"Expected Closure but got $this")

  given CombineValue[W <: Widening](using Combine[VInt, W], Combine[VClosure, W]): Combine[Value, W] with
    override def apply(v1: Value, v2: Value): MaybeChanged[Value] = (v1, v2) match
      case (Value.Int(i1), Value.Int(i2)) => Combine(i1, i2).map(Value.Int.apply)
      case (Value.Closure(c1), Value.Closure(c2)) => Combine(c1, c2).map(Value.Closure.apply)

  given PartialOrderValue(using PartialOrder[VInt], PartialOrder[VClosure]): PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (Value.Int(i1), Value.Int(i2)) => PartialOrder.lteq(i1, i2)
      case (Value.Closure(c1), Value.Closure(c2)) => PartialOrder.lteq(c1, c2)
      case _ => false

  given ValueIntegerOps(using Failure, IntegerOps[Int, VInt]): IntegerOps[Int, Value] =
    new LiftedIntegerOps(_.asInt, Value.Int.apply)
  given ValueEqOps(using Failure, EqOps[VInt, VBoolean]): EqOps[Value, Value] with
    override def equ(v1: Value, v2: Value): Value = (v1, v2) match
      case (Value.Int(i1), Value.Int(i2)) => boolean(EqOps.equ(i1, i2))
      case _ => Failure(TypeError, s"Cannot compare $v1 and $v2")
    override def neq(v1: Value, v2: Value): Value = (v1, v2) match
      case (Value.Int(i1), Value.Int(i2)) => boolean(EqOps.neq(i1, i2))
      case _ => Failure(TypeError, s"Cannot compare $v1 and $v2")
  given ValueOrderingOps(using Failure, OrderingOps[VInt, VBoolean]): OrderingOps[Value, Value] =
    new LiftedOrderingOps(_.asInt, boolean)
  given ValueBranchOps(using Failure, BooleanBranching[VBoolean, Value]): BooleanBranching[Value, Value] =
    new LiftedBooleanBranching(asBoolean)
  given ValueClosureOps(using Failure, ClosureOps[String, Exp, Env, Value, VClosure]): ClosureOps[String, Exp, Env, Value, Value] =
    new LiftedClosureOps(_.asClosure, Value.Closure.apply)

  type Instance <: GenericInstance
  abstract class GenericInstance extends GenericInterpreter[Value, Env, J]:
    given Instance = this.asInstanceOf[Instance]
