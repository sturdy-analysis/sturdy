package sturdy.language.tutorial

import sturdy.values.{Changed, Combine, Finite, Join, MayMust, MaybeChanged, Unchanged, Widening}
import sturdy.data.{MayJoin, WithJoin}
import sturdy.values.{CombineMayMust, finitely}
import sturdy.data.{CombineUnit, finiteUnit}
import sturdy.data.WidenFiniteKeyMap
import sturdy.{IsSound, fix}
import sturdy.fix.{Combinator, ContextInsensitive, Contextual}
import sturdy.fix.context.{Sensitivity, none}
import sturdy.Soundness

import scala.collection.mutable.ListBuffer
import GenericInterpreter.*

/*
 * We can now derive a sign interpreter from the generic interpreter by instantiating all components and
 * configuring a fixpoint algorithm. We extend ContextInsensitive in order to implement a context insensitive
 * fixpoint algorithm.
 */
class SignInterpreter extends GenericInterpreter[Sign, WithJoin] with ContextInsensitive[FixIn, FixOut[Sign]]:
  // value components
  override val numericOps: SignNumericOps = new SignNumericOps
  override val branching: SignBranching[Unit] = new SignBranching[Unit]
  // effect components
  override val store: SignStore = new SignStore
  override val failure: AFailure = new AFailure
  // effect stack
  override val jv: WithJoin[Sign] = MayJoin.WithJoin(CombineSign[Widening.No], effectStack)

  // we need to configure the context insensitive fixpoint algorithm by providing a fixpoint combinator
  // we choose to apply the topmost iteration strategy to while loop statements (all other statements can't diverge)
  override protected def contextInsensitive: Contextual[Unit, FixIn, FixOut[Sign]] ?=> Combinator[FixIn, FixOut[Sign]] =
    fix.filter(isLoop, fix.iter.topmost)

  private def isLoop(dom: FixIn): Boolean = dom match
    case FixIn.Run(Stm.While(_,_)) => true
    case _ => false

  // For simplicity we define Strings (used as keys into our store) as finite, because a program can only have finitely
  // many different variables.
  given Finite[String] with {}

// We define what is means for a sign interpreter to be sound with respect to a concrete interpreter
given Soundness[ConcreteInterpreter, SignInterpreter] with
  def isSound(c: ConcreteInterpreter, a: SignInterpreter): IsSound = a.store.storeIsSound(c.store)