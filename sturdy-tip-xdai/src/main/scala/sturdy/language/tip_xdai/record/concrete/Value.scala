package sturdy.language.tip_xdai.record.concrete

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.Value
import sturdy.values.{Finite, Structural, Topped}
import sturdy.language.tip_xdai.record.Field
import sturdy.language.tip_xdai.core.concrete.{ ConcreteEqOps as CoreConcreteEqOps, given }
import sturdy.values.ordering.{EqOps, StructuralEqOps}
import sturdy.values.StructuralMap
import sturdy.language.tip_xdai.record.given_Structural_Field

case class RecordValue(value: Map[Field, Value]) extends Value:
  override def toString: String = value.toString

given Structural[RecordValue] with {}


trait ConcreteEqOps extends CoreConcreteEqOps:
  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (RecordValue(r1), RecordValue(r2)) => boolToInt(EqOps.equ(r1, r2))
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (RecordValue(r1), RecordValue(r2)) => boolToInt(EqOps.neq(r1, r2))
    case _ => super.neq(v1, v2)