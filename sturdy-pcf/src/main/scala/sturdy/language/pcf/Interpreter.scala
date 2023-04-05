package sturdy.language.pcf

import sturdy.data.MayJoin
import sturdy.effect.failure.Failure
import sturdy.values.booleans.BooleanBranching
import sturdy.values.booleans.LiftedBooleanBranching
import sturdy.values.closures.ClosureOps
import sturdy.values.closures.LiftedClosureOps
import sturdy.values.integer.IntegerOps
import sturdy.values.integer.LiftedIntegerOps
import sturdy.values.relational.EqOps
import sturdy.values.relational.LiftedOrderingOps
import sturdy.values.relational.OrderingOps

trait Interpreter:

  type J[A] <: MayJoin[A]

  type VInt
  type VClosure
  type VBoolean
  type Env

  def boolean(b: VBoolean): Value
  def asBoolean(v: Value)(using Failure): VBoolean

  enum Value:
    case Int(i: VInt)
    case Closure(closure: VClosure)

    def asInt(using failure: Failure): VInt = this match
      case Int(i) => i
      case _ => failure(TypeError, s"Expected Int but got $this")
    def asClosure(using Failure): VClosure = this match
      case Closure(closure) => closure
      case _ => Failure(TypeError, s"Expected Closure but got $this")

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