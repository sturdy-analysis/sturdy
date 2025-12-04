package sturdy.language.tip_xdai.arithmetic.sign

import sturdy.effect.EffectStack
import sturdy.effect.except.Except
import sturdy.effect.failure.Failure
import sturdy.language.tip_xdai.core.{CoreEqOps, Value}
import sturdy.language.tip_xdai.core.abstractions.{BoolValue, TopValue}
import sturdy.language.tip_xdai.core.concrete.ConcreteEqOps as CoreConcreteEqOps
import sturdy.language.tip_xdai.core.sign.{SignEqOps as CoreSignEqOps, SignJoin as CoreSignJoin}
import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.integer.{CombineIntSign, IntSign, IntegerDivisionByZero, IntegerOps, given}
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.{Combine, Finite, MaybeChanged, Structural, Topped, Widening}
import sturdy.values.ordering.StructuralEqOps

case class SignIntValue(sign: IntSign) extends Value:
  def <(s2: SignIntValue): Boolean = sign < s2.sign

object SignIntValue:
  def apply(sign: IntSign): Value = sign match
    case IntSign.TopSign => TopValue
    case _ => new SignIntValue(sign)
  def Pos: SignIntValue = new SignIntValue(IntSign.Pos)
  def Neg: SignIntValue = new SignIntValue(IntSign.Neg)
  def Zero: SignIntValue = new SignIntValue(IntSign.Zero)
  def ZeroOrPos: SignIntValue = new SignIntValue(IntSign.ZeroOrPos)
  def NegOrZero: SignIntValue = new SignIntValue(IntSign.NegOrZero)

trait SignEqOps extends CoreSignEqOps:
  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (SignIntValue(i1), SignIntValue(i2)) => boolToValue(EqOps.equ(i1, i2))
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (SignIntValue(i1), SignIntValue(i2)) => boolToValue(EqOps.neq(i1, i2))
    case _ => super.neq(v1, v2)


trait SignJoin extends CoreSignJoin:
  override def combine(lhs: Value, rhs: Value): MaybeChanged[Value] = (lhs, rhs) match
    case (SignIntValue(i1), SignIntValue(i2)) => CombineIntSign.apply(i2, i2).map(SignIntValue.apply)
    case _ => super.combine(lhs, rhs)

