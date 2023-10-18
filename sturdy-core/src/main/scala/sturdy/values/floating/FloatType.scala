package sturdy.values.floating

import sturdy.effect.failure.Failure
import sturdy.values.Structural
import sturdy.values.config
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.*
import sturdy.values.relational.OrderingOps
import sturdy.values.relational.EqOps
import sturdy.values.types.BaseType

import scala.util.Random
import java.lang.Float as JFloat
import java.lang.Double as JDouble
import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.reflect.ClassTag


given TypeFloatOps[B: ClassTag] (using Fractional[B]): FloatOps[B, BaseType[B]] with
  def floatingLit(f: B): BaseType[B] = BaseType[B]
  override def NaN: BaseType[B] = BaseType[B]
  override def posInfinity: BaseType[B] = BaseType[B]
  override def negInfinity: BaseType[B] = BaseType[B]

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
  def ceil(v: BaseType[B]): BaseType[B] = BaseType[B]
  def floor(v: BaseType[B]): BaseType[B] = BaseType[B]
  def truncate(v: BaseType[B]): BaseType[B] = BaseType[B]
  def nearest(v: BaseType[B]): BaseType[B] = BaseType[B]
  def copysign(v: BaseType[B], sign: BaseType[B]): BaseType[B] = BaseType[B]
