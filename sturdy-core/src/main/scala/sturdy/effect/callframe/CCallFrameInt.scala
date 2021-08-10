package sturdy.effect.callframe

import scala.reflect.ClassTag

trait CCallFrameInt[Data, V](_data: Data, _vars: Iterable[V])(using ClassTag[V]) extends CallFrame[Data, Int, V]:
  type CallFrameJoin[A] = Unit

  private var data: Data = _data
  protected var vars: Array[V] = _vars.toArray

  def getFrameData: Data = data
  def getCallFrame: (Data, Vector[V]) = (data, vars.toVector)

  def getLocal[A](ix: Int, found: V => A, notFound: => A): CallFrameJoined[A] =
    if (ix >= 0 && ix < vars.size)
      found(vars(ix))
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

trait CMutableCallFrameInt[Data, V](using ClassTag[V]) extends CCallFrameInt[Data, V] with MutableCallFrame[Data, Int, V]:
  override def setLocal[A](ix: Int, v: V, notFound: => Unit): CallFrameJoined[Unit] =
    if (ix >= 0 && ix < vars.size)
      vars = vars.updated(ix, v)
    else
      notFound
