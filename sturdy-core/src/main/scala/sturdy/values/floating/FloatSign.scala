package sturdy.values.floating

import sturdy.effect.Effectful
import sturdy.values.*
import sturdy.values.relational.*

enum FloatSign:
  case TopSign
  case Neg
  case NegOrZero
  case Zero
  case ZeroOrPos
  case Pos

  def <(s2: FloatSign): Boolean = s2 == TopSign || (this match
    case Neg => s2 == NegOrZero
    case Zero => s2 == NegOrZero || s2 == ZeroOrPos
    case Pos => s2 == ZeroOrPos
    case _ => false
  )
  def negated: FloatSign = this match
    case TopSign => TopSign
    case Neg => Pos
    case NegOrZero => ZeroOrPos
    case Zero => Zero
    case ZeroOrPos => NegOrZero
    case Pos => Neg


import FloatSign.*

given Abstractly[Float, FloatSign] with
  override def abstractly(d: Float): FloatSign =
    if d < 0 then Neg
    else if d > 0 then Pos
    else Zero

given PartialOrder[FloatSign] with
  override def lteq(x: FloatSign, y: FloatSign): Boolean = x == y || x < y

given CombineFloatSign[W <: Widening]: Combine[FloatSign, W] with
  override def apply(v1: FloatSign, v2: FloatSign): MaybeChanged[FloatSign] =
    if v1 == v2 then Unchanged(v1)
    else if v1 < v2 then Changed(v2)
    else if v2 < v1 then Changed(v1)
    else (v1, v2) match
      case (TopSign, _) => Unchanged(TopSign)
      case (_, TopSign) => Changed(TopSign)
      case (Neg, Zero) => Changed(NegOrZero)
      case (Zero, Neg) => Changed(NegOrZero)
      case (Zero, Pos) => Changed(ZeroOrPos)
      case (Pos, Zero) => Changed(ZeroOrPos)
      case _ => Changed(TopSign)


given SignFloatingOps[B] (using base: Fractional[B]): FloatingOps[B, FloatSign] with
  def floatingLit(f: B): FloatSign =
    if base.lt(f, base.zero) then Neg
    else if base.gt(f, base.zero) then Pos
    else Zero

  def randomFloat(): FloatSign = ZeroOrPos

  def add(v1: FloatSign, v2: FloatSign): FloatSign = (v1, v2) match
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

  def sub(v1: FloatSign, v2: FloatSign): FloatSign =
    val negV2 = v2 match
      case TopSign => TopSign
      case Neg => Pos
      case Zero => Zero
      case NegOrZero => ZeroOrPos
      case ZeroOrPos => NegOrZero
      case Pos => Neg
    add(v1, negV2)

  def mul(v1: FloatSign, v2: FloatSign): FloatSign = (v1, v2) match
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

  def div(v1: FloatSign, v2: FloatSign): FloatSign = v2 match
    case Zero => v1 // division by zero yields infinity with the sign of v1
    case ZeroOrPos => v1
    case NegOrZero => v1.negated
    case _ => mul(v1, v2)

  def min(v1: FloatSign, v2: FloatSign): FloatSign = ???
  def max(v1: FloatSign, v2: FloatSign): FloatSign = ???

  def absolute(v: FloatSign): FloatSign = ???
  def negated(v: FloatSign): FloatSign = ???
  def sqrt(v: FloatSign): FloatSign = ???
  def ceil(v: FloatSign): FloatSign = ???
  def floor(v: FloatSign): FloatSign = ???
  def truncate(v: FloatSign): FloatSign = ???
  def nearest(v: FloatSign): FloatSign = ???
  def copysign(v: FloatSign, sign: FloatSign): FloatSign = ???

given SignCompareOps: CompareOps[FloatSign, Topped[Boolean]] with
  def lt(v1: FloatSign, v2: FloatSign): Topped[Boolean] = (v1, v2) match
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
  def le(v1: FloatSign, v2: FloatSign): Topped[Boolean] = (v1, v2) match
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
  def ge(v1: FloatSign, v2: FloatSign): Topped[Boolean] = lt(v2, v1)
  def gt(v1: FloatSign, v2: FloatSign): Topped[Boolean] = le(v2, v1)

given SignEqOps: EqOps[FloatSign, Topped[Boolean]] with
  def equ(v1: FloatSign, v2: FloatSign): Topped[Boolean] = (v1, v2) match
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
  def neq(v1: FloatSign, v2: FloatSign): Topped[Boolean] = equ(v1, v2).map(!_)

