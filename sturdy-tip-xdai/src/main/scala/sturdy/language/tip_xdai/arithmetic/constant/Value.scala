package sturdy.language.tip_xdai.arithmetic.constant

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.{CoreEqOps, CoreJoinV, Value}
import sturdy.values.{Structural, Topped}

case class ConstantIntV(value: Int) extends Value:
  override def toString: String = value.toString

given Structural[ConstantIntV] with {}

trait ConstantEqOps extends CoreEqOps:
  override def equ(v1: Value, v2: Value): Topped[Boolean] = (v1, v2) match
    case (ConstantIntV(i1), ConstantIntV(i2)) => Topped.Actual(i1 == i2)
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Topped[Boolean] = (v1, v2) match
    case (ConstantIntV(i1), ConstantIntV(i2)) => Topped.Actual(i1 != i2)
    case _ => super.neq(v1, v2)

trait ConstantJoinV extends CoreJoinV:
  override def combine(lhs: Value, rhs: Value): Value = (lhs, rhs) match
    case (ConstantIntV(i1), ConstantIntV(i2)) if i1 == i2 => lhs
    case _ => super.combine(lhs, rhs)


