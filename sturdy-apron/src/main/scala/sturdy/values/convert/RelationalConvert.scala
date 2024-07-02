package sturdy.values.convert

import apron.*
import gmp.*
import sturdy.apron.ApronExpr.*
import sturdy.apron.ApronCons.*
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.config.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}

import scala.reflect.ClassTag

given RelationalConvertIntLong[Addr: Ordering: ClassTag, Type: ApronType: Join](using intOps: RelationalIntOps[Addr,Type], convertType: ConvertIntLong[Type, Type]): ConvertIntLong[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] = {
  case (from, conf@Bits.Signed)   => cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
  case (from, conf@Bits.Unsigned) => cast(intOps.interpretSignedAsUnsigned(from), RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
  case (_,    conf@Bits.Raw)      => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)
}

given RelationalConvertLongInt[Addr: Ordering: ClassTag, Type: ApronType: Join](using intOps: RelationalIntOps[Addr,Type], convertType: ConvertLongInt[Type, Type]): ConvertLongInt[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] =
  (from, conf) =>
    intOps.foldInteger(cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf)))

given RelationalConvertFloatLong[Addr: Ordering: ClassTag, Type: ApronType: Join]
    (using
      failure: Failure,
      effectStack: EffectStack,
      apronState: ApronState[Addr,Type],
      longOps: RelationalLongOps[Addr,Type],
      convertType: ConvertFloatLong[Type, Type]
    ): ConvertFloatLong[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] with
  def apply(from: ApronExpr[Addr,Type], config: Overflow && Bits) = (from,config) match
    case (from, conf@(_ && Bits.Raw)) => ???
    case (from, conf@(Overflow.Allow && Bits.Signed)) =>
      val toType = convertType(from._type, conf)
      val expr = cast(from, RoundingType.Int, RoundingDir.Zero, toType)
      val iv = apronState.getInterval(expr)
      if(iv.isLeq(Interval(Long.MinValue, Long.MaxValue))) {
        expr
      } else {
        apronState.withTempVars(toType) {
          (res, _) =>
            val resExpr = addr(res, toType)
            apronState.assign(res, expr)
            apronState.ifThenElse(le(resExpr, longLit(Long.MaxValue, toType))) {
            } {
              apronState.assign(res, longLit(Long.MaxValue, toType))
            }
            apronState.ifThenElse(le(longLit(Long.MinValue, toType), resExpr)) {
            } {
              apronState.assign(res, longLit(Long.MinValue, toType))
            }
            resExpr
        }
      }
    case (from, conf@(Overflow.Allow && Bits.Unsigned)) => throw UnsupportedConfiguration(conf, this.getClass.getSimpleName)
    case (from, conf@(Overflow.Fail && Bits.Signed)) =>
      val iv = apronState.getInterval(from)
      if(MpqScalar(Mpq(Long.MaxValue)).cmp(iv.inf()) < 0 || iv.sup().cmp(MpqScalar(Mpq(Long.MinValue))) < 0) {
        failure.fail(ConversionFailure, s"float $iv out of long range")
      } else if(iv.isLeq(Interval(Long.MinValue, Long.MaxValue))) {
        cast(from, RoundingType.Int, RoundingDir.Zero, convertType(from._type, conf))
      } else {
        joinWithFailure {
          apply(from, (Overflow.Allow && Bits.Signed))
        } {
          Failure(ConversionFailure, "float $iv out of long range")
        }
      }
    case (from, conf@(Overflow.Fail && Bits.Unsigned)) => ???
    case (from, conf@(Overflow.JumpToBounds && Bits.Signed)) => ???
    case (from, conf@(Overflow.JumpToBounds && Bits.Unsigned)) => ???
