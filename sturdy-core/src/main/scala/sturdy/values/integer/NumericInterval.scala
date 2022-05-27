package sturdy.values.integer

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.convert.&&
import sturdy.values.convert.*
import sturdy.values.relational.*

import java.nio.ByteOrder
import scala.collection.immutable.TreeSet
import Ordering.Implicits.infixOrderingOps
import Numeric.Implicits.infixNumericOps
import Integral.Implicits.infixIntegralOps

object NumericInterval:
  def top[I]: NumericInterval[I] = NumericInterval.Top()
  def apply[I](l: I, h: I)(using Ordering[I]): NumericInterval[I] =
    if (l <= h)
      NumericInterval.Bounded(l, h)
    else
      NumericInterval.Top()
  def fromBounds[I](b1: I, b2: I)(using Ordering[I]): NumericInterval[I] =
    if (b1 <= b2)
      NumericInterval.Bounded(b1, b2)
    else
      NumericInterval.Bounded(b2, b1)


enum NumericInterval[I]:
  case Top()
  case Bounded(low: I, high: I)

  def isTop: Boolean = this match
    case Top() => true
    case _ => false

  def join(other: NumericInterval[I])(using Ordering[I]): NumericInterval[I] = (this, other) match
    case (Top(), _) => this
    case (_, Top()) => other
    case (Bounded(l1, h1), Bounded(l2, h2)) => NumericInterval(l1.min(l2), h1.max(h2))

  def +(y: NumericInterval[I])(using Numeric[I]): NumericInterval[I] = (this, y) match
    case (Top(), _) => this
    case (_, Top()) => y
    case (Bounded(x1, x2), Bounded(y1, y2)) =>
      NumericInterval(x1 + y1, x2 + y2)

  def -(y: NumericInterval[I])(using Numeric[I]): NumericInterval[I] = (this, y) match
    case (Top(), _) => this
    case (_, Top()) => y
    case (Bounded(x1, x2), Bounded(y1, y2)) => NumericInterval(x1 - y2, x2 - y1)

  def *(y: NumericInterval[I])(using Numeric[I]): NumericInterval[I] = (this, y) match
    case (Top(), _) => this
    case (_, Top()) => y
    case (Bounded(x1, x2), Bounded(y1, y2)) =>
      val x1y1 = x1 * y1
      val x1y2 = x1 * y2
      val x2y1 = x2 * y1
      val x2y2 = x2 * y2
      NumericInterval(x1y1.min(x1y2).min(x2y1).min(x2y2), x1y1.max(x1y2).max(x2y1).max(x2y2))

  def /(y: NumericInterval[I])(using Integral[I]): NumericInterval[I] = (this, y) match
    case (Top(), _) => this
    case (_, Top()) => y
    case (Bounded(x1, x2), Bounded(y1, y2)) =>
      val x1y1 = x1 / y1
      val x1y2 = x1 / y2
      val x2y1 = x2 / y1
      val x2y2 = x2 / y2
      NumericInterval(x1y1.min(x1y2).min(x2y1).min(x2y2), x1y1.max(x1y2).max(x2y1).max(x2y2))

  def isZero(using Numeric[I]): Topped[Boolean] = this match
    case Top() => Topped.Top
    case Bounded(l, h) =>
      val fromInt = summon[Numeric[I]].fromInt
      if (l == fromInt(0) && h == fromInt(0))
        Topped.Actual(true)
      else if (l <= fromInt(0) && h >= fromInt(0))
        Topped.Top
      else
        Topped.Actual(false)

  def isConstant(using Ordering[I]): Boolean = this match
    case Top() => false
    case Bounded(l, h) => l == h

