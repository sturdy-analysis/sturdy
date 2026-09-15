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
import sturdy.values.config.{BitSign, *, given}
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.apron.FloatInterval as FInterval

import scala.math.BigInt.javaBigInteger2bigInt
import java.math.BigInteger
import java.nio.ByteOrder
import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

given RelationalConvertIntLong[Addr, Type: ApronType](using failure: Failure, intOps: RelationalIntOps[Addr,Type], convertType: ConvertIntLong[Type, Type]): ConvertIntLong[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] = {
  case (from, conf@BitSign.Signed)   => cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
  case (from, conf@BitSign.Unsigned) => cast(intOps.interpretSignedAsUnsigned(from), RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
  case (_,    conf@BitSign.Raw)      => unsupportedConfiguration(conf, this)
}

given RelationalConvertLongInt[Addr, Type: ApronType](using intOps: RelationalIntOps[Addr,Type], convertType: ConvertLongInt[Type, Type]): ConvertLongInt[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
  (from, conf) =>
    intOps.handleOverflow(cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf)))

given RelationalConvertFloatInt[Addr: Ordering: ClassTag, Type: ApronType]
  (using
      failure: Failure,
      effectStack: EffectStack,
      apronState: ApronState[Addr,Type],
      integerOps: RelationalBaseIntegerOps[Int, Addr,Type],
      typeOps: IntegerOps[Int, Type],
      convertType: Convert[Float, Int, Type, Type, Overflow && BitSign]): ConvertFloatInt[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
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
   convertType: Convert[Float, Long, Type, Type, Overflow && BitSign]): ConvertFloatLong[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
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
   convertType: Convert[Double, Int, Type, Type, Overflow && BitSign]): ConvertDoubleInt[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
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
   convertType: Convert[Double, Long, Type, Type, Overflow && BitSign]): ConvertDoubleLong[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
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
   convertType: Convert[From, To, Type, Type, Overflow && BitSign]
  ) extends Convert[From, To, ApronExpr[Addr,Type], ApronExpr[Addr,Type], Overflow && BitSign]:

  given defaultResolveState: ResolveState = ResolveState.Internal

  def apply(from: ApronExpr[Addr,Type], config: Overflow && BitSign) =
    val fromType = from._type
    val toType = convertType(fromType, config)
    val signedMinVal = integerOps.signedMinValue(toType.byteSize).bigInteger
    val signedMaxVal = integerOps.signedMaxValue(toType.byteSize).bigInteger
    val unsignedMinVal = integerOps.unsignedMinValue(toType.byteSize).bigInteger
    val unsignedMaxVal = integerOps.unsignedMaxValue(toType.byteSize).bigInteger

    config match
      case (_ && BitSign.Raw) | (Overflow.Allow && BitSign.Unsigned) =>
        joinWithFailure {
          constant(Interval(Mpq(signedMinVal), Mpq(signedMaxVal)), toType)
        } {
          unsupportedConfiguration(config,this)
        }

      case (Overflow.Fail && BitSign.Signed) =>
        val iv = apronState.getFloatInterval(from)
        if(iv.onlySpecials || iv.sup().cmp(DoubleScalar(signedMinFail)) <= 0 || DoubleScalar(signedMaxFail).cmp(iv.inf()) <= 0) {
          failure.fail(ConversionFailure, s"float $iv cannot be converted")
        } else if(iv.isLeq(FInterval(signedMinFail, signedMaxFail, FloatSpecials.Bottom))) {
          apply(from, Overflow.JumpToBounds && BitSign.Signed)
        } else {
          joinWithFailure {
            apply(from.setFloatSpecials(FloatSpecials.Bottom), (Overflow.JumpToBounds && BitSign.Signed))
          } {
            Failure(ConversionFailure, s"float $iv cannot be converted")
          }
        }
      case (Overflow.JumpToBounds && BitSign.Signed) | (Overflow.Allow && BitSign.Signed) =>
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

      case (Overflow.Fail && BitSign.Unsigned) =>
        val iv = apronState.getFloatInterval(from)
        if (iv.onlySpecials || iv.sup().cmp(DoubleScalar(unsignedMinFail)) <= 0 || DoubleScalar(unsignedMaxFail).cmp(iv.inf()) <= 0) {
          failure.fail(ConversionFailure, s"float $iv cannot be converted")
        } else if (iv.isLeq(FInterval(unsignedMinFail, unsignedMaxFail, FloatSpecials.Bottom))) {
          apply(from, (Overflow.JumpToBounds && BitSign.Unsigned))
        } else {
          joinWithFailure {
            apply(from.setFloatSpecials(FloatSpecials.Bottom), (Overflow.JumpToBounds && BitSign.Unsigned))
          } {
            Failure(ConversionFailure, s"float $iv cannot be converted")
          }
        }


      case (Overflow.JumpToBounds && BitSign.Unsigned) =>
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
      val iv = apronState.getInterval(from)(using ResolveState.Internal)
      floatOps.checkForNewFloatSpecials(
        floatCast(from, RoundingType.Single, RoundingDir.Nearest, from.floatSpecials, convertType(from._type, conf))
      )
}

