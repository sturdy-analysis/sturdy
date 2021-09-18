package sturdy.effect.callframe

import sturdy.data.*

trait CCallFrame[Data, Var, V](_data: Data, _vars: Map[Var, V]) extends CallFrame[Data, Var, V]:
  type CallFrameJoin[A] = NoJoin[A]

  private var data: Data = _data
  protected var vars: Map[Var, V] = _vars

  def getFrameData: Data = data
  def getCallFrame: (Data, Map[Var, V]) = (data, vars)

  def getLocal(x: Var): OptionC[V] = OptionC(vars.get(x))

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
  override def setLocal(x: Var, v: V): OptionC[Unit] = vars.get(x) match
    case Some(_) =>
      vars += x -> v
      OptionC.Some(())
    case _ => OptionC.none

