package sturdy.values.abstraction.taint

import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.*
import sturdy.values.floating.FloatOps
import sturdy.values.integer.IntegerOps
import sturdy.values.relational.{EqOps, OrderingOps, UnsignedOrderingOps}

enum Taint:
  case Tainted
  case Untainted
  case TopTaint

  inline def <=(s2: Taint): Boolean = this == s2 || s2 == TopTaint

import Taint.*

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