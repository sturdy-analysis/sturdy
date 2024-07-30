package sturdy.values.floating

import sturdy.values.{Combine, MaybeChanged, Widening}
import sturdy.values.floating.FloatSpecials.{NegInfinity, PosInfinity}

case class FloatSpecials(negInfinity: Boolean, posInfinity: Boolean, nan: Boolean)

given combineFloatSpecials[W <: Widening]: Combine[FloatSpecials, W] with
  override def apply(v1: FloatSpecials, v2: FloatSpecials): MaybeChanged[FloatSpecials] =
    val result = FloatSpecials(negInfinity = v1.negInfinity || v2.negInfinity, posInfinity = v1.posInfinity || v2.posInfinity, nan = v1.nan || v2.nan)
    MaybeChanged(result, result != v1)
object FloatSpecials:
  val Float: FloatSpecials = FloatSpecials(negInfinity = false, posInfinity = false, nan = false)
  val Bottom: FloatSpecials = Float
  val NegInfinity: FloatSpecials = FloatSpecials(negInfinity = true, posInfinity = false, nan = false)
  val PosInfinity: FloatSpecials = FloatSpecials(negInfinity = false, posInfinity = true, nan = false)
  val NaN: FloatSpecials = FloatSpecials(negInfinity = false, posInfinity = false, nan = true)

given FloatSpecialsOps[F: Numeric]: FloatOps[F, FloatSpecials] with
  import FloatSpecials.*
  override def floatingLit(f: F): FloatSpecials =
    val d = Numeric[F].toDouble(f)
    if(d.isPosInfinity)
      PosInfinity
    else if(d.isNegInfinity)
      NegInfinity
    else if(d.isNaN)
      NaN
    else
      Float

  override def randomFloat(): FloatSpecials = Float

  override def add(v1: FloatSpecials, v2: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = true,
      posInfinity = true,
      nan = v1.nan || v2.nan || (v1.negInfinity && v2.posInfinity) || (v1.posInfinity && v2.negInfinity)
    )

  override def sub(v1: FloatSpecials, v2: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = true,
      posInfinity = true,
      nan = v1.nan || v2.nan || (v1.posInfinity && v2.posInfinity) || (v1.negInfinity && v2.negInfinity)
    )

  override def mul(v1: FloatSpecials, v2: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = true,
      posInfinity = true,
      nan = v1.nan || v2.nan
    )

  override def div(v1: FloatSpecials, v2: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = v1.negInfinity || v1.posInfinity || v2.negInfinity || v2.posInfinity,
      posInfinity = v1.negInfinity || v1.posInfinity || v2.negInfinity || v2.posInfinity,
      nan = true
    )

  override def min(v1: FloatSpecials, v2: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = v1.negInfinity || v2.negInfinity,
      posInfinity = v1.posInfinity && v2.posInfinity,
      nan = v1.nan || v2.nan
    )

  override def max(v1: FloatSpecials, v2: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = v1.negInfinity && v2.negInfinity,
      posInfinity = v1.posInfinity || v2.posInfinity,
      nan = v1.nan || v2.nan
    )

  override def absolute(v: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = false,
      posInfinity = v.negInfinity || v.posInfinity,
      nan = v.nan
    )

  override def negated(v: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = v.posInfinity,
      posInfinity = v.negInfinity,
      nan = v.nan
    )

  override def sqrt(v: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = false,
      posInfinity = v.posInfinity,
      nan = true
    )

  override def ceil(v: FloatSpecials): FloatSpecials = v

  override def floor(v: FloatSpecials): FloatSpecials = v

  override def truncate(v: FloatSpecials): FloatSpecials = v

  override def nearest(v: FloatSpecials): FloatSpecials = v

  override def copysign(v: FloatSpecials, sign: FloatSpecials): FloatSpecials =
    FloatSpecials(
      negInfinity = v.negInfinity || v.posInfinity,
      posInfinity = v.negInfinity || v.posInfinity,
      nan = v.nan
    )
