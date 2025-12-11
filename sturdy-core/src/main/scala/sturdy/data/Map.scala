package sturdy.data

import sturdy.values.*

import scala.collection.immutable.{HashMap, IntMap, SortedMap}

given FiniteMap[K, V](using Finite[K], Finite[V]): Finite[Map[K, V]] with {}

given JoinMap[K, V](using j: Join[V]): Join[Map[K, V]] with
  override def apply(vs1: Map[K, V], vs2: Map[K, V]): MaybeChanged[Map[K, V]] =
    if(vs1 eq vs2)
      Unchanged(vs1)
    else
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
    if(v1 eq v2)
      Unchanged(v1)
    else
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


given CombineFiniteKeyMap[K, V, W <: Widening](using j: Combine[V, W], fk: Finite[K]): Combine[Map[K, V], W] with
  override def apply(v1: Map[K, V], v2: Map[K, V]): MaybeChanged[Map[K, V]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
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

given CombineIntMap[V, W <: Widening](using j: Combine[V, W]): Combine[IntMap[V], W] with
  override def apply(vs1: IntMap[V], vs2: IntMap[V]): MaybeChanged[IntMap[V]] =
    if (vs1 eq vs2)
      Unchanged(vs1)
    else
      var changed = false
      val joined = vs1.unionWith(vs2, (_key, v1, v2) =>
        val joined = j(v1, v2)
        changed |= joined.hasChanged
        joined.get
      )
      MaybeChanged(joined, changed)

given CombineFiniteKeySortedMap[K, V, W <: Widening](using j: Combine[V, W], fk: Finite[K]): Combine[SortedMap[K, V], W] with
  override def apply(v1: SortedMap[K, V], v2: SortedMap[K, V]): MaybeChanged[SortedMap[K, V]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
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

given CombineFiniteKeyHashMap[K, V, W <: Widening](using combineValue: Combine[V, W], finiteKey: Finite[K]): Combine[HashMap[K, V], W] with
  override def apply(v1: HashMap[K, V], v2: HashMap[K, V]): MaybeChanged[HashMap[K, V]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
      var changed = false
      val result = v1.merged(v2){case ((k,v1),(_,v2)) =>
        val combined = Combine(v1,v2)
        changed |= combined.hasChanged
        (k, combined.get)
      }
      MaybeChanged(result, changed || result.size > v1.size)

object MapEquals:
  inline def apply[K,V](m1: Map[K,V], m2: Map[K,V], eqVals: (V,V) => Boolean): Boolean =
    m1.size == m2.size && m1.forall((k1, v1) =>
      m2.get(k1) match
        case None => false
        case Some(v2) => eqVals(v1,v2)
    )
  inline def apply[K,V](m1: Map[K,V], m2: Map[K,V]): Boolean =
    apply(m1, m2, _ == _)