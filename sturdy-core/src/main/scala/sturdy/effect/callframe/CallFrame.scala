package sturdy.effect.callframe

import sturdy.data.MayJoin
import sturdy.data.{NoJoin, JOption}
import sturdy.effect.Effect

trait CallFrame[Data, Var, V, J[_] <: MayJoin[_]] extends Effect:
  def data: Data
  def getLocal(x: Int): JOption[J, V]
  def getLocalByName(x: Var): JOption[J, V]
  def withNew[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V, J[_] <: MayJoin[_]] extends CallFrame[Data, Var, V, J]:
  def setLocal(x: Int, v: V): JOption[J, Unit]
  def setLocalByName(x: Var, v: V): JOption[J, Unit]