given IntervalIntegerOps[I](using Integral[I])(using f: Failure, j: EffectStack): IntegerOps[I, NumericInterval[I]] with
  private def fromInt(x: Int): I = summon[Numeric[I]].fromInt(x)

  def integerLit(i: I): NumericInterval[I] = NumericInterval(i, i)
  def randomInteger(): NumericInterval[I] = NumericInterval.top
  def add(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v1 + v2
  def sub(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v1 - v2
  def mul(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v1 * v2
  def div(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v2 match
    case NumericInterval.Top() => j.joinWithFailure(v2)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case NumericInterval.Bounded(0, 0) => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    case NumericInterval.Bounded(0, h) => j.joinWithFailure(v1 / NumericInterval(fromInt(0), h))(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case NumericInterval.Bounded(l, 0) => j.joinWithFailure(v1 / NumericInterval(l, fromInt(-1)))(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case NumericInterval.Bounded(l, h) =>
      if (l <= fromInt(0) && h >= fromInt(0))
        j.joinWithFailure(v1 / v2)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      else
        v1 / v2

  def max(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def min(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  def divUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def remainder(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def remainderUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def modulo(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def gcd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???

  def absolute(v: NumericInterval[I]): NumericInterval[I] = ???
  def bitAnd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def bitOr(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def bitXor(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = ???
  def shiftLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???
  def shiftRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???
  def shiftRightUnsigned(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???
  def rotateLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???
  def rotateRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = ???
  def countLeadingZeros(v: NumericInterval[I]): NumericInterval[I] = ???
  def countTrailinZeros(v: NumericInterval[I]): NumericInterval[I] = ???
  def nonzeroBitCount(v: NumericInterval[I]): NumericInterval[I] = ???



given NumericIntervalAbstractly[I](using Ordering[I]): Abstractly[I, NumericInterval[I]] with
  override def abstractly(i: I): NumericInterval[I] =
    NumericInterval(i, i)

given NumericIntervalOrdering[I](using Ordering[I]): PartialOrder[NumericInterval[I]] with
  override def lteq(x: NumericInterval[I], y: NumericInterval[I]): Boolean = (x, y) match
    case (NumericInterval.Top(), _) => false
    case (_, NumericInterval.Top()) => true
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      l2 <= l1 && h1 <= h2

given NumericIntervalJoin[I](using Ordering[I]): Join[NumericInterval[I]] with
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] =
    MaybeChanged(v1.join(v2), v1)

class NumericIntervalWiden[I](bounds: => Set[I], minValue: I, maxValue: I)(using Numeric[I]) extends Widen[NumericInterval[I]]:
  private lazy val treeSet: TreeSet[I] = TreeSet.from(bounds)
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] = (v1, v2) match
    case (NumericInterval.Top(), _) => MaybeChanged.Unchanged(v1)
    case (_, NumericInterval.Top()) => MaybeChanged.Changed(v2)
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      val low =
        if (l1 <= l2) l1
        else treeSet.maxBefore(l2 + summon[Numeric[I]].fromInt(1)).getOrElse(minValue)
      val high =
        if (h1 >= h2) h1
        else treeSet.minAfter(h2).getOrElse(maxValue)
  //    println(s"$v1 widen $v2 = ${NumericInterval(low, high)}")
      MaybeChanged(NumericInterval(low, high), v1)

given NumericIntervalOrderingOps[I](using Ordering[I]): OrderingOps[NumericInterval[I], Topped[Boolean]] with
  def lt(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (h1 < l2) Topped.Actual(true)
      else if (h2 <= l1) Topped.Actual(false)
      else Topped.Top
  def le(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (h1 <= l2) Topped.Actual(true)
      else if (h2 < l1) Topped.Actual(false)
      else Topped.Top

given NumericIntervalUnsignedOrderingOps[I](using ops: UnsignedOrderingOps[I, Boolean]): UnsignedOrderingOps[NumericInterval[I], Topped[Boolean]] with
  def ltUnsigned(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (ops.ltUnsigned(h1, l2)) Topped.Actual(true)
      else if (ops.leUnsigned(h2, l1)) Topped.Actual(false)
      else Topped.Top
  def leUnsigned(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (ops.leUnsigned(h1, l2)) Topped.Actual(true)
      else if (ops.ltUnsigned(h2, l1)) Topped.Actual(false)
      else Topped.Top

given NumericIntervalEqOps[I](using Ordering[I]): EqOps[NumericInterval[I], Topped[Boolean]] with
  override def equ(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (l1 == h1 && h1 == l2 && l2 == h2) Topped.Actual(true)
      else if (h1 < l2 || h2 < l1) Topped.Actual(false)
      else Topped.Top
  override def neq(iv1: NumericInterval[I], iv2: NumericInterval[I]): Topped[Boolean] = (iv1, iv2) match
    case (NumericInterval.Top(), _) => Topped.Top
    case (_, NumericInterval.Top()) => Topped.Top
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      if (l1 == h1 && h1 == l2 && l2 == h2) Topped.Actual(false)
      else if (h1 < l2 || h2 < l1) Topped.Actual(true)
      else Topped.Top

given ConvertNumericIntervals[From, To, I1, I2, Config <: ConvertConfig[_]]
  (using convert: Convert[From, To, I1, I2, Config])(using Failure, EffectStack)
  : Convert[From, To, NumericInterval[I1], NumericInterval[I2], Config] with

  def apply(i: NumericInterval[I1], conf: Config): NumericInterval[I2] = i match
    case NumericInterval.Top() => safeConversion(conf, NumericInterval.Top())
    case NumericInterval.Bounded(l, h) => NumericInterval.Bounded(convert(l, conf), convert(h, conf))

given ConvertNumericIntervalToConstant[From, To, I]: Convert[From, To, NumericInterval[I], Topped[I], NilCC.type] with
  def apply(i: NumericInterval[I], conf: NilCC.type): Topped[I] = i match
    case NumericInterval.Top() => Topped.Top
    case NumericInterval.Bounded(l, h) => if (l == h) Topped.Actual(l) else Topped.Top

given ConvertConstantToNumericInterval[From, To, I]: Convert[From, To, Topped[I], NumericInterval[I], NilCC.type] with
  def apply(i: Topped[I], conf: NilCC.type): NumericInterval[I] = i match
    case Topped.Top => NumericInterval.Top()
    case Topped.Actual(l) => NumericInterval.Bounded(l, l)

given ConvertNumericIntervalToBytes[From, To, I, B, Config <: ConvertConfig[_]]
  (using convert: Convert[From, To, I, Seq[B], config.BytesSize && Config])
  (using Failure, EffectStack, Ordering[B])
  : Convert[From, To, NumericInterval[I], Seq[NumericInterval[B]], config.BytesSize && Config] with
  def apply(i: NumericInterval[I], conf: config.BytesSize && Config): Seq[NumericInterval[B]] = i match
    case NumericInterval.Top() =>
      val bytes = Seq.fill(conf._1.bytes)(NumericInterval.Top[B]())
      safeConversion(conf, bytes)
    case NumericInterval.Bounded(l, h) =>
      val lowBytes = convert(l, conf)
      val highBytes = convert(h, conf)
      lowBytes.zip(highBytes).map(NumericInterval.fromBounds)

given ConvertBytesToNumericInterval[From, To, I, B]
  (using convert: Convert[From, To, Seq[B], I, config.BytesSize && SomeCC[ByteOrder] && config.Bits])
  (using Failure, EffectStack, Ordering[I])
  : Convert[From, To, Seq[NumericInterval[B]], NumericInterval[I], config.BytesSize && SomeCC[ByteOrder] && config.Bits] with
  def apply(bytes: Seq[NumericInterval[B]], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): NumericInterval[I] =
    val boundedBytes: Seq[NumericInterval.Bounded[B]] = bytes.map {
      case NumericInterval.Top() => return NumericInterval.Top()
      case b@NumericInterval.Bounded(_, _) => b
    }
    val (msb, bigEndianRest) =
      if (conf.c1.c2.t == ByteOrder.BIG_ENDIAN) {
        (boundedBytes.head, boundedBytes.tail)
      } else {
        val rev = boundedBytes.reverse
        (rev.head, rev.tail)
      }
    val l1 = Convert(msb.low +: bigEndianRest.map(_.low), conf)
    val h1 = Convert(msb.high +: bigEndianRest.map(_.high), conf)
    val iv1 = NumericInterval.fromBounds(l1, h1)

    val l2 = Convert(msb.low +: bigEndianRest.map(_.high), conf)
    val h2 = Convert(msb.high +: bigEndianRest.map(_.low), conf)
    val iv2 = NumericInterval.fromBounds(l2, h2)

    iv1.join(iv2)



