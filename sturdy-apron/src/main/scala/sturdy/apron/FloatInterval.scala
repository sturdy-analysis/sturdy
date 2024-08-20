package sturdy.apron

import apron.*

import java.lang.{Double => JDouble}

import scala.collection.mutable.ArrayBuffer

import sturdy.values.floating.FloatSpecials

object FloatInterval:
  def apply(inf: Double, sup: Double, floatSpecials: FloatSpecials) =
    new FloatInterval(DoubleScalar(inf), DoubleScalar(sup), floatSpecials)

  def apply(inf: Scalar, sup: Scalar, floatSpecials: FloatSpecials) =
    new FloatInterval(inf, sup, floatSpecials)

  def apply(floatSpecials: FloatSpecials): FloatInterval =
    FloatInterval(0, -1, floatSpecials)

  def apply(d: Double): FloatInterval =
    if(d.isNegInfinity)
      FloatInterval(FloatSpecials.NegInfinity)
    else if(d.isPosInfinity)
      FloatInterval(FloatSpecials.PosInfinity)
    else if(d.isNaN)
      FloatInterval(FloatSpecials.NaN)
    else if(JDouble.doubleToRawLongBits(d) == JDouble.doubleToRawLongBits(-0.0d))
      new FloatInterval(DoubleScalar(0), DoubleScalar(0), FloatSpecials.NegZero)
    else
      new FloatInterval(DoubleScalar(d), DoubleScalar(d), FloatSpecials.Bottom)

class FloatInterval(infimum: Scalar, supremum: Scalar, var floatSpecials: FloatSpecials) extends Interval(infimum, supremum):
//  if(! isNonSpecialBottom && nonSpecialInf.sgn() < 0 && nonSpecialSup.sgn() > 0)
//    setNegZero(true)

  override def inf(): Scalar =
    if(floatSpecials.negInfinity)
      DoubleScalar(Double.NegativeInfinity)
    else if(! isNonSpecialBottom)
      nonSpecialInf
    else if(isNonSpecialBottom && floatSpecials.negZero)
      DoubleScalar(-0.0d)
    else if(floatSpecials.posInfinity)
      DoubleScalar(Double.PositiveInfinity)
    else
      nonSpecialInf

  def nonSpecialInf: Scalar =
    super.inf()

  override def sup(): Scalar =
    if(floatSpecials.posInfinity)
      DoubleScalar(Double.PositiveInfinity)
    else if(! isNonSpecialBottom)
      nonSpecialSup
    else if(isNonSpecialBottom && floatSpecials.negZero)
      DoubleScalar(-0.0d)
    else if(floatSpecials.negInfinity)
      DoubleScalar(Double.NegativeInfinity)
    else
      nonSpecialSup

  def nonSpecialSup: Scalar =
    super.sup()

  def setNegInfinity(b: Boolean): Unit =
    floatSpecials = floatSpecials.setNegInfinity(b)

  def setNegZero(b: Boolean): Unit =
    floatSpecials = floatSpecials.setNegZero(b)

  def setPosInfinity(b: Boolean): Unit =
    floatSpecials = floatSpecials.setPosInfinity(b)

  def setNaN(b: Boolean): Unit =
    floatSpecials = floatSpecials.setNaN(b)

  override def clone(): FloatInterval =
    new FloatInterval(super.inf().copy(), super.sup().copy(), floatSpecials)

  override def isBottom: Boolean =
    isNonSpecialBottom && floatSpecials == FloatSpecials.Bottom

  def isNonSpecialBottom: Boolean =
    super.isBottom

  inline def onlySpecials: Boolean =
    isNonSpecialBottom

  override def setBottom(): Unit =
    super.setBottom()
    floatSpecials = FloatSpecials.Bottom

  override def isTop: Boolean =
    super.isTop && floatSpecials == FloatSpecials.Top

  override def setTop(): Unit =
    super.setTop()
    floatSpecials = FloatSpecials.Top

  override def isLeq(interval: Interval): Boolean =
    interval match
      case other: FloatInterval =>
        this.floatSpecials.isLeq(other.floatSpecials) && (
          isNonSpecialBottom ||
            (other.nonSpecialInf.cmp(this.nonSpecialInf) <= 0 &&
              this.nonSpecialSup.cmp(other.nonSpecialSup) <= 0)
        )
      case _ => throw IllegalArgumentException(s"Can only compare float intervals, but got $interval")

  override def isEqual(interval: Interval): Boolean =
    interval match
      case other: FloatInterval =>
        super.isEqual(other) && this.floatSpecials == other.floatSpecials
      case other: Interval => false

  override def isScalar: Boolean =
    (super.isScalar && floatSpecials.size == 0) || (super.isBottom && floatSpecials.size == 1)

  override def isZero: Boolean =
    super.isZero && floatSpecials == FloatSpecials.Bottom

  override def neg(): Unit =
    super.neg()
    floatSpecials = FloatSpecials(
      negInfinity = floatSpecials.posInfinity,
      negZero = ! isNonSpecialBottom && nonSpecialInf.sgn() < 0 && nonSpecialSup.sgn() >= 0,
      posInfinity = floatSpecials.negInfinity,
      nan = floatSpecials.nan
    )

  def meet(iv2: FloatInterval): FloatInterval =
    val iv1 = this
    val res = FloatInterval(iv1.floatSpecials.meet(iv2.floatSpecials))
    if (iv1.isNonSpecialBottom || iv2.isNonSpecialBottom || iv1.nonSpecialSup.cmp(iv2.nonSpecialInf) < 0 || iv2.nonSpecialSup.cmp(iv1.nonSpecialInf) < 0) { // no overlap
      res.nonSpecialInf.set(0)
      res.nonSpecialSup.set(-1)
    } else {
      if (iv1.nonSpecialInf.cmp(iv2.nonSpecialInf) >= 0)
        res.setInf(iv1.nonSpecialInf)
      else
        res.setInf(iv2.nonSpecialInf)
      if (iv1.nonSpecialSup.cmp(iv2.nonSpecialSup) <= 0)
        res.setSup(iv1.nonSpecialSup)
      else
        res.setSup(iv2.nonSpecialSup)
    }
    res

  override def toString: String =
    s"${if(isNonSpecialBottom) "⟂" else super.toString} ∪ $floatSpecials"