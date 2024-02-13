package sturdy.values.integer

import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.{Finite, Top}

given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(List(failure))

class NumericIntervalIntervalIntegerOps[I]
  (using 
   ordering: Ordering[I], 
   ops: IntegerOps[I, I],
   strict: StrictIntegerOps[I, I, NoJoin], 
   num: Numeric[I], 
   t: Top[NumericInterval[I]])
  extends NumericIntervalIntegerOps[I](20)
    with IntervalIntegerOps[I, NumericInterval[I]]:
  override def integerLit(i: I): NumericInterval[I] = NumericInterval(i, i)

  override def interval(low: I, high: I): NumericInterval[I] = NumericInterval(low, high)

  override def getBounds(n: NumericInterval[I]): (I, I) = (n.low, n.high)

class NumericIntervalIntIntegerOpsTest extends IntegerOpsTest[Int,NumericInterval[Int]](
  minValue = Integer.MIN_VALUE,
  maxValue = Integer.MAX_VALUE,
  makeIntegerOps = new NumericIntervalIntervalIntegerOps
)

class NumericIntervalLongIntegerOpsTest extends IntegerOpsTest[Long,NumericInterval[Long]](
  minValue = Long.MinValue,
  maxValue = Long.MaxValue,
  makeIntegerOps = new NumericIntervalIntervalIntegerOps
)