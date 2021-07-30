package sturdy.values

trait JoinValue[V]:
  def joinValues(v1: V, v2: V): V

given unit: Unit = ()
given joinUnit: JoinValue[Unit] with
  override def joinValues(v1: Unit, v2: Unit): Unit = v1

given joinMap[K, V] (using j: JoinValue[V]): JoinValue[Map[K, V]] with
  override def joinValues(v1: Map[K, V], v2: Map[K, V]): Map[K, V] =
    var joined = v1
    for ((x, v2V) <- v2)
      joined.get(x) match
        case None => joined += x -> v2V
        case Some(v1V) =>
          val joinedV = j.joinValues(v1V, v2V)
          joined += x -> joinedV
    joined

given joinTuple2[T1, T2](using j1: JoinValue[T1], j2: JoinValue[T2]): JoinValue[(T1, T2)] with
  override def joinValues(old: (T1, T2), now: (T1, T2)): (T1, T2) = (j1.joinValues(old._1, now._1), j2.joinValues(old._2, now._2))

given joinTuple3[T1, T2, T3](using j1: JoinValue[T1], j2: JoinValue[T2], j3: JoinValue[T3]): JoinValue[(T1, T2, T3)] with
  override def joinValues(old: (T1, T2, T3), now: (T1, T2, T3)): (T1, T2, T3) = (j1.joinValues(old._1, now._1), j2.joinValues(old._2, now._2), j3.joinValues(old._3, now._3))
