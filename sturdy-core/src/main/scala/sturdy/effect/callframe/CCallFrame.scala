package sturdy.effect.callframe

trait CCallFrame[Data, Var, V](_data: Data, _vars: Map[Var, V]) extends CallFrame[Data, Var, V]:
  type CallFrameJoin[A] = Unit

  private var data: Data = _data
  protected var vars: Map[Var, V] = _vars
  
  def getFrameData: Data = data
  def getCallFrame: (Data, Map[Var, V]) = (data, vars)

  def getLocal[A](x: Var, found: V => A, notFound: => A): CallFrameJoined[A] = vars.get(x) match
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

trait CMutableCallFrame[Data, Var, V] extends CCallFrame[Data, Var, V] with MutableCallFrame[Data, Var, V]:
  override def setLocal[A](x: Var, v: V, notFound: => Unit): CallFrameJoined[Unit] = vars.get(x) match
    case Some(_) => vars += x -> v
    case _ => notFound

