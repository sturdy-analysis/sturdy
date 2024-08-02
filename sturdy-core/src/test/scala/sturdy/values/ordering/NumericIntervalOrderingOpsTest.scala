package sturdy.values.ordering

import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.{Finite, Structural, Top, Topped}
import sturdy.values.integer.{IntegerOps, NumericInterval, NumericIntervalOrderingOps, StrictIntegerOps, given}
import sturdy.values.ordering.{*, given}
import sturdy.util.{*, given}

class NumericIntervalTestingOrderingOps[I](using ordering: Ordering[I])
  extends NumericIntervalOrderingOps[I]
    with TestingOrderingOps[I, NumericInterval[I], Topped[Boolean]]:
  override def getBool(b: Topped[Boolean]): Topped[Boolean] = b

class NumericIntervalIntOrderingOpsTest extends OrderingOpsTest[Int, NumericInterval[Int], Topped[Boolean]](
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeOrderingOps = new NumericIntervalTestingOrderingOps
)

class NumericIntervalLongOrderingOpsTest extends OrderingOpsTest[Long, NumericInterval[Long], Topped[Boolean]](
  specials = List(Long.MinValue, -1, 0, 1, Long.MaxValue),
  makeOrderingOps = new NumericIntervalTestingOrderingOps
)