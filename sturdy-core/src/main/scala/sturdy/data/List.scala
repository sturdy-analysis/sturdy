package sturdy.data

import sturdy.values.{Finite, Widening, Combine}

given FiniteSeq[V](using Finite[V]): Finite[Seq[V]] with {}

given CombineEquiSeq[V, W <: Widening](using j: Combine[V, W]): Combine[Seq[V], W] with
  override def apply(v1: Seq[V], v2: Seq[V]): Seq[V] =
    if (v1.size != v2.size)
      throw new IllegalStateException()
    v1.zip(v2).map(j.apply.tupled)

given CombineEquiList[V, W <: Widening](using j: Combine[V, W]): Combine[List[V], W] with
  override def apply(v1: List[V], v2: List[V]): List[V] =
    if (v1.size != v2.size)
      throw new IllegalStateException()
    v1.zip(v2).map(j.apply.tupled)

given CombineEquiVector[V, W <: Widening](using j: Combine[V, W]): Combine[Vector[V], W] with
  override def apply(v1: Vector[V], v2: Vector[V]): Vector[V] =
    if (v1.size != v2.size)
      throw new IllegalStateException()
    v1.zip(v2).map(j.apply.tupled)

