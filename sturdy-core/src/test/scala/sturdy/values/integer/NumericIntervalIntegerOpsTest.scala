package sturdy.values.integer

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.{Finite, Top}

import math.Ordered.orderingToOrdered


given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(failure)

class NumericIntervalTestingIntegerOps[I]
  (using 
   ordering: Ordering[I], 
   ops: IntegerOps[I, I],
   strict: StrictIntegerOps[I, I, NoJoin], 
   num: Numeric[I], 
   t: Top[NumericInterval[I]])
  extends NumericIntervalIntegerOps[I](20)
    with TestingIntegerOps[I, NumericInterval[I]]:
  override def integerLit(i: I): NumericInterval[I] = NumericInterval(i, i)

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
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeIntegerOps = new NumericIntervalTestingIntegerOps
)

class NumericIntervalLongIntegerOpsTest extends IntegerOpsTest[Long,NumericInterval[Long]](
  minValue = Long.MinValue,
  maxValue = Long.MaxValue,
  makeIntegerOps = new NumericIntervalTestingIntegerOps
)