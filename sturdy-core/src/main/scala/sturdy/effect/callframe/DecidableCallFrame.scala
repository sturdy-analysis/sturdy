package sturdy.effect.callframe

import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Concrete
import sturdy.effect.TrySturdy
import sturdy.values.{Join, Widen}
import sturdy.{IsSound, Soundness, seqIsSound}


import scala.reflect.ClassTag

trait DecidableCallFrame[Data, Var, V, Site] extends CallFrame[Data, Var, V, Site, NoJoin]

abstract class DecidableMutableCallFrame[Data, Var, V, Site](initData: Data, initVars: Iterable[(Var, Option[V])])(using ClassTag[V]) extends MutableCallFrame[Data, Var, V, Site, NoJoin], DecidableCallFrame[Data, Var, V, Site]:
  protected var _data: Data = initData
  protected var _callSite: Option[Site] = None
  protected var vars: Array[V] = _
  protected var names: Map[Var, Int] = _

  def setVars(newVars: Iterable[(Var, Option[V])]): Unit = {
    val builder = Map.newBuilder[Var, Int]
    vars = Array.ofDim(newVars.size)
    var i = 0
    for ((name, v) <- newVars) {
      builder += name -> i
      v.foreach(vars.update(i, _))
      i += 1
    }
    names = builder.result()
  }
  setVars(initVars)

  def getVars: Array[V] = vars.clone()

  def data: Data = _data
  def callSite: Option[Site] = _callSite
  def getFrameNames: Map[Var, Int] = names

  def getLocal(ix: Int): JOptionC[V] =
    if (ix >= 0 && ix < vars.length) {
      val v = vars(ix)
      if (v == null)
        JOptionC.none
      else
        JOptionC.Some(v)
    }
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

  def withNew[A](d: Data, vars: Iterable[(Var, Option[V])], site: Site)(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapVars = this.vars
    this._data = d
    this._callSite = Some(site)
    setVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.vars = snapVars
    }
  }

  def isSound[cData, cV](c: ConcreteCallFrame[cData, Var, cV, Site])(using vSoundness: Soundness[cV,V], dSoundness: Soundness[cData,Data]): IsSound =
    val dataIsSound = dSoundness.isSound(c.data, data)
    if (dataIsSound.isNotSound)
      return dataIsSound
    if (getFrameNames != c.getFrameNames)
      return IsSound.NotSound(s"Variable names in call frame differ: concrete=${c.getFrameNames}, abstract=$getFrameNames")
    val aVals = vars.toList
    val cVals = c.vars.toList
    seqIsSound.isSound(cVals, aVals)


class ConcreteCallFrame[Data, Var, V, Site](initData: Data, initVars: Iterable[(Var, Option[V])])(using ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V, Site](initData, initVars), Concrete

class JoinableDecidableCallFrame[Data, Var, V, Site](initData: Data, initVars: Iterable[(Var, Option[V])])(using Join[V], Widen[V], ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V, Site](initData, initVars):
  override type State = List[V]
  override def getState: State = if(vars == null) List() else vars.toList
  override def setState(s: State): Unit =
    if(vars == null) vars = s.toArray
    else  s.zipWithIndex.foreach { case (v, ix) => vars(ix) = v }
  override def setBottom: Unit = vars = null
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(CallFrameJoiner[A])
  private class CallFrameJoiner[A] extends ComputationJoiner[A] {
    private val before = vars
    private var afterFirst: Array[V] = _

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = vars
      vars = before

    override def retainNone(): Unit =
      setBottom

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      vars = afterFirst

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (vars.length != afterFirst.length)
        throw IllegalStateException()
      vars = afterFirst.zip(vars).map(Join[V](_,_).get)
  }

  override def toString: String =
    getState.toString
