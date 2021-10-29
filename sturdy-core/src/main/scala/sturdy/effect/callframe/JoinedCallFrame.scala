package sturdy.effect.callframe
import sturdy.{IsSound, Soundness, seqIsSound}
import sturdy.effect.operandstack.GenericOperandStack
import sturdy.effect.{ComputationJoiner, ComputationJoinerWithSuper, TrySturdy}
import sturdy.values.Join

import scala.reflect.ClassTag

trait JoinedCallFrame[Data, Var, V](using Join[V], ClassTag[V]) extends ConcreteCallFrame[Data, Var, V]:
  override def makeComputationJoiner[A]: ComputationJoiner[A] = new CallFrameJoiner[A] 
  class CallFrameJoiner[A] extends ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    private val snapshot = vars
    private var fVars: Array[V] = _

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

  def joinedCallFrameNumberedIsSound[cData, cV](c: ConcreteCallFrame[cData, Var, cV])(using vSoundness: Soundness[cV,V], dSoundness: Soundness[cData,Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.getFrameData, getFrameData)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$getFrameNames")
    val aVals = getFrameVars
    val cVals = c.getFrameVars
    seqIsSound.isSound(cVals, aVals)
