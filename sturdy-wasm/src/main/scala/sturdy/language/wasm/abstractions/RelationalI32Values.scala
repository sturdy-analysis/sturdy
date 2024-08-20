package sturdy.language.wasm.abstractions

import apron.*
import sturdy.apron.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.wasm.Interpreter
import sturdy.data.{*,given}
import sturdy.util.Lazy
import sturdy.values.config.{Bits, BytesSize, Overflow}
import sturdy.values.convert.{&&, Convert, LiftedConvert, NilCC, SomeCC}
import sturdy.values.floating.{ConvertDoubleInt, ConvertFloatInt}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteOrder

trait RelationalI32Values extends Interpreter with RelationalAddresses:
  import Type.*

  final type I32 = Either[ApronExpr[VirtAddr, Type], Bool]
  final type Bool = ApronCons[VirtAddr, Type]

  extension (i32: I32)
    inline def asApronExpr(using apronState: ApronState[VirtAddr, Type]): ApronExpr[VirtAddr, Type] =
      i32 match
        case Left(expr) => expr
        case Right(cons) => apronState.getBoolean(cons) match
          case Topped.Actual(true) => ApronExpr.intLit(1, I32Type)
          case Topped.Actual(false) => ApronExpr.intLit(0, I32Type)
          case Topped.Top => ApronExpr.intInterval(0, 1, I32Type)

    def asApronExprLazy(using lazyApronState: Lazy[ApronState[VirtAddr, Type]]): ApronExpr[VirtAddr, Type] =
      given ApronState[VirtAddr, Type] = lazyApronState.value
      i32.asApronExpr

  final override def topI32: I32 = Left(ApronExpr.constant(ApronExpr.topInterval, I32Type))
  final override def boolean(b: Bool): Value = Value.Int32(Right(b))

  given CombineI32[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W], apronState: Lazy[ApronState[VirtAddr,Type]]): Combine[I32, W] with
    override def apply(v1: I32, v2: I32): MaybeChanged[I32] =
      if(v1 == v2)
        Unchanged(v1)
      else
        combineApronExpr(v1.asApronExprLazy, v2.asApronExprLazy).map(Left.apply)

  given I32IntegerOps(using apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack): IntegerOps[Int, I32] =
    LiftedIntegerOps[Int, I32, ApronExpr[VirtAddr,Type]](extract = _.asApronExpr, inject = Left(_))

  given I32EqOps(using apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack): EqOps[I32, Bool] = new EqOps[I32, Bool]:
    override def equ(v1: I32, v2: I32): Bool =
      (v1,v2) match
        case (Right(c1), Left(i2@ApronExpr.Constant(coeff, _, tpe))) =>
          val c1ContainsNaN = c1.e1.floatSpecials.nan || c1.e2.floatSpecials.nan
          if(coeff.isEqual(0) && ! c1ContainsNaN)
            c1.negated
          else if(coeff.isScalar && ! c1ContainsNaN /* && ! coeff.isEqual(0) */)
            c1
          else
            EqOps.equ(v1.asApronExpr, i2)
        case (Left(_), Right(_)) =>
          equ(v2, v1)
        case (Right(c1), Right(c2)) =>
          ApronCons.from(Type.I32Type) {
            apronState.join {
              apronState.addConstraints(c1, c2)
              Topped.Actual(true)
            } {
              apronState.addConstraints(c1.negated, c2.negated)
              Topped.Actual(false)
            }
          }
        case (Left(_),Left(_)) | (Right(_),Left(_)) => EqOps.equ(v1.asApronExpr, v2.asApronExpr)


    override def neq(v1: I32, v2: I32): Bool = equ(v1,v2).negated


  given I32OrderingOps(using ApronState[VirtAddr, Type], Failure, EffectStack): OrderingOps[I32, Bool] =
    LiftedOrderingOps[I32, Bool, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](extract = _.asApronExpr, inject = x => x)

  given I32UnsignedOrderingOps(using ApronState[VirtAddr, Type], Failure, EffectStack): UnsignedOrderingOps[I32, Bool] =
    LiftedUnsignedOrderingOps[I32, Bool, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](extract = _.asApronExpr, inject = x => x)

  given I32ConvertIntLong(using ApronState[VirtAddr, Type], ConvertIntLong[ApronExpr[VirtAddr, Type], I64]): ConvertIntLong[I32, I64] =
    LiftedConvert[Int, Long, I32, I64, ApronExpr[VirtAddr, Type], I64, Bits](extract = _.asApronExpr, inject = x => x)

  given I32ConvertIntFloat(using ApronState[VirtAddr, Type], ConvertIntFloat[ApronExpr[VirtAddr, Type], F32]): ConvertIntFloat[I32, F32] =
    LiftedConvert[Int, Float, I32, F32, ApronExpr[VirtAddr, Type], F32, Bits](extract = _.asApronExpr, inject = x => x)

  given I32ConvertIntDouble(using ApronState[VirtAddr, Type], ConvertIntDouble[ApronExpr[VirtAddr, Type], F64]): ConvertIntDouble[I32, F64] =
    LiftedConvert[Int, Double, I32, F64, ApronExpr[VirtAddr, Type], F64, Bits](extract = _.asApronExpr, inject = x => x)

  given I32ConvertLongInt(using ApronState[VirtAddr, Type], ConvertLongInt[I64, ApronExpr[VirtAddr, Type]]): ConvertLongInt[I64, I32] =
    LiftedConvert[Long, Int, I64, I32, I64, ApronExpr[VirtAddr, Type], NilCC.type](extract = x => x, inject = Left(_))

  given I32ConvertFloatInt(using ApronState[VirtAddr, Type], ConvertFloatInt[F32, ApronExpr[VirtAddr, Type]]): ConvertFloatInt[F32, I32] =
    LiftedConvert[Float, Int, F32, I32, F32, ApronExpr[VirtAddr, Type], Overflow && Bits](extract = x => x, inject = Left(_))

  given I32ConvertDoubleInt(using ApronState[VirtAddr, Type], ConvertDoubleInt[F64, ApronExpr[VirtAddr, Type]]): ConvertDoubleInt[F64, I32] =
    LiftedConvert[Double, Int, F64, I32, F64, ApronExpr[VirtAddr, Type], Overflow && Bits](extract = x => x, inject = Left(_))

  given I32ConvertIntBytes(using ApronState[VirtAddr, Type], ConvertIntBytes[ApronExpr[VirtAddr, Type], Bytes]): ConvertIntBytes[I32, Bytes] =
    LiftedConvert[Int, Seq[Byte], I32, Bytes, ApronExpr[VirtAddr, Type], Bytes, BytesSize && SomeCC[ByteOrder]](extract = _.asApronExpr, inject = x => x)

  given I32ConvertBytesInt(using ApronState[VirtAddr, Type], ConvertBytesInt[Bytes, ApronExpr[VirtAddr, Type]]): ConvertBytesInt[Bytes, I32] =
    LiftedConvert[Seq[Byte], Int, Bytes, I32, Bytes, ApronExpr[VirtAddr, Type], BytesSize && SomeCC[ByteOrder] && Bits](extract = x => x, inject = Left(_))
