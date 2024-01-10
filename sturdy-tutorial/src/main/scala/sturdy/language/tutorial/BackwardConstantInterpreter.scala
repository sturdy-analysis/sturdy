package sturdy.language.tutorial

import sturdy.values.{Changed, Combine, Finite, Join, MayMust, MaybeChanged, Unchanged, Widening}
import sturdy.data.{MayJoin, WithJoin}
import sturdy.values.{CombineMayMust, finitely}
import sturdy.data.{CombineUnit, JoinMap, WidenFiniteKeyMap, finiteUnit}
import sturdy.IsSound
import sturdy.fix
import sturdy.Soundness

import scala.collection.mutable.ListBuffer
import BackwardSignInstances.*
import sturdy.fix.{Fixpoint, StackConfig}

import sturdy.effect.EffectStack
import sturdy.values.{CombineMayMust, finitely}
import ConstantInstances.*


class BackwardConstantInterpreter extends BackGenericInterpreter[Const] {
  val failure = new CollectedFailures
  val effectStack: EffectStack = new EffectStack(List())

  // Creating instances of the necessary components
  val numericOps = new ConstantNumericOps(using failure, effectStack)
  val invertOps = new ConstantInvertOps(using failure, effectStack)
  val unifiable = new ConstantUnifiable()
  val backJoin = new ConstantBackJoin()
  val widen    = new ConstantWidener()

}