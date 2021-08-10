package sturdy.effect.callframe

import sturdy.effect.MayCompute

trait CallFrame[Data, Var, V]:
  type CallFrameJoin[A]
  type CallFrameJoinComp

  def getFrameData: Data
  def getLocal(x: Var): MayCompute[V, CallFrameJoin, CallFrameJoinComp]
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V] extends CallFrame[Data, Var, V]:
  def setLocal(x: Var, v: V): MayCompute[Unit, CallFrameJoin, CallFrameJoinComp]
