package sturdy.language.tip_xdai.references.concrete

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.{AllocationSite, CoreEqOps, CoreJoinV, Value}
import sturdy.values.{Finite, Structural, Topped}
import sturdy.values.references.Reference

type ConcreteAddr = (AllocationSite, Int)

case class RefV(ref: Reference[ConcreteAddr]) extends Value:
  override def toString: String = ref.toString

given Structural[RefV] with {}


