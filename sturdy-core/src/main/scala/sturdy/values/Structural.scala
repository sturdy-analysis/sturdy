package sturdy.values

import sturdy.util.Label

/* Type class to mark values that may be compared structurally using the built-in `==`. */
trait Structural[T]

given Structural[Unit] with {}
given Structural[String] with {}
given DiscretelyOrderedString[W <: Widening]: Combine[String, W] = DiscretelyOrdered[String, W]
given Structural[Label] with {}
given DiscretelyOrderedLabel[W <: Widening]: Combine[Label, W] = DiscretelyOrdered[Label, W]
given Structural[Byte] with {}
given DiscretelyOrderedByte[W <: Widening]: Combine[Byte, W] = DiscretelyOrdered[Byte, W]
given Structural[Int] with {}
given DiscreteOrderedInteger[W <: Widening]: Combine[Int, W] = DiscretelyOrdered[Int, W]
given Structural[Long] with {}
given DiscreteOrderedLong[W <: Widening]: Combine[Long, W] = DiscretelyOrdered[Long, W]
given Structural[Float] with {}
given DiscreteOrderedFloat[W <: Widening]: Combine[Float, W] with
  override def apply(v1: Float, v2: Float): MaybeChanged[Float] =
    if(lteq(v1,v2))
      MaybeChanged.Unchanged(v1)
    else
      throw new IllegalArgumentException(s"Cannot join different floats $v1 and $v2")
  override def lteq(x: Float, y: Float): Boolean =
    (x.isNaN && y.isNaN) || x == y
given Structural[Double] with {}
given DiscreteOrderedDouble[W <: Widening]: Combine[Double, W] with
  override def apply(v1: Double, v2: Double): MaybeChanged[Double] =
    if(lteq(v1,v2))
      MaybeChanged.Unchanged(v1)
    else
      throw new IllegalArgumentException(s"Cannot join different doubles $v1 and $v2")
  override def lteq(x: Double, y: Double): Boolean =
    (x.isNaN && y.isNaN) || x == y

given StructuralOption[A](using Structural[A]): Structural[Option[A]] with {}
given StructuralMap[K, V](using Structural[K], Structural[V]): Structural[Map[K, V]] with {}
