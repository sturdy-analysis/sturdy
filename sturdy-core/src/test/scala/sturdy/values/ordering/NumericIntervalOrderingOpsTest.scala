package sturdy.values.ordering

import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.{Finite, Structural, Top, Topped}
import sturdy.values.integer.{IntegerOps, NumericInterval, NumericIntervalOrderingOps, StrictIntegerOps, given}
import sturdy.values.ordering.{*, given}

class NumericIntervalTestingOrderingOps[I](using ordering: Ordering[I])
  extends NumericIntervalOrderingOps[I]
    with TestingOrderingOps[I, NumericInterval[I], Topped[Boolean]]:
  override def integerLit(i: I): NumericInterval[I] = NumericInterval(i,i)
  override def interval(low: I, high: I): NumericInterval[I] = NumericInterval(low,high)
  override def getBool(b: Topped[Boolean]): Topped[Boolean] = b

class NumericIntervalIntOrderingOpsTest extends OrderingOpsTest[Int, NumericInterval[Int], Topped[Boolean]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeOrderingOps = new NumericIntervalTestingOrderingOps
)

class NumericIntervalLongOrderingOpsTest extends OrderingOpsTest[Long, NumericInterval[Long], Topped[Boolean]](
  minValue = Long.MinValue,
  maxValue = Long.MaxValue,
  makeOrderingOps = new NumericIntervalTestingOrderingOps
)