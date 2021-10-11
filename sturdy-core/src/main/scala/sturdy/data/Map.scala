package sturdy.data

import sturdy.values.{Finite, Widening, Combine, Widen, Join, MaybeChanged}

given FiniteMap[K, V](using Finite[K], Finite[V]): Finite[Map[K, V]] with {}

given JoinMap[K, V, W <: Widening](using j: Combine[V, W]): Join[Map[K, V]] with
  override def apply(vs1: Map[K, V], vs2: Map[K, V]): MaybeChanged[Map[K, V]] =
    var joined = vs1
    var changed = false
    for ((x, v2) <- vs2)
      joined.get(x) match
        case None => joined += x -> v2
        case Some(v1) =>
          val joinedV = j(v1, v2)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(joined, changed)

given WidenFiniteKeyMap[K, V](using j: Widen[V], fk: Finite[K]): Widen[Map[K, V]] with
  override def apply(v1: Map[K, V], v2: Map[K, V]): MaybeChanged[Map[K, V]] =
    var joined = v1
    var changed = false
    for ((x, v2V) <- v2)
      joined.get(x) match
        case None => joined += x -> v2V
        case Some(v1V) =>
          val joinedV = j(v1V, v2V)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(joined, changed)

