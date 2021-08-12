package sturdy.values.ints

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.values.Abstractly
import sturdy.values.Topped
import sturdy.values.Topped.*
import sturdy.values.JoinValue
import sturdy.values.PartialOrder
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


import IntSign.*

given Abstractly[Int, IntSign] with
  override def abstractly(i: Int): IntSign =
    if i < 0 then Neg
    else if i > 0 then Pos
    else Zero

given PartialOrder[IntSign] with
  override def lteq(x: IntSign, y: IntSign): Boolean = x == y || x < y

given IntSignJoin: JoinValue[IntSign] with
  override def joinValues(v1: IntSign, v2: IntSign): IntSign =
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

given SignIntOps(using f: Failure, j: JoinComputation): IntOps[IntSign] with
  def intLit(i: Int): IntSign =
    if i < 0 then Neg
    else if i > 0 then Pos
    else Zero

  def randomInt(): IntSign = ZeroOrPos

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

  def div(v1: IntSign, v2: IntSign): IntSign = v2 match
    case Zero => f.fail(IntDivisionByZero, s"$v1 / $v2")
    case ZeroOrPos => j.joinComputations(v1)(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case NegOrZero => j.joinComputations(v1.negated)(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case TopSign => j.joinComputations(TopSign)(f.fail(IntDivisionByZero, s"$v1 / $v2"))
    case _ => mul(v1, v2)

  def abs(i: IntSign): IntSign = ???
  def ceiling(i: IntSign): IntSign = ???
  def floor(i: IntSign): IntSign = ???
  def gcd(v1: IntSign, v2: IntSign): IntSign = ???
  def lcm(v1: IntSign, v2: IntSign): IntSign = ???
  def max(v1: IntSign, v2: IntSign): IntSign = ???
  def min(v1: IntSign, v2: IntSign): IntSign = ???
  def modulo(v1: IntSign, v2: IntSign): IntSign = ???
  def quotient(v1: IntSign, v2: IntSign): IntSign = ???
  def remainder(v1: IntSign, v2: IntSign): IntSign = ???

given SignCompareOps: CompareOps[IntSign, Topped[Boolean]] with
  def equiv(v1: IntSign, v2: IntSign): Topped[Boolean] = ???
  def lt(v1: IntSign, v2: IntSign): Topped[Boolean] = (v1, v2) match
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
  def le(v1: IntSign, v2: IntSign): Topped[Boolean] = (v1, v2) match
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
  def ge(v1: IntSign, v2: IntSign): Topped[Boolean] = lt(v2, v1)
  def gt(v1: IntSign, v2: IntSign): Topped[Boolean] = le(v2, v1)
  def isEven(v1: IntSign): Topped[Boolean] = ???
  def isNegative(v1: IntSign): Topped[Boolean] = ???
  def isOdd(v1: IntSign): Topped[Boolean] = ???
  def isPositive(v1: IntSign): Topped[Boolean] = ???
  def isZero(v1: IntSign): Topped[Boolean] = ???

given SignEqOps: EqOps[IntSign, Topped[Boolean]] with
  def equ(v1: IntSign, v2: IntSign): Topped[Boolean] = (v1, v2) match
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
  def neq(v1: IntSign, v2: IntSign): Topped[Boolean] = equ(v1, v2).map(!_)

