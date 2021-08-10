package sturdy.effect.callframe

trait CallFrame[Var, V, Data]:
  type CallFrameJoin[A]
  final type CallFrameJoined[A] = CallFrameJoin[A] ?=> A

  def getFrameData: Data
  def lookupInFrame[A](x: Var, found: V => A, notFound: => A): CallFrameJoined[A]
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

