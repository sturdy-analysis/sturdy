package sturdy.language.tutorial

import sturdy.values.{Changed, Combine, Join, MayMust, MaybeChanged, Unchanged, Widening}
import sturdy.data.{WithJoin,MayJoin}
import sturdy.values.CombineMayMust
import sturdy.data.CombineUnit

import scala.collection.mutable.ListBuffer

class SignInterpreter extends GenericInterpreterFirstShot[Sign, WithJoin]:
  override val numericOps: NumericOps[Sign] = new SignNumericOps
  override val branching: Branching[Sign, Unit] = new SignBranching[Unit]
  override val store: Store[Sign, WithJoin] = new SignStore
  override val failure: Failure = new AFailure

  override val jv: WithJoin[Sign] = MayJoin.WithJoin(CombineSign[Widening.No], effectStack)