package sturdy.effect.callframe

import sturdy.data.MayJoin
import sturdy.data.{NoJoin, JOption}
import sturdy.effect.Effectful

trait CallFrame[Data, Var, V, J[_] <: MayJoin[_]] extends CallFrame.Effectful:
  def data: Data
  def getLocal(x: Int): JOption[J, V]
  def getLocalByName(x: Var): JOption[J, V]
  def withNew[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V, J[_] <: MayJoin[_]] extends CallFrame[Data, Var, V, J]:
  def setLocal(x: Int, v: V): JOption[J, Unit]
  def setLocalByName(x: Var, v: V): JOption[J, Unit]

trait DecidableCallFrame[Data, Var, V] extends CallFrame[Data, Var, V, NoJoin]
trait DecidableMutableCallFrame[Data, Var, V] extends DecidableCallFrame[Data, Var, V] with MutableCallFrame[Data, Var, V, NoJoin]

object CallFrame:
  trait Effectful extends sturdy.effect.Effectful:
    type Locals
    def getLocals: Locals
    def setLocals(ls: Locals): Unit