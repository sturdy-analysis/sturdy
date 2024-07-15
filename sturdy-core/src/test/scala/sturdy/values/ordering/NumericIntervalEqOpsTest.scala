package sturdy.values.ordering

import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.integer.{IntegerOps, NumericInterval, NumericIntervalOrderingOps, StrictIntegerOps, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{Finite, Structural, Top, Topped}
import sturdy.util.{*,given}

class NumericIntervalIntervalEqOps[I](using ordering: Ordering[I])
  extends NumericIntervalEqOps[I]
    with IntervalEqOps[I, NumericInterval[I], Topped[Boolean]]:
  override def getBool(b: Topped[Boolean]): Topped[Boolean] = b

given Structural[Int] with {}

class NumericIntervalIntEqOpsTest extends EqOpsTest[Int, NumericInterval[Int], Topped[Boolean]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeEqOps = new NumericIntervalIntervalEqOps
)

class NumericIntervalLongEqOpsTest extends EqOpsTest[Long, NumericInterval[Long], Topped[Boolean]](
  minValue = Long.MinValue,
  maxValue = Long.MaxValue,
  makeEqOps = new NumericIntervalIntervalEqOps
)