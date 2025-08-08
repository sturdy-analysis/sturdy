package sturdy.values.floating

import sturdy.values.{Combine, MaybeChanged, Widening}
import sturdy.values.floating.FloatSpecials.{NegInfinity, PosInfinity}

import scala.collection.mutable.ArrayBuffer

case class FloatSpecials(negInfinity: Boolean, negZero: Boolean, posInfinity: Boolean, nan: Boolean):
  inline def setNegInfinity(b: Boolean): FloatSpecials = copy(negInfinity = b)

  inline def setNegZero(b: Boolean): FloatSpecials = copy(negZero = b)

  inline def setPosInfinity(b: Boolean): FloatSpecials = copy(posInfinity = b)

  inline def setNaN(b: Boolean): FloatSpecials = copy(nan = b)

  inline def isInfinite: Boolean = negInfinity || posInfinity

  inline def isBottom: Boolean = this == FloatSpecials.Bottom

  def isLeq(other: FloatSpecials): Boolean =
    implies(this.negInfinity, other.negInfinity) &&
    implies(this.negZero, other.negZero) &&
    implies(this.posInfinity, other.posInfinity) &&
    implies(this.nan, other.nan)

  inline def isScalar: Boolean = size == 1

  def size: Int =
    var numValues = 0
    if (negInfinity) numValues += 1
    if (negZero) numValues += 1
    if (posInfinity) numValues += 1
    if (nan) numValues += 1
    numValues

  private inline def implies(b1: Boolean, b2: Boolean): Boolean = !b1 || b2

  def inf: Double =
    if(negInfinity)
      Double.NegativeInfinity
    if (negZero)
      -0.0d
    else if(posInfinity)
      Double.PositiveInfinity
    else
      Double.NaN

  def meet(other: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = this.negInfinity && other.negInfinity,
      negZero = this.negZero && other.negZero,
      posInfinity = this.posInfinity && other.posInfinity,
      nan = this.nan && other.nan
    )

  override def toString(): String =
    val result: ArrayBuffer[String] = ArrayBuffer.empty
    if(negInfinity)
      result += "-∞"
    if(negZero)
      result += "-0.0"
    if(nan)
      result += "NaN"
    if (posInfinity)
      result += "∞"
    result.mkString("{", ",", "}")

given combineFloatSpecials[W <: Widening]: Combine[FloatSpecials, W] with
  override def apply(v1: FloatSpecials, v2: FloatSpecials): MaybeChanged[FloatSpecials] =
    val result = FloatSpecials(
      negInfinity = v1.negInfinity || v2.negInfinity,
      negZero = v1.negZero || v2.negZero,
      posInfinity = v1.posInfinity || v2.posInfinity,
      nan = v1.nan || v2.nan
    )
    MaybeChanged(result, v1)

object FloatSpecials:
  val Float: FloatSpecials = FloatSpecials(negInfinity = false, negZero = false, posInfinity = false, nan = false)
  val Integer: FloatSpecials = FloatSpecials(negInfinity = false, negZero = false, posInfinity = false, nan = false)
  val Bottom: FloatSpecials = FloatSpecials(negInfinity = false, negZero = false, posInfinity = false, nan = false)
  val NegInfinity: FloatSpecials = FloatSpecials(negInfinity = true, negZero = false, posInfinity = false, nan = false)
  val NegZero: FloatSpecials = FloatSpecials(negInfinity = false, negZero = true, posInfinity = false, nan = false)
  val PosInfinity: FloatSpecials = FloatSpecials(negInfinity = false, negZero = false, posInfinity = true, nan = false)
  val NaN: FloatSpecials = FloatSpecials(negInfinity = false, negZero = false, posInfinity = false, nan = true)
  val Top: FloatSpecials = FloatSpecials(negInfinity = true, negZero = true, posInfinity = true, nan = true)
