package sturdy.language.whilelang

import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.environment.CEnvironment
import sturdy.effect.store.CStore
import sturdy.effect.failure.{Failure, CFailure}
import sturdy.fix.CFixpoint
import sturdy.values.booleans.{_, given}
import sturdy.values.doubles.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.given

object ConcreteInterpreter:
  enum Value:
    case BooleanValue(b: Boolean)
    case DoubleValue(d: Double)

    def asBoolean: Boolean = this match
      case BooleanValue(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
    def asDouble: Double = this match
      case DoubleValue(d) => d
      case _ => throw new IllegalArgumentException(s"Expected Double but got $this")
  
  import Value._

  type Addr = Int
  type Environment = Map[String, Int]
  type Store = Map[Int, Value]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends CBoolBranching[Value]
    with CEnvironment[String, Int](initEnvironment)
    with CStore[Int, Value](initStore)
    with CAllocationIntIncrement[Statement.Assign]
    with CFailure
  type Fix = CFixpoint[Statement, Unit]

  def apply(initEnvironment: Environment, initStore: Store): ConcreteInterpreter = {
    val effects = new Effects(initEnvironment, initStore)
    val fixpoint = new CFixpoint[Statement, Unit]

    given BooleanOps[Value] = new LiftedBooleanOps[Value, Boolean](_.asBoolean, BooleanValue.apply)
    given DoubleOps[Value] = new LiftedDoubleOps[Value, Double](_.asDouble, DoubleValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Double, Boolean](_.asDouble, BooleanValue.apply)
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(b1 == b2)
        case (DoubleValue(d1), DoubleValue(d2)) => BooleanValue(d1 == d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(b1 != b2)
        case (DoubleValue(d1), DoubleValue(d2)) => BooleanValue(d1 != d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

    new ConcreteInterpreter(using effects)(using fixpoint)
  }

import ConcreteInterpreter.*

class ConcreteInterpreter
  (using effectOps: Effects)
  (using fix: Fix)
  (using boolOps: BooleanOps[Value], doubleOps: DoubleOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value])
  extends GenericInterpreter[Value, Addr, Effects, Fix]
