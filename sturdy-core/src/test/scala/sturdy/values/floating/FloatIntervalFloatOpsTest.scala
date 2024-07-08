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

given FloatIsInterval: IsInterval[Float, FloatInterval] with
  override def constant(i: Float): FloatInterval = FloatInterval(i,i)
  override def interval(low: Float, high: Float) = FloatInterval(low,high)


class FloatIntervalFloatOpsTest extends FloatOpsTest[Float,FloatInterval](
  makeFloatOps = (FloatIntervalFloatOps, SoundnessFloatInterval)
)