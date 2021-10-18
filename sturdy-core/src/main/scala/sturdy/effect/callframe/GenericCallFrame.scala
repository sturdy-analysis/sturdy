package sturdy.effect.callframe

import sturdy.data.*

trait GenericCallFrame[Data, Var, V](_data: Data, _vars: Map[Var, V]) extends CallFrame[Data, Var, V, NoJoin]:
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

trait GenericMutableCallFrame[Data, Var, V] extends GenericCallFrame[Data, Var, V] with MutableCallFrame[Data, Var, V, NoJoin]:
  override def setLocal(x: Var, v: V): OptionC[Unit] = vars.get(x) match
    case Some(_) =>
      vars += x -> v
      OptionC.Some(())
    case _ => OptionC.none

