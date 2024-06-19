package sturdy.language.tip.analysis

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.integer.IntSign.{Neg, NegOrZero, Pos, TopSign, Zero, ZeroOrPos}
import sturdy.values.integer.{IntSign, IntegerDivisionByZero, IntegerOps}
import sturdy.values.integer.SignIntegerOps
import sturdy.values.PartialOrder
import sturdy.values.integer.{given PartialOrder[IntSign]}


object Foo{

  trait Showable[A]:
    extension (a: A) def show: String

  given X: Showable[IntSign] with
    extension (a: IntSign) def show: String = a match
      case Neg => "Neg"
      case NegOrZero => "NegOrZero"
      case Zero => "Zero"
      case ZeroOrPos => "ZeroOrPos"
      case Pos => "Pos"
      case TopSign => "TopSign"

  def showAll[A: Showable](as: List[A]): Unit =
    as.foreach(a => println(a.show))


  showAll(List(Neg, Zero))

}
trait GradualOps[T]:
  def insertCheck(uv: T, sv: T)(using po: PartialOrder[T]): T =
    println(s"uv=${uv} | sv=${sv}")
    if(po.lt(uv, sv))
      println(s"insert a check to test if value is more precise than ${uv}")
    else if(!po.lteq(sv, uv))
      throw new Exception(s"unsafe value ${uv} is not related to safe value ${sv}")
    uv

  def withCheck(sv: T)(uv: => T)(using po: PartialOrder[T]) =
    insertCheck(uv,sv)


given GradualOps[IntSign] with {}

class GradualSignIntegerOps[B](unsafe: IntegerOps[B, IntSign], safe: IntegerOps[B, IntSign])(using f: Failure, j: EffectStack, base: Integral[B], g: GradualOps[IntSign]) extends SignIntegerOps[B]:
  override def add(v1: IntSign, v2: IntSign): IntSign =
    g.insertCheck(unsafe.add(v1, v2), safe.add(v1,v2))


class UnsafeSignIntegerOps[B](using f: Failure, j: EffectStack, base: Integral[B], g: GradualOps[IntSign]) extends SafeSignIntegerOps[Int]:
  override def add(v1: IntSign, v2: IntSign): IntSign =
    g.withCheck(super.add(v1,v2)){
      (v1, v2) match
        case (TopSign, _) => TopSign
        case (_, TopSign) => TopSign
        case (_, Zero) => v1
        case (Zero, _) => v2

        case (Neg, Neg) => Neg
        case (Neg, NegOrZero) => Neg
        case (Neg, ZeroOrPos) => TopSign
        case (Neg, Pos) => TopSign

        case (NegOrZero, Neg) => Neg
        case (NegOrZero, NegOrZero) => NegOrZero
        case (NegOrZero, ZeroOrPos) => TopSign
        case (NegOrZero, Pos) => TopSign

        case (ZeroOrPos, Neg) => TopSign
        case (ZeroOrPos, NegOrZero) => TopSign
        case (ZeroOrPos, ZeroOrPos) => ZeroOrPos
        case (ZeroOrPos, Pos) => Pos

        case (Pos, Neg) => TopSign
        case (Pos, NegOrZero) => TopSign
        case (Pos, ZeroOrPos) => Pos
        case (Pos, Pos) => Pos
    }

  override def sub(v1: IntSign, v2: IntSign): IntSign =
    // It is always safe to negate an integer (cannot produce overflow).
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    this.add(v1, negV2) // Possible check might be added by the unsafe addition.

  override def mul(v1: IntSign, v2: IntSign): IntSign =
    g.withCheck(super.mul(v1, v2)){
      (v1, v2) match
        case (_, Zero) => Zero
        case (Zero, _) => Zero
        case (TopSign, _) => TopSign
        case (_, TopSign) => TopSign

        case (Neg, Neg) => Pos
        case (Neg, NegOrZero) => ZeroOrPos
        case (Neg, ZeroOrPos) => NegOrZero
        case (Neg, Pos) => Neg

        case (NegOrZero, Neg) => ZeroOrPos
        case (NegOrZero, NegOrZero) => ZeroOrPos
        case (NegOrZero, ZeroOrPos) => NegOrZero
        case (NegOrZero, Pos) => NegOrZero

        case (ZeroOrPos, Neg) => NegOrZero
        case (ZeroOrPos, NegOrZero) => NegOrZero
        case (ZeroOrPos, ZeroOrPos) => ZeroOrPos
        case (ZeroOrPos, Pos) => ZeroOrPos

        case (Pos, Neg) => Neg
        case (Pos, NegOrZero) => NegOrZero
        case (Pos, ZeroOrPos) => ZeroOrPos
        case (Pos, Pos) => Pos
    }

  override def div(v1: IntSign, v2: IntSign): IntSign =
    v2 match
      case Zero => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
      case ZeroOrPos => j.joinWithFailure(v1)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case NegOrZero => j.joinWithFailure(v1.negated)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case TopSign => j.joinWithFailure(TopSign)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
      case _ => this.mul(v1, v2)


class SafeSignIntegerOps[B](using f: Failure, j: EffectStack, base: Integral[B]) extends IntegerOps[B, IntSign]:
  def integerLit(i: B): IntSign =
    if base.lt(i, base.zero) then Neg
    else if base.gt(i, base.zero) then Pos
    else Zero

  def randomInteger(): IntSign = ZeroOrPos

  def add(v1: IntSign, v2: IntSign): IntSign = (v1, v2) match
    case (_, Zero) => v1
    case (Zero, _) => v2
    case (_, _) => TopSign

  def sub(v1: IntSign, v2: IntSign): IntSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    add(v1, negV2)

  def mul(v1: IntSign, v2: IntSign): IntSign = (v1, v2) match
    case (_, Zero) => Zero
    case (Zero, _) => Zero
    case (_, _) => TopSign

  def max(v1: IntSign, v2: IntSign): IntSign = ???
  def min(v1: IntSign, v2: IntSign): IntSign = ???

  def div(v1: IntSign, v2: IntSign): IntSign = v2 match
    case Zero => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    case ZeroOrPos => j.joinWithFailure(v1)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case NegOrZero => j.joinWithFailure(v1.negated)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case TopSign => j.joinWithFailure(TopSign)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case _ => mul(v1, v2)

  def divUnsigned(v1: IntSign, v2: IntSign): IntSign = ???
  def remainder(v1: IntSign, v2: IntSign): IntSign = ???
  def remainderUnsigned(v1: IntSign, v2: IntSign): IntSign = ???
  def modulo(v1: IntSign, v2: IntSign): IntSign = ???
  def gcd(v1: IntSign, v2: IntSign): IntSign = ???

  def absolute(v: IntSign): IntSign = ???
  def bitAnd(v1: IntSign, v2: IntSign): IntSign = ???
  def bitOr(v1: IntSign, v2: IntSign): IntSign = ???
  def bitXor(v1: IntSign, v2: IntSign): IntSign = ???
  def shiftLeft(v: IntSign, shift: IntSign): IntSign = ???
  def shiftRight(v: IntSign, shift: IntSign): IntSign = ???
  def shiftRightUnsigned(v: IntSign, shift: IntSign): IntSign = ???
  def rotateLeft(v: IntSign, shift: IntSign): IntSign = ???
  def rotateRight(v: IntSign, shift: IntSign): IntSign = ???
  def countLeadingZeros(v: IntSign): IntSign = ???
  def countTrailingZeros(v: IntSign): IntSign = ???
  def nonzeroBitCount(v: IntSign): IntSign = ???
  def invertBits(v: IntSign): IntSign = ???



