package sturdy.values.ordering

import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.{Finite, Topped}
import sturdy.values.integer.{NumericInterval, NumericIntervalOrderingOps}

given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(List(failure))

class NumericIntervalOrderingOpsTest extends OrderingOpsTest[NumericInterval[Int], Topped[Boolean]](
  100,
  makeOrderingOps = new NumericIntervalOrderingOps[Int] with IntervalOrderingOps[NumericInterval[Int], Topped[Boolean]] {
    override def integerLit(i: Int): NumericInterval[Int] = NumericInterval(i,i)
    override def interval(low: Int, high: Int): NumericInterval[Int] = NumericInterval(low,high)
    override def getBool(b: Topped[Boolean]): Topped[Boolean] = b
  }
)