package sturdy.language.tip_xdai.references.concrete

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.concrete.{ ConcreteEqOps as CoreConcreteEqOps, given }
import sturdy.language.tip_xdai.core.{AllocationSite, Value}
import sturdy.language.tip_xdai.record.concrete.RecordValue
import sturdy.values.ordering.EqOps
import sturdy.values.{Finite, Structural, Topped}
import sturdy.values.references.Reference
import sturdy.values.ordering.StructuralEqOps
import sturdy.values.references.structuralReference

type ConcreteAddr = (AllocationSite, Int)
given Structural[ConcreteAddr] with {}

case class RefValue(ref: Reference[ConcreteAddr]) extends Value:
  override def toString: String = ref.toString

given Structural[RefValue] with {}


trait ConcreteEqOps extends CoreConcreteEqOps:
  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (RefValue(r1), RefValue(r2)) => boolToInt(EqOps.equ(r1, r2))
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (RefValue(r1), RefValue(r2)) => boolToInt(EqOps.neq(r1, r2))
    case _ => super.neq(v1, v2)
