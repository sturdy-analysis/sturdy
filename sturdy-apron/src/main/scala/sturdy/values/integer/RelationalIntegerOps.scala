package sturdy.values.integer

import apron.{Interval, MpqScalar}
import sturdy.data.given
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*

trait RelationalBaseIntegerOps
    [
      L,
      Addr: Ordering: ClassTag,
      Type : ApronType : Join
    ]
    (using
       apronState: ApronState[Addr,Type],
       f: Failure,
       typeIntOps: IntegerOps[L,Type]
    ) extends IntegerOps[L, ApronExpr[Addr,Type]]:

  override def add(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    toFixedSize(intAdd(v1, v2))

  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    toFixedSize(intSub(v1, v2))

  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    toFixedSize(intMul(v1, v2))


  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if(iv1.sup.cmp(iv2.inf) <= 0) {
      v2
    } else if(iv2.sup.cmp(iv1.inf) <= 0) {
      v1
    } else {
      val resultType = typeIntOps.max(v1._type, v2._type)
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
      val resultType = typeIntOps.min(v1._type, v2._type)
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
    if(iv.inf.sgn() >= 0) {
      v
    } else if(iv.sup.sgn() < 0) {
      toFixedSize(intNegate(v))
    } else {
      toFixedSize(unary(UnOp.Sqrt, intPow(v, intLit(2, v._type)), typeIntOps.absolute(v._type)))
    }

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v2)
    val res = if(! Interval(0,0).isLeq(iv)) {
      intDiv(v1, v2)
    } else {
      val resultType = typeIntOps.div(v1._type, v2._type)
      apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
        apronState.join {
          apronState.addConstraint(lt(intLit(0, y._type), y))
          apronState.assign(result, intDiv(x, y))
        } {
          apronState.addConstraint(lt(y, intLit(0, y._type)))
          apronState.assign(result, intDiv(x, y))
        }
        addr(result, resultType)
      }
    }
    toFixedSize(res)

  override def divUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    interpretUnsignedAsSigned(div(interpretSignedAsUnsigned(v1), interpretSignedAsUnsigned(v2)))


  override def remainder(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v2)
    if (!Interval(0, 0).isLeq(iv)) {
      intMod(v1, v2)
    } else {
      val resultType = typeIntOps.remainder(v1._type, v2._type)
      apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
        apronState.join {
          apronState.addConstraint(lt(intLit(0, y._type), y))
          apronState.assign(result, intMod(x, y))
        } {
          apronState.addConstraint(lt(y, intLit(0, y._type)))
          apronState.assign(result, intMod(x, y))
        }
        addr(result, resultType)
      }
    }


  override def remainderUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    interpretUnsignedAsSigned(remainder(interpretSignedAsUnsigned(v1), interpretSignedAsUnsigned(v2)))

  override def modulo(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val absV2 = absolute(v2)
    intMod(intAdd(intMod(v1, absV2), absV2), absV2)
//
//
//    apronState.withTempVars(resultType, remainder(v1, v2)) { case (result, List(x)) =>
//      apronState.ifThenElse(lt(x, intLit(0, x._type))) {
//        apronState.assign(result, intAdd(x, v2))
//      } {
//        apronState.assign(result, x)
//      }
//      addr(result, resultType)
//    }

  override def shiftLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    toFixedSize(intMul(v, intPow(intLit(2, v._type), modulo(shift, intLit(32, shift._type)))))

  override def shiftRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intDiv(v, intPow(intLit(2, v._type), modulo(shift, intLit(32, shift._type))))

  override def shiftRightUnsigned(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    interpretUnsignedAsSigned(shiftRight(interpretSignedAsUnsigned(v), shift))

  override def rotateLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def rotateRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def gcd(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def bitAnd(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def bitOr(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def bitXor(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def countLeadingZeros(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.countLeadingZeros(v._type)
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      apronState.ifThenElse(lt(intLit(0, x._type), x)) {
        apronState.assign(result, intSub(intLit(v._type.byteSize * 8, resultType), mostSignificantBit(x)))
      } {
        apronState.assign(result, intLit(0, resultType))
      }
      addr(result, resultType)
    }

  def mostSignificantBit(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intAdd(log(2, v), intLit(1, v._type))

  def log(n: Int, v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.countLeadingZeros(v._type)
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      val resultExpr = addr(result, resultType)
      apronState.assign(result, ApronExpr.top(resultType))
      apronState.addConstraint(ApronCons.eq(intPow(intLit(n, resultExpr._type), resultExpr), x))
      resultExpr
    }

  override def countTrailingZeros(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def nonzeroBitCount(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def invertBits(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???


  private def signedMinValue(tpe: Type): BigInt =
    -BigInt(2).pow(tpe.byteSize * 8 - 1)

  private def signedMaxValue(tpe: Type): BigInt =
    BigInt(2).pow(tpe.byteSize * 8 - 1) - 1

  private def unsignedMinValue(tpe: Type): BigInt =
    0

  private def unsignedMaxValue(tpe: Type): BigInt =
    BigInt(2).pow(tpe.byteSize * 8)

  private def toUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intSub(v, bigIntLit(signedMinValue(v._type), v._type))

  private def toSigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intAdd(v, bigIntLit(signedMinValue(v._type), v._type))

  val infty = new MpqScalar();
  infty.setInfty(1)

  /**
   * Maps a whole number to a fixed-size integer by folding over- and underflows.
   */
  private def toFixedSize(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)

    // Interval within range of the fixed-size integer
    if (iv.isLeq(Interval(signedMinValue(v._type).bigInteger, signedMaxValue(v._type).bigInteger))) {
      v

      // No underflow
    } else if (iv.isLeq(Interval(MpqScalar(signedMinValue(v._type).bigInteger), infty))) {
      val uMax = bigIntLit[Addr, Type](unsignedMaxValue(v._type), v._type)
      toSigned(intMod(toUnsigned(v), uMax))

      // Over and underflow
    } else {
      // Apron doesn't have a modulo operator with a positive domain, i.e., negative numbers are left unchanged.
      // To solve this, we apply the modulo operator for a second time, such that negative numbers from -1 to -unsignedMaxValue are folded.
      val uMax = bigIntLit[Addr, Type](unsignedMaxValue(v._type), v._type)
      val foldFirstRound = intMod(toUnsigned(v), uMax)
      val foldSecondRound = intMod(intAdd(foldFirstRound, uMax), uMax)
      toSigned(foldSecondRound)
    }

  private def interpretSignedAsUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    val uMax = bigIntLit[Addr, Type](unsignedMaxValue(v._type), v._type)
    if(iv.inf.sgn() >= 0) {
      v
    } else if(iv.sup.sgn() < 0) {
      intAdd(v, uMax)
    } else {
      val resultType = typeIntOps.divUnsigned(v._type, v._type)
      apronState.withTempVars(resultType, v) { case (result, List(x)) =>
        apronState.ifThenElse(lt(x, intLit(0, x._type))) {
          apronState.assign(result, intAdd(x, uMax))
        } {
          apronState.assign(result, x)
        }
        addr(result, resultType)
      }
    }


  private def interpretUnsignedAsSigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    val sMax = signedMaxValue(v._type)
    val uMax = unsignedMaxValue(v._type)
    if(iv.sup.cmp(MpqScalar(sMax.bigInteger)) <= 0) {
      v
    } else if(iv.inf.cmp(MpqScalar(sMax.bigInteger)) > 0) {
      intSub(v, bigIntLit(uMax, v._type))
    } else {
      val resultType = typeIntOps.divUnsigned(v._type, v._type)
      apronState.withTempVars(resultType, v) { case (result, List(x)) =>
        apronState.ifThenElse(lt(bigIntLit(sMax, x._type), x)) {
          apronState.assign(result, intSub(x, bigIntLit(uMax, x._type)))
        } {
          apronState.assign(result, x)
        }
        addr(result, resultType)
      }
    }



given RelationalIntOps
  [
    Addr : Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
    apronState: ApronState[Addr,Type],
    f: Failure,
    typeIntOps: IntegerOps[Int,Type]
  ): RelationalBaseIntegerOps[Int, Addr, Type] with

  override def integerLit(i: Int): ApronExpr[Addr, Type] =
    intLit(i, typeIntOps.integerLit(i))

  override def randomInteger(): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.randomInteger())


given RelationalLongOps
  [
    Addr : Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   f: Failure,
   typeIntOps: IntegerOps[Long,Type]
  ): RelationalBaseIntegerOps[Long, Addr, Type] with

  override def integerLit(i: Long): ApronExpr[Addr, Type] =
    longLit(i, typeIntOps.integerLit(i))

  override def randomInteger(): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.randomInteger())