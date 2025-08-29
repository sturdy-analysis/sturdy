package sturdy.data

import sturdy.values.*

import scala.collection.immutable.ArraySeq

given FiniteSeq[V](using Finite[V]): Finite[Seq[V]] with {}

given CombineEquiSeq[V, W <: Widening](using j: Combine[V, W]): Combine[Seq[V], W] with
  override def apply(vs1: Seq[V], vs2: Seq[V]): MaybeChanged[Seq[V]] =
    if (vs1.size != vs2.size)
      throw CannotJoinException(s"Cannot join seqs of different sizes (size 1: ${vs1.size}, size 2: ${vs2.size})")
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
      throw CannotJoinException(s"Cannot join lists of different sizes (size 1: ${vs1.size}, size 2: ${vs2.size})")
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
      throw CannotJoinException(s"Cannot join vectors of different sizes (size 1: ${vs1.size}, size 2: ${vs2.size})")
    var changed = false
    val vs = vs1.zip(vs2).map {
      case (v1, v2) =>
        val v = j(v1, v2)
        changed |= v.hasChanged
        v.get
    }
    MaybeChanged(vs, changed)

given CombineEquiArraySeq[V, W <: Widening](using j: Combine[V, W]): Combine[ArraySeq[V], W] with
  override def apply(vs1: ArraySeq[V], vs2: ArraySeq[V]): MaybeChanged[ArraySeq[V]] =
    if (vs1.size != vs2.size)
      throw CannotJoinException(s"Cannot join array seqs of different sizes (size 1: ${vs1.size}, size 2: ${vs2.size})")
    var changed = false
    val vs = vs1.zip(vs2).map {
      case (v1, v2) =>
        val v = j(v1, v2)
        changed |= v.hasChanged
        v.get
    }
    MaybeChanged(vs, changed)