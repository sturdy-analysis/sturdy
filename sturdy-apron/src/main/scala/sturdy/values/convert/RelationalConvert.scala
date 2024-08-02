package sturdy.values.convert

import apron.*
import gmp.*
import sturdy.apron.ApronExpr.*
import sturdy.apron.ApronCons.*
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.util.{*, given}
import sturdy.values.{config, *, given}
import sturdy.values.config.{Bits, *, given}
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.apron.{FloatInterval => FInterval}

import scala.math.BigInt.javaBigInteger2bigInt
import java.math.BigInteger
import java.nio.ByteOrder
import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

given RelationalConvertIntLong[Addr, Type: ApronType](using failure: Failure, intOps: RelationalIntOps[Addr,Type], convertType: ConvertIntLong[Type, Type]): ConvertIntLong[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] = {
  case (from, conf@Bits.Signed)   => cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
  case (from, conf@Bits.Unsigned) => cast(intOps.interpretSignedAsUnsigned(from), RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
  case (_,    conf@Bits.Raw)      => unsupportedConfiguration(conf, this)
}

given RelationalConvertLongInt[Addr, Type: ApronType](using intOps: RelationalIntOps[Addr,Type], convertType: ConvertLongInt[Type, Type]): ConvertLongInt[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
  (from, conf) =>
    intOps.foldInteger(cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf)))

