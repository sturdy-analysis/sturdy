package sturdy.values.taint

import sturdy.effect.failure.Failure
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import sturdy.values.ints.IntOps
import sturdy.values.longs.LongOps
import sturdy.values.relational.{CompareOps, EqOps, UnsignedCompareOps}
import sturdy.values.{Combine, Top, Topped, Widening, taint}
import sturdy.values.convert.*

enum Taint:
  case Tainted
  case Untainted
  case TopTaint

  def <(s2: Taint): Boolean = this == s2 || s2 == TopTaint

import sturdy.values.taint.Taint.*

given TaintTop: Top[Taint] with
  override def top: Taint = TopTaint

given CombineTaint[W <: Widening]: Combine[Taint, W] with
  override def apply(v1: Taint, v2: Taint): Taint = (v1,v2) match
    case (Tainted, Tainted) => Tainted
    case (Untainted, Untainted) => Untainted
    case (_, _) => TopTaint

case class TaintProduct[V](taint: Taint, value: V):

  inline def binary[B, A >: V](f: (V, A) => B, other: TaintProduct[A]): TaintProduct[B] =
    TaintProduct(Combine(this.taint,other.taint), f(this.value,other.value))

  inline def unary[B](f: V => B): TaintProduct[B] = TaintProduct(this.taint, f(this.value))

  inline def copyTaint[B](b: B): TaintProduct[B] = TaintProduct(this.taint, b)

def untainted[V](v: V) = TaintProduct(Untainted, v)
def tainted[V](v: V) = TaintProduct(Tainted, v)

given TaintProductTop[V, W <: Widening](using vTop: Top[V]): Top[TaintProduct[V]] with
  override def top: TaintProduct[V] = TaintProduct(Top.top, Top.top)

given CombineTaintProduct[V, W <: Widening](using comb: Combine[V, W]): Combine[TaintProduct[V], W] with
  override def apply(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] =
    TaintProduct(Combine(v1.taint, v2.taint), comb.apply(v1.value, v2.value))

given TaintIntOps[V](using ops: IntOps[V]): IntOps[TaintProduct[V]] with
  def intLit(i: Int): TaintProduct[V] = untainted(ops.intLit(i))
  def randomInt(): TaintProduct[V] = untainted(ops.randomInt())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)

  def max(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.max, v2)
  def min(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.min, v2)

  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def divUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainderUnsigned, v2)
  def modulo(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.modulo, v2)
  def gcd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.gcd, v2)

  def absolute(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.absolute)
  def bitAnd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countLeadingZeros)
  def countTrailinZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countTrailinZeros)
  def nonzeroBitCount(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nonzeroBitCount)

given TaintLongOps[V](using ops: LongOps[V], f: Failure): LongOps[TaintProduct[V]] with
  def longLit(l: Long): TaintProduct[V] = untainted(ops.longLit(l))
  def randomLong(): TaintProduct[V] = untainted(ops.randomLong())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)

  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def divUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainderUnsigned, v2)

  def bitAnd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countLeadingZeros)
  def countTrailinZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countTrailinZeros)
  def nonzeroBitCount(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nonzeroBitCount)

given TaintDoubleOps[V](using ops: DoubleOps[V]): DoubleOps[TaintProduct[V]] with
  def doubleLit(d: Double): TaintProduct[V] = untainted(ops.doubleLit(d))
  def randomDouble(): TaintProduct[V] = untainted(ops.randomDouble())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)
  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def min(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.min, v2)
  def max(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.max, v2)

  def absolute(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.absolute)
  def negated(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.negated)
  def sqrt(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.sqrt)
  def ceil(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.ceil)
  def floor(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.floor)
  def truncate(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.truncate)
  def nearest(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nearest)
  def copysign(v: TaintProduct[V], sign: TaintProduct[V]): TaintProduct[V] = v.binary(ops.copysign, sign)

  def logNatural(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.logNatural)

given TaintFloatOps[V](using ops: FloatOps[V]): FloatOps[TaintProduct[V]] with
  def floatLit(f: Float): TaintProduct[V] =  untainted(ops.floatLit(f))
  def randomFloat(): TaintProduct[V] = untainted(ops.randomFloat())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)
  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def min(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.min, v2)
  def max(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.max, v2)

  def absolute(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.absolute)
  def negated(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.negated)
  def sqrt(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.sqrt)
  def ceil(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.ceil)
  def floor(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.floor)
  def truncate(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.truncate)
  def nearest(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nearest)
  def copysign(v: TaintProduct[V], sign: TaintProduct[V]): TaintProduct[V] = v.binary(ops.copysign, sign)

given TaintEqOps[A,B](using ops: EqOps[A,B]): EqOps[TaintProduct[A],TaintProduct[B]] with
  override def equ(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.equ, v2)
  override def neq(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.neq, v2)

given TaintCompareOps[A,B](using ops: CompareOps[A,B]): CompareOps[TaintProduct[A],TaintProduct[B]] with
  override def lt(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.lt, v2)
  override def le(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.le, v2)
  override def ge(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.ge, v2)
  override def gt(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.gt, v2)

given TaintUnsignedCompareOps[A,B](using ops: UnsignedCompareOps[A,B]): UnsignedCompareOps[TaintProduct[A],TaintProduct[B]] with
  override def ltUnsigned(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.ltUnsigned, v2)
  override def leUnsigned(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.leUnsigned, v2)
  override def geUnsigned(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.geUnsigned, v2)
  override def gtUnsigned(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.gtUnsigned, v2)

given TaintConvert[From, To, VFrom, VTo, Config](using conv: Convert[From, To, VFrom, VTo, Config]):
  Convert[From, To, TaintProduct[VFrom], TaintProduct[VTo], Config] with
  override def apply(from: TaintProduct[VFrom], conf: Config): TaintProduct[VTo] =
    from.unary(x => conv.apply(x, conf))
