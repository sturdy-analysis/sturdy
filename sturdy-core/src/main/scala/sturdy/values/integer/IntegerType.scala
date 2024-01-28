package sturdy.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.types.BaseType

import scala.reflect.ClassTag

given TypeIntegerOps[B: ClassTag](using f: Failure, j: EffectStack, base: Integral[B]): IntegerOps[B, BaseType[B]] with
  def integerLit(i: B): BaseType[B] = BaseType[B]
  def randomInteger(): BaseType[B] = BaseType[B]

  override def neg(v: BaseType[B]): BaseType[B] = ???

  def add(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def sub(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def mul(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def neg(v: BaseType[B]): BaseType[B] = BaseType[B]

  def max(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def min(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]

  def div(v1: BaseType[B], v2: BaseType[B]): BaseType[B] =
    j.joinWithFailure(BaseType[B])(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
  def divUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[B] =
    j.joinWithFailure(BaseType[B])(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
  def remainder(v1: BaseType[B], v2: BaseType[B]): BaseType[B] =
    j.joinWithFailure(BaseType[B])(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
  def remainderUnsigned(v1: BaseType[B], v2: BaseType[B]): BaseType[B] =
    j.joinWithFailure(BaseType[B])(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
  def modulo(v1: BaseType[B], v2: BaseType[B]): BaseType[B] =
    j.joinWithFailure(BaseType[B])(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
  def gcd(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]

  def absolute(v: BaseType[B]): BaseType[B] = BaseType[B]
  def bitAnd(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def bitOr(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def bitXor(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def shiftLeft(v: BaseType[B], shift: BaseType[B]): BaseType[B] = BaseType[B]
  def shiftRight(v: BaseType[B], shift: BaseType[B]): BaseType[B] = BaseType[B]
  def shiftRightUnsigned(v: BaseType[B], shift: BaseType[B]): BaseType[B] = BaseType[B]
  def rotateLeft(v: BaseType[B], shift: BaseType[B]): BaseType[B] = BaseType[B]
  def rotateRight(v: BaseType[B], shift: BaseType[B]): BaseType[B] = BaseType[B]
  def countLeadingZeros(v: BaseType[B]): BaseType[B] = BaseType[B]
  def countTrailingZeros(v: BaseType[B]): BaseType[B] = BaseType[B]
  def nonzeroBitCount(v: BaseType[B]): BaseType[B] = BaseType[B]
  def invertBits(v: BaseType[B]): BaseType[B] = BaseType[B]
