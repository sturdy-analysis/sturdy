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


class BackwardSignInterpreter extends BackGenericInterpreter[Sign] {
  val failure = new CollectedFailures
  val effectStack: EffectStack = new EffectStack(List())

  // Creating instances of the necessary components
  val numericOps = new SignNumericOps(using failure, effectStack)
  val invertOps = new SignInvertOps(using failure, effectStack)
  val unifiable = new SignUnifiable()
  val backJoin = new SignBackJoin()
  val widen    = new SignWidener()

}


//class BackwardSignInterpreter extends BackGenericInterpreter[Sign]:
//  val failure = new CollectedFailures
//  val effectStack: EffectStack = new EffectStack(List())
//
//  // Creating instances of the necessary components
//  val numericOps = new SignNumericOps(using failure, effectStack)
//  val invertOps = new SignInvertOps(using failure, effectStack)
//  val unifiable = new SignUnifiable()
//  val backJoin = new SignBackJoin()
//
//  // effect stack
//  override val jv: WithJoin[Sign] = MayJoin.WithJoin(CombineSign[Widening.No], effectStack)
//
//  override val fixpoint: Fixpoint[BFixIn[Sign], BFixOut[Sign]] = new fix.ContextInsensitiveFixpoint[BFixIn[Sign], BFixOut[Sign]] {
//    // we need to configure the context insensitive fixpoint algorithm by providing a fixpoint combinator
//    // we choose to apply the topmost iteration strategy to while loop statements (all other statements can't diverge)
//    override protected def contextInsensitive: fix.Contextual[Unit, BFixIn[Sign], BFixOut[Sign]] ?=> fix.Combinator[BFixIn[Sign], BFixOut[Sign]] =
//      fix.filter(isLoop, fix.iter.topmost(StackConfig.StackedCfgNodes()))
//
//    private def isLoop(dom: BFixIn[Sign]): Boolean = dom match
//      case BFixIn.BRun(Stm.While(_,_),_) => true
//      case _ => false
//
//    // For simplicity we define Strings (used as keys into our store) as finite, because a program can only have finitely
//    // many different variables.
//    given Finite[String] with {}
//
//  }