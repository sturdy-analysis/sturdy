package sturdy.effect.callframe

import sturdy.data.*

import scala.reflect.ClassTag

import ConcreteCallFrame.*

trait ConcreteCallFrame[Data, Var, V](using ClassTag[V]) extends DecidableMutableCallFrame[Data, Var, V]:

  def initialCallFrameData: Data
  def initialCallFrameVars: Iterable[(Var, V)]

  protected var data: Data = initialCallFrameData
  protected var vars: Array[V] = _
  protected var names: Map[Var, Int] = _

  private def setVars(newvars: Iterable[(Var, V)]) = {
    val builder = Map.newBuilder[Var, Int]
    vars = Array.ofDim(newvars.size)
    var i = 0
    for ((name, v) <- newvars) {
      builder += name -> i
      vars(i) = v
      i += 1
    }
    names = builder.result()
  }
  setVars(initialCallFrameVars)

  def getFrameData: Data = data
  def getFrameNames: Map[Var, Int] = names
  def getFrameVars: Vars[V] = vars.toSeq

  protected def setFrameVars(vs: Vars[V]): Unit =
    var ix = 0
    for (v <- vs) {
      vars(ix) = v
      ix += 1
    }

  def getCallFrame: (Data, Seq[V]) = (data, getFrameVars)
  protected def setCallFrame(s: (Data, Seq[V])): Unit =
    data = s._1
    setFrameVars(s._2)

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

  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A = {
    val snapData = this.data
    val snapNames = this.names
    val snapVars = this.vars
    this.data = d
    setVars(vars)
    try f finally {
      this.data = snapData
      this.names = snapNames
      this.vars = snapVars
    }
  }


object ConcreteCallFrame:
  type Vars[V] = Seq[V]

