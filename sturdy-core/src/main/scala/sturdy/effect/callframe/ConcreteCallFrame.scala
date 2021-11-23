package sturdy.effect.callframe

import sturdy.data.*

import scala.reflect.ClassTag

class ConcreteCallFrame[Data, Var, V](initData: Data, initVars: Iterable[(Var, V)])(using ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V]:

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

  override type State = (Data, List[V])
  override def getState: (Data, List[V]) = (_data, vars.toList)
  override def setState(s: (Data, List[V])): Unit =
    _data = s._1
    setLocals(s._2)

  override type Locals = List[V]
  override def getLocals: List[V] = vars.toList
  override def setLocals(ls: List[V]): Unit =
    ls.zipWithIndex.foreach { case (v, ix) => vars(ix) = v }


