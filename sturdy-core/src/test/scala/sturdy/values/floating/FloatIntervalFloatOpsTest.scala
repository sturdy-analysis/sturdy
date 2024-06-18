package sturdy.values.floating

import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.floating.{FloatInterval, IntervalFloatOps}
import sturdy.values.{Finite, Top}
given Ordering[Float] = scala.math.Ordering.Float.TotalOrdering
given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(failure)

class FloatIntervalTestingIntegerOps
  extends IntervalFloatOps
    with TestingFloatOps[Float, FloatInterval]:
  override def floatLit(i: Float): FloatInterval = FloatInterval(i,i)
  override def interval(low: Float, high: Float) = FloatInterval(low,high)
  override def getBounds(iv: FloatInterval) = (iv.l, iv.h)


class FloatIntervalFloatOpsTest extends FloatOpsTest[Float,FloatInterval](
  minValue = Float.NegativeInfinity,
  maxValue = Float.PositiveInfinity,
  makeFloatOps = new FloatIntervalTestingIntegerOps
)(using
  org.scalacheck.Arbitrary.arbFloat,
  org.scalacheck.Gen.Choose.chooseFloat,
  scala.math.Ordering.Float.TotalOrdering,
  scala.math.Numeric.FloatIsFractional,
  ConcreteFloatOps
)