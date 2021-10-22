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
    builder.result()
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

  def getLocal(ix: Int): OptionC[V] =
    if (ix >= 0 && ix < vars.length)
      OptionC.Some(vars(ix))
    else
      OptionC.none

  def getLocalByName(x: Var): OptionC[V] = names.get(x) match
    case Some(ix) => getLocal(ix)
    case None => OptionC.none

  def setLocal(ix: Int, v: V): OptionC[Unit] =
    if (ix >= 0 && ix < vars.length) {
      vars = vars.updated(ix, v)
      OptionC.Some(())
    } else {
      OptionC.none
    }

  def setLocalByName(x: Var, v: V): Option[NoJoin, Unit] = names.get(x) match
    case Some(ix) => setLocal(ix, v)
    case None => OptionC.none

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

