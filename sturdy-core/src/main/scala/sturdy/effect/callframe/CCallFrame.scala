package sturdy.effect.callframe

trait CCallFrame[Var, V, Data](_data: Data, _vars: Map[Var, V]) extends CallFrame[Var, V, Data]:
  type CallFrameJoin[A] = Unit

  private var data: Data = _data
  private var vars: Map[Var, V] = _vars

  def getFrameData: Data = data

  def lookupInFrame[A](x: Var, found: V => A, notFound: => A): CallFrameJoined[A] = vars.get(x) match
    case Some(v) => found(v)
    case None => notFound

  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A =
    val snapshotData = this.data
    val snapshotVars = this.vars
    this.data = d
    this.vars = vars.toMap
    try f finally {
      this.data = snapshotData
      this.vars = snapshotVars
    }

