package sturdy.values

import sturdy.util.Label

/* Type class to mark values that may be compared structurally using the built-in `==`. */
trait Structural[T]

given Structural[Unit] with {}
given Structural[String] with {}
given Structural[Label] with {}

given StructuralOption[A](using Structural[A]): Structural[Option[A]] with {}
given StructuralMap[K, V](using Structural[K], Structural[V]): Structural[Map[K, V]] with {}
