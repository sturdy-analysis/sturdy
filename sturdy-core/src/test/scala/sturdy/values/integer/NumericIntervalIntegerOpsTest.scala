package sturdy.values.integer

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.util.{*, given}
import sturdy.values.{Finite, Top}

import math.Ordered.orderingToOrdered

given NumericIntervalIsInterval[I: Ordering]: IsInterval[I, NumericInterval[I]] with
  override def constant(i: I): NumericInterval[I] = NumericInterval(i, i)

  override def interval(low: I, high: I): NumericInterval[I] = NumericInterval(low, high)

given IntegerOps[Int, Int] = ConcreteIntegerOps(using new ConcreteFailure())
given IntegerOps[Long, Long] = ConcreteLongOps(using new ConcreteFailure())

class NumericIntervalIntIntegerOpsTest extends IntegerOpsTest[Int,NumericInterval[Int]]({
  given failure: Failure = new CollectedFailures[FailureKind]
  given Finite[FailureKind] with {}
  given effectState: EffectStack = EffectStack(failure)
  (new NumericIntervalIntegerOps[Int](20), SoundnessNumericInterval)
})

class NumericIntervalLongIntegerOpsTest extends IntegerOpsTest[Long,NumericInterval[Long]]({
  given failure: Failure = new CollectedFailures[FailureKind]
  given Finite[FailureKind] with {}
  given effectState: EffectStack = EffectStack(failure)
  (new NumericIntervalIntegerOps[Long](20), SoundnessNumericInterval)
})