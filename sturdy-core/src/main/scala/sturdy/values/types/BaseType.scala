package sturdy.values.types

import sturdy.data.{WithJoin, joinComputations, joinWithFailure, MakeJoined}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.convert.Convert
import sturdy.values.relational.{UnsignedCompareOps, EqOps, OrderingOps}
import sturdy.values.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.ConversionFailure
import sturdy.values.convert.ConvertConfig

import scala.reflect.ClassTag

class BaseType[B](using val tag: ClassTag[B]):
  override def equals(obj: Any): Boolean = obj match
    case that: BaseType[_] => this.tag == that.tag
    case _ => false

  override def hashCode(): Int = tag.hashCode()

object BaseType:
  def apply[B: ClassTag] = new BaseType[B]

given CombineBaseType[B: ClassTag, W <: Widening]: Combine[BaseType[B], W] with
  override def apply(v1: BaseType[B], v2: BaseType[B]): MaybeChanged[BaseType[B]] = MaybeChanged.Unchanged(v1)

given TopBaseType[B: ClassTag]: Top[BaseType[B]] with
  def top: BaseType[B] = BaseType[B]

given BaseTypeEqOps[B: ClassTag]: EqOps[BaseType[B], BaseType[Boolean]] with
  def equ(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def neq(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeOrderingOps[B: ClassTag]: OrderingOps[BaseType[B], BaseType[Boolean]] with
  def lt(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def le(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def ge(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def gt(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeUnsignedCompareOps[B: ClassTag]: UnsignedCompareOps[BaseType[B], BaseType[Boolean]] with
  def ltUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def leUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def geUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def gtUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeConvert[B1: ClassTag, B2: ClassTag, Config <: ConvertConfig[_]](using Failure, EffectStack): Convert[B1, B2, BaseType[B1], BaseType[B2], Config] with
  override def apply(from: BaseType[B1], conf: Config): BaseType[B2] =
    joinWithFailure(BaseType[B2])(Failure(ConversionFailure, "Potential conversion failure from $from to $to"))

given BaseTypeBooleanBranching[R](using EffectStack, Join[R]): BooleanBranching[BaseType[Boolean], R] with
  override def boolBranch(v: BaseType[Boolean], thn: => R, els: => R): R =
    joinComputations(thn)(els)