given RelationalConvertFloatDouble[Addr: ClassTag, Type: ApronType](using convertType: ConvertFloatDouble[Type, Type]):
  ConvertFloatDouble[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] = {
  (from, conf) =>
    floatCast(from, RoundingType.Double, RoundingDir.Nearest, from.floatSpecials, convertType(from._type, conf))
}

given RelationalConvertIntFloat[Addr: Ordering: ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Int, Addr, Type], convertType: Convert[Int, Float, Type, Type, BitSign]):
  ConvertIntFloat[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
    RelationalConvertIntegerFloating[Int,Float,Addr,Type]

given RelationalConvertIntDouble[Addr: Ordering : ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Int, Addr, Type], convertType: Convert[Int, Double, Type, Type, BitSign]):
  ConvertIntDouble[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertIntegerFloating[Int, Double, Addr, Type]

given RelationalConvertLongFloat[Addr: Ordering : ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Long, Addr, Type], convertType: Convert[Long, Float, Type, Type, BitSign]):
  ConvertLongFloat[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertIntegerFloating[Long, Float, Addr, Type]

given RelationalConvertLongDouble[Addr: Ordering : ClassTag, Type: ApronType]
  (using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[Long, Addr, Type], convertType: Convert[Long, Double, Type, Type, BitSign]):
  ConvertLongDouble[ApronExpr[Addr, Type], ApronExpr[Addr, Type]] =
    RelationalConvertIntegerFloating[Long, Double, Addr, Type]

private final class RelationalConvertIntegerFloating[From, To: Numeric: Bounded, Addr: Ordering: ClassTag, Type: ApronType](using failure: Failure, effectStack: EffectStack, intOps: RelationalBaseIntegerOps[From, Addr, Type], convertType: Convert[From, To, Type, Type, BitSign])
  extends Convert[From, To, ApronExpr[Addr,Type], ApronExpr[Addr,Type], BitSign]:
  def apply(from: ApronExpr[Addr,Type], conf: BitSign) = {
    val tpe = convertType(from._type, conf)
    conf match
      case conf@BitSign.Signed =>
        cast(from, tpe.roundingType, RoundingDir.Nearest, tpe)
      case conf@BitSign.Unsigned =>
        cast(intOps.interpretSignedAsUnsigned(from), tpe.roundingType, RoundingDir.Nearest, tpe)
      case conf@BitSign.Raw =>
        val topIv = Interval(Numeric[To].toDouble(Bounded[To].minValue), Numeric[To].toDouble(Bounded[To].maxValue))
        joinWithFailure(
          floatConstant(topIv, FloatSpecials.Top, convertType(from._type, conf))
        ) (
          unsupportedConfiguration(conf, this)
        )
  }

//private final class RelationalConvertIntegerBytes[From, Addr: Ordering: ClassTag, Type: ApronType]
//  (using failure: Failure, apronState: ApronState[Addr,Type], typeOps: IntegerOps[From, Type])
//  extends Convert[From, Seq[Byte], ApronExpr[Addr,Type], Bytes[Addr,Type], BytesSize && SomeCC[ByteOrder]]:
//  def apply(from: ApronExpr[Addr,Type], config: BytesSize && SomeCC[ByteOrder]): Bytes[Addr,Type] =
//    val byteSize && SomeCC(byteOrder, _) = config
//    Bytes(Topped.Actual(from), NumericInterval(byteSize.bytes, byteSize.bytes), Topped.Actual(byteOrder))
//
//given RelationalConvertIntBytes[Addr: Ordering: ClassTag, Type: ApronType](using failure: Failure, apronState: ApronState[Addr,Type], typeOps: IntegerOps[Int, Type]):
//  ConvertIntBytes[ApronExpr[Addr,Type], Bytes[Addr,Type]] = RelationalConvertIntegerBytes[Int, Addr, Type]
//
//given RelationalConvertLongBytes[Addr: Ordering : ClassTag, Type: ApronType](using failure: Failure, apronState: ApronState[Addr, Type], typeOps: IntegerOps[Long, Type]):
//  ConvertLongBytes[ApronExpr[Addr, Type], Bytes[Addr, Type]] = RelationalConvertIntegerBytes[Long, Addr, Type]
//
//given RelationalConvertBytesInteger[To: Numeric, Addr: Ordering : ClassTag, Type: ApronType](using failure: Failure, apronState: ApronState[Addr, Type], intOps: RelationalBaseIntegerOps[To, Addr,Type], typeOps: IntegerOps[To, Type]):
//  Convert[Seq[Byte], To, Bytes[Addr, Type], ApronExpr[Addr, Type], BytesSize && SomeCC[ByteOrder] && BitSign] with
//  def apply(from: Bytes[Addr, Type], config: BytesSize && SomeCC[ByteOrder] && BitSign): ApronExpr[Addr, Type] =
//    val ((byteSize && SomeCC(byteOrder, _)) && BitSign) = config
//    val toType = typeOps.integerLit(implicitly[Numeric[To]].zero)
//    from match
//      case Bytes(Topped.Actual(fromExpr), NumericInterval(l,u), Topped.Actual(fromByteOrder)) if(fromExpr._type == ApronRepresentation.Int && fromByteOrder == byteOrder) =>
//        val signedMinValue = -BigInt(2).pow(byteSize.bytes * 8 - 1)
//        val unsignedMaxValue = BigInt(2).pow(byteSize.bytes * 8)
//        var result: ApronExpr[Addr,Type] =
//          intAdd(
//            intMod(from.value, bigIntLit(unsignedMaxValue, fromType), fromType),
//            bigIntLit(signedMinValue, fromType),
//            fromType
//          )
//        result
//      case _ =>
//        // Don't know how to precisely convert from a byte sequence of a floating point number to an integer
//        // Don't know how to precisely convert between different byte orders
//        constant(topInterval, toType)

//given RelationalConvertToBytes[From, Addr: Ordering: ClassTag, Type: ApronType, Config <: ConvertConfig[_]]: Convert[From, Seq[Byte], ApronExpr[Addr,Type], Interval, Config] with
//  def apply(from: ApronExpr[Addr,Type], config: Config) = topInterval

given RelationalConvertBytesFloat[Addr: Ordering: ClassTag, Type: ApronType, Config <: ConvertConfig[_]](using typeFloatOps: FloatOps[Float,Type]): Convert[Seq[Byte], Float, Interval, ApronExpr[Addr,Type], Config] with
  def apply(_from: Interval, config: Config) = ApronExpr.floatConstant(Interval(Float.MinValue,Float.MaxValue), FloatSpecials.Top, typeFloatOps.floatingLit(0))

given RelationalConvertBytesDouble[Addr: Ordering : ClassTag, Type: ApronType, Config <: ConvertConfig[_]] (using typeFloatOps: FloatOps[Double, Type]): Convert[Seq[Byte], Double, Interval, ApronExpr[Addr, Type], Config] with
  def apply(_from: Interval, config: Config) = ApronExpr.floatConstant(Interval(Double.MinValue,Double.MaxValue), FloatSpecials.Top, typeFloatOps.floatingLit(0))