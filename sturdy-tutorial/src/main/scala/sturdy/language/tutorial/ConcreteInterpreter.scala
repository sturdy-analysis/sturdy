package sturdy.language.tutorial

import sturdy.data.JOptionC
import sturdy.data.MayJoin.NoJoin
import sturdy.data.noJoin
import sturdy.effect.{Stateless, SturdyFailure}
import sturdy.effect.failure.FailureKind
import sturdy.fix.Concrete
import GenericInterpreter.*

/*
 * We now implement a concrete interpreter for the while language.
 * The concrete interpreter can be derived from the generic interpreter. All we need to do is to instantiate
 * it with the concrete component instances.
 * For illustration purpose we provide concrete implementations for all components in ConcreteInstances. This step can
 * be skipped when reusing existing components from the Sturdy library.
 */
class ConcreteInterpreter extends GenericInterpreter[Int, NoJoin] with Concrete[FixIn, FixOut[Int]]:
  override val numericOps: NumericOps[Int] = new CNumericOps()
  override val branching: Branching[Int, Unit] = new CBranching[Unit]()
  override val store: Store[Int, NoJoin] = new CStore()
  override val failure: Failure = new CFailure()

  override val jv: NoJoin[Int] = noJoin