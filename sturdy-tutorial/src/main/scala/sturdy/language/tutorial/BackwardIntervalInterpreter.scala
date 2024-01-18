package sturdy.language.tutorial

import sturdy.values.{Changed, Combine, Finite, Join, MayMust, MaybeChanged, Unchanged, Widening}
import sturdy.data.{MayJoin, WithJoin}
import sturdy.values.{CombineMayMust, finitely}
import sturdy.data.{CombineUnit, JoinMayMap, WidenFiniteKeyMayMap, finiteUnit}
import sturdy.IsSound
import sturdy.fix
import sturdy.Soundness

import scala.collection.mutable.ListBuffer
import BackwardIntervalInstances.*  // Assuming this file contains all the necessary instances for Interval
import sturdy.fix.{Fixpoint, StackConfig}

import sturdy.effect.EffectStack
import sturdy.values.{CombineMayMust, finitely}


class BackwardIntervalInterpreter extends BackGenericInterpreter[Interval] {
  val failure = new CollectedFailures
  val effectStack: EffectStack = new EffectStack(List())

  // Creating instances of the necessary components for Interval
  val numericOps = new IntervalNumericOps(using failure, effectStack)
  val invertOps = new IntervalInvertOps(using failure, effectStack)
  val unifiable = new IntervalUnifiable()
  val backJoin = new IntervalBackJoin()
  val widen = new IntervalWidener()

  // Add other necessary methods and fields specific to Interval here
}
