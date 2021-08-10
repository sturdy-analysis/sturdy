package sturdy.effect.callframe

trait CCallFrameInt[V, Data](_data: Data, _vars: Vector[V]) extends CallFrame[Int, V, Data]:
  type CallFrameJoin[A] = Unit

  private var data: Data = _data
  private var vars: Vector[V] = _vars

  def getFrameData: Data = data

  def lookupInFrame[A](x: Int, found: V => A, notFound: => A): CallFrameJoined[A] =
    if (x >= 0 && x < vars.size)
      found(vars(x))
    else
      notFound

  def inNewFrame[A](d: Data, vars: Iterable[(Int, V)])(f: => A): A =
    val snapshotData = this.data
    val snapshotVars = this.vars
    this.data = d
    this.vars = vars.map(_._2).toVector
    try f finally {
      this.data = snapshotData
      this.vars = snapshotVars
    }

