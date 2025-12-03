package sturdy.language.tip_xdai.arithmetic.concrete

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.Value
import sturdy.language.tip_xdai.core.concrete.{ FunValue, ConcreteEqOps as CoreConcreteEqOps }
import sturdy.values.ordering.{ EqOps, StructuralEqOps }
import sturdy.values.{Structural, Topped}

case class IntValue(value: Int) extends Value:
  override def toString: String = value.toString

given Structural[IntValue] with {}


trait ConcreteEqOps extends CoreConcreteEqOps:
  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (i1: IntValue, i2: IntValue) => boolToInt(EqOps.equ(i1, i2))
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (i1: IntValue, i2: IntValue) => boolToInt(EqOps.neq(i1, i2))
    case _ => super.neq(v1, v2)