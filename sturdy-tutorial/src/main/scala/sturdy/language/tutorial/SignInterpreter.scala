package sturdy.language.tutorial

import sturdy.values.{Changed, Combine, Join, MayMust, MaybeChanged, Unchanged, Widening}
import sturdy.data.{WithJoin,MayJoin}
import sturdy.values.CombineMayMust
import sturdy.data.CombineUnit

import scala.collection.mutable.ListBuffer

class SignInterpreter extends GenericInterpreterFirstShot[Sign, WithJoin]:
  override val numericOps: SignNumericOps = new SignNumericOps
  override val branching: SignBranching[Unit] = new SignBranching[Unit]
  override val store: SignStore = new SignStore
  override val failure: AFailure = new AFailure

  override val jv: WithJoin[Sign] = MayJoin.WithJoin(CombineSign[Widening.No], effectStack)