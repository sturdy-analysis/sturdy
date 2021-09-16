package sturdy.values.doubles

import sturdy.effect.Effectful
import sturdy.values.*
import sturdy.values.JoinValue
import sturdy.values.relational.*

enum DoubleSign:
  case TopSign
  case Neg
  case NegOrZero
  case Zero
  case ZeroOrPos
  case Pos

  def <(s2: DoubleSign): Boolean = s2 == TopSign || (this match
    case Neg => s2 == NegOrZero
    case Zero => s2 == NegOrZero || s2 == ZeroOrPos
    case Pos => s2 == ZeroOrPos
    case _ => false
  )
  def negated: DoubleSign = this match
    case TopSign => TopSign
    case Neg => Pos
    case NegOrZero => ZeroOrPos
    case Zero => Zero
    case ZeroOrPos => NegOrZero
    case Pos => Neg


import DoubleSign.*

given Abstractly[Double, DoubleSign] with
  override def abstractly(d: Double): DoubleSign =
    if d < 0 then Neg
    else if d > 0 then Pos
    else Zero

given PartialOrder[DoubleSign] with
  override def lteq(x: DoubleSign, y: DoubleSign): Boolean = x == y || x < y

given DoubleSignJoin: JoinValue[DoubleSign] with
  override def joinValues(v1: DoubleSign, v2: DoubleSign): DoubleSign =
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


given SignDoubleOps: DoubleOps[DoubleSign] with
  def doubleLit(d: Double): DoubleSign =
    if d < 0 then Neg
    else if d > 0 then Pos
    else Zero

  def randomDouble(): DoubleSign = ZeroOrPos

  def add(v1: DoubleSign, v2: DoubleSign): DoubleSign = (v1, v2) match
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

  def sub(v1: DoubleSign, v2: DoubleSign): DoubleSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    add(v1, negV2)

  def mul(v1: DoubleSign, v2: DoubleSign): DoubleSign = (v1, v2) match
    case (TopSign, _) => TopSign
    case (_, TopSign) => TopSign
    case (_, Zero) => Zero
    case (Zero, _) => Zero

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

  def div(v1: DoubleSign, v2: DoubleSign): DoubleSign = v2 match
    case Zero => v1 // division by zero yields infinity with the sign of v1
    case ZeroOrPos => v1
    case NegOrZero => v1.negated
    case _ => mul(v1, v2)

  def min(v1: DoubleSign, v2: DoubleSign): DoubleSign = ???
  def max(v1: DoubleSign, v2: DoubleSign): DoubleSign = ???

  def absolute(v: DoubleSign): DoubleSign = ???
  def negated(v: DoubleSign): DoubleSign = ???
  def sqrt(v: DoubleSign): DoubleSign = ???
  def ceil(v: DoubleSign): DoubleSign = ???
  def floor(v: DoubleSign): DoubleSign = ???
  def truncate(v: DoubleSign): DoubleSign = ???
  def nearest(v: DoubleSign): DoubleSign = ???
  def copysign(v: DoubleSign, sign: DoubleSign): DoubleSign = ???

  def logNatural(v: DoubleSign): DoubleSign = ???

given SignCompareOps: CompareOps[DoubleSign, Topped[Boolean]] with
  def lt(v1: DoubleSign, v2: DoubleSign): Topped[Boolean] = (v1, v2) match
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
  def le(v1: DoubleSign, v2: DoubleSign): Topped[Boolean] = (v1, v2) match
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
  def ge(v1: DoubleSign, v2: DoubleSign): Topped[Boolean] = lt(v2, v1)
  def gt(v1: DoubleSign, v2: DoubleSign): Topped[Boolean] = le(v2, v1)

given SignEqOps: EqOps[DoubleSign, Topped[Boolean]] with
  def equ(v1: DoubleSign, v2: DoubleSign): Topped[Boolean] = (v1, v2) match
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
  def neq(v1: DoubleSign, v2: DoubleSign): Topped[Boolean] = equ(v1, v2).map(!_)

