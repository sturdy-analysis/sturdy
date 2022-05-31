package sturdy.values.integer

import sturdy.data.{NoJoin, noJoin, joinComputations, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.convert.*
import sturdy.values.relational.*

import java.nio.ByteOrder
import scala.collection.immutable.TreeSet
import Ordering.Implicits.infixOrderingOps
import Numeric.Implicits.infixNumericOps
import Integral.Implicits.infixIntegralOps

object NumericInterval:
  def top[I]: NumericInterval[I] = NumericInterval.Top()
  def apply[I](l: I, h: I)(using ord: Ordering[I]): NumericInterval[I] =
    if (ord.lteq(l, h))
      NumericInterval.Bounded(l, h)
    else
      NumericInterval.Top()
  def fromBounds[I](b1: I, b2: I)(using ord: Ordering[I]): NumericInterval[I] =
    if (ord.lteq(b1, b2))
      NumericInterval.Bounded(b1, b2)
    else
      NumericInterval.Bounded(b2, b1)
  def constant[I](i: I): NumericInterval[I] = NumericInterval.Bounded(i, i)

  enum IsZero[I]:
    case Zero()
    case NotZero()
    case MaybeZeroTop()
    case MaybeZero(nonZeroLow: I, nonZeroHigh: I)

    def toBoolean: Topped[Boolean] = this match
      case IsZero.Zero() => Topped.Actual(false)
      case IsZero.NotZero() => Topped.Actual(true)
      case IsZero.MaybeZeroTop() | IsZero.MaybeZero(_, _) => Topped.Top

  private inline def fromInt[I](i: Int)(using Numeric[I]): I = summon[Numeric[I]].fromInt(i)

enum NumericInterval[I]:
  case Top()
  case Bounded(low: I, high: I)

  import NumericInterval.*

  inline def map[J](f: I => J)(using Ordering[J]): NumericInterval[J] = this match
    case Top() => Top()
    case Bounded(l, h) =>
      NumericInterval.fromBounds(f(l), f(h))

  inline def mapConstant[J](f: I => J)(using ord: Ordering[I]): NumericInterval[J] = this match
    case Top() => Top()
    case Bounded(l, h) =>
      if (ord.equiv(l, h))
        NumericInterval.constant(f(l))
      else
        NumericInterval.Top()

  inline def combine[J, K](other: NumericInterval[J], fl: (I, J) => K, fh: (I, J) => K)(using Ordering[K]): NumericInterval[K] = (this, other) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(l1, h1), Bounded(l2, h2)) =>
      NumericInterval.fromBounds(fl(l1, l2), fh(h1, h2))

  inline def combineCross[J, K](other: NumericInterval[J], f: (I, J) => K)(using Ordering[K]): NumericInterval[K] = (this, other) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(x1, x2), Bounded(y1, y2)) =>
      val x1y1 = f(x1, y1)
      val x1y2 = f(x1, y2)
      val x2y1 = f(x2, y1)
      val x2y2 = f(x2, y2)
      NumericInterval.fromBounds(x1y1.min(x1y2).min(x2y1).min(x2y2), x1y1.max(x1y2).max(x2y1).max(x2y2))

  def isTop: Boolean = this match
    case Top() => true
    case _ => false

  def isZero(using num: Numeric[I], ord: Ordering[I]): IsZero[I] = this match
    case Top() => IsZero.MaybeZeroTop()
    case Bounded(l, h) =>
      val zero = num.fromInt(0)
      if (l == zero && h == zero)
        IsZero.Zero()
      else if (l == zero)
        IsZero.MaybeZero(num.fromInt(1), h)
      else if (h == zero)
        IsZero.MaybeZero(l, num.fromInt(-1))
      else
        IsZero.NotZero()

  def isConstant(using Ordering[I]): Boolean = this match
    case Top() => false
    case Bounded(l, h) => l == h


