package sturdy.values.floating

import apron.{DoubleScalar, Interval}

import scala.reflect.ClassTag
import sturdy.{IsSound, Soundness}
import sturdy.apron.{*, given}
import sturdy.data.given
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.util.{Bounded, Enumerable, Lazy}

import java.lang.Double as JDouble
import ApronExpr.*
import ApronCons.*

object FloatingLit:
  def apply[L: Numeric: Bounded, Addr, Type](f: L, tpe: Type): ApronExpr[Addr,Type] =
    val d = Numeric[L].toDouble(f)
    if (d.isPosInfinity)
      floatConstant(bottomInterval, FloatSpecials.PosInfinity, tpe)
    else if (d.isNegInfinity)
      floatConstant(bottomInterval, FloatSpecials.NegInfinity, tpe)
    else if(d.isNaN)
      floatConstant(bottomInterval, FloatSpecials.NaN, tpe)
    else if(JDouble.doubleToRawLongBits(d) == JDouble.doubleToRawLongBits(-0.0d))
      floatConstant(DoubleScalar(0), FloatSpecials.NegZero, tpe)
    else
      floatConstant(DoubleScalar(Numeric[L].toDouble(f)), FloatSpecials.Bottom, tpe)


given RelationalFloatOps
  [
    L: Numeric: Bounded,
    Addr: Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   f: Failure,
   typeFloatOps: FloatOps[L,Type]
  ): FloatOps[L, ApronExpr[Addr,Type]] with

  given Lazy[ApronState[Addr,Type]] = Lazy(apronState)

  override def floatingLit(f: L): ApronExpr[Addr, Type] =
    FloatingLit(f, typeFloatOps.floatingLit(f))

  // TODO: is this correct?
  override def NaN: ApronExpr[Addr, Type] = floatConstant(
    Interval(
      Numeric[L].toDouble(Numeric[L].fromInt(0)),
      Numeric[L].toDouble(Numeric[L].fromInt(1))
    ),
    FloatSpecials.Bottom,
    typeFloatOps.NaN
  )

  // TODO: is this correct?
  override def posInfinity: ApronExpr[Addr, Type] = floatConstant(
    Interval(
      Numeric[L].toDouble(Numeric[L].fromInt(0)),
      Numeric[L].toDouble(Numeric[L].fromInt(1))
    ),
    FloatSpecials.Bottom,
    typeFloatOps.posInfinity
  )

  // TODO: is this correct?
  override def negInfinity: ApronExpr[Addr, Type] = floatConstant(
    Interval(
      Numeric[L].toDouble(Numeric[L].fromInt(0)),
      Numeric[L].toDouble(Numeric[L].fromInt(1))
    ),
    FloatSpecials.Bottom,
    typeFloatOps.negInfinity
  )

  override def randomFloat(): ApronExpr[Addr, Type] =
    floatConstant(
      Interval(
        Numeric[L].toDouble(Numeric[L].fromInt(0)),
        Numeric[L].toDouble(Numeric[L].fromInt(1))
      ),
      FloatSpecials.Bottom,
      typeFloatOps.randomFloat()
    )

  override def add(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // -∞   + -∞   = -∞
    // -∞   +  ∞   =  NaN
    //  ∞   + -∞   =  NaN
    //  ∞   +  ∞   =  ∞
    //  ∞   +  x   =  ∞
    // -∞   +  x   = -∞
    // -0.0 + -0.0 = -0.0
    // -0.0 +  0.0 =  0.0
    //  0.0 + -0.0 =  0.0
    //  x   + -0.0 =  x
    // -0.0 +  y   =  y
    //  NaN +  y   =  NaN
    //  x   +  NaN =  NaN
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    checkForNewFloatSpecials(
      floatAdd(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.negInfinity || v2Specials.negInfinity,
          posInfinity = v1Specials.posInfinity || v2Specials.posInfinity,
          negZero = v1Specials.negZero && v2Specials.negZero,
          nan = v1Specials.nan
            || v2Specials.nan
            || (v1Specials.posInfinity && v2Specials.negInfinity)
            || (v1Specials.negInfinity && v2Specials.posInfinity)
        )
      )
    )


  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // -∞   - -∞   =  NaN
    // -∞   -  ∞   = -∞
    //  ∞   - -∞   =  ∞
    //  ∞   -  ∞   =  NaN
    //  ∞   -  x   =  ∞
    // -∞   -  x   = -∞
    // -0.0 - -0.0 =  0.0
    // -0.0 -  0.0 = -0.0
    //  0.0 - -0.0 =  0.0
    //  0.0 -  0.0 =  0.0 !!!
    // -0.0 -    y = -y
    //  x   - -0.0 =  x
    //  NaN -  y   =  NaN
    //  x   -  NaN =  NaN
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    checkForNewFloatSpecials(
      floatSub(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.negInfinity || v2Specials.posInfinity,
          posInfinity = v1Specials.posInfinity || v2Specials.negInfinity,
          negZero = v1Specials.negZero,
          nan = v1Specials.nan
            || v2Specials.nan
            || (v1Specials.posInfinity && v2Specials.posInfinity)
            || (v1Specials.negInfinity && v2Specials.negInfinity)
        )
      )
    )



  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // -∞   * -∞   =  ∞
    // -∞   *  ∞   = -∞
    //  ∞   * -∞   = -∞
    //  ∞   *  ∞   =  ∞
    // inf  *  0.0 = NaN
    // inf  * -0.0 = NaN
    //  ∞   * neg  = -∞
    //  ∞   * pos  =  ∞
    //  0.0 * neg  = -0.0
    //  0.0 * pos  =  0.0
    // -0.0 * pos  = -0.0
    // -0.0 * neg  =  0.0
    //  NaN *  y   =  NaN
    //  x   *  NaN =  NaN
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    checkForNewFloatSpecials(
      floatMul(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.isInfinite || v2Specials.isInfinite,
          posInfinity = v1Specials.isInfinite || v2Specials.isInfinite,
          negZero = true,
          nan = v1Specials.nan
            || v2Specials.nan
            || v1Specials.isInfinite || v2Specials.isInfinite
        )
      )
    )

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // -∞   / -∞   = NaN
    // -∞   /  ∞   = NaN
    //  ∞   / -∞   = NaN
    //  ∞   /  ∞   = NaN
    // pos  / -∞   = -0.0
    // neg  / -∞   =  0.0
    // pos  /  ∞   =  0.0
    // neg  /  ∞   = -0.0
    // -∞   /  0.0 = -∞  !!!
    //  ∞   /  0.0 =  ∞  !!!
    // -∞   / -0.0 =  ∞  !!!
    //  ∞   / -0.0 = -∞  !!!
    //  x   /  0.0 = NaN
    //  x   / -0.0 = NaN
    //  0.0 / neg  = -0.0
    //  0.0 / pos  =  0.0
    // -0.0 / pos  = -0.0
    // -0.0 / neg  =  0.0
    //  NaN /  y   =  NaN
    //  x   /  NaN =  NaN
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    val iv2 = apronState.getInterval(v2)
    val result = checkForNewFloatSpecials(
      floatDiv(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.isInfinite,
          posInfinity = v1Specials.isInfinite,
          negZero = true,
          nan = v1Specials.nan
            || v2Specials.nan
            || (v1Specials.isInfinite && v2Specials.isInfinite)
            || v2Specials.negZero
            || containsZero(iv2)
        )
      )
    )
    if(v2Specials.isInfinite)
      ensureResultContains(0.0, result)
    else
      result

  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // max(-∞,   -∞  ) = -∞
    // max(-∞,    y  ) =  y
    // max( ∞,    y  ) =  ∞
    // max(-0.0, -0.0) = -0.0
    // max(-0.0,  0.0) =  0.0
    // max(-0.0, -1.0) = -0.0
    // max( NaN,  y  ) =  NaN
    // max( x,    NaN) =  NaN
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    val resultSpecials = FloatSpecials(
      negInfinity = v1Specials.negInfinity && v2Specials.negInfinity,
      posInfinity = v1Specials.posInfinity || v2Specials.posInfinity,
      negZero = v1Specials.negZero || v2Specials.negZero,
      nan = v1Specials.nan || v2Specials.nan
    )
    if(v1Specials.negInfinity || v2Specials.negInfinity) {
      Join(v1, v2).get.setFloatSpecials(resultSpecials)
    } else if (iv1.isBottom || iv1.sup().cmp(iv2.inf()) <= 0) {
      v2.setFloatSpecials(resultSpecials)
    } else if (iv2.isBottom || iv2.sup().cmp(iv1.inf()) <= 0) {
      v1.setFloatSpecials(resultSpecials)
    } else {
      apronState.ifThenElse(lt(v1,v2)) {
        v2
      } {
        v1
      }
    }

  override def min(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    val resultSpecials = FloatSpecials(
      negInfinity = v1Specials.negInfinity || v2Specials.negInfinity,
      posInfinity = v1Specials.posInfinity && v2Specials.posInfinity,
      negZero = v1Specials.negZero || v2Specials.negZero,
      nan = v1Specials.nan || v2Specials.nan
    )
    if (v1Specials.posInfinity || v2Specials.posInfinity)
      Join(v1, v2).get.setFloatSpecials(resultSpecials)
    else if (iv1.isBottom)
      v2.setFloatSpecials(resultSpecials)
    else if(iv2.isBottom)
      v1.setFloatSpecials(resultSpecials)
    else if (iv1.sup().cmp(iv2.inf()) <= 0)
      v1.setFloatSpecials(resultSpecials)
    else if (iv2.sup().cmp(iv1.inf()) <= 0)
      v2.setFloatSpecials(resultSpecials)
    else
      apronState.ifThenElse(lt(v1, v2)) {
        v1
      } {
        v2
      }


  override def absolute(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // absolute(-∞  ) =  ∞
    // absolute( ∞  ) =  ∞
    // absolute(-0.0) =  0.0
    // absolute( NaN) =  NaN
    // absolute( pos) =  pos
    // absolute( neg) = -neg
    val iv = apronState.getInterval(v)
    val specials = v.floatSpecials
    val returnSpecials = FloatSpecials(
      negInfinity = false,
      posInfinity = specials.negInfinity || specials.posInfinity,
      negZero = false,
      nan = specials.nan
    )
    if (iv.isBottom || iv.inf.sgn() >= 0) {
      v.setFloatSpecials(returnSpecials)
    } else if (iv.sup.sgn() < 0) {
      floatNegate(v, returnSpecials)
    } else {
      floatSqrt(floatPow(v, lit(2, v._type), returnSpecials), returnSpecials)
    }

  override def negated(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // negated(-∞  ) =  ∞
    // negated( ∞  ) = -∞
    // negated(-0.0) =  0.0
    // negated( 0.0) = -0.0
    // negated( NaN) =  NaN
    // negated( pos) =  neg
    // negated( neg) =  pos
    val specials = v.floatSpecials
    checkForNewFloatSpecials(
      floatNegate(
        v,
        FloatSpecials(
          negInfinity = specials.posInfinity,
          posInfinity = specials.negInfinity,
          negZero = true,
          nan = specials.nan
        )
      )
    )

  override def sqrt(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // sqrt(-∞  ) =  NaN
    // sqrt( ∞  ) =  ∞
    // sqrt(-0.0) = -0.0
    // sqrt( 0.0) =  0.0
    // sqrt( NaN) =  NaN
    val iv = apronState.getFloatInterval(v)
    val specials = v.floatSpecials
    floatSqrt(
      v,
      FloatSpecials(
        negInfinity = false,
        posInfinity = iv.floatSpecials.posInfinity,
        negZero = specials.negZero,
        nan = specials.nan || iv.inf().sgn() < 0
      )
    )

  override def ceil(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // ceil(-∞  ) = -∞
    // ceil( ∞  ) =  ∞
    // ceil(-0.0) = -0.0
    // ceil( NaN) =  NaN
    // ceil(-0.5) = -0.0 !!!
    floatCast(v, RoundingType.Int, RoundingDir.Up, v.floatSpecials.setNegZero(true), typeFloatOps.ceil(v._type))

  override def floor(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // floor(-∞  ) = -∞
    // floor( ∞  ) =  ∞
    // floor(-0.0) = -0.0
    // floor( NaN) =  NaN
    floatCast(v, RoundingType.Int, RoundingDir.Down, v.floatSpecials, typeFloatOps.floor(v._type))

  override def truncate(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // truncate(-∞  ) = -∞
    // truncate( ∞  ) =  ∞
    // truncate(-0.0) = -0.0
    // truncate( NaN) =  NaN
    // truncate(-0.5) = -0.0 !!!
    floatCast(v, RoundingType.Int, RoundingDir.Zero, v.floatSpecials.setNegZero(true), typeFloatOps.truncate(v._type))

  override def nearest(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // truncate(-∞  ) = -∞
    // truncate( ∞  ) =  ∞
    // truncate(-0.0) = -0.0
    // truncate( NaN) =  NaN
    // truncate(-0.5) = -0.0 !!!
    floatCast(v, RoundingType.Int, RoundingDir.Nearest, v.floatSpecials.setNegZero(true), typeFloatOps.nearest(v._type))

  override def copysign(v: ApronExpr[Addr, Type], sign: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    // copySign(-∞  , pos) =  ∞
    // copySign(-∞  , neg) = -∞
    // copySign( ∞  , pos) =  ∞
    // copySign( ∞  , neg) = -∞
    // copySign(-0.0, pos) =  0.0
    // copySign(-0.0, neg) = -0.0
    // copySign( 0.0, neg) = -0.0
    // copySign( 0.0, pos) =  0.0
    // copySign( neg,+NaN) = -neg !!!
    // copySign( pos,-NaN) = -pos !!!

    val ivSign = apronState.getFloatInterval(sign)
    val vSpecials = v.floatSpecials
    val signSpecials = sign.floatSpecials

    if(ivSign.inf().sgn() >= 0 && !signSpecials.negZero && !signSpecials.nan) {
      absolute(v).setNegZero(true)
    } else if(ivSign.sup().sgn() < 0 && !signSpecials.nan) {
      negated(absolute(v)).setNegZero(true)
    } else {
      apronState.join {
        v
      } {
        negated(v)
      }.setFloatSpecials(
        FloatSpecials(
          negInfinity = vSpecials.isInfinite,
          posInfinity = vSpecials.isInfinite,
          negZero = true,
          nan = vSpecials.nan
        )
      )
    }

  def checkForNewFloatSpecials(v: ApronExpr[Addr,Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    if(iv.isBottom) {
      v
    } else {
      var specials = v.floatSpecials
      val minVal = Numeric[L].toDouble(Bounded[L].minValue)
      val maxVal = Numeric[L].toDouble(Bounded[L].maxValue)
      if (iv.inf.cmp(DoubleScalar(minVal)) < 0) {
        specials = specials.setNegInfinity(true)
      }
      if (DoubleScalar(maxVal).cmp(iv.sup) < 0) {
        specials = specials.setPosInfinity(true)
      }
      if(Interval(0,0).isLeq(iv)) {
        specials = specials.setNegZero(true)
      }
      v.setFloatSpecials(specials)
    }


  private def ensureResultContains(d: Double, v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    if (d.isNegInfinity)
      v.setNegInfinity(true)
    else if (d.isPosInfinity)
      v.setPosInfinity(true)
    else if (d.isNaN)
      v.setNaN(true)
    else if (JDouble.doubleToRawLongBits(d) == JDouble.doubleToRawLongBits(-0.0d))
      v.setNegZero(true)
    else
      val iv = apronState.getInterval(v)
      if (Interval(d, d).isLeq(iv))
        v
      else if (iv.isBottom)
        floatConstant(Interval(d, d), v.floatSpecials, v._type)
      else
        val resultType = v._type
        apronState.withTempVars(resultType) { case (result, List()) =>
          apronState.join {
            apronState.assign(result, v)
          } {
            apronState.assign(result, lit(d, resultType))
          }
          Addr(ApronVar(result), v.floatSpecials, resultType)
        }


  private inline def containsZero(iv: Interval): Boolean =
      Interval(0.0d, 0.0d).isLeq(iv)


given SoundnessFloatApronExpr[Addr, Type](using apronState: ApronState[Addr, Type]): Soundness[Float, ApronExpr[Addr, Type]] with
  override def isSound(c: Float, expr: ApronExpr[Addr, Type]): IsSound =
    val iv = this.apronState.getFloatInterval(expr)
    if (sturdy.apron.FloatInterval(c.toDouble).isLeq(iv))
      IsSound.Sound
    else
      IsSound.NotSound(s"$expr with interval $iv does not contain $c")

given SoundnessDoubleApronExpr[Addr, Type](using apronState: ApronState[Addr, Type]): Soundness[Double, ApronExpr[Addr, Type]] with
  override def isSound(c: Double, expr: ApronExpr[Addr, Type]): IsSound =
    val iv = this.apronState.getFloatInterval(expr)
    if(sturdy.apron.FloatInterval(c).isLeq(iv))
      IsSound.Sound
    else
      IsSound.NotSound(s"$expr with interval $iv does not contain $c")