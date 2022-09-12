package sturdy.values.utils

trait BitPrecision[B] {
  val value: Int
}

given BitPrecision[Float] with
  val value: Int = 24

given BitPrecision[Double] with
  val value: Int = 53