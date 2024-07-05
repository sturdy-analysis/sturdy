package sturdy.values.floating

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.util.{*,given}
import sturdy.values.floating.{FloatInterval, IntervalFloatOps}
import sturdy.values.{Finite, Top}
given Ordering[Float] = scala.math.Ordering.Float.TotalOrdering

class FloatTestIntervalOps extends TestIntervalOps[Float, FloatInterval]:
  override def constant(i: Float): FloatInterval = FloatInterval(i,i)
  override def interval(low: Float, high: Float) = FloatInterval(low,high)

  override def contains(n: FloatInterval, m: Float): Boolean =
    n.l <= m && m <= n.h

  override def equals(n: FloatInterval, l: Float, u: Float): Boolean =
    n.l == l && n.h == u


class FloatIntervalFloatOpsTest extends FloatOpsTest[Float,FloatInterval](
  makeFloatOps = (new FloatTestIntervalOps, FloatIntervalFloatOps, SoundnessFloatInterval)
)(using
  org.scalacheck.Arbitrary.arbFloat,
  org.scalacheck.Gen.Choose.chooseFloat,
  FloatBounds,
  scala.math.Ordering.Float.TotalOrdering,
  scala.math.Numeric.FloatIsFractional,
  ConcreteFloatOps
)