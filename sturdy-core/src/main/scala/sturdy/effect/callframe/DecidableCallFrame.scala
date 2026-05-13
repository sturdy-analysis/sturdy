package sturdy.effect.callframe

import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Concrete
import sturdy.effect.TrySturdy
import sturdy.values.{Join, Widen}
import sturdy.{IsSound, Soundness, seqIsSound}

import scala.collection.immutable.{ArraySeq, IntMap}
import scala.reflect.ClassTag

trait DecidableCallFrame[Data, Var, V, Site] extends CallFrame[Data, Var, V, Site, NoJoin]

abstract class DecidableMutableCallFrame[Data, Var, V, Site](initData: Data, initVars: Iterable[(Var, V)])(using ClassTag[V]) extends MutableCallFrame[Data, Var, V, Site, NoJoin], DecidableCallFrame[Data, Var, V, Site]:
  protected var _data: Data = initData
  protected var _callSite: Option[Site] = None
  protected var vars: ArraySeq[V] = _
  protected var names: Map[Var, Int] = _

  def setVars(newVars: Iterable[(Var, V)]): Unit = {
    val namesBuilder = Map.newBuilder[Var, Int]
    val varsBuilder = ArraySeq.newBuilder[V]
    var i = 0
    for ((name, v) <- newVars) {
      namesBuilder += name -> i
      varsBuilder += v
      i += 1
    }
    names = namesBuilder.result()
    vars = varsBuilder.result()
  }
  setVars(initVars)

  def getVars: ArraySeq[V] = vars

  def data: Data = _data
  def callSite: Option[Site] = _callSite
  def getFrameNames: Map[Var, Int] = names

  def getLocal(ix: Int): JOptionC[V] = if (vars.isDefinedAt(ix)) {
    JOptionC.some(vars(ix))
  } else {
    JOptionC.none
  }

  def getLocalByName(x: Var): JOptionC[V] = names.get(x) match
    case Some(ix) => getLocal(ix)
    case None => JOptionC.none

  def setLocal(ix: Int, v: V): JOptionC[Unit] = if (vars.isDefinedAt(ix)) {
      vars = vars.updated(ix, v)
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  def setLocalByName(x: Var, v: V): JOption[NoJoin, Unit] = names.get(x) match
    case Some(ix) => setLocal(ix, v)
    case None => JOptionC.none

  def withNew[A](d: Data, vars: Iterable[(Var, V)], site: Site)(f: => A): A = {
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
    val aVals = vars
    val cVals = c.vars
    seqIsSound(using vSoundness).isSound(cVals, aVals)


class ConcreteCallFrame[Data, Var, V, Site](initData: Data, initVars: Iterable[(Var, V)])(using ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V, Site](initData, initVars), Concrete

class JoinableDecidableCallFrame[Data, Var, V, Site](initData: Data, initVars: Iterable[(Var, V)])(using Join[V], Widen[V], ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V, Site](initData, initVars):
  override type State = ArraySeq[V]
  override def getState: State = if(vars == null) ArraySeq() else vars
  override def setState(s: State): Unit = vars = s
  override def setBottom: Unit = vars = null
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(CallFrameJoiner[A])
  private class CallFrameJoiner[A] extends ComputationJoiner[A] {
    private val before = vars
    private var afterFirst: State = _

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = vars
      vars = before

    override def retainNone(): Unit =
      setBottom

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      vars = afterFirst

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      vars = Join(vars, afterFirst).get
  }

  override def toString: String =
    getState.toString
