package sturdy.values.integer

import gmp.*
import apron.*
import sturdy.data.{joinWithFailure, given}
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*
import ApronBool.*
import sturdy.effect.EffectStack
import sturdy.util.Lazy
import sturdy.{IsSound, Soundness}
import sturdy.values.config.{Bits, UnsupportedConfiguration}
import sturdy.values.integer.OverflowHandling.WrapAround

enum OverflowHandling:
  case WrapAround
  case Fail

trait RelationalBaseIntegerOps
    [
      L,
      Addr: Ordering: ClassTag,
      Type : ApronType : Join
    ]
    (using
       apronState: ApronState[Addr,Type],
       effectStack: EffectStack,
       f: Failure,
       overflowHandling: OverflowHandling,
       typeIntOps: IntegerOps[L,Type]
    ) extends IntegerOps[L, ApronExpr[Addr,Type]]:
  given Lazy[ApronState[Addr,Type]] = Lazy(apronState)

  override def add(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    handleOverflow(intAdd(v1, v2))

  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    handleOverflow(intSub(v1, v2))

  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    handleOverflow(intMul(v1, v2))


  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if(iv1.sup.cmp(iv2.inf) <= 0)
      v2
    else if(iv2.sup.cmp(iv1.inf) <= 0)
      v1
    else
      apronState.ifThenElse(lt(v1, v2)) {
        v2
      } {
        v1
      }


  override def min(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if (iv1.sup.cmp(iv2.inf) <= 0)
      v1
    else if (iv2.sup.cmp(iv1.inf) <= 0)
      v2
    else
      apronState.ifThenElse(lt(v1, v2)) {
        v1
      } {
        v2
      }

  override def absolute(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    if(iv.inf.sgn() >= 0)
      v
    else if(iv.sup.sgn() < 0)
      handleOverflow(intNegate(v))
    else
      handleOverflow(unary(UnOp.Sqrt, intPow(v, intLit(2, v._type)), typeIntOps.absolute(v._type)))

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v2)
    val res = if(! Interval(0,0).isLeq(iv)) {
      intDiv(v1, v2)
    } else {
      joinWithFailure {
        apronState.join {
          apronState.addConstraints(le(intLit(1, v2._type), v2))
          intDiv(v1, v2)
        } {
          apronState.addConstraints(le(v2, intLit(-1, v2._type)))
          intDiv(v1, v2)
        }
      } {
        Failure(IntegerDivisionByZero, s"divisor $v2 could be zero")
      }

    }
    handleOverflow(res)

  override def divUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    interpretUnsignedAsSigned(div(interpretSignedAsUnsigned(v1), interpretSignedAsUnsigned(v2)))


  override def remainder(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intMod(v1, v2)

  override def remainderUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    interpretUnsignedAsSigned(remainder(interpretSignedAsUnsigned(v1), interpretSignedAsUnsigned(v2)))

  override def modulo(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    if(iv1.inf().sgn() >= 0)
      intMod(v1, v2)
    else
      val iv2 = apronState.getInterval(v2)
      // We need an absolute without overflow, hence we cannot use this.absolute
      val absV2 = if(iv2.inf.sgn() >= 0) {
        v2
      } else if(iv2.sup.sgn() < 0) {
        intNegate(v2)
      } else {
        unary(UnOp.Sqrt, intPow(v2, intLit(2, v2._type)), typeIntOps.absolute(v2._type))
      }
      intMod(intAdd(intMod(v1, absV2), absV2), absV2)

  override def shiftLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val numBits = v._type.byteSize * 8
    handleOverflow(intMul(v, intPow(intLit(2, v._type), modulo(shift, intLit(numBits, shift._type)))))

  override def shiftRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val ivV = apronState.getInterval(v)
    val modShift = modulo(shift, intLit(v._type.byteSize * 8, shift._type))
    val ivShift = apronState.getInterval(modShift)
    val resultType = typeIntOps.shiftRight(v._type, shift._type)
    if(ivV.inf().sgn() >= 0)
      intDiv(v, intPow(intLit(2, v._type), modShift), resultType)
    else if(ivV.sup().sgn() < 0)
      intSub(intDiv(intAdd(v,intLit(1,v._type)), intPow(intLit(2, v._type), modShift), resultType), intLit(1, resultType))
    else
      apronState.ifThenElse(ApronCons.le(intLit(0, v._type), v)) {
        intDiv(v, intPow(intLit(2, v._type), modShift), resultType)
      } {
        intSub(intDiv(intAdd(v, intLit(1, v._type)), intPow(intLit(2, v._type), modShift), resultType), intLit(1, resultType))
      }

  override def shiftRightUnsigned(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    interpretUnsignedAsSigned(shiftRight(interpretSignedAsUnsigned(v), shift))

  override def rotateLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.rotateRight(v._type, shift._type))

  override def rotateRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.rotateRight(v._type, shift._type))

  override def gcd(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val absMax1 = absoluteMax(apronState.getInterval(v1))
    val absMax2 = absoluteMax(apronState.getInterval(v2))
    val sup = max(absMax1, absMax2)
    handleOverflow(ApronExpr.constant(Interval(DoubleScalar(0.0), sup), typeIntOps.gcd(v1._type, v2._type)))

  private def absoluteMax(iv: Interval): Scalar =
    max(abs(iv.inf), abs(iv.sup))

  def abs(v: Scalar): Scalar =
    if(v.sgn() < 0) {
      v.neg(); v
    } else {
      v
    }

  def max(v1: Scalar, v2: Scalar): Scalar =
    if(v1.cmp(v2) <= 0) v2 else v1

  override def bitAnd(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if(iv1.isLeq(Interval(0,1)) && iv2.isLeq(Interval(0,1)))
      mul(v1, v2)
    else
      ApronExpr.top(typeIntOps.bitAnd(v1._type, v2._type))

  override def bitOr(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    if(iv1.isLeq(Interval(0,1)) && iv2.isLeq(Interval(0,1)))
      max(v1, v2)
    else
      ApronExpr.top(typeIntOps.bitAnd(v1._type, v2._type))

  override def bitXor(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv1 = apronState.getInterval(v1)
    val iv2 = apronState.getInterval(v2)
    val resultType = typeIntOps.bitXor(v1._type, v2._type)
    if (iv1.isLeq(Interval(0, 1)) && iv2.isLeq(Interval(0, 1)))
      apronState.ifThenElse(neq(v1, v2)) {
        ApronExpr.intLit[Addr,Type](1, resultType).asInstanceOf[ApronExpr[Addr,Type]]
      } {
        ApronExpr.intLit[Addr,Type](0, resultType).asInstanceOf[ApronExpr[Addr,Type]]
      }
    else
      ApronExpr.top(typeIntOps.bitAnd(v1._type, v2._type))

  override def countLeadingZeros(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val (low,high) = apronState.getLongInterval(v)
    val byteSize = v._type.byteSize
    val leadingZerosHigh = if(byteSize == 4) Integer.numberOfLeadingZeros(high.intValue) else java.lang.Long.numberOfLeadingZeros(high)
    val leadingZerosLow = if(byteSize == 4) Integer.numberOfLeadingZeros(low.intValue) else java.lang.Long.numberOfLeadingZeros(low)
    val inf = math.min(leadingZerosLow, leadingZerosHigh)
    val sup = if(low <= 0 && high >= 0) byteSize * 8 else math.max(leadingZerosLow, leadingZerosHigh)
    ApronExpr.constant(Interval(inf,sup), typeIntOps.countTrailingZeros(v._type))

  override def countTrailingZeros(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronExpr.intInterval(0, v._type.byteSize * 8, typeIntOps.countTrailingZeros(v._type))

  override def nonzeroBitCount(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronExpr.intInterval(0, v._type.byteSize * 8, typeIntOps.nonzeroBitCount(v._type))

  override def invertBits(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.invertBits(v._type))


  def signedMinValue(numBytes: Int): BigInt =
    -BigInt(2).pow(numBytes * 8 - 1)

  def signedMaxValue(numBytes: Int): BigInt =
    BigInt(2).pow(numBytes * 8 - 1) - 1

  def unsignedMinValue(numBytes: Int): BigInt =
    0

  def unsignedMaxValue(numBytes: Int): BigInt =
    BigInt(2).pow(numBytes * 8)

  def toUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intSub(v, bigIntLit(signedMinValue(v._type.byteSize), v._type))

  def toSigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intAdd(v, bigIntLit(signedMinValue(v._type.byteSize), v._type))

  val infty = new MpqScalar();
  infty.setInfty(1)

  def castTo(v: ApronExpr[Addr,Type], toType: Type): ApronExpr[Addr, Type] =
    val fromType = v._type
    if(fromType == toType)
      v
    else
      cast(v, RoundingType.Int, RoundingDir.Zero, toType)

  /**
   * Maps a whole number to a fixed-size integer by folding over- and underflows.
   */
  def handleOverflow(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)

    val sMin = signedMinValue(v._type.byteSize).bigInteger
    val sMax = signedMaxValue(v._type.byteSize).bigInteger
    val signedRange = Interval(sMin, sMax)
    val uMin = unsignedMinValue(v._type.byteSize).bigInteger
    val uMax = unsignedMaxValue(v._type.byteSize).bigInteger

    overflowHandling match
      case OverflowHandling.WrapAround =>
        val fromType = v._type

        inline def inSignedRange(v: ApronExpr[Addr,Type]) = And(Constraint(le(constant(MpqScalar(sMin), v._type), v)), Constraint(le(v, constant(MpqScalar(sMax), v._type))))

        apronState.ifThenElse(inSignedRange(v)) {
          v
        } {
          constant(Interval(MpqScalar(sMin), MpqScalar(sMax)), fromType)
        }
      case OverflowHandling.Fail =>
        if (! iv.isLeq(signedRange)) {
          effectStack.joinWithFailure(v) {
            Failure(IntegerOverflow, s"$v with interval $iv overflows bounds ${signedRange}")
          }
        } else {
          v
        }


  inline def interpretSignedAsUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr,Type] =
    interpretSignedAsUnsigned(v, v._type.byteSize)

  def interpretSignedAsUnsigned(v: ApronExpr[Addr, Type], fromNumBytes: Int): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    val uMax = unsignedMaxValue(fromNumBytes)
    val resultType = v._type
    if(iv.inf.sgn() >= 0) { // v >= 0
      v
    } else if(iv.sup.sgn() < 0) { // v < 0
      intAdd(v, bigIntLit(uMax, resultType), resultType)
    } else { // iv.inf < 0 <= iv.sup
      intAdd(v, constant(Interval(MpqScalar(0), MpqScalar(uMax.bigInteger)), resultType), resultType)
    }

  inline def interpretUnsignedAsSigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr,Type] =
    interpretUnsignedAsSigned(v, v._type.byteSize)

  def interpretUnsignedAsSigned(v: ApronExpr[Addr, Type], fromNumBytes: Int): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v)
    val sMax = signedMaxValue(fromNumBytes)
    val uMax = unsignedMaxValue(fromNumBytes)
    val resultType = v._type
    if(iv.sup.cmp(MpqScalar(sMax.bigInteger)) <= 0) { // v <= signedMax
      v
    } else if(iv.inf.cmp(MpqScalar(sMax.bigInteger)) > 0) { // v > signedMax
      intSub(v, bigIntLit(uMax, resultType), resultType)
    } else { // iv.inf <= signedMax < iv.sup
      intSub(v, constant(Interval(MpqScalar(0), MpqScalar(uMax.bigInteger)), resultType), resultType)
    }

given RelationalIntOps
  [
    Addr : Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
     apronState: ApronState[Addr,Type],
     effectStack: EffectStack,
     f: Failure,
     overflowHandling: OverflowHandling,
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
    effectStack: EffectStack,
    f: Failure,
    overflowHandling: OverflowHandling,
    typeIntOps: IntegerOps[Long,Type]
  ): RelationalBaseIntegerOps[Long, Addr, Type] with

  override def integerLit(i: Long): ApronExpr[Addr, Type] =
    longLit(i, typeIntOps.integerLit(i))

  override def randomInteger(): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.randomInteger())


given SoundnessIntApronExpr[Addr, Type](using apronState: ApronState[Addr, Type]): Soundness[Int, ApronExpr[Addr, Type]] with
  override def isSound(c: Int, expr: ApronExpr[Addr, Type]): IsSound =
    val iv = apronState.getInterval(expr)
    if (Interval(c, c).isLeq(iv))
      IsSound.Sound
    else
      IsSound.NotSound(s"$expr with interval $iv does not contain $c")

given SoundnessLongApronExpr[Addr, Type](using apronState: ApronState[Addr,Type]): Soundness[Long, ApronExpr[Addr,Type]] with
  override def isSound(c: Long, expr: ApronExpr[Addr, Type]): IsSound =
    val iv = apronState.getInterval(expr)
    val bc = BigInt(c).bigInteger
    if(Interval(bc, bc).isLeq(iv))
      IsSound.Sound
    else
      IsSound.NotSound(s"$expr with interval $iv does not contain $c")