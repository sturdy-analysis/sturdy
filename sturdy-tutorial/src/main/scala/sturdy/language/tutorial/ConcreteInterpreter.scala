package sturdy.language.tutorial

import sturdy.data.JOptionC
import sturdy.data.MayJoin.NoJoin
import sturdy.data.noJoin
import sturdy.effect.{Stateless, SturdyFailure}
import sturdy.effect.failure.FailureKind
import sturdy.fix.ConcreteFixpoint
import GenericInterpreter.*
import sturdy.fix.Fixpoint

/*
 * We now implement a concrete interpreter for the while language.
 * The concrete interpreter can be derived from the generic interpreter. All we need to do is to instantiate
 * it with the concrete component instances.
 * For illustration purpose we provide concrete implementations for all components in ConcreteInstances. This step can
 * be skipped when reusing existing components from the Sturdy library.
 */
class ConcreteInterpreter extends GenericInterpreter[Int, NoJoin]:
  override val stringOps: StringOps[Int] = new CStringOps()
  override val numericOps: CNumericOps = new CNumericOps()
  override val branching: CBranching[Unit] = new CBranching[Unit]()
  override val store: CStore = new CStore()
  override val failure: CFailure = new CFailure()

  override val jv: NoJoin[Int] = noJoin

  override val fixpoint: Fixpoint[FixIn, FixOut[Int]] = new ConcreteFixpoint[FixIn, FixOut[Int]]