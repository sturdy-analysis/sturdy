package sturdy.values.integer

import gmp.*
import apron.*
import sturdy.data.{joinWithFailure, given}
import sturdy.apron.{ApronExpr, *, given}
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
import sturdy.values.config.{BitSign, UnsupportedConfiguration}
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
    apronState.ifThenElse(lt(v1, v2)) {
      v2
    } {
      v1
    }


  override def min(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    apronState.ifThenElse(lt(v1, v2)) {
      v1
    } {
      v2
    }

  override def absolute(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    apronState.ifThenElse(lt(lit(0, v._type), v)) {
      v
    } {
      handleOverflow(intNegate(v))
    }

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val iv = apronState.getInterval(v2)
    val res = if(! Interval(0,0).isLeq(iv)) {
      intDiv(v1, v2)
    } else {
      joinWithFailure {
        apronState.join {
          apronState.addConstraints(le(lit(1, v2._type), v2))
          intDiv(v1, v2)
        } {
          apronState.addConstraints(le(v2, lit(-1, v2._type)))
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
    apronState.ifThenElse(le(lit(0, v1._type), intMod(v1, v2))) {
      intMod(v1, v2)
    } {
      apronState.ifThenElse(le(lit(0, v2._type), v2)) {
        intAdd(intMod(v1, v2), v2)
      } {
        intAdd(intMod(v1, v2), intNegate(v2))
      }
    }

  override def shiftLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val numBits = v._type.byteSize * 8
    handleOverflow(intMul(v, intPow(lit(2, v._type), modulo(shift, lit(numBits, shift._type)))))

  override def shiftRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val ivV = apronState.getInterval(v)
    val modShift = modulo(shift, lit(v._type.byteSize * 8, shift._type))
    val ivShift = apronState.getInterval(modShift)
    val resultType = typeIntOps.shiftRight(v._type, shift._type)
    apronState.ifThenElse(ApronCons.le(lit(0, v._type), v)) {
      intDiv(v, intPow(lit(2, v._type), modShift), resultType)
    } {
      intSub(intDiv(intAdd(v, lit(1, v._type)), intPow(lit(2, v._type), modShift), resultType), lit(1, resultType))
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
        ApronExpr.lit[Addr,Type](1, resultType).asInstanceOf[ApronExpr[Addr,Type]]
      } {
        ApronExpr.lit[Addr,Type](0, resultType).asInstanceOf[ApronExpr[Addr,Type]]
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
    ApronExpr.interval(0, v._type.byteSize * 8, typeIntOps.countTrailingZeros(v._type))

  override def nonzeroBitCount(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronExpr.interval(0, v._type.byteSize * 8, typeIntOps.nonzeroBitCount(v._type))

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
    intSub(v, lit(signedMinValue(v._type.byteSize), v._type))

  def toSigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intAdd(v, lit(signedMinValue(v._type.byteSize), v._type))

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
    val sMin = signedMinValue(v._type.byteSize)
    val sMax = signedMaxValue(v._type.byteSize)
    val uMin = unsignedMinValue(v._type.byteSize)
    val uMax = unsignedMaxValue(v._type.byteSize)

    def inSignedRange(v: ApronExpr[Addr, Type]) =
      And(Constraint(le(lit(sMin, v._type), v)), Constraint(le(v, lit(sMax, v._type))))

    overflowHandling match
      case OverflowHandling.WrapAround =>
        val fromType = v._type


        apronState.ifThenElse(inSignedRange(v)) {
          v
        } {
          constant(ApronExpr.topInterval, fromType)
        }
      case OverflowHandling.Fail =>
        apronState.ifThenElse(inSignedRange(v)) {
          v
        } {
          Failure(IntegerOverflow, s"$v overflows bounds [${sMin},${sMax}]")
        }



  inline def interpretSignedAsUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr,Type] =
    interpretSignedAsUnsigned(v, v._type.byteSize)

  def interpretSignedAsUnsigned(v: ApronExpr[Addr, Type], fromNumBytes: Int): ApronExpr[Addr, Type] =
    val uMax = unsignedMaxValue(fromNumBytes)
    val fromType = v._type

    apronState.ifThenElse(le(lit(0, fromType), v)) {
      v
    } {
      intAdd(v, lit(uMax, fromType), fromType)
    }

  inline def interpretUnsignedAsSigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr,Type] =
    interpretUnsignedAsSigned(v, v._type.byteSize)

  def interpretUnsignedAsSigned(v: ApronExpr[Addr, Type], fromNumBytes: Int): ApronExpr[Addr, Type] =
    val sMax = signedMaxValue(fromNumBytes)
    val uMax = unsignedMaxValue(fromNumBytes)
    val fromType = v._type

    apronState.ifThenElse(le(v, lit(sMax, fromType))) {
      v
    } {
      intSub(v, lit(uMax, fromType), fromType)
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
    lit(i, typeIntOps.integerLit(i))

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
    lit(i, typeIntOps.integerLit(i))

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
    if(Interval(ApronExpr.scalar(c), ApronExpr.scalar(c)).isLeq(iv))
      IsSound.Sound
    else
      IsSound.NotSound(s"$expr with interval $iv does not contain $c")