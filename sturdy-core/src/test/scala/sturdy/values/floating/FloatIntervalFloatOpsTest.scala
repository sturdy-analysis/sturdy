package sturdy.values.floating

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.utils.TestIntervalOps
import sturdy.values.floating.{FloatInterval, IntervalFloatOps}
import sturdy.values.{Finite, Top}
given Ordering[Float] = scala.math.Ordering.Float.TotalOrdering
given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(failure)

class FloatTestIntervalOps extends TestIntervalOps[Float, FloatInterval]:
  override def constant(i: Float): FloatInterval = FloatInterval(i,i)
  override def interval(low: Float, high: Float) = FloatInterval(low,high)

  override def shouldContain(n: FloatInterval, m: Float): Assertion =
    if(n.l <= m && m <= n.h)
      succeed
    else
      fail(s"$n does not contain $m")

  override def shouldEqual(n: FloatInterval, l: Float, u: Float): Assertion =
    if (n.l == l && n.h == u)
      succeed
    else
      fail(s"$n does not equal [$l,$u]")


class FloatIntervalFloatOpsTest extends FloatOpsTest[Float,FloatInterval](
  minValue = Float.MinValue,
  maxValue = Float.MaxValue,
  makeFloatOps = new FloatTestIntervalOps with IntervalFloatOps {}
)(using
  org.scalacheck.Arbitrary.arbFloat,
  org.scalacheck.Gen.Choose.chooseFloat,
  scala.math.Ordering.Float.TotalOrdering,
  scala.math.Numeric.FloatIsFractional,
  ConcreteFloatOps
)