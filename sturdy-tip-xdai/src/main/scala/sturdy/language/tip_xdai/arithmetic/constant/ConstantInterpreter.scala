package sturdy.language.tip_xdai.arithmetic.constant

import sturdy.data.WithJoin
import sturdy.language.tip_xdai.arithmetic.GenericInterpreter
import sturdy.language.tip_xdai.core.Value
import sturdy.values.Topped
import sturdy.values.integer.{ConcreteIntegerOps, IntegerOps, LiftedIntegerOps, ToppedIntegerOps}
import sturdy.values.ordering.{LiftedOrderingOps, OrderingOps, ToppedCertainOrderingOps}
import sturdy.values.integer.given_OrderingOps_Int_Boolean

private def constantIntFromToppedInt(value: Topped[Int]): Value = value match
  case Topped.Top => Value.Top
  case Topped.Actual(i) => ConstantIntV(i)

private def toppedIntAsConstantInt(v: Value): Topped[Int] = v match
  case ConstantIntV(i) => Topped.Actual(i)
  case Value.Top => Topped.Top
  case _ => throw IllegalArgumentException(s"Can not convert $v to int")

trait ConstantInterpreter[Addr] extends GenericInterpreter[Value, WithJoin]:
  val intOps: IntegerOps[Int, Value] = new LiftedIntegerOps[Int, Value, Topped[Int]](toppedIntAsConstantInt, constantIntFromToppedInt)(
    using ToppedIntegerOps[Int, Int](using implicitly, failure, effectStack)
  )

  val intOrderingOps: OrderingOps[Value, Value] = ??? //new LiftedOrderingOps[Value, Topped[Boolean], Topped[Int], Topped[Boolean]](toppedIntAsConstantInt, identity)
