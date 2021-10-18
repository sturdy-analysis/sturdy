package sturdy.effect.callframe
import sturdy.effect.{ComputationJoiner, ComputationJoinerWithSuper, TrySturdy}
import sturdy.values.Join

import scala.reflect.ClassTag

trait JoinedCallFrameNumbered[Data,V](using Join[V], ClassTag[V]) extends GenericCallFrameNumbered[Data,V]:
  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    val snapshot = vars
    var fVars: Array[V] = null

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

  private def joinWith(other: Array[V]): Array[V] =
    if (vars.length != other.length)
      throw IllegalStateException()
    vars.zip(other).map(Join[V](_,_).get)
    
trait JoinedMutableCallFrameNumbered[Data,V](using ClassTag[V]) extends JoinedCallFrameNumbered[Data,V] with GenericMutableCallFrameNumbered[Data,V] 