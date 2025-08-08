package sturdy.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.types.BaseType

import scala.reflect.ClassTag

trait TypeIntegerOps[I,T](val intType: T, val typeError: String => T) extends IntegerOps[I, T]:
  override def integerLit(i: I): T = intType
  override def randomInteger(): T = intType
  override def add(v1: T, v2: T): T = binIntOp("+", v1, v2)
  override def sub(v1: T, v2: T): T = binIntOp("-", v1, v2)
  override def mul(v1: T, v2: T): T = binIntOp("*", v1, v2)
  override def max(v1: T, v2: T): T = binIntOp("max", v1, v2)
  override def min(v1: T, v2: T): T = binIntOp("min", v1, v2)
  override def absolute(v: T): T = unIntOp("abs", v)
  override def div(v1: T, v2: T): T = binIntOp("/", v1, v2)
  override def divUnsigned(v1: T, v2: T): T = binIntOp("/ unsigned", v1, v2)
  override def remainder(v1: T, v2: T): T = binIntOp("remainder", v1, v2)
  override def remainderUnsigned(v1: T, v2: T): T = binIntOp("remainder unsigned", v1, v2)
  override def modulo(v1: T, v2: T): T = binIntOp("modulo", v1, v2)
  override def gcd(v1: T, v2: T): T = binIntOp("gcd", v1, v2)
  override def bitAnd(v1: T, v2: T): T = binIntOp("bitAnd", v1, v2)
  override def bitOr(v1: T, v2: T): T = binIntOp("bitOr", v1, v2)
  override def bitXor(v1: T, v2: T): T = binIntOp("bitXor", v1, v2)
  override def shiftLeft(v: T, shift: T): T = binIntOp("shiftLeft", v, shift)
  override def shiftRight(v: T, shift: T): T = binIntOp("shiftRight", v, shift)
  override def shiftRightUnsigned(v: T, shift: T): T = binIntOp("shiftRight unsigned", v, shift)
  override def rotateLeft(v: T, shift: T): T = binIntOp("rotateLeft", v, shift)
  override def rotateRight(v: T, shift: T): T = binIntOp("rotateRight", v, shift)
  override def countLeadingZeros(v: T): T = unIntOp("countLeadingZeros", v)
  override def countTrailingZeros(v: T): T = unIntOp("countTrailingZeros", v)
  override def nonzeroBitCount(v: T): T = unIntOp("nonzeroBitCount", v)
  override def invertBits(v: T): T = unIntOp("invertBits", v)

  private inline def unIntOp(op: String, v1: T): T =
    if(v1 == intType)
      intType
    else
      typeError(s"Expected $intType as argument to operator $op, but got $v1")
  private inline def binIntOp(op: String, v1: T, v2: T): T =
    if(v1 == intType && v2 == intType)
      intType
    else
      typeError(s"Expected two $intType as arguments to operator $op, but got $v1 and $v2")


given BaseTypeIntegerOps[B: ClassTag](using f: Failure, j: EffectStack, base: Integral[B]): IntegerOps[B, BaseType[B]] with
  def integerLit(i: B): BaseType[B] = BaseType[B]
  def randomInteger(): BaseType[B] = BaseType[B]

  def add(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def sub(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]
  def mul(v1: BaseType[B], v2: BaseType[B]): BaseType[B] = BaseType[B]

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
