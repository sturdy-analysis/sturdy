package sturdy.values

trait PathSensitive[V]:
  def assert(cond: Any, v: V): V

case class NotPathSensitive[V]() extends PathSensitive[V]:
  override def assert(cond: Any, v: V): V = v


extension [V : PathSensitive](v: V)
  def assertPath(cond: Any): V = summon[PathSensitive[V]].assert(cond, v)

