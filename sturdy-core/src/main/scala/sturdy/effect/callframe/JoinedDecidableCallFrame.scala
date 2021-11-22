package sturdy.effect.callframe
import sturdy.{IsSound, Soundness, seqIsSound}
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.{ComputationJoiner, ComputationJoinerWithSuper, TrySturdy}
import sturdy.values.Join

import scala.reflect.ClassTag

class JoinedDecidableCallFrame[Data, Var, V](initData: Data, initVars: Iterable[(Var, V)])(using Join[V], ClassTag[V]) extends ConcreteCallFrame[Data, Var, V](initData, initVars):
  override def makeComputationJoiner[A]: ComputationJoiner[A] = new CallFrameJoiner[A] 
  class CallFrameJoiner[A] extends ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    private val snapshot = vars
    private var fVars: Array[V] = _

    override def inbetween_(): Unit =
      fVars = vars
      vars = snapshot

    override def retainNone_(): Unit =
      vars = snapshot

    override def retainFirst_(fRes: TrySturdy[A]): Unit =
      vars = fVars

    override def retainSecond_(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      vars = joinWith(fVars)
  }

  private def joinWith(other: Array[V]): Array[V] =
    if (vars.length != other.length)
      throw IllegalStateException()
    vars.zip(other).map(Join[V](_,_).get)

  def joinedDecidableCallFrameIsSound[cData, cV](c: ConcreteCallFrame[cData, Var, cV])(using vSoundness: Soundness[cV,V], dSoundness: Soundness[cData,Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.getFrameData, getFrameData)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$getFrameNames")
    val aVals = getLocals
    val cVals = c.getLocals
    seqIsSound.isSound(cVals, aVals)
