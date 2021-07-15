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
import sturdy.util.given

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

  def apply(initEnvironment: Environment, initStore: Store) = {
    val effects =
      new  CBoolBranching[Value]
      with CEnvironment[String, Int](initEnvironment)
      with CStore[Int, Value](initStore)
      with CAllocationIntIncrement
      with CFailure
    val fixpoint = new CFixpoint[Statement, Unit]

    given Failure = effects
    given BooleanOps[Value] = new LiftedBooleanOps[Value, Boolean](_.asBoolean, BooleanValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Double, Boolean](_.asDouble, BooleanValue.apply)
    given DoubleOps[Value] = new LiftedDoubleOps[Value, Double](_.asDouble, DoubleValue.apply)
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(b1 == b2)
        case (DoubleValue(d1), DoubleValue(d2)) => BooleanValue(d1 == d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(b1 != b2)
        case (DoubleValue(d1), DoubleValue(d2)) => BooleanValue(d1 != d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

    new GenericInterpreter(using effects)(using fixpoint) {}
  }

