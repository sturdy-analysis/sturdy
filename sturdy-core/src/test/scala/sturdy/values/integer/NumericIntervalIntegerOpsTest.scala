package sturdy.values.integer

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.utils.{*, given}
import sturdy.values.{Finite, Top}

import math.Ordered.orderingToOrdered


given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(failure)

given NumericIntervalTestIntervalOps[I: Ordering]: TestIntervalOps[I, NumericInterval[I]] with
  override def constant(i: I): NumericInterval[I] = NumericInterval(i, i)

  override def interval(low: I, high: I): NumericInterval[I] = NumericInterval(low, high)

  override def contains(n: NumericInterval[I], m: I): Boolean =
    n.low <= m && m <= n.high

  override def equals(n: NumericInterval[I], l: I, u: I): Boolean =
    n.low == l && n.high == u

class NumericIntervalIntIntegerOpsTest extends IntegerOpsTest[Int,NumericInterval[Int]](
  (NumericIntervalTestIntervalOps[Int], new NumericIntervalIntegerOps[Int](20))
)

class NumericIntervalLongIntegerOpsTest extends IntegerOpsTest[Long,NumericInterval[Long]](
  (NumericIntervalTestIntervalOps[Long], new NumericIntervalIntegerOps[Long](20))
)