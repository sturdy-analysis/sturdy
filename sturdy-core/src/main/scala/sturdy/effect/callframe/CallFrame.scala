package sturdy.effect.callframe

import sturdy.data.{NoJoin, Option}
import sturdy.effect.Effectful

trait CallFrame[Data, Var, V, MayJoin[_]] extends Effectful:
  def getFrameData: Data
  
  def getLocal(x: Int): Option[MayJoin, V]
  def getLocalByName(x: Var): Option[MayJoin, V]
  
  def inNewFrame[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A

trait MutableCallFrame[Data, Var, V, MayJoin[_]] extends CallFrame[Data, Var, V, MayJoin]:
  def setLocal(x: Int, v: V): Option[MayJoin, Unit]
  def setLocalByName(x: Var, v: V): Option[MayJoin, Unit]

trait DecidableCallFrame[Data, Var, V] extends CallFrame[Data, Var, V, NoJoin]
trait DecidableMutableCallFrame[Data, Var, V] extends DecidableCallFrame[Data, Var, V] with MutableCallFrame[Data, Var, V, NoJoin]
