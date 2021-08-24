package sturdy.effect.callframe

import sturdy.effect.CMayCompute
import sturdy.effect.CMayCompute.*
import sturdy.effect.NoJoin

trait CCallFrame[Data, Var, V](_data: Data, _vars: Map[Var, V]) extends CallFrame[Data, Var, V]:
  type CallFrameJoin[A] = NoJoin[A]
  type CallFrameJoinComp = Unit

  private var data: Data = _data
  protected var vars: Map[Var, V] = _vars

  def getFrameData: Data = data
  def getCallFrame: (Data, Map[Var, V]) = (data, vars)

  def getLocal(x: Var): CMayCompute[V] = CMayCompute(vars.get(x))

  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A =
    val snapshotData = this.data
    val snapshotVars = this.vars
    this.data = d
    this.vars = vars.toMap
    try f finally {
      this.data = snapshotData
      this.vars = snapshotVars
    }

trait CMutableCallFrame[Data, Var, V] extends CCallFrame[Data, Var, V] with MutableCallFrame[Data, Var, V]:
  override def setLocal(x: Var, v: V): CMayCompute[Unit] = vars.get(x) match
    case Some(_) =>
      vars += x -> v
      Computes(())
    case _ => ComputesNot()

