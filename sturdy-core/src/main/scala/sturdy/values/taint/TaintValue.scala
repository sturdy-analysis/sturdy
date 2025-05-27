package sturdy.values.taint

import sturdy.effect.failure.Failure
import sturdy.values.MaybeChanged
import sturdy.values.booleans.BooleanBranching
import sturdy.values.floating.FloatOps
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.{UnsignedOrderingOps, EqOps, OrderingOps}
import sturdy.values.*
import sturdy.values.convert.*

enum Taint:
  case Tainted
  case Untainted
  case TopTaint

  inline def <=(s2: Taint): Boolean = this == s2 || s2 == TopTaint

import sturdy.values.taint.Taint.*

given TaintTop: Top[Taint] with
  override def top: Taint = TopTaint

given CombineTaint[W <: Widening]: Combine[Taint, W] with
  override def apply(v1: Taint, v2: Taint): MaybeChanged[Taint] = (v1,v2) match
    case (Tainted, Tainted) => Unchanged(Tainted)
    case (Untainted, Untainted) => Unchanged(Untainted)
    case (TopTaint, _) => Unchanged(TopTaint)
    case (_, _) => Changed(TopTaint)

case class TaintProduct[V](taint: Taint, value: V):

  def map[W](f: V => W): TaintProduct[W] =
    TaintProduct(taint, f(value))

  inline def binary[B, A >: V](f: (V, A) => B, other: TaintProduct[A]): TaintProduct[B] =
    TaintProduct(Combine(this.taint,other.taint).get, f(this.value,other.value))

  inline def unary[B](f: V => B): TaintProduct[B] = TaintProduct(this.taint, f(this.value))

  inline def copyTaint[B](b: B): TaintProduct[B] = TaintProduct(this.taint, b)

def untainted[V](v: V) = TaintProduct(Untainted, v)
def tainted[V](v: V) = TaintProduct(Tainted, v)

def injectTaint[V](t: Taint, v: V) = TaintProduct(t,v)

given TaintProductTop[V, W <: Widening](using vTop: Top[V]): Top[TaintProduct[V]] with
  override def top: TaintProduct[V] = TaintProduct(Top.top, Top.top)

given CombineTaintProduct[V, W <: Widening](using comb: Combine[V, W]): Combine[TaintProduct[V], W] with
  override def apply(v1: TaintProduct[V], v2: TaintProduct[V]): MaybeChanged[TaintProduct[V]] =
    val joinedTaint = Join(v1.taint, v2.taint)
    val joinedVal = comb(v1.value, v2.value)
    MaybeChanged(TaintProduct(joinedTaint.get, joinedVal.get), joinedTaint.hasChanged || joinedVal.hasChanged)


given TaintIntegerOps[B, V] (using ops: IntegerOps[B, V]): IntegerOps[B, TaintProduct[V]] with
  def integerLit(i: B): TaintProduct[V] = untainted(ops.integerLit(i))
  def randomInteger(): TaintProduct[V] = untainted(ops.randomInteger())

  def add(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.add, v2)
  def sub(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.sub, v2)
  def mul(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.mul, v2)

  def max(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.max, v2)
  def min(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.min, v2)
  def absolute(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.absolute)

  def div(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.div, v2)
  def divUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.divUnsigned, v2)
  def remainder(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainder, v2)
  def remainderUnsigned(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.remainderUnsigned, v2)
  def modulo(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.modulo, v2)
  def gcd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.gcd, v2)

  def bitAnd(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitAnd, v2)
  def bitOr(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitOr, v2)
  def bitXor(v1: TaintProduct[V], v2: TaintProduct[V]): TaintProduct[V] = v1.binary(ops.bitXor, v2)
  def shiftLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftLeft, shift)
  def shiftRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRight, shift)
  def shiftRightUnsigned(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.shiftRightUnsigned, shift)
  def rotateLeft(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateLeft, shift)
  def rotateRight(v: TaintProduct[V], shift: TaintProduct[V]): TaintProduct[V] = v.binary(ops.rotateRight, shift)
  def countLeadingZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countLeadingZeros)
  def countTrailingZeros(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.countTrailingZeros)
  def nonzeroBitCount(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.nonzeroBitCount)
  def invertBits(v: TaintProduct[V]): TaintProduct[V] = v.unary(ops.invertBits)

given TaintFloatOps[B, V] (using ops: FloatOps[B, V]): FloatOps[B, TaintProduct[V]] with
  def floatingLit(f: B): TaintProduct[V] =  untainted(ops.floatingLit(f))
  def NaN: TaintProduct[V] = untainted(ops.NaN)
  def posInfinity: TaintProduct[V] = untainted(ops.posInfinity)
  def negInfinity: TaintProduct[V] = untainted(ops.negInfinity)

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

given TaintOrderingOps[A,B] (using ops: OrderingOps[A,B]): OrderingOps[TaintProduct[A],TaintProduct[B]] with
  override def lt(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.lt, v2)
  override def le(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.le, v2)

given TaintUnsignedOrderingOps[A,B] (using ops: UnsignedOrderingOps[A,B]): UnsignedOrderingOps[TaintProduct[A],TaintProduct[B]] with
  override def ltUnsigned(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.ltUnsigned, v2)
  override def leUnsigned(v1: TaintProduct[A], v2: TaintProduct[A]): TaintProduct[B] = v1.binary(ops.leUnsigned, v2)

given TaintConvert[From, To, VFrom, VTo, Config <: ConvertConfig[_]](using conv: Convert[From, To, VFrom, VTo, Config]):
  Convert[From, To, TaintProduct[VFrom], TaintProduct[VTo], Config] with
  override def apply(from: TaintProduct[VFrom], conf: Config): TaintProduct[VTo] =
    from.unary(x => conv(x, conf))

given TaintPointwiseConvert[From, To, VFrom, VToElem, Config <: ConvertConfig[_]](using conv: Convert[From, To, VFrom, Seq[VToElem], Config]):
  Convert[From, To, TaintProduct[VFrom], Seq[TaintProduct[VToElem]], Config] with
  override def apply(from: TaintProduct[VFrom], conf: Config): Seq[TaintProduct[VToElem]] =
    val converted = conv(from.value, conf)
    converted.map(from.copyTaint(_))

given TaintCoPointwiseConvert[From, To, VFromElem, VTo, Config <: ConvertConfig[_]](using conv: Convert[From, To, Seq[VFromElem], VTo, Config]):
  Convert[From, To, Seq[TaintProduct[VFromElem]], TaintProduct[VTo], Config] with
  override def apply(from: Seq[TaintProduct[VFromElem]], conf: Config): TaintProduct[VTo] =
    var taint = if (from.isEmpty) Untainted else from.head.taint
    val fromValue = from.map { tv =>
      taint = Join(taint, tv.taint).get
      tv.value
    }
    val converted = conv(fromValue, conf)
    TaintProduct(taint, converted)

given TaintBooleanBranching[V, R](using ops: BooleanBranching[V, R]): BooleanBranching[TaintProduct[V], R] with
  def boolBranch(v: TaintProduct[V], thn: => R, els: => R): R = ops.boolBranch(v.value, thn, els)