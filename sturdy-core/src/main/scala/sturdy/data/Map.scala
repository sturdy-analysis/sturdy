package sturdy.data

import sturdy.values.*

import scala.collection.immutable.IntMap

given FiniteMap[K, V](using Finite[K], Finite[V]): Finite[Map[K, V]] with {}

given JoinMap[K, V](using j: Join[V]): Join[Map[K, V]] with
  override def apply(vs1: Map[K, V], vs2: Map[K, V]): MaybeChanged[Map[K, V]] =
    var joined = vs1
    var changed = false
    for ((x, v2) <- vs2)
      joined.get(x) match
        case None =>
          joined += x -> v2
          changed = true
        case Some(v1) =>
          val joinedV = j(v1, v2)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(joined, changed)

given JoinIntMap[V, W <: Widening](using j: Combine[V, W]): Join[IntMap[V]] with
  override def apply(vs1: IntMap[V], vs2: IntMap[V]): MaybeChanged[IntMap[V]] =
    var joined = vs1
    var changed = false
    for ((x, v2) <- vs2)
      joined.get(x) match
        case None =>
          joined += x -> v2
          changed = true
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
        case None =>
          joined += x -> v2V
          changed = true
        case Some(v1V) =>
          val joinedV = j(v1V, v2V)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(joined, changed)

inline def combineMaps[K, V](m1: Map[K, V], m2: Map[K, V], inline combine: (V, V) => V): Map[K, V] =
  val (large, small) = if (m1.size >= m2.size) (m1, m2) else (m2, m1)
  var result = large
  for ((k, v1) <- small)
    val v = large.get(k) match
      case None => v1
      case Some(v2) => combine(v1, v2)
    result += k -> v
  result