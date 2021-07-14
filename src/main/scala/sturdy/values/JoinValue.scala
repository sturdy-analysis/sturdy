package sturdy.values

trait JoinValue[V]:
  def joinValues(v1: V, v2: V): V