given RelationalConvertFloatInt[Addr: Ordering: ClassTag, Type: ApronType]
  (using
      failure: Failure,
      effectStack: EffectStack,
      apronState: ApronState[Addr,Type],
      integerOps: RelationalBaseIntegerOps[Int, Addr,Type],
      typeOps: IntegerOps[Int, Type],
      convertType: Convert[Float, Int, Type, Type, Overflow && Bits]): ConvertFloatInt[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
  RelationalConvertFloatingInteger[Float,Int,Addr,Type](
    signedMinFail = Math.nextDown(Int.MinValue.toFloat),
    signedMaxFail = -Int.MinValue.toFloat,
    unsignedMinFail = -1.0f,
    unsignedMaxFail = -Int.MinValue.toDouble * 2.0d,
    unsignedMinJump = Math.nextDown(0.0f),
    unsignedMaxJump = -Int.MinValue.toFloat * 2.0f
  )

given RelationalConvertFloatLong[Addr: Ordering: ClassTag, Type: ApronType]
  (using
   failure: Failure,
   effectStack: EffectStack,
   apronState: ApronState[Addr, Type],
   integerOps: RelationalBaseIntegerOps[Long, Addr, Type],
   typeOps: IntegerOps[Long, Type],
   convertType: Convert[Float, Long, Type, Type, Overflow && Bits]): ConvertFloatLong[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertFloatingInteger[Float, Long, Addr, Type](
      signedMinFail = Math.nextDown(Long.MinValue.toFloat),
      signedMaxFail = -Long.MinValue.toFloat,
      unsignedMinFail = -1.0d,
      unsignedMaxFail = -Long.MinValue.toFloat * 2.0d,
      unsignedMinJump = Math.nextDown(0.0d),
      unsignedMaxJump = -Long.MinValue.toFloat * 2.0d
    )


given RelationalConvertDoubleInt[Addr: Ordering: ClassTag, Type: ApronType]
  (using
   failure: Failure,
   effectStack: EffectStack,
   apronState: ApronState[Addr, Type],
   integerOps: RelationalBaseIntegerOps[Int, Addr, Type],
   typeOps: IntegerOps[Int, Type],
   convertType: Convert[Double, Int, Type, Type, Overflow && Bits]): ConvertDoubleInt[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertFloatingInteger[Double, Int, Addr, Type](
      signedMinFail = Int.MinValue.toDouble - 1,
      signedMaxFail = -Int.MinValue.toDouble,
      unsignedMinFail = -1.0d,
      unsignedMaxFail = -Int.MinValue.toDouble * 2.0d,
      unsignedMinJump = Math.nextDown(0.0d),
      unsignedMaxJump = -Int.MinValue.toDouble * 2.0d
    )

given RelationalConvertDoubleLong[Addr: Ordering: ClassTag, Type: ApronType]
  (using
   failure: Failure,
   effectStack: EffectStack,
   apronState: ApronState[Addr, Type],
   integerOps: RelationalBaseIntegerOps[Long, Addr, Type],
   typeOps: IntegerOps[Long, Type],
   convertType: Convert[Double, Long, Type, Type, Overflow && Bits]): ConvertDoubleLong[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertFloatingInteger[Double, Long, Addr, Type](
      signedMinFail = Math.nextDown(Long.MinValue.toDouble),
      signedMaxFail = -Long.MinValue.toDouble,
      unsignedMinFail = -1.0d,
      unsignedMaxFail = -Long.MinValue.toDouble * 2.0d,
      unsignedMinJump = Math.nextDown(0.0d),
      unsignedMaxJump = -Long.MinValue.toDouble * 2.0d
    )

private final class RelationalConvertFloatingInteger[From, To, Addr: Ordering: ClassTag, Type: ApronType]
  (
    signedMinFail: Double,
    signedMaxFail: Double,
    unsignedMinFail: Double,
    unsignedMaxFail: Double,
    unsignedMinJump: Double,
    unsignedMaxJump: Double
  )
  (using
   failure: Failure,
   effectStack: EffectStack,
   apronState: ApronState[Addr,Type],
   integerOps: RelationalBaseIntegerOps[To, Addr,Type],
   typeOps: IntegerOps[To, Type],
   convertType: Convert[From, To, Type, Type, Overflow && Bits]
  ) extends Convert[From, To, ApronExpr[Addr,Type], ApronExpr[Addr,Type], Overflow && Bits]:

  def apply(from: ApronExpr[Addr,Type], config: Overflow && Bits) =
    val fromType = from._type
    val toType = convertType(fromType, config)
    val signedMinVal = integerOps.signedMinValue(toType).bigInteger
    val signedMaxVal = integerOps.signedMaxValue(toType).bigInteger
    val unsignedMinVal = integerOps.unsignedMinValue(toType).bigInteger
    val unsignedMaxVal = integerOps.unsignedMaxValue(toType).bigInteger

    config match
      case (_ && Bits.Raw) | (Overflow.Allow && Bits.Unsigned) =>
        joinWithFailure {
          constant(Interval(Mpq(signedMinVal), Mpq(signedMaxVal)), toType)
        } {
          unsupportedConfiguration(config,this)
        }

      case (Overflow.Fail && Bits.Signed) =>
        val iv = apronState.getFloatInterval(from)
        if(iv.onlySpecials || iv.sup().cmp(DoubleScalar(signedMinFail)) <= 0 || DoubleScalar(signedMaxFail).cmp(iv.inf()) <= 0) {
          failure.fail(ConversionFailure, s"float $iv cannot be converted")
        } else if(iv.isLeq(FInterval(signedMinFail, signedMaxFail, FloatSpecials.Bottom))) {
          apply(from, Overflow.JumpToBounds && Bits.Signed)
        } else {
          joinWithFailure {
            apply(from.setFloatSpecials(FloatSpecials.Bottom), (Overflow.JumpToBounds && Bits.Signed))
          } {
            Failure(ConversionFailure, s"float $iv cannot be converted")
          }
        }
      case (Overflow.JumpToBounds && Bits.Signed) | (Overflow.Allow && Bits.Signed) =>
        val iv = apronState.getFloatInterval(from)
        if(iv.isLeq(FInterval(Math.nextUp(signedMinFail), Math.nextDown(signedMaxFail), FloatSpecials.Bottom))) {
          cast(from, RoundingType.Int, RoundingDir.Zero, toType)
        } else if(iv.onlySpecials) {
          val specials = iv.floatSpecials
          var resultIv = Interval()
          if(specials.negInfinity)
            resultIv = Join(resultIv, Interval(MpqScalar(signedMinVal),MpqScalar(signedMinVal))).get
          if(specials.posInfinity)
            resultIv = Join(resultIv, Interval(MpqScalar(signedMaxVal), MpqScalar(signedMaxVal))).get
          if(specials.nan)
            resultIv = Join(resultIv, Interval(0,0)).get
          constant(resultIv, toType)
        } else { // iv.onlySpecials == false, i.e., the interval contains regular floating point numbers
          var resultIv = Interval(iv.nonSpecialInf, iv.nonSpecialSup)
          if(iv.inf().cmp(DoubleScalar(signedMinFail)) <= 0) {
            resultIv.setInf(MpqScalar(signedMinVal))
            if(resultIv.isBottom)
              resultIv.setSup(MpqScalar(signedMinVal))
          }
          if (DoubleScalar(signedMaxFail).cmp(iv.sup()) <= 0) {
            resultIv.setSup(MpqScalar(signedMaxVal))
            if (resultIv.isBottom)
              resultIv.setInf(MpqScalar(signedMaxVal))
          }
          if(iv.floatSpecials.nan)
            resultIv = Join(resultIv, Interval(0,0)).get
          cast(constant(resultIv, fromType), RoundingType.Int, RoundingDir.Zero, toType)
        }

      case (Overflow.Fail && Bits.Unsigned) =>
        val iv = apronState.getFloatInterval(from)
        if (iv.onlySpecials || iv.sup().cmp(DoubleScalar(unsignedMinFail)) <= 0 || DoubleScalar(unsignedMaxFail).cmp(iv.inf()) <= 0) {
          failure.fail(ConversionFailure, s"float $iv cannot be converted")
        } else if (iv.isLeq(FInterval(unsignedMinFail, unsignedMaxFail, FloatSpecials.Bottom))) {
          apply(from, (Overflow.JumpToBounds && Bits.Unsigned))
        } else {
          joinWithFailure {
            apply(from.setFloatSpecials(FloatSpecials.Bottom), (Overflow.JumpToBounds && Bits.Unsigned))
          } {
            Failure(ConversionFailure, s"float $iv cannot be converted")
          }
        }


      case (Overflow.JumpToBounds && Bits.Unsigned) =>
        val iv = apronState.getFloatInterval(from)
        if(iv.isLeq(FInterval(Math.nextUp(unsignedMinJump), Math.nextDown(unsignedMaxJump), FloatSpecials.Bottom))) {
          integerOps.interpretUnsignedAsSigned(cast(from, RoundingType.Int, RoundingDir.Zero, toType))
        } else if (iv.onlySpecials) {
          val specials = iv.floatSpecials
          var resultIv = Interval()
          resultIv.setBottom()
          if (specials.negInfinity)
            resultIv = Join(resultIv, Interval(unsignedMinVal,unsignedMinVal)).get
          if (specials.posInfinity)
            resultIv = Join(resultIv, Interval(-1,-1)).get
          if (specials.nan)
            resultIv = Join(resultIv, Interval(0, 0)).get
          constant(resultIv, toType)
        } else { // iv.onlySpecials == false, i.e., the interval contains regular floating point numbers
          var resultIv = Interval(iv.nonSpecialInf, iv.nonSpecialSup)
          if (iv.inf().cmp(DoubleScalar(unsignedMinJump)) <= 0) {
            resultIv.setInf(MpqScalar(unsignedMinVal))
            if (resultIv.isBottom)
              resultIv.setSup(MpqScalar(unsignedMinVal))
          }
          if (MpqScalar(signedMaxVal).cmp(iv.sup()) <= 0) {
            resultIv.setInf(MpqScalar(signedMinVal))
            resultIv.setSup(MpqScalar(signedMaxVal))
          }
          if (iv.floatSpecials.nan)
            resultIv = Join(resultIv, Interval(0, 0)).get
          cast(constant(resultIv, fromType), RoundingType.Int, RoundingDir.Zero, toType)
        }

given RelationalConvertDoubleFloat[Addr: Ordering: ClassTag, Type: ApronType](using apronState: ApronState[Addr,Type], floatOps: RelationalFloatOps[Float, Addr, Type], convertType: ConvertDoubleFloat[Type, Type]):
  ConvertDoubleFloat[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] = {
  case (from, conf) =>
    val iv = apronState.getInterval(from)
    var specials = from.floatSpecials
    val minVal = Float.MinValue.toDouble
    val maxVal = Float.MaxValue.toDouble
    if(iv.isBottom)
      from
    else if(iv.isLeq(Interval(minVal, maxVal)))
      floatCast(from, RoundingType.Single, RoundingDir.Nearest, from.floatSpecials, convertType(from._type, conf))
    else
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
      floatConstant(iv, specials, from._type)
}

given RelationalConvertFloatDouble[Addr: ClassTag, Type: ApronType](using convertType: ConvertFloatDouble[Type, Type]):
  ConvertFloatDouble[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] = {
  (from, conf) =>
    floatCast(from, RoundingType.Double, RoundingDir.Nearest, from.floatSpecials, convertType(from._type, conf))
}

given RelationalConvertIntFloat[Addr: Ordering: ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Int, Addr, Type], convertType: Convert[Int, Float, Type, Type, Bits]):
  ConvertIntFloat[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
    RelationalConvertIntegerFloating[Int,Float,Addr,Type]

given RelationalConvertIntDouble[Addr: Ordering : ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Int, Addr, Type], convertType: Convert[Int, Double, Type, Type, Bits]):
  ConvertIntDouble[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertIntegerFloating[Int, Double, Addr, Type]

given RelationalConvertLongFloat[Addr: Ordering : ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Long, Addr, Type], convertType: Convert[Long, Float, Type, Type, Bits]):
  ConvertLongFloat[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertIntegerFloating[Long, Float, Addr, Type]

given RelationalConvertLongDouble[Addr: Ordering : ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Long, Addr, Type], convertType: Convert[Long, Double, Type, Type, Bits]):
  ConvertLongDouble[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertIntegerFloating[Long, Double, Addr, Type]

private final class RelationalConvertIntegerFloating[From, To: Numeric: Bounded, Addr: Ordering: ClassTag, Type: ApronType](using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[From, Addr, Type], convertType: Convert[From, To, Type, Type, Bits])
  extends Convert[From, To, ApronExpr[Addr,Type], ApronExpr[Addr,Type], Bits]:
  def apply(from: ApronExpr[Addr,Type], conf: Bits) = conf match
    case conf@Bits.Signed =>
      cast(from, RoundingType.Single, RoundingDir.Nearest, convertType(from._type, conf))
    case conf@Bits.Unsigned =>
      cast(intOps.interpretSignedAsUnsigned(from), RoundingType.Single, RoundingDir.Nearest, convertType(from._type, conf))
    case conf@Bits.Raw =>
      val topIv = Interval(Numeric[To].toDouble(Bounded[To].minValue), Numeric[To].toDouble(Bounded[To].maxValue))
      joinWithFailure(
        floatConstant(topIv, FloatSpecials.Top, convertType(from._type, conf))
      ) (
        unsupportedConfiguration(conf, this)
      )

given RelationalConvertToBytes[From, Addr: Ordering: ClassTag, Type: ApronType, Config <: ConvertConfig[_]]: Convert[From, Seq[Byte], ApronExpr[Addr,Type], Interval, Config] with
  def apply(from: ApronExpr[Addr,Type], config: Config) = topInterval

given RelationalConvertBytesInt[Addr: Ordering: ClassTag, Type: ApronType, Config <: ConvertConfig[_]](using typeIntegerOps: IntegerOps[Int,Type]): Convert[Seq[Byte], Int, Interval, ApronExpr[Addr,Type], Config] with
  def apply(_from: Interval, config: Config) = constant(Interval(Int.MinValue, Int.MaxValue), typeIntegerOps.integerLit(0))

given RelationalConvertBytesLong[Addr: Ordering: ClassTag, Type: ApronType, Config <: ConvertConfig[_]](using typeIntegerOps: IntegerOps[Long,Type]): Convert[Seq[Byte], Long, Interval, ApronExpr[Addr,Type], Config] with
  def apply(_from: Interval, config: Config) = constant(Interval(BigInt(Long.MinValue).bigInteger, BigInt(Long.MaxValue).bigInteger), typeIntegerOps.integerLit(0))

given RelationalConvertBytesFloat[Addr: Ordering: ClassTag, Type: ApronType, Config <: ConvertConfig[_]](using typeFloatOps: FloatOps[Float,Type]): Convert[Seq[Byte], Float, Interval, ApronExpr[Addr,Type], Config] with
  def apply(_from: Interval, config: Config) = ApronExpr.floatConstant(Interval(Float.MinValue,Float.MaxValue), FloatSpecials.Top, typeFloatOps.floatingLit(0))

given RelationalConvertBytesDouble[Addr: Ordering : ClassTag, Type: ApronType, Config <: ConvertConfig[_]] (using typeFloatOps: FloatOps[Double, Type]): Convert[Seq[Byte], Double, Interval, ApronExpr[Addr, Type], Config] with
  def apply(_from: Interval, config: Config) = ApronExpr.floatConstant(Interval(Double.MinValue,Double.MaxValue), FloatSpecials.Top, typeFloatOps.floatingLit(0))