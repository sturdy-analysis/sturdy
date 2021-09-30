package sturdy.data

import sturdy.values.{Finite, Widening, Combine, Widen, Join}

given FiniteMap[K, V](using Finite[K], Finite[V]): Finite[Map[K, V]] with {}

given JoinMap[K, V, W <: Widening](using j: Combine[V, W]): Join[Map[K, V]] with
  override def apply(v1: Map[K, V], v2: Map[K, V]): Map[K, V] =
    var joined = v1
    for ((x, v2V) <- v2)
      joined.get(x) match
        case None => joined += x -> v2V
        case Some(v1V) =>
          val joinedV = j(v1V, v2V)
          joined += x -> joinedV
    joined

given WidenFiniteKeyMap[K, V](using j: Widen[V], fk: Finite[K]): Widen[Map[K, V]] with
  override def apply(v1: Map[K, V], v2: Map[K, V]): Map[K, V] =
    var joined = v1
    for ((x, v2V) <- v2)
      joined.get(x) match
        case None => joined += x -> v2V
        case Some(v1V) =>
          val joinedV = j(v1V, v2V)
          joined += x -> joinedV
    joined

