package sturdy.effect.callframe

import sturdy.data.{NoJoin, JOption}
import sturdy.effect.Effectful

trait CallFrame[Data, Var, V, MayJoin[_]] extends Effectful:
  def getFrameData: Data
  
  def getLocal(x: Int): JOption[MayJoin, V]
  def getLocalByName(x: Var): JOption[MayJoin, V]
  
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V, MayJoin[_]] extends CallFrame[Data, Var, V, MayJoin]:
  def setLocal(x: Int, v: V): JOption[MayJoin, Unit]
  def setLocalByName(x: Var, v: V): JOption[MayJoin, Unit]

trait DecidableCallFrame[Data, Var, V] extends CallFrame[Data, Var, V, NoJoin]
trait DecidableMutableCallFrame[Data, Var, V] extends DecidableCallFrame[Data, Var, V] with MutableCallFrame[Data, Var, V, NoJoin]
