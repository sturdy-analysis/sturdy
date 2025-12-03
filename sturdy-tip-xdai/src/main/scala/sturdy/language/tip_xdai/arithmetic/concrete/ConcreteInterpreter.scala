package sturdy.language.tip_xdai.arithmetic.concrete

import sturdy.data.MayJoin.NoJoin
import sturdy.effect.failure.Failure
import sturdy.language.tip_xdai.arithmetic.GenericInterpreter
import sturdy.language.tip_xdai.core.{TypeError, Value}
import sturdy.values.integer.{ConcreteIntegerOps, IntegerOps, LiftedIntegerOps}
import sturdy.values.ordering.OrderingOps

trait ConcreteInterpreter extends GenericInterpreter[Value, NoJoin]:

  private def fromInt(value: Int): Value = IntValue(value)

  private def asInt(v: Value): Int = v match
    case IntValue(i) => i
    case _ => failure(TypeError, s"Expected Int but got $this")

  val intOps: IntegerOps[Int, Value] = LiftedIntegerOps[Int, Value, Int] (asInt, fromInt)

  val intOrderingOps: OrderingOps[Value, Value] = new OrderingOps[Value, Value]:
    override def lt(v1: Value, v2: Value): Value = if (asInt(v1) < asInt(v2)) IntValue(1) else IntValue(0)
    override def le(v1: Value, v2: Value): Value = if (asInt(v1) <= asInt(v2)) IntValue(1) else IntValue(0)
