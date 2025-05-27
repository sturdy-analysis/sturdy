package sturdy.values.floating

import sturdy.apron.{ApronCons, ApronExpr, ApronState, ApronType, RoundingDir, RoundingType, UnOp}
import sturdy.data.given
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*

trait RelationalFloatOps
  [
    L: Numeric,
    Addr: Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   f: Failure,
   typeFloatOps: FloatOps[L,Type]
  ) extends FloatOps[L, ApronExpr[Addr,Type]]:
  override def floatingLit(f: L): ApronExpr[Addr, Type] =
    ApronExpr.doubleLit(Numeric[L].toDouble(f), typeFloatOps.floatingLit(f))
  override def randomFloat(): ApronExpr[Addr, Type] =
    ApronExpr.doubleInterval(
      Numeric[L].toDouble(Numeric[L].fromInt(0)),
      Numeric[L].toDouble(Numeric[L].fromInt(1)),
      typeFloatOps.randomFloat())

  override def add(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatAdd(v1, v2)

  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatSub(v1, v2)

  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatMul(v1, v2)

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatDiv(v1, v2)

  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if (iv1.sup.cmp(iv2.inf) <= 0) {
      v2
    } else if (iv2.sup.cmp(iv1.inf) <= 0) {
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
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if (iv1.sup.cmp(iv2.inf) <= 0) {
      v1
    } else if (iv2.sup.cmp(iv1.inf) <= 0) {
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
    if (iv.inf.sgn() >= 0) {
      v
    } else if (iv.sup.sgn() < 0) {
      floatNegate(v)
    } else {
      floatSqrt(floatPow(v, intLit(2, v._type)))
    }

  override def negated(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatNegate(v)

  override def sqrt(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatSqrt(v)

  override def ceil(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    cast(v, RoundingType.Int, RoundingDir.Up, typeFloatOps.ceil(v._type))

  override def floor(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    cast(v, RoundingType.Int, RoundingDir.Down, typeFloatOps.floor(v._type))

  override def truncate(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    cast(v, RoundingType.Int, RoundingDir.Zero, typeFloatOps.truncate(v._type))

  override def nearest(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    cast(v, RoundingType.Int, RoundingDir.Nearest, typeFloatOps.nearest(v._type))

  override def copysign(v: ApronExpr[Addr, Type], sign: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(sign)
    if(iv.inf.sgn() >= 0) {
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