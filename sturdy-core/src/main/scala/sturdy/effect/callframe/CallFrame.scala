package sturdy.effect.callframe

import sturdy.data.Option

trait CallFrame[Data, Var, V, MayJoin[_]]:
  def getFrameData: Data
  def getLocal(x: Var): Option[MayJoin, V]
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V, MayJoin[_]] extends CallFrame[Data, Var, V, MayJoin]:
  def setLocal(x: Var, v: V): Option[MayJoin, Unit]
