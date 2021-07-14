package sturdy.values

given Powerset[A]: JoinValue[Set[A]] with
  override def joinValues(v1: Set[A], v2: Set[A]): Set[A] = v1 ++ v2