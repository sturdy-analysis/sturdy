package sturdy.values.types

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.convert.Convert
import sturdy.values.ordering.{EqOps, OrderingOps, UnsignedOrderingOps}
import sturdy.values.*
import sturdy.values.booleans.{BooleanBranching, BooleanOps, BreakIf}
import sturdy.values.convert.ConversionFailure
import sturdy.values.convert.ConvertConfig

import scala.reflect.ClassTag

class BaseType[B](using val tag: ClassTag[B]):
  override def equals(obj: Any): Boolean = obj match
    case that: BaseType[_] => this.tag == that.tag
    case _ => false

  override def hashCode(): Int = tag.hashCode()

  override def toString: String = tag.runtimeClass.getSimpleName

object BaseType:
  def apply[B: ClassTag] = new BaseType[B]

given FiniteBaseType[B]: Finite[BaseType[B]] with {}

given JoinBaseType[B: ClassTag]: Join[BaseType[B]] with
  override def apply(v1: BaseType[B], v2: BaseType[B]): MaybeChanged[BaseType[B]] = MaybeChanged.Unchanged(v1)

export sturdy.values.finitely

given TopBaseType[B: ClassTag]: Top[BaseType[B]] with
  def top: BaseType[B] = BaseType[B]

given BaseTypeEqOps[B: ClassTag]: EqOps[BaseType[B], BaseType[Boolean]] with
  def equ(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def neq(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeOrderingOps[B: ClassTag]: OrderingOps[BaseType[B], BaseType[Boolean]] with
  def lt(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def le(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeUnsignedOrderingOps[B: ClassTag]: UnsignedOrderingOps[BaseType[B], BaseType[Boolean]] with
  def ltUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]
  def leUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeBooleanOps: BooleanOps[BaseType[Boolean]] with
  override def boolLit(b: Boolean): BaseType[Boolean] = BaseType[Boolean]
  override def and(v1: BaseType[Boolean], v2: BaseType[Boolean]): BaseType[Boolean] = BaseType[Boolean]
  override def not(v: BaseType[Boolean]): BaseType[Boolean] = BaseType[Boolean]
  override def or(v1: BaseType[Boolean], v2: BaseType[Boolean]): BaseType[Boolean] = BaseType[Boolean]

given BaseTypeConvert[B1: ClassTag, B2: ClassTag, Config <: ConvertConfig[_]](using Failure, EffectStack): Convert[B1, B2, BaseType[B1], BaseType[B2], Config] with
  override def apply(from: BaseType[B1], conf: Config): BaseType[B2] =
    joinWithFailure(BaseType[B2])(Failure(ConversionFailure, s"Potential conversion failure from $from to ${BaseType[B2]}"))

given BaseTypeBooleanBranching[R](using EffectStack, Join[R]): BooleanBranching[BaseType[Boolean], R] with
  override def boolBranch(v: BaseType[Boolean], thn: => R, els: => R): R =
    joinComputations(thn)(els)

given BaseTypeBreakIf[B](using effectStack: EffectStack): BreakIf[BaseType[Boolean]] with
  override type State = Unit

  override def break(br: State => Unit): Unit = br(())
  override def breakIf(cond: BaseType[Boolean])(break: State => Unit): Unit =
    joinComputations { } { break(()) }

  override def assertCondition(cond: BaseType[Boolean], state: State): Unit = {}

  override def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = (v1,v2) => Join(v1._1,v2._1).map((_,()))
  override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = (v1,v2) => Widen(v1._1,v2._1).map((_,()))

given BaseTypeOrdering[B: ClassTag]: Ordering[BaseType[B]] =
  (t1: BaseType[B], t2: BaseType[B]) =>
    if(t1 == t2)
      0
    else
      -1