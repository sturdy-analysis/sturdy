package sturdy.language.tip_xdai.arithmetic.sign

import sturdy.data.WithJoin
import sturdy.language.tip_xdai.arithmetic.GenericInterpreter
import sturdy.language.tip_xdai.core.{TypeError, Value}
import sturdy.language.tip_xdai.core.abstractions.TopValue
import sturdy.values.Topped
import sturdy.values.integer.{IntSign, IntegerOps, LiftedIntegerOps}
import sturdy.values.ordering.{LiftedOrderingOps, OrderingOps}
import sturdy.language.tip_xdai.core.abstractions.BoolValue
import sturdy.values.integer.{SignIntegerOps, SignOrderingOps}


trait SignInterpreter[Addr] extends GenericInterpreter[Value, WithJoin]:
  def extract(intValue: Value): IntSign = intValue match
    case TopValue => IntSign.TopSign
    case SignIntValue(sign) => sign
    case _ => failure(TypeError, s"Expected SignIntValue but got $this")

  val intOps: IntegerOps[Int, Value] = new LiftedIntegerOps[Int, Value, IntSign](extract, SignIntValue.apply)

  val intOrderingOps: OrderingOps[Value, Value] = new LiftedOrderingOps[Value, Value, IntSign, Topped[Boolean]](extract, BoolValue.apply)

