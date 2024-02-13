package sturdy.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.Finite

given failure: Failure = new CollectedFailures[FailureKind]
given Finite[FailureKind] with {}
given effectState: EffectStack = EffectStack(List(failure))

class NumericIntervalIntegerOpsTest extends IntegerOpsTest[Int,NumericInterval[Int]](
  100,
  makeIntegerOps = new NumericIntervalIntegerOps[Int](20) with IntervalIntegerOps[Int, NumericInterval[Int]] {
    override def integerLit(i: Int): NumericInterval[Int] = NumericInterval(i, i)
    override def interval(low: Int, high: Int): NumericInterval[Int] = NumericInterval(low, high)
    override def getBounds(n: NumericInterval[Int]): (Int, Int) = (n.low, n.high)
  }
)