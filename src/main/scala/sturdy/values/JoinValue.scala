package sturdy.values

trait JoinValue[V]:
  def joinValues(v1: V, v2: V): V

given unit: Unit = ()
given JoinValue[Unit] with
  override def joinValues(v1: Unit, v2: Unit): Unit = v1
