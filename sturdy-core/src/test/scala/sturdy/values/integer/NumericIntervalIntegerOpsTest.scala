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

  override def shouldContain(n: NumericInterval[I], m: I): Assertion =
    if(n.low <= m && m <= n.high)
      succeed
    else
      fail(s"$n did not contain $m")

  override def shouldEqual(n: NumericInterval[I], l: I, u: I): Assertion =
    if (n.low == l && n.high == u)
      succeed
    else
      fail(s"$n did not equal [$l,$u]")

class NumericIntervalIntIntegerOpsTest extends IntegerOpsTest[Int,NumericInterval[Int]](
  (NumericIntervalTestIntervalOps[Int], new NumericIntervalIntegerOps[Int](20))
)

class NumericIntervalLongIntegerOpsTest extends IntegerOpsTest[Long,NumericInterval[Long]](
  (NumericIntervalTestIntervalOps[Long], new NumericIntervalIntegerOps[Long](20))
)