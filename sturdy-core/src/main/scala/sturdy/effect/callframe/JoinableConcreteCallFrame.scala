package sturdy.effect.callframe
import sturdy.{IsSound, Soundness, seqIsSound}
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.{ComputationJoiner, TrySturdy}
import sturdy.values.Join

import scala.reflect.ClassTag

class JoinableConcreteCallFrame[Data, Var, V](initData: Data, initVars: Iterable[(Var, V)])(using Join[V], ClassTag[V]) extends ConcreteCallFrame[Data, Var, V](initData, initVars):
  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new CallFrameJoiner[A])
  private class CallFrameJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = vars
    private var fVars: Array[V] = _

    override def inbetween(): Unit =
      fVars = vars
      vars = snapshot

    override def retainNone(): Unit =
      vars = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      vars = fVars

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      vars = joinWith(fVars)
  }

  private def joinWith(other: Array[V]): Array[V] =
    if (vars.length != other.length)
      throw IllegalStateException()
    vars.zip(other).map(Join[V](_,_).get)

  def joinedDecidableCallFrameIsSound[cData, cV](c: ConcreteCallFrame[cData, Var, cV])(using vSoundness: Soundness[cV,V], dSoundness: Soundness[cData,Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.data, data)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$getFrameNames")
    val aVals = getLocals
    val cVals = c.getLocals
    seqIsSound.isSound(cVals, aVals)
