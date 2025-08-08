package sturdy.util

import sturdy.apron.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.{*, given}
import sturdy.values.booleans.{BooleanOps, LiftedBooleanOps}
import sturdy.values.config.{Bits, Overflow}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.types.{*, given}

object TestTypes:

  enum Error extends FailureKind:
    case TypeError

  import Error.*

  enum Type:
    case IntType
    case LongType
    case FloatType
    case DoubleType
    case BoolType

    def asInt(using f: Failure): BaseType[Int] =
      this match
        case IntType => BaseType[Int]
        case _ => f.fail(TypeError, s"Expected int, but got $this")
    def asLong(using f: Failure): BaseType[Long] =
      this match
        case LongType => BaseType[Long]
        case _ => f.fail(TypeError, s"Expected long, but got $this")
    def asFloat(using f: Failure): BaseType[Float] =
      this match
        case FloatType => BaseType[Float]
        case _ => f.fail(TypeError, s"Expected float, but got $this")

    def asDouble(using f: Failure): BaseType[Double] =
      this match
        case DoubleType => BaseType[Double]
        case _ => f.fail(TypeError, s"Expected float, but got $this")

    def asBool(using f: Failure): BaseType[Boolean] =
      this match
        case BoolType => BaseType[Boolean]
        case _ => f.fail(TypeError, s"Expected bool, but got $this")

    override def toString: String =
      this match
        case IntType => "int"
        case LongType => "long"
        case FloatType => "float"
        case DoubleType => "doulbe"
        case BoolType => "boolean"

  given Ordering[Type] = {
    case (Type.IntType, Type.IntType) |
         (Type.LongType, Type.LongType) |
         (Type.FloatType, Type.FloatType) |
         (Type.DoubleType, Type.DoubleType) |
         (Type.BoolType, Type.BoolType) => 0

    case (Type.IntType, Type.LongType) |
         (Type.IntType, Type.FloatType) |
         (Type.IntType, Type.DoubleType) |
         (Type.IntType, Type.BoolType) |

         (Type.LongType, Type.FloatType) |
         (Type.LongType, Type.DoubleType) |
         (Type.LongType, Type.BoolType) |

         (Type.FloatType, Type.DoubleType) |
         (Type.FloatType, Type.BoolType) |

         (Type.DoubleType, Type.BoolType)
           => 1
    case
         (Type.BoolType, Type.DoubleType) |
         (Type.BoolType, Type.FloatType) |
         (Type.BoolType, Type.LongType) |
         (Type.BoolType, Type.IntType) |

         (Type.DoubleType, Type.FloatType) |
         (Type.DoubleType, Type.LongType) |
         (Type.DoubleType, Type.IntType) |

         (Type.FloatType, Type.LongType) |
         (Type.FloatType, Type.IntType) |

         (Type.LongType, Type.IntType)
            => -1
  }

  given (using failure: Failure, effectStack: EffectStack): IntegerOps[Int, Type] = LiftedIntegerOps[Int, Type, BaseType[Int]](extract = _.asInt, inject = _ => Type.IntType)
  given (using failure: Failure, effectStack: EffectStack): IntegerOps[Long, Type] = LiftedIntegerOps[Long, Type, BaseType[Long]](extract = _.asLong, inject = _ => Type.LongType)
  given (using failure: Failure, effectStack: EffectStack): FloatOps[Float, Type] = LiftedFloatOps[Float, Type, BaseType[Float]](extract = _.asFloat, inject = _ => Type.FloatType)
  given (using failure: Failure, effectStack: EffectStack): FloatOps[Double, Type] = LiftedFloatOps[Double, Type, BaseType[Double]](extract = _.asDouble, inject = _ => Type.DoubleType)
  given (using failure: Failure, effectStack: EffectStack): OrderingOps[Type, Type] = LiftedOrderingOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = _ => Type.BoolType)
  given (using failure: Failure, effectStack: EffectStack): EqOps[Type, Type] = LiftedEqOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = _ => Type.BoolType)
  given (using failure: Failure, effectStack: EffectStack): BooleanOps[Type] = LiftedBooleanOps[Type, BaseType[Boolean]](extract = _.asBool, inject = _ => Type.BoolType)
  given (using failure: Failure, effectStack: EffectStack): ConvertIntLong[Type, Type] = LiftedConvert[Int, Long, Type, Type, BaseType[Int], BaseType[Long], Bits](extract = _.asInt, inject = _ => Type.LongType)
  given (using failure: Failure, effectStack: EffectStack): ConvertLongInt[Type, Type] = LiftedConvert[Long, Int, Type, Type, BaseType[Long], BaseType[Int], NilCC.type](extract = _.asLong, inject = _ => Type.IntType)
  given (using failure: Failure, effectStack: EffectStack): ConvertFloatLong[Type, Type] = LiftedConvert[Float, Long, Type, Type, BaseType[Float], BaseType[Long], Overflow && Bits](extract = _.asFloat, inject = _ => Type.LongType)
  given (using failure: Failure, effectStack: EffectStack): ConvertFloatInt[Type, Type] = LiftedConvert[Float, Int, Type, Type, BaseType[Float], BaseType[Int], Overflow && Bits](extract = _.asFloat, inject = _ => Type.IntType)
  given (using failure: Failure, effectStack: EffectStack): ConvertDoubleLong[Type, Type] = LiftedConvert[Double, Long, Type, Type, BaseType[Double], BaseType[Long], Overflow && Bits](extract = _.asDouble, inject = _ => Type.LongType)
  given (using failure: Failure, effectStack: EffectStack): ConvertDoubleInt[Type, Type] = LiftedConvert[Double, Int, Type, Type, BaseType[Double], BaseType[Int], Overflow && Bits](extract = _.asDouble, inject = _ => Type.IntType)
  given (using failure: Failure, effectStack: EffectStack): ConvertDoubleFloat[Type, Type] = LiftedConvert[Double, Float, Type, Type, BaseType[Double], BaseType[Float], NilCC.type](extract = _.asDouble, inject = _ => Type.FloatType)
  given (using failure: Failure, effectStack: EffectStack): ConvertFloatDouble[Type, Type] = LiftedConvert[Float, Double, Type, Type, BaseType[Float], BaseType[Double], NilCC.type](extract = _.asFloat, inject = _ => Type.DoubleType)
  given (using failure: Failure, effectStack: EffectStack): ConvertIntFloat[Type, Type] = LiftedConvert[Int, Float, Type, Type, BaseType[Int], BaseType[Float], Bits](extract = _.asInt, inject = _ => Type.FloatType)
  given (using failure: Failure, effectStack: EffectStack): ConvertIntDouble[Type, Type] = LiftedConvert[Int, Double, Type, Type, BaseType[Int], BaseType[Double], Bits](extract = _.asInt, inject = _ => Type.DoubleType)
  given (using failure: Failure, effectStack: EffectStack): ConvertLongFloat[Type, Type] = LiftedConvert[Long, Float, Type, Type, BaseType[Long], BaseType[Float], Bits](extract = _.asLong, inject = _ => Type.FloatType)
  given (using failure: Failure, effectStack: EffectStack): ConvertLongDouble[Type, Type] = LiftedConvert[Long, Double, Type, Type, BaseType[Long], BaseType[Double], Bits](extract = _.asLong, inject = _ => Type.DoubleType)

  given ApronType[Type] with
    extension (tpe: Type)
      override def apronRepresentation: ApronRepresentation =
        tpe match
          case Type.IntType => BaseType[Int].apronRepresentation
          case Type.LongType => BaseType[Long].apronRepresentation
          case Type.BoolType => BaseType[Boolean].apronRepresentation
          case Type.FloatType => BaseType[Float].apronRepresentation
          case Type.DoubleType => BaseType[Double].apronRepresentation

      override def roundingDir: RoundingDir =
        tpe match
          case Type.IntType => BaseType[Int].roundingDir
          case Type.LongType => BaseType[Long].roundingDir
          case Type.BoolType => BaseType[Boolean].roundingDir
          case Type.FloatType => BaseType[Float].roundingDir
          case Type.DoubleType => BaseType[Double].roundingDir

      override def roundingType: RoundingType =
        tpe match
          case Type.IntType => BaseType[Int].roundingType
          case Type.LongType => BaseType[Long].roundingType
          case Type.BoolType => BaseType[Boolean].roundingType
          case Type.FloatType => BaseType[Float].roundingType
          case Type.DoubleType => BaseType[Double].roundingType

      override def byteSize: Int =
        tpe match
          case Type.IntType => BaseType[Int].byteSize
          case Type.LongType => BaseType[Long].byteSize
          case Type.BoolType => BaseType[Boolean].byteSize
          case Type.FloatType => BaseType[Float].byteSize
          case Type.DoubleType => BaseType[Double].byteSize

  given[W <: Widening]: Combine[Type, W] = {
    case (t@Type.IntType, Type.IntType) => MaybeChanged.Unchanged(t)
    case (t@Type.LongType, Type.LongType) => MaybeChanged.Unchanged(t)
    case (t@Type.FloatType, Type.FloatType) => MaybeChanged.Unchanged(t)
    case (t@Type.DoubleType, Type.DoubleType) => MaybeChanged.Unchanged(t)
    case (t@Type.BoolType, Type.BoolType) => MaybeChanged.Unchanged(t)
    case (t1, t2) => throw new IllegalStateException(s"Cannot join type $t1 with type $t2")
  }
