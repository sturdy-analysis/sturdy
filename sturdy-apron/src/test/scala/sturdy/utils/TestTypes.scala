package sturdy.utils

import sturdy.apron.{*, given}
import sturdy.effect.failure.{*, given}
import sturdy.values.booleans.{BooleanOps, LiftedBooleanOps}
import sturdy.values.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.types.{*, given}

object TestTypes:

  enum Error extends FailureKind:
    case TypeError

  import Error.*

  enum Type:
    case IntType(baseType: BaseType[Int])
    case BoolType(baseType: BaseType[Boolean])

    def asInt(using f: Failure): BaseType[Int] =
      this match
        case IntType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected int, but got $this")

    def asBool(using f: Failure): BaseType[Boolean] =
      this match
        case BoolType(tpe) => tpe
        case _ => f.fail(TypeError, s"Expected bool, but got $this")

    override def toString: String =
      this match
        case IntType(_) => "int"
        case BoolType(_) => "boolean"

  given Ordering[Type] = {
    case (Type.IntType(_), Type.IntType(_)) | (Type.BoolType(_), Type.BoolType(_)) => 0
    case (Type.IntType(_), Type.BoolType(_)) => 1
    case (Type.BoolType(_), Type.IntType(_)) => -1
  }

  given IntegerOps[Int, Type] = LiftedIntegerOps[Int, Type, BaseType[Int]](extract = _.asInt, inject = Type.IntType(_))
  given OrderingOps[Type, Type] = LiftedOrderingOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = Type.BoolType(_))
  given EqOps[Type, Type] = LiftedEqOps[Type, Type, BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = Type.BoolType(_))
  given BooleanOps[Type] = LiftedBooleanOps[Type, BaseType[Boolean]](extract = _.asBool, inject = Type.BoolType(_))

  given ApronType[Type] with
    extension (tpe: Type)
      override def apronRepresentation: ApronRepresentation =
        tpe match
          case Type.IntType(baseType) => baseType.apronRepresentation
          case Type.BoolType(baseType) => baseType.apronRepresentation

      override def roundingDir: RoundingDir =
        tpe match
          case Type.IntType(baseType) => baseType.roundingDir
          case Type.BoolType(baseType) => baseType.roundingDir

      override def roundingType: RoundingType =
        tpe match
          case Type.IntType(baseType) => baseType.roundingType
          case Type.BoolType(baseType) => baseType.roundingType

      override def byteSize: Int =
        tpe match
          case Type.IntType(baseType) => baseType.byteSize
          case Type.BoolType(baseType) => baseType.byteSize

  given[W <: Widening]: Combine[Type, W] = {
    case (t@Type.IntType(_), Type.IntType(_)) => MaybeChanged.Unchanged(t)
    case (t@Type.BoolType(_), Type.BoolType(_)) => MaybeChanged.Unchanged(t)
    case (t1, t2) => throw new IllegalStateException(s"Cannot join type $t1 with type $t2")
  }