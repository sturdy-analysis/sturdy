package sturdy.data

import sturdy.values.*

import scala.collection.mutable

given FiniteSeq[V](using Finite[V]): Finite[Seq[V]] with {}

given CombineEquiSeq[V, W <: Widening](using j: Combine[V, W]): Combine[Seq[V], W] with
  override def apply(vs1: Seq[V], vs2: Seq[V]): MaybeChanged[Seq[V]] =
    if (vs1.size != vs2.size)
      throw new IllegalStateException()
    var changed = false
    val vs = vs1.zip(vs2).map {
      case (v1, v2) =>
        val v = j(v1, v2)
        changed |= v.hasChanged
        v.get
    }
    MaybeChanged(vs, changed)


given CombineEquiList[V, W <: Widening](using j: Combine[V, W]): Combine[List[V], W] with
  override def apply(vs1: List[V], vs2: List[V]): MaybeChanged[List[V]] =
    if (vs1.size != vs2.size)
      throw new IllegalStateException(s"Cannot combine $vs1 with $vs2")
    var changed = false
    val vs = vs1.zip(vs2).map {
      case (v1, v2) =>
        val v = j(v1, v2)
        changed |= v.hasChanged
        v.get
    }
    MaybeChanged(vs, changed)

given CombineEquiVector[V, W <: Widening](using j: Combine[V, W]): Combine[Vector[V], W] with
  override def apply(vs1: Vector[V], vs2: Vector[V]): MaybeChanged[Vector[V]] =
    if (vs1.size != vs2.size)
      throw new IllegalStateException()
    var changed = false
    val vs = vs1.zip(vs2).map {
      case (v1, v2) =>
        val v = j(v1, v2)
        changed |= v.hasChanged
        v.get
    }
    MaybeChanged(vs, changed)

given CombineEquiArraySeq[V, W <: Widening](using j: Combine[V, W]): Combine[mutable.ArraySeq[V], W] with
  override def apply(v1: mutable.ArraySeq[V], v2: mutable.ArraySeq[V]): MaybeChanged[mutable.ArraySeq[V]] =
    if (v1.length != v2.length)
      throw new IllegalStateException()
    var changed = false
    val result = v1.clone()
    for(i <- v1.indices) {
      val v = j(v1(i), v2(i))
      changed |= v.hasChanged
      result(i) = v.get
    }
    MaybeChanged(result, changed)