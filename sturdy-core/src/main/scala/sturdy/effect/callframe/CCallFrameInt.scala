package sturdy.effect.callframe

import scala.reflect.ClassTag

trait CCallFrameInt[V, Data](_data: Data, _vars: Iterable[V])(using ClassTag[V]) extends CallFrame[Int, V, Data]:
  type CallFrameJoin[A] = Unit

  private var data: Data = _data
  private var vars: Array[V] = _vars.toArray

  def getFrameData: Data = data

  def getLocal[A](ix: Int, found: V => A, notFound: => A): CallFrameJoined[A] =
    if (ix >= 0 && ix < vars.size)
      found(vars(ix))
    else
      notFound

  override def setLocal[A](ix: Int, v: V, notFound: => Unit): CallFrameJoined[Unit] =
    if (ix >= 0 && ix < vars.size)
      vars = vars.updated(ix, v)
    else
      notFound

  def inNewFrame[A](d: Data, vars: Iterable[(Int, V)])(f: => A): A =
    val snapshotData = this.data
    val snapshotVars = this.vars
    this.data = d
    this.vars = vars.map(_._2).toArray
    try f finally {
      this.data = snapshotData
      this.vars = snapshotVars
    }

