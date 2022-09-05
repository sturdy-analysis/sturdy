package sturdy.values.utils

import gmp.Mpz

trait ConvertInterval[B] {
  def convertTo(x : Double) : B
  def convertFrom(x : B) : Mpz
}

given ConvertIntervalInt: ConvertInterval[Int] with
  override def convertTo(x: Double): Int = x.toInt
  override def convertFrom(x: Int): Mpz = new Mpz(x)

given ConvertIntervalLong: ConvertInterval[Long] with
  override def convertTo(x: Double): Long = x.toLong
  override def convertFrom(x: Long): Mpz = new Mpz(x)