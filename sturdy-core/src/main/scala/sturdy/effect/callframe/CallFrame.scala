package sturdy.effect.callframe

import sturdy.data.MayJoin
import sturdy.data.{NoJoin, JOption}
import sturdy.effect.Effect

/** [[CallFrame]] contains local variables valid within a function call.
 * The variables may be mutable or immutable.
 */
trait CallFrame[Data, Var, V, Site, J[_] <: MayJoin[_]] extends Effect:
  def data: Data
  def getLocal(x: Int): JOption[J, V]
  def getLocalByName(x: Var): JOption[J, V]
  def withNew[A](d: Data, vars: Iterable[(Var, Option[V])], site: Site)(f: => A): A

  final def getLocalOrElse(x: Int, default:  => V)(using J[V]): V =
    getLocal(x).getOrElse(default)
  final def getLocalByNameOrElse(x: Var, default:  => V)(using J[V]): V =
    getLocalByName(x).getOrElse(default)

trait MutableCallFrame[Data, Var, V, Site, J[_] <: MayJoin[_]] extends CallFrame[Data, Var, V, Site, J]:
  def setLocal(x: Int, v: V): JOption[J, Unit]
  def setLocalByName(x: Var, v: V): JOption[J, Unit]

  final def setLocalOrElse(x: Int, v: V, default: => Unit)(using J[Unit]): Unit =
    setLocal(x, v).getOrElse(default)

