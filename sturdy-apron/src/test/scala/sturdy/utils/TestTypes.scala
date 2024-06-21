package sturdy.utils

import sturdy.apron.{*, given}
import sturdy.effect.failure.{*, given}
import sturdy.values.booleans.{BooleanOps, LiftedBooleanOps}
import sturdy.values.config.Bits
import sturdy.values.convert.LiftedConvert
import sturdy.values.floating.{FloatOps, LiftedFloatOps}
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
    case IntType(baseType: BaseType[Int])
    case LongType(baseType: BaseType[Long])
    case FloatType(baseType: BaseType[Float])
    case DoubleType(baseType: BaseType[Double])
    case BoolType(baseType: BaseType[Boolean])

    def asInt(using f: Failure): BaseType[Int] =
      this match
        case IntType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected int, but got $this")
    def asLong(using f: Failure): BaseType[Long] =
      this match
        case LongType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected int, but got $this")
    def asFloat(using f: Failure): BaseType[Float] =
      this match
        case FloatType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected float, but got $this")

    def asDouble(using f: Failure): BaseType[Double] =
      this match
        case DoubleType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected float, but got $this")

    def asBool(using f: Failure): BaseType[Boolean] =
      this match
        case BoolType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected bool, but got $this")

    override def toString: String =
      this match
        case IntType(_) => "int"
        case LongType(_) => "int"
        case FloatType(_) => "float"
        case DoubleType(_) => "doulbe"
        case BoolType(_) => "boolean"

  given Ordering[Type] = {
    case (Type.IntType(_), Type.IntType(_)) |
         (Type.LongType(_), Type.LongType(_)) |
         (Type.FloatType(_), Type.FloatType(_)) |
         (Type.DoubleType(_), Type.DoubleType(_)) |
         (Type.BoolType(_), Type.BoolType(_)) => 0

    case (Type.IntType(_), Type.LongType(_)) |
         (Type.IntType(_), Type.FloatType(_)) |
         (Type.IntType(_), Type.DoubleType(_)) |
         (Type.IntType(_), Type.BoolType(_)) |

         (Type.LongType(_), Type.FloatType(_)) |
         (Type.LongType(_), Type.DoubleType(_)) |
         (Type.LongType(_), Type.BoolType(_)) |

         (Type.FloatType(_), Type.DoubleType(_)) |
         (Type.FloatType(_), Type.BoolType(_)) |

         (Type.DoubleType(_), Type.BoolType(_))
           => 1
    case
         (Type.BoolType(_), Type.DoubleType(_)) |
         (Type.BoolType(_), Type.FloatType(_)) |
         (Type.BoolType(_), Type.LongType(_)) |
         (Type.BoolType(_), Type.IntType(_)) |

         (Type.DoubleType(_), Type.FloatType(_)) |
         (Type.DoubleType(_), Type.LongType(_)) |
         (Type.DoubleType(_), Type.IntType(_)) |

         (Type.FloatType(_), Type.LongType(_)) |
         (Type.FloatType(_), Type.IntType(_)) |

         (Type.LongType(_), Type.IntType(_))
            => -1
  }

  given IntegerOps[Int, Type] = LiftedIntegerOps[Int, Type, BaseType[Int]](extract = _.asInt, inject = Type.IntType(_))
  given IntegerOps[Long, Type] = LiftedIntegerOps[Long, Type, BaseType[Long]](extract = _.asLong, inject = Type.LongType(_))
  given FloatOps[Float, Type] = LiftedFloatOps[Float, Type, BaseType[Float]](extract = _.asFloat, inject = Type.FloatType(_))
  given FloatOps[Double, Type] = LiftedFloatOps[Double, Type, BaseType[Double]](extract = _.asDouble, inject = Type.DoubleType(_))
  given OrderingOps[Type, Type] = LiftedOrderingOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = Type.BoolType(_))
  given EqOps[Type, Type] = LiftedEqOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = Type.BoolType(_))
  given BooleanOps[Type] = LiftedBooleanOps[Type, BaseType[Boolean]](extract = _.asBool, inject = Type.BoolType(_))
  given ConvertIntLong[Type, Type] = LiftedConvert[Int, Long, Type, Type, BaseType[Int], BaseType[Long], Bits](extract = _.asInt, inject = Type.LongType(_))

  given ApronType[Type] with
    extension (tpe: Type)
      override def apronRepresentation: ApronRepresentation =
        tpe match
          case Type.IntType(baseType) => baseType.apronRepresentation
          case Type.LongType(baseType) => baseType.apronRepresentation
          case Type.BoolType(baseType) => baseType.apronRepresentation
          case Type.FloatType(baseType) => baseType.apronRepresentation
          case Type.DoubleType(baseType) => baseType.apronRepresentation

      override def roundingDir: RoundingDir =
        tpe match
          case Type.IntType(baseType) => baseType.roundingDir
          case Type.LongType(baseType) => baseType.roundingDir
          case Type.FloatType(baseType) => baseType.roundingDir
          case Type.DoubleType(baseType) => baseType.roundingDir
          case Type.BoolType(baseType) => baseType.roundingDir

      override def roundingType: RoundingType =
        tpe match
          case Type.IntType(baseType) => baseType.roundingType
          case Type.LongType(baseType) => baseType.roundingType
          case Type.FloatType(baseType) => baseType.roundingType
          case Type.DoubleType(baseType) => baseType.roundingType
          case Type.BoolType(baseType) => baseType.roundingType

      override def byteSize: Int =
        tpe match
          case Type.IntType(baseType) => baseType.byteSize
          case Type.LongType(baseType) => baseType.byteSize
          case Type.FloatType(baseType) => baseType.byteSize
          case Type.DoubleType(baseType) => baseType.byteSize
          case Type.BoolType(baseType) => baseType.byteSize

  given[W <: Widening]: Combine[Type, W] = {
    case (t@Type.IntType(_), Type.IntType(_)) => MaybeChanged.Unchanged(t)
    case (t@Type.LongType(_), Type.LongType(_)) => MaybeChanged.Unchanged(t)
    case (t@Type.FloatType(_), Type.FloatType(_)) => MaybeChanged.Unchanged(t)
    case (t@Type.DoubleType(_), Type.DoubleType(_)) => MaybeChanged.Unchanged(t)
    case (t@Type.BoolType(_), Type.BoolType(_)) => MaybeChanged.Unchanged(t)
    case (t1, t2) => throw new IllegalStateException(s"Cannot join type $t1 with type $t2")
  }
