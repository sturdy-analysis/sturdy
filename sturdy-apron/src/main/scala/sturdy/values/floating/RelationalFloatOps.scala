package sturdy.values.floating

import sturdy.apron.{ApronCons, ApronExpr, ApronState, ApronType, RoundingDir, RoundingType, UnOp}
import sturdy.data.given
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.util.{Bounded, Enumerable}

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*
import apron.{DoubleScalar, Interval}
import sturdy.{IsSound, Soundness}

object FloatingLit:
  def apply[L: Numeric: Bounded, Addr, Type](f: L, tpe: Type): ApronExpr[Addr,Type] =
    val d = Numeric[L].toDouble(f)
    if (d.isPosInfinity)
      floatConstant(bottomInterval, FloatSpecials.PosInfinity, tpe)
    else if (d.isNegInfinity)
      floatConstant(bottomInterval, FloatSpecials.NegInfinity, tpe)
    else if(d.isNaN)
      floatConstant(bottomInterval, FloatSpecials.NaN, tpe)
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
  override def floatingLit(f: L): ApronExpr[Addr, Type] =
    FloatingLit(f, typeFloatOps.floatingLit(f))

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
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    handleOverflow(
      floatAdd(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.negInfinity || v2Specials.negInfinity,
          posInfinity = v1Specials.posInfinity || v2Specials.posInfinity,
          nan = v1Specials.nan
            || v2Specials.nan
            || (v1Specials.posInfinity && v2Specials.negInfinity)
            || (v1Specials.negInfinity && v2Specials.posInfinity)
        )
      )
    )

  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    handleOverflow(
      floatSub(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.negInfinity || v2Specials.posInfinity,
          posInfinity = v1Specials.posInfinity || v2Specials.negInfinity,
          nan = v1Specials.nan
            || v2Specials.nan
            || (v1Specials.posInfinity && v2Specials.posInfinity)
            || (v1Specials.negInfinity && v2Specials.negInfinity)
        )
      ))

  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    handleOverflow(
      floatMul(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.negInfinity || v1Specials.posInfinity || v2Specials.negInfinity || v2Specials.posInfinity,
          posInfinity = v1Specials.negInfinity || v1Specials.posInfinity || v2Specials.negInfinity || v2Specials.posInfinity,
          nan = v1Specials.nan || v2Specials.nan
        )
      ))

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val v1Specials = v1.floatSpecials
    val v2Specials = v2.floatSpecials
    val iv2 = apronState.getInterval(v2)
    handleOverflow(
      floatDiv(
        v1,
        v2,
        FloatSpecials(
          negInfinity = v1Specials.negInfinity || v1Specials.posInfinity || v2Specials.negInfinity || v2Specials.posInfinity,
          posInfinity = v1Specials.negInfinity || v1Specials.posInfinity || v2Specials.negInfinity || v2Specials.posInfinity,
          nan = v1Specials.nan || v2Specials.nan || Interval(0, 0).isLeq(iv2)
        )
      )
    )

  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getFloatInterval(v1)
    val iv2 = apronState.getFloatInterval(v2)
    if (iv1.sup().cmp(iv2.inf()) <= 0) {
      v2
    } else if (iv2.sup().cmp(iv1.inf()) <= 0) {
      v1
    } else {
      val resultType = typeFloatOps.max(v1._type, v2._type)
      apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
        apronState.ifThenElse(lt(x, y)) {
          apronState.assign(result, y)
        } {
          apronState.assign(result, x)
        }
        addr(result, resultType)
      }
    }

  override def min(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getFloatInterval(v1)
    val iv2 = apronState.getFloatInterval(v2)
    if (iv1.sup().cmp(iv2.inf()) <= 0) {
      v1
    } else if (iv2.sup().cmp(iv1.inf()) <= 0) {
      v2
    } else {
      val resultType = typeFloatOps.min(v1._type, v2._type)
      apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
        apronState.ifThenElse(lt(x, y)) {
          apronState.assign(result, x)
        } {
          apronState.assign(result, y)
        }
        addr(result, resultType)
      }
    }


  override def absolute(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    val specials = v.floatSpecials
    val returnSpecials = FloatSpecials(
      negInfinity = false,
      posInfinity = specials.negInfinity || specials.posInfinity,
      nan = specials.nan
    )
    if (iv.isBottom || iv.inf.sgn() >= 0) {
      v.setFloatSpecials(returnSpecials)
    } else if (iv.sup.sgn() < 0) {
      floatNegate(v, returnSpecials)
    } else {
      floatSqrt(floatPow(v, intLit(2, v._type), returnSpecials), returnSpecials)
    }

  override def negated(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val specials = v.floatSpecials
    floatNegate(
      v,
      FloatSpecials(
        negInfinity = specials.posInfinity,
        posInfinity = specials.negInfinity,
        nan = specials.nan
      )
    )

  override def sqrt(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getFloatInterval(v)
    val specials = v.floatSpecials
    floatSqrt(
      v,
      FloatSpecials(
        negInfinity = false,
        posInfinity = iv.floatSpecials.posInfinity,
        nan = specials.nan || iv.inf().sgn() < 0
      )
    )

  override def ceil(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatCast(v, RoundingType.Int, RoundingDir.Up, v.floatSpecials, typeFloatOps.ceil(v._type))

  override def floor(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatCast(v, RoundingType.Int, RoundingDir.Down, v.floatSpecials, typeFloatOps.floor(v._type))

  override def truncate(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatCast(v, RoundingType.Int, RoundingDir.Zero, v.floatSpecials, typeFloatOps.truncate(v._type))

  override def nearest(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatCast(v, RoundingType.Int, RoundingDir.Nearest, v.floatSpecials, typeFloatOps.nearest(v._type))

  override def copysign(v: ApronExpr[Addr, Type], sign: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getFloatInterval(sign)
    if(iv.inf().sgn() >= 0) {
      absolute(v)
    } else if(iv.sup().sgn() < 0) {
      negated(absolute(v))
    } else {
      val resultType = typeFloatOps.copysign(v._type, sign._type)
      apronState.withTempVars(resultType, sign) { case (result, List(s)) =>
        apronState.ifThenElse(le(doubleLit(0, s._type), s)) {
          apronState.assign(result, absolute(v))
        } {
          apronState.assign(result, negated(absolute(v)))
        }
        addr(result, resultType)
      }
    }

  def handleOverflow(v: ApronExpr[Addr,Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    var specials = v.floatSpecials
    val minVal = Numeric[L].toDouble(Bounded[L].minValue)
    val maxVal = Numeric[L].toDouble(Bounded[L].maxValue)
    if(iv.isBottom || iv.isLeq(Interval(minVal, maxVal))) {
      v
    } else {
      var underflow = false
      var overflow = false
      if (iv.inf.cmp(DoubleScalar(minVal)) < 0) {
        specials = specials.setNegInfinity(true)
        iv.setInf(DoubleScalar(minVal))
        if (iv.sup.cmp(DoubleScalar(minVal)) < 0)
          iv.setSup(DoubleScalar(minVal))
      }
      if (DoubleScalar(maxVal).cmp(iv.sup) < 0) {
        specials = specials.setPosInfinity(true)
        iv.setSup(DoubleScalar(maxVal))
        if (DoubleScalar(maxVal).cmp(iv.inf) < 0)
          iv.setInf(DoubleScalar(maxVal))
      }
      floatConstant(iv, specials, v._type)
    }


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