package sturdy.language.tip_xdai.core.concrete

import sturdy.language.tip_xdai.core.{CoreEqOps, Function, StructuralFunction, Value}
import sturdy.values.Structural
import sturdy.values.ordering.EqOps
import sturdy.values.ordering.StructuralEqOps

case class FunValue(f: Function) extends Value:
  override def toString: String = f.toString

given Structural[FunValue] with {}
given Structural[Value] with {}

trait ConcreteEqOps extends CoreEqOps[Boolean, Value]:
  override def boolToValue(b: Boolean): Value

  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (FunValue(f1), FunValue(f2)) => boolToValue(EqOps.equ(f1, f2))

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (FunValue(f1), FunValue(f2)) => boolToValue(EqOps.neq(f1, f2))