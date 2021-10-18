package sturdy.effect.callframe
import sturdy.effect.{ComputationJoiner, ComputationJoinerWithSuper, TrySturdy}
import sturdy.values.Join

import scala.reflect.ClassTag
import scala.collection.mutable.Builder

trait JoinedCallFrame[Data, Var, V](_data: Data, _vars: Map[Var, V])(using Join[V], ClassTag[V]) extends GenericCallFrame[Data, Var, V]:
  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    val snapshot = vars
    var fVars: Map[Var,V] = null

    override def inbetween_(): Unit =
      fVars = vars
      vars = snapshot

    override def retainOnlyFirst_(fRes: TrySturdy[A]): Unit =
      if (fRes.isSuccess)
        vars = fVars
      else
        vars = snapshot

    override def retainOnlySecond_(gRes: TrySturdy[A]): Unit =
      if (!gRes.isSuccess)
        vars = snapshot

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (gRes.isSuccess) {
        if (fRes.isSuccess)
          vars = joinWith(fVars)
        // else nothing
      } else if (fRes.isSuccess) {
        vars = fVars
      } else {
        vars = snapshot
      }
  }

  private def joinWith(other: Map[Var,V]): Map[Var,V] =
    if (vars.keySet != other.keySet)
      throw IllegalStateException()
    val newVars: Builder[(Var,V), Map[Var,V]] = Map.newBuilder
    vars.keySet.foreach { key =>
      newVars += key -> Join[V](vars(key), other(key)).get
    }
    newVars.result