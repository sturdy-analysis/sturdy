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
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeEqOps = () => (NumericIntervalIsInterval[Int], new NumericIntervalIntervalEqOps)
)

class NumericIntervalLongEqOpsTest extends EqOpsTest[Long, NumericInterval[Long], Topped[Boolean]](
  specials = List(Long.MinValue, -1, 0, 1, Long.MaxValue),
  makeEqOps = () => (NumericIntervalIsInterval[Long], new NumericIntervalIntervalEqOps)
)