package sturdy.effect.callframe

import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Concrete
import sturdy.effect.TrySturdy
import sturdy.values.{Join, Widen}
import sturdy.{IsSound, Soundness, seqIsSound}


import scala.reflect.ClassTag

trait DecidableCallFrame[Data, Var, V] extends CallFrame[Data, Var, V, NoJoin]

abstract class DecidableMutableCallFrame[Data, Var, V](initData: Data, initVars: Iterable[(Var, V)])(using ClassTag[V]) extends MutableCallFrame[Data, Var, V, NoJoin], DecidableCallFrame[Data, Var, V]:
  protected var _data: Data = initData
  protected var vars: Array[V] = _
  protected var names: Map[Var, Int] = _

  def setVars(newVars: Iterable[(Var, V)]): Unit = {
    val builder = Map.newBuilder[Var, Int]
    vars = Array.ofDim(newVars.size)
    var i = 0
    for ((name, v) <- newVars) {
      builder += name -> i
      vars(i) = v
      i += 1
    }
    names = builder.result()
  }
  setVars(initVars)

  def data: Data = _data
  def getFrameNames: Map[Var, Int] = names

  def getLocal(ix: Int): JOptionC[V] =
    if (ix >= 0 && ix < vars.length)
      JOptionC.Some(vars(ix))
    else
      JOptionC.none

  def getLocalByName(x: Var): JOptionC[V] = names.get(x) match
    case Some(ix) => getLocal(ix)
    case None => JOptionC.none

  def setLocal(ix: Int, v: V): JOptionC[Unit] =
    if (ix >= 0 && ix < vars.length) {
      vars = vars.updated(ix, v)
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  def setLocalByName(x: Var, v: V): JOption[NoJoin, Unit] = names.get(x) match
    case Some(ix) => setLocal(ix, v)
    case None => JOptionC.none

  def withNew[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapVars = this.vars
    this._data = d
    setVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.vars = snapVars
    }
  }

  def isSound[cData, cV](c: ConcreteCallFrame[cData, Var, cV])(using vSoundness: Soundness[cV,V], dSoundness: Soundness[cData,Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.data, data)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$getFrameNames")
    val aVals = vars.toList
    val cVals = c.vars.toList
    seqIsSound.isSound(cVals, aVals)


class ConcreteCallFrame[Data, Var, V](initData: Data, initVars: Iterable[(Var, V)])(using ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V](initData, initVars), Concrete

class JoinableDecidableCallFrame[Data, Var, V](initData: Data, initVars: Iterable[(Var, V)])(using Join[V], Widen[V], ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V](initData, initVars):
  override type State = List[V]
  override def getState: List[V] = vars.toList
  override def setState(s: List[V]): Unit =
    s.zipWithIndex.foreach { case (v, ix) => vars(ix) = v }
  override def join: Join[List[V]] = implicitly
  override def widen: Widen[List[V]] = implicitly

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
      if (vars.length != fVars.length)
        throw IllegalStateException()
      vars = vars.zip(fVars).map(Join[V](_,_).get)
  }
