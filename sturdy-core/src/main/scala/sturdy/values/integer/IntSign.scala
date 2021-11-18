package sturdy.values.integer

import sturdy.effect.Effectful
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.relational.*

enum IntSign:
  case TopSign
  case Neg
  case NegOrZero
  case Zero
  case ZeroOrPos
  case Pos

  def <(s2: IntSign): Boolean = s2 == TopSign || (this match
    case Neg => s2 == NegOrZero
    case Zero => s2 == NegOrZero || s2 == ZeroOrPos
    case Pos => s2 == ZeroOrPos
    case _ => false
  )
  def negated: IntSign = this match
    case TopSign => TopSign
    case Neg => Pos
    case NegOrZero => ZeroOrPos
    case Zero => Zero
    case ZeroOrPos => NegOrZero
    case Pos => Neg


import sturdy.values.integer.IntSign.*

given Abstractly[Int, IntSign] with
  override def abstractly(i: Int): IntSign =
    if i < 0 then Neg
    else if i > 0 then Pos
    else Zero

given PartialOrder[IntSign] with
  override def lteq(x: IntSign, y: IntSign): Boolean = x == y || x < y

given CombineIntSign[W <: Widening]: Combine[IntSign, W] with
  override def apply(v1: IntSign, v2: IntSign): MaybeChanged[IntSign] =
    if v1 == v2 then Unchanged(v1)
    else if v1 < v2 then Changed(v2)
    else if v2 < v1 then Unchanged(v1)
    else (v1, v2) match
      case (Neg, Zero) => Changed(NegOrZero)
      case (Zero, Neg) => Changed(NegOrZero)
      case (Zero, Pos) => Changed(ZeroOrPos)
      case (Pos, Zero) => Changed(ZeroOrPos)
      case _ => Changed(TopSign)

given SignIntegerOps[B](using f: Failure, j: Effectful, base: Integral[B]): IntegerOps[B, IntSign] with
  def integerLit(i: B): IntSign =
    if base.lt(i, base.zero) then Neg
    else if base.gt(i, base.zero) then Pos
    else Zero

  def randomInteger(): IntSign = ZeroOrPos

  def add(v1: IntSign, v2: IntSign): IntSign = (v1, v2) match
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

  def max(v1: IntSign, v2: IntSign): IntSign = ???
  def min(v1: IntSign, v2: IntSign): IntSign = ???

  def div(v1: IntSign, v2: IntSign): IntSign = v2 match
    case Zero => f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    case ZeroOrPos => j.joinComputations(v1)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case NegOrZero => j.joinComputations(v1.negated)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
    case TopSign => j.joinComputations(TopSign)(f.fail(IntegerDivisionByZero, s"$v1 / $v2"))
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
  def countTrailinZeros(v: IntSign): IntSign = ???
  def nonzeroBitCount(v: IntSign): IntSign = ???

given SignOrderingOps: OrderingOps[IntSign, Topped[Boolean]] with
  def lt(v1: IntSign, v2: IntSign): Topped[Boolean] = (v1, v2) match
    case (Neg, Zero) => Topped.Actual(true)
    case (Neg, ZeroOrPos) => Topped.Actual(true)
    case (Neg, Pos) => Topped.Actual(true)
    case (NegOrZero, Pos) => Topped.Actual(true)
    case (Zero, Neg) => Topped.Actual(false)
    case (Zero, Zero) => Topped.Actual(false)
    case (Zero, Pos) => Topped.Actual(true)
    case (ZeroOrPos, Neg) => Topped.Actual(false)
    case (Pos, Neg) => Topped.Actual(false)
    case (Pos, NegOrZero) => Topped.Actual(false)
    case (Pos, Zero) => Topped.Actual(false)
    case _ => Topped.Top
  def le(v1: IntSign, v2: IntSign): Topped[Boolean] = (v1, v2) match
    case (Neg, Zero) => Topped.Actual(true)
    case (Neg, ZeroOrPos) => Topped.Actual(true)
    case (Neg, Pos) => Topped.Actual(true)
    case (NegOrZero, ZeroOrPos) => Topped.Actual(true)
    case (NegOrZero, Pos) => Topped.Actual(true)
    case (Zero, Neg) => Topped.Actual(false)
    case (Zero, Zero) => Topped.Actual(true)
    case (Zero, Pos) => Topped.Actual(true)
    case (Pos, Neg) => Topped.Actual(false)
    case (Pos, NegOrZero) => Topped.Actual(false)
    case (Pos, Zero) => Topped.Actual(false)
    case _ => Topped.Top
  def ge(v1: IntSign, v2: IntSign): Topped[Boolean] = le(v2, v1)
  def gt(v1: IntSign, v2: IntSign): Topped[Boolean] = lt(v2, v1)

given SignEqOps: EqOps[IntSign, Topped[Boolean]] with
  def equ(v1: IntSign, v2: IntSign): Topped[Boolean] = (v1, v2) match
    case (Neg, Zero) => Topped.Actual(false)
    case (Neg, ZeroOrPos) => Topped.Actual(false)
    case (Neg, Pos) => Topped.Actual(false)
    case (NegOrZero, Pos) => Topped.Actual(false)
    case (Zero, Neg) => Topped.Actual(false)
    case (Zero, Zero) => Topped.Actual(true)
    case (Zero, Pos) => Topped.Actual(false)
    case (ZeroOrPos, Neg) => Topped.Actual(false)
    case (Pos, Neg) => Topped.Actual(false)
    case (Pos, NegOrZero) => Topped.Actual(false)
    case (Pos, Zero) => Topped.Actual(false)
    case _ => Topped.Top
  def neq(v1: IntSign, v2: IntSign): Topped[Boolean] = equ(v1, v2).map(!_)

given FiniteIntSign: Finite[IntSign] with {}