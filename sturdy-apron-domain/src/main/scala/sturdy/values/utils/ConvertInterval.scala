package sturdy.values.utils

import apron.Interval
import gmp.{Mpfr, Mpq, Mpz}
import sturdy.values.integer.NumericInterval
import sturdy.values.utils.{ConvertCoeff, given}

trait ConvertInterval[B] {
  def apply(x: Interval): NumericInterval[B]
  def apply(x: NumericInterval[B]): Interval
}

given ConvertInterval[Int] with
  override def apply(x: Interval): NumericInterval[Int] =
    val inf, sup = Mpq(32)
    x.inf.toMpq(inf, 0)
    x.sup.toMpq(sup, 0)
    NumericInterval(inf.doubleValue().toInt, sup.doubleValue().toInt)

  override def apply(x: NumericInterval[Int]): Interval =
    Interval(x.low, x.high)

given ConvertInterval[Long] with
  override def apply(x: Interval): NumericInterval[Long] =
    val inf, sup = Mpq(64)
    x.inf.toMpq(inf, 0)
    x.sup.toMpq(sup, 0)
    NumericInterval(inf.doubleValue().toLong, sup.doubleValue().toLong)

  override def apply(x: NumericInterval[Long]): Interval =
    Interval(x.low, x.high)
