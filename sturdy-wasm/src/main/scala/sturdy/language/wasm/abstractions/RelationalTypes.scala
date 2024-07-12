package sturdy.language.wasm.abstractions

import sturdy.apron.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.wasm.generic.WasmFailure
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.config.{Bits, Overflow}
import sturdy.values.convert.{&&, LiftedConvert, NilCC}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.types.{*, given}

trait RelationalTypes:
  enum Type:
    case I32Type
    case I64Type
    case F32Type
    case F64Type

    def asI32(using f: Failure): BaseType[Int] =
      this match
        case I32Type => BaseType[Int]
        case _ => f.fail(WasmFailure.TypeError, s"Expected i32, but got $this")

    def asI64(using f: Failure): BaseType[Long] =
      this match
        case I64Type => BaseType[Long]
        case _ => f.fail(WasmFailure.TypeError, s"Expected i64, but got $this")

    def asF32(using f: Failure): BaseType[Float] =
      this match
        case F32Type => BaseType[Float]
        case _ => f.fail(WasmFailure.TypeError, s"Expected f32, but got $this")

    def asF64(using f: Failure): BaseType[Double] =
      this match
        case F64Type => BaseType[Double]
        case _ => f.fail(WasmFailure.TypeError, s"Expected f64, but got $this")

    override def toString: String =
      this match
        case I32Type => "i32"
        case I64Type => "i64"
        case F32Type => "f32"
        case F64Type => "f64"

  import Type.*

  given Ordering[Type] = Ordering.by{
    case I32Type => 0
    case I64Type => 1
    case F32Type => 2
    case F64Type => 3
  }

  given CombineType[W <: Widening]: Combine[Type, W] = {
    case (I32Type, I32Type) => Unchanged(I32Type)
    case (I64Type, I64Type) => Unchanged(I64Type)
    case (F32Type, F32Type) => Unchanged(F32Type)
    case (F64Type, F64Type) => Unchanged(F64Type)
    case (t1, t2) => throw new IllegalArgumentException(s"Incompatible types $t1 and $t2")
  }

  given ApronType[Type] with
    extension (t: Type)
      def apronRepresentation: ApronRepresentation =
        t match
          case I32Type => BaseType[Int].apronRepresentation
          case I64Type => BaseType[Long].apronRepresentation
          case F32Type => BaseType[Float].apronRepresentation
          case F64Type => BaseType[Double].apronRepresentation
      def roundingDir: RoundingDir =
        t match
          case I32Type => BaseType[Int].roundingDir
          case I64Type => BaseType[Long].roundingDir
          case F32Type => BaseType[Float].roundingDir
          case F64Type => BaseType[Double].roundingDir
      def roundingType: RoundingType =
        t match
          case I32Type => BaseType[Int].roundingType
          case I64Type => BaseType[Long].roundingType
          case F32Type => BaseType[Float].roundingType
          case F64Type => BaseType[Double].roundingType
      def byteSize: Int =
        t match
          case I32Type => BaseType[Int].byteSize
          case I64Type => BaseType[Long].byteSize
          case F32Type => BaseType[Float].byteSize
          case F64Type => BaseType[Double].byteSize

  given (using failure: Failure, effectStack: EffectStack): IntegerOps[Int, Type] = LiftedIntegerOps[Int, Type, BaseType[Int]](extract = _.asI32, inject = _ => Type.I32Type)

  given (using failure: Failure, effectStack: EffectStack): IntegerOps[Long, Type] = LiftedIntegerOps[Long, Type, BaseType[Long]](extract = _.asI64, inject = _ => Type.I64Type)

  given (using failure: Failure): FloatOps[Float, Type] = LiftedFloatOps[Float, Type, BaseType[Float]](extract = _.asF32, inject = _ => Type.F32Type)

  given (using failure: Failure): FloatOps[Double, Type] = LiftedFloatOps[Double, Type, BaseType[Double]](extract = _.asF64, inject = _ => Type.F64Type)

  given OrderingOps[Type, Type] = LiftedOrderingOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _ => BaseType[Int], inject = _ => Type.I32Type)

  given BooleanOps[Type] = LiftedBooleanOps[Type, BaseType[Boolean]](extract = _ => BaseType[Boolean], inject = _ => Type.I32Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertIntLong[Type, Type] = LiftedConvert[Int, Long, Type, Type, BaseType[Int], BaseType[Long], Bits](extract = _.asI32, inject = _ => Type.I64Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertIntFloat[Type, Type] = LiftedConvert[Int, Float, Type, Type, BaseType[Int], BaseType[Float], Bits](extract = _.asI32, inject = _ => Type.F32Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertIntDouble[Type, Type] = LiftedConvert[Int, Double, Type, Type, BaseType[Int], BaseType[Double], Bits](extract = _.asI32, inject = _ => Type.F64Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertLongInt[Type, Type] = LiftedConvert[Long, Int, Type, Type, BaseType[Long], BaseType[Int], NilCC.type](extract = _.asI64, inject = _ => Type.I32Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertLongFloat[Type, Type] = LiftedConvert[Long, Float, Type, Type, BaseType[Long], BaseType[Float], Bits](extract = _.asI64, inject = _ => Type.F32Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertLongDouble[Type, Type] = LiftedConvert[Long, Double, Type, Type, BaseType[Long], BaseType[Double], Bits](extract = _.asI64, inject = _ => Type.F64Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertFloatInt[Type, Type] = LiftedConvert[Float, Int, Type, Type, BaseType[Float], BaseType[Int], Overflow && Bits](extract = _.asF32, inject = _ => Type.I32Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertFloatLong[Type, Type] = LiftedConvert[Float, Long, Type, Type, BaseType[Float], BaseType[Long], Overflow && Bits](extract = _.asF32, inject = _ => Type.I64Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertFloatDouble[Type, Type] = LiftedConvert[Float, Double, Type, Type, BaseType[Float], BaseType[Double], NilCC.type](extract = _.asF32, inject = _ => Type.F64Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertDoubleInt[Type, Type] = LiftedConvert[Double, Int, Type, Type, BaseType[Double], BaseType[Int], Overflow && Bits](extract = _.asF64, inject = _ => Type.I32Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertDoubleLong[Type, Type] = LiftedConvert[Double, Long, Type, Type, BaseType[Double], BaseType[Long], Overflow && Bits](extract = _.asF64, inject = _ => Type.I64Type)

  given (using failure: Failure, effectStack: EffectStack): ConvertDoubleFloat[Type, Type] = LiftedConvert[Double, Float, Type, Type, BaseType[Double], BaseType[Float], NilCC.type](extract = _.asF64, inject = _ => Type.F32Type)