given IntervalIntegerOps[I](using ops: IntegerOps[I, I], strict: StrictIntegerOps[I, I, NoJoin], num: Numeric[I])(using f: Failure, j: EffectStack): IntegerOps[I, NumericInterval[I]] with
  import NumericInterval.*

  private val zero = num.fromInt(0)

  def integerLit(i: I): NumericInterval[I] = NumericInterval(i, i)
  def randomInteger(): NumericInterval[I] = NumericInterval.top

  def add(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    val addStrict = (i1: I, i2: I) => strict.addStrict(i1, i2).getOrElse(return NumericInterval.top)
    v1.combine(v2, addStrict, addStrict)
  def sub(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    val subStrict = (i1: I, i2: I) => strict.subStrict(i1, i2).getOrElse(return NumericInterval.top)
    v1.combine(v2, subStrict, subStrict)

  def mul(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    val mulStrict = (i1: I, i2: I) => strict.mulStrict(i1, i2).getOrElse(return NumericInterval.top)
    v1.combineCross(v2, mulStrict)

  def max(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = (v1, v2) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(l1, h1), Bounded(l2, h2)) => NumericInterval(ops.max(l1, l2), ops.max(h1, h2))

  def min(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = (v1, v2) match
    case (Top(), _) => Top()
    case (_, Top()) => Top()
    case (Bounded(l1, h1), Bounded(l2, h2)) => NumericInterval(ops.min(l1, l2), ops.min(h1, h2))

  def absolute(v: NumericInterval[I]): NumericInterval[I] = v match
    case Top() => v
    case Bounded(l, h) =>
      if (l < zero) {
        if (h < zero) {
          // neg, neg
          NumericInterval.Bounded(h.abs, l.abs)
        } else {
          // neg, pos
          NumericInterval.Bounded(zero, l.abs.max(h))
        }
      } else {
        // pos, pos
        v
      }


  private inline def divByZero(v1: NumericInterval[I], v2: NumericInterval[I]) = f.fail(IntegerDivisionByZero, s"$v1 / $v2")
  private inline def divWith(f: (I, I) => I, v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] =
    def divBy(nonzeroDenom: NumericInterval[I]): NumericInterval[I] = v1.combineCross(nonzeroDenom, f)
    v2.isZero match
      case IsZero.Zero() => divByZero(v1, v2)
      case IsZero.NotZero() => divBy(v2)
      case IsZero.MaybeZero(l, h) => joinComputations(divBy(NumericInterval.Bounded(l, h)))(divByZero(v1, v2))
      case IsZero.MaybeZeroTop() => joinComputations(divBy(v2))(divByZero(v1, v2))

  def div(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.div, v1, v2)
  def divUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.divUnsigned, v1, v2)
  def remainder(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.remainder, v1, v2)
  def remainderUnsigned(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.remainderUnsigned, v1, v2)
  def modulo(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.modulo, v1, v2)
  def gcd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = divWith(ops.gcd, v1, v2)

    def bitAnd(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v1.combineCross(v2, ops.bitAnd)

    def bitOr(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v1.combineCross(v2, ops.bitOr)

    def bitXor(v1: NumericInterval[I], v2: NumericInterval[I]): NumericInterval[I] = v1.combineCross(v2, ops.bitXor)

    def shiftLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = v.combineCross(shift, ops.shiftLeft)

    def shiftRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = v.combineCross(shift, ops.shiftRight)

    def shiftRightUnsigned(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = v.combineCross(shift, ops.shiftRightUnsigned)

    def rotateLeft(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = v.combineCross(shift, ops.rotateLeft)

    def rotateRight(v: NumericInterval[I], shift: NumericInterval[I]): NumericInterval[I] = v.combineCross(shift, ops.rotateRight)

    def countLeadingZeros(v: NumericInterval[I]): NumericInterval[I] = v.map(ops.countLeadingZeros)

    def countTrailingZeros(v: NumericInterval[I]): NumericInterval[I] = v.mapConstant(ops.countTrailingZeros)

    def nonzeroBitCount(v: NumericInterval[I]): NumericInterval[I] = v.mapConstant(ops.nonzeroBitCount)

given TopNumericInterval[I]: Top[NumericInterval[I]] with
  override def top: NumericInterval[I] = NumericInterval.top

given NumericIntervalAbstractly[I](using Ordering[I]): Abstractly[I, NumericInterval[I]] with
  override def apply(i: I): NumericInterval[I] =
    NumericInterval.Bounded(i, i)

given NumericIntervalOrdering[I](using Ordering[I]): PartialOrder[NumericInterval[I]] with
  override def lteq(x: NumericInterval[I], y: NumericInterval[I]): Boolean = (x, y) match
    case (NumericInterval.Top(), _) => false
    case (_, NumericInterval.Top()) => true
    case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
      l2 <= l1 && h1 <= h2

given NumericIntervalJoin[I](using Ordering[I]): Join[NumericInterval[I]] with
  import NumericInterval.*
  override def apply(v1: NumericInterval[I], v2: NumericInterval[I]): MaybeChanged[NumericInterval[I]] = (v1, v2) match
    case (Top(), _) => MaybeChanged.Unchanged(v1)
    case (_, Top()) => MaybeChanged.Changed(v2)
    case (Bounded(l1, h1), Bounded(l2, h2)) => MaybeChanged(Bounded(l1.min(l2), h1.max(h2)), v1)

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
      MaybeChanged(NumericInterval.Bounded(low, high), v1)

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
  def apply(i: NumericInterval[I], conf: config.BytesSize && Config): Seq[NumericInterval[B]] =
    encode(i, conf)

given ConvertBytesToNumericInterval[From, To, I, B]
  (using convert: Convert[From, To, Seq[B], I, config.BytesSize && SomeCC[ByteOrder] && config.Bits])
  (using Failure, EffectStack, Ordering[I])
  : Convert[From, To, Seq[NumericInterval[B]], NumericInterval[I], config.BytesSize && SomeCC[ByteOrder] && config.Bits] with
  def apply(bytes: Seq[NumericInterval[B]], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits): NumericInterval[I] =
    decode(bytes, conf)

def encode[From, To, I, B, Config <: ConvertConfig[_]](i: NumericInterval[I], conf: config.BytesSize && Config)(using convert: Convert[From, To, I, Seq[B], config.BytesSize && Config])
          (using Failure, EffectStack, Ordering[B]): Seq[NumericInterval[B]] = i match
  case NumericInterval.Top() =>
    val bytes = Seq.fill(conf._1.bytes)(NumericInterval.Top[B]())
    safeConversion(conf, bytes)
  case NumericInterval.Bounded(l, h) =>
    val lowBytes = convert(l, conf)
    val highBytes = convert(h, conf)
    val value = lowBytes.zip(highBytes)
    val bytes = value.map(NumericInterval.fromBounds)
    bytes

def decode[B, I, From, To](bytes: Seq[NumericInterval[B]], conf: config.BytesSize && SomeCC[ByteOrder] && config.Bits)(using convert: Convert[From, To, Seq[B], I, config.BytesSize && SomeCC[ByteOrder] && config.Bits])
                (using Failure, EffectStack, Ordering[I]): NumericInterval[I] =
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

  val bigEndianConf = conf.c1.c1 && SomeCC(ByteOrder.BIG_ENDIAN, false) && conf.c2

  val l1 = Convert(msb.low +: bigEndianRest.map(_.low), bigEndianConf)
  val h1 = Convert(msb.high +: bigEndianRest.map(_.high), bigEndianConf)
  val iv1 = NumericInterval.fromBounds(l1, h1)

  val l2 = Convert(msb.low +: bigEndianRest.map(_.high), bigEndianConf)
  val h2 = Convert(msb.high +: bigEndianRest.map(_.low), bigEndianConf)
  val iv2 = NumericInterval.fromBounds(l2, h2)

  Join(iv1, iv2).get

