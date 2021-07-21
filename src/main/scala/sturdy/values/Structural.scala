package sturdy.values

import sturdy.util.Label

/* Type class to mark values that may be compared structurally using the built-in `==`. */
trait Structural[T]

given Structural[Unit] with {}
given Structural[Boolean] with {}
given Structural[Int] with {}
given Structural[Double] with {}
given Structural[String] with {}
given Structural[Label] with {}

