package sturdy.values.longs

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.values.Abstractly
import sturdy.values.Topped
import sturdy.values.Topped.*
import sturdy.values.JoinValue
import sturdy.values.PartialOrder
import sturdy.values.relational.*

enum LongSign:
  case TopSign
  case Neg
  case NegOrZero
  case Zero
  case ZeroOrPos
  case Pos

  def <(s2: LongSign): Boolean = s2 == TopSign || (this match
    case Neg => s2 == NegOrZero
    case Zero => s2 == NegOrZero || s2 == ZeroOrPos
    case Pos => s2 == ZeroOrPos
    case _ => false
  )
  def negated: LongSign = this match
    case TopSign => TopSign
    case Neg => Pos
    case NegOrZero => ZeroOrPos
    case Zero => Zero
    case ZeroOrPos => NegOrZero
    case Pos => Neg


import LongSign.*

given Abstractly[Long, LongSign] with
  override def abstractly(i: Long): LongSign =
    if i < 0 then Neg
    else if i > 0 then Pos
    else Zero

given PartialOrder[LongSign] with
  override def lteq(x: LongSign, y: LongSign): Boolean = x == y || x < y

given LongSignJoin: JoinValue[LongSign] with
  override def joinValues(v1: LongSign, v2: LongSign): LongSign =
    if v1 == v2 then v1
    else if v1 < v2 then v2
    else if v2 < v1 then v1
    else (v1, v2) match
      case (TopSign, _) => TopSign
      case (_, TopSign) => TopSign
      case (Neg, Zero) => NegOrZero
      case (Zero, Neg) => NegOrZero
      case (Zero, Pos) => ZeroOrPos
      case (Pos, Zero) => ZeroOrPos
      case _ => TopSign

given SignLongOps(using f: Failure, j: JoinComputation): LongOps[LongSign] with
  def longLit(l: Long): LongSign =
    if l < 0 then Neg
    else if l > 0 then Pos
    else Zero

  def randomLong(): LongSign = ZeroOrPos

  def add(v1: LongSign, v2: LongSign): LongSign = (v1, v2) match
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

  def sub(v1: LongSign, v2: LongSign): LongSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    add(v1, negV2)

  def mul(v1: LongSign, v2: LongSign): LongSign = (v1, v2) match
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

  def div(v1: LongSign, v2: LongSign): LongSign = v2 match
    case Zero => f.fail(LongDivisionByZero, s"$v1 / $v2")
    case ZeroOrPos => j.joinComputations(v1)(f.fail(LongDivisionByZero, s"$v1 / $v2"))
    case NegOrZero => j.joinComputations(v1.negated)(f.fail(LongDivisionByZero, s"$v1 / $v2"))
    case TopSign => j.joinComputations(TopSign)(f.fail(LongDivisionByZero, s"$v1 / $v2"))
    case _ => mul(v1, v2)

  def divUnsigned(v1: LongSign, v2: LongSign): LongSign = ???
  def remainder(v1: LongSign, v2: LongSign): LongSign = ???
  def remainderUnsigned(v1: LongSign, v2: LongSign): LongSign = ???

  def bitAnd(v1: LongSign, v2: LongSign): LongSign = ???
  def bitOr(v1: LongSign, v2: LongSign): LongSign = ???
  def bitXor(v1: LongSign, v2: LongSign): LongSign = ???
  def shiftLeft(v: LongSign, shift: LongSign): LongSign = ???
  def shiftRight(v: LongSign, shift: LongSign): LongSign = ???
  def shiftRightUnsigned(v: LongSign, shift: LongSign): LongSign = ???
  def rotateLeft(v: LongSign, shift: LongSign): LongSign = ???
  def rotateRight(v: LongSign, shift: LongSign): LongSign = ???
  def countLeadingZeros(v: LongSign): LongSign = ???
  def countTrailinZeros(v: LongSign): LongSign = ???
  def nonzeroBitCount(v: LongSign): LongSign = ???

given SignCompareOps: CompareOps[LongSign, Topped[Boolean]] with
  def lt(v1: LongSign, v2: LongSign): Topped[Boolean] = (v1, v2) match
    case (Neg, Zero) => Actual(true)
    case (Neg, ZeroOrPos) => Actual(true)
    case (Neg, Pos) => Actual(true)
    case (NegOrZero, Pos) => Actual(true)
    case (Zero, Neg) => Actual(false)
    case (Zero, Zero) => Actual(false)
    case (Zero, Pos) => Actual(true)
    case (ZeroOrPos, Neg) => Actual(false)
    case (Pos, Neg) => Actual(false)
    case (Pos, NegOrZero) => Actual(false)
    case (Pos, Zero) => Actual(false)
    case _ => Top
  def le(v1: LongSign, v2: LongSign): Topped[Boolean] = (v1, v2) match
    case (Neg, Zero) => Actual(true)
    case (Neg, ZeroOrPos) => Actual(true)
    case (Neg, Pos) => Actual(true)
    case (NegOrZero, ZeroOrPos) => Actual(true)
    case (NegOrZero, Pos) => Actual(true)
    case (Zero, Neg) => Actual(false)
    case (Zero, Zero) => Actual(true)
    case (Zero, Pos) => Actual(true)
    case (Pos, Neg) => Actual(false)
    case (Pos, NegOrZero) => Actual(false)
    case (Pos, Zero) => Actual(false)
    case _ => Top
  def ge(v1: LongSign, v2: LongSign): Topped[Boolean] = le(v2, v1)
  def gt(v1: LongSign, v2: LongSign): Topped[Boolean] = lt(v2, v1)

given SignEqOps: EqOps[LongSign, Topped[Boolean]] with
  def equ(v1: LongSign, v2: LongSign): Topped[Boolean] = (v1, v2) match
    case (Neg, Zero) => Actual(false)
    case (Neg, ZeroOrPos) => Actual(false)
    case (Neg, Pos) => Actual(false)
    case (NegOrZero, Pos) => Actual(false)
    case (Zero, Neg) => Actual(false)
    case (Zero, Zero) => Actual(true)
    case (Zero, Pos) => Actual(false)
    case (ZeroOrPos, Neg) => Actual(false)
    case (Pos, Neg) => Actual(false)
    case (Pos, NegOrZero) => Actual(false)
    case (Pos, Zero) => Actual(false)
    case _ => Top
  def neq(v1: LongSign, v2: LongSign): Topped[Boolean] = equ(v1, v2).map(!_)

