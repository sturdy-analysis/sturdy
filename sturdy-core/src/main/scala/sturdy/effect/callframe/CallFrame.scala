package sturdy.effect.callframe

import sturdy.data.Option

trait CallFrame[Data, Var, V]:
  type CallFrameJoin[A]

  def getFrameData: Data
  def getLocal(x: Var): Option[CallFrameJoin, V]
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V] extends CallFrame[Data, Var, V]:
  def setLocal(x: Var, v: V): Option[CallFrameJoin, Unit]
