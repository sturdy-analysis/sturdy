package sturdy.values.floating

import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.config
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.*
import sturdy.values.ordering.OrderingOps
import sturdy.values.ordering.EqOps
import sturdy.values.types.BaseType

import scala.util.Random
import java.lang.Float as JFloat
import java.lang.Double as JDouble
import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.reflect.ClassTag

trait TypeFloatOps[F, T](val floatType: T, typeError: String => T) extends FloatOps[F, T]:
  override def floatingLit(f: F): T = floatType
  override def randomFloat(): T = floatType
  override def add(v1: T, v2: T): T = binFloatOp("+", v1, v2)
  override def sub(v1: T, v2: T): T = binFloatOp("-", v1, v2)
  override def mul(v1: T, v2: T): T = binFloatOp("*", v1, v2)
  override def div(v1: T, v2: T): T = binFloatOp("/", v1, v2)
  override def min(v1: T, v2: T): T = binFloatOp("min", v1, v2)
  override def max(v1: T, v2: T): T = binFloatOp("max", v1, v2)
  override def absolute(v: T): T = unFloatOp("abs", v)
  override def negated(v: T): T = unFloatOp("negated", v)
  override def sqrt(v: T): T = unFloatOp("sqrt", v)
  override def ceil(v: T): T = unFloatOp("ceil", v)
  override def floor(v: T): T = unFloatOp("floor", v)
  override def truncate(v: T): T = unFloatOp("truncate", v)
  override def nearest(v: T): T = unFloatOp("nearest", v)
  override def copysign(v: T, sign: T): T = binFloatOp("copysign", v, sign)

  private inline def unFloatOp(op: String, v1: T): T =
    if (v1 == floatType)
      floatType
    else
      typeError(s"Expected $floatType as argument to operator $op, but got $v1")

  private inline def binFloatOp(op: String, v1: T, v2: T): T =
    if (v1 == floatType && v2 == floatType)
      floatType
    else
      typeError(s"Expected two $floatType as arguments to operator $op, but got $v1 and $v2")


given BaseTypeFloatOps[B: ClassTag](using Fractional[B]): FloatOps[B, BaseType[B]] with
  def floatingLit(f: B): BaseType[B] = BaseType[B]
  def randomFloat(): BaseType[B] = BaseType[B]
  def add(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def sub(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def mul(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def div(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]

  def min(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def max(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]

  def absolute(v: BaseType[B]): BaseType[B] = BaseType[B]
  def negated(v: BaseType[B]): BaseType[B] = BaseType[B]
  def sqrt(v: BaseType[B]): BaseType[B] = BaseType[B]
  def pow(base: BaseType[B], exponent: BaseType[B]): BaseType[B] = BaseType[B]
  def ceil(v: BaseType[B]): BaseType[B] = BaseType[B]
  def floor(v: BaseType[B]): BaseType[B] = BaseType[B]
  def truncate(v: BaseType[B]): BaseType[B] = BaseType[B]
  def nearest(v: BaseType[B]): BaseType[B] = BaseType[B]
  def copysign(v: BaseType[B], sign: BaseType[B]): BaseType[B] = BaseType[B]
