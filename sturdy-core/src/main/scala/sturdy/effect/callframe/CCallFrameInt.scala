package sturdy.effect.callframe

import sturdy.effect.CMayCompute
import sturdy.effect.CMayCompute.*
import sturdy.effect.NoJoin

import scala.reflect.ClassTag
import scala.annotation.targetName

trait CCallFrameInt[Data, V](_data: Data, _vars: Iterable[V])(using ClassTag[V]) extends CallFrame[Data, Int, V]:
  type CallFrameJoin[A] = NoJoin[A]
  type CallFrameJoinComp = Unit

  private var data: Data = _data
  protected var vars: Array[V] = _vars.toArray

  def getFrameData: Data = data
  def getCallFrame: (Data, Vector[V]) = (data, vars.toVector)

  def getLocal(ix: Int): CMayCompute[V] =
    if (ix >= 0 && ix < vars.size)
      Computes(vars(ix))
    else
      ComputesNot()

  def inNewFrame[A](d: Data, vars: Iterable[(Int, V)])(f: => A): A =
    val snapshotData = this.data
    val snapshotVars = this.vars
    this.data = d
    this.vars = vars.map(_._2).toArray
    try f finally {
      this.data = snapshotData
      this.vars = snapshotVars
    }

  def inNewFrameNoIndex[A](d: Data, vars: Iterable[V])(f: => A): A =
    val snapshotData = this.data
    val snapshotVars = this.vars
    this.data = d
    this.vars = vars.toArray
    try f finally {
      this.data = snapshotData
      this.vars = snapshotVars
    }

trait CMutableCallFrameInt[Data, V](using ClassTag[V]) extends CCallFrameInt[Data, V] with MutableCallFrame[Data, Int, V]:
  override def setLocal(ix: Int, v: V): CMayCompute[Unit] =
    if (ix >= 0 && ix < vars.size) {
      vars = vars.updated(ix, v)
      Computes(())
    } else
      ComputesNot()
