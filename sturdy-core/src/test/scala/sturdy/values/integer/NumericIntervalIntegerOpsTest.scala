package sturdy.values.integer

import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import sturdy.data.NoJoin
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.util.{*, given}
import sturdy.values.floating.FloatSpecials
import sturdy.values.{Finite, Top}

import math.Ordered.orderingToOrdered

given NumericIntervalIsInterval[I: Ordering]: IsInterval[I, NumericInterval[I]] with
  override def constant(i: I): NumericInterval[I] = NumericInterval(i, i)
  override def interval(low: I, high: I, floatSpecials: FloatSpecials): NumericInterval[I] = NumericInterval(low, high)

given IntegerOps[Int, Int] = ConcreteIntegerOps(using new ConcreteFailure())
given IntegerOps[Long, Long] = ConcreteLongOps(using new ConcreteFailure())

class NumericIntervalIntIntegerOpsTest extends IntegerOpsTest[Int,NumericInterval[Int]](
  specials = List(Int.MinValue, -1, 0, 1, Int.MaxValue),
  makeIntegerOps = () => {
    given failure: Failure = new CollectedFailures[FailureKind]
    given Finite[FailureKind] with {}
    given effectState: EffectStack = EffectStack(failure)
    (NumericIntervalIsInterval[Int], new NumericIntervalIntegerOps[Int], SoundnessNumericInterval)
  }
)

class NumericIntervalLongIntegerOpsTest extends IntegerOpsTest[Long,NumericInterval[Long]](
  specials = List(Long.MinValue, -1, 0, 1, Long.MaxValue),
  makeIntegerOps = () => {
    given failure: Failure = new CollectedFailures[FailureKind]
    given Finite[FailureKind] with {}
    given effectState: EffectStack = EffectStack(failure)
    (NumericIntervalIsInterval[Long], new NumericIntervalIntegerOps[Long], SoundnessNumericInterval)
  }
)