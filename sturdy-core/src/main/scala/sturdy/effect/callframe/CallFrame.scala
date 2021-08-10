package sturdy.effect.callframe

trait CallFrame[Var, V, Data]:
  type CallFrameJoin[A]
  final type CallFrameJoined[A] = CallFrameJoin[A] ?=> A

  def getFrameData: Data
  def getLocal[A](x: Var, found: V => A, notFound: => A): CallFrameJoined[A]
  def setLocal[A](x: Var, v: V, notFound: => Unit): CallFrameJoined[Unit]
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

  def getLocalOrElse(x: Var, notFound: => V): CallFrameJoined[V] =
    getLocal(x, identity, notFound)
  def getLocalOrElseAndThen[A](x: Var, notFound: => V)(f: V => A): CallFrameJoined[A] =
    getLocal(x, f, f(notFound))
