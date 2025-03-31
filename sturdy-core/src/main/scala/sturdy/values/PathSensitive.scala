package sturdy.values

trait PathSensitive[V]:
  def assert(cond: Any, v: V): V

extension [V : PathSensitive](v: V)
  def assertPath(cond: Any): V = summon[PathSensitive[V]].assert(cond, v)
