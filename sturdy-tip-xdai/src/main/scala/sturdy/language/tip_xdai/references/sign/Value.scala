package sturdy.language.tip_xdai.references.sign

import sturdy.effect.EffectStack
import sturdy.effect.except.Except
import sturdy.effect.failure.Failure
import sturdy.language.tip_xdai.core.Value
import sturdy.language.tip_xdai.core.sign.{SignEqOps as CoreSignEqOps, SignJoin as CoreSignJoin}
import sturdy.language.tip_xdai.core.abstractions.TopValue
import sturdy.language.tip_xdai.record.Field
import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.integer.{IntSign, IntegerDivisionByZero, IntegerOps}
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.*
import sturdy.values.references.{AbstractReference, AllocationSiteAddr, PowersetAddr, Reference, abstractAddrEqOps, abstractReferenceEqOps, combineAbstractReference}
import sturdy.values.references.joinPowersetAddr

type AbstractSignAddr = PowersetAddr[AllocationSiteAddr, AllocationSiteAddr]

case class RefValue(ref: AbstractReference[AbstractSignAddr]) extends Value:
  override def toString: String = ref.toString

trait SignEqOps extends CoreSignEqOps:
  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (RefValue(r1), RefValue(r2)) => boolToValue(abstractReferenceEqOps.equ(r1, r2))
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (RefValue(r1), RefValue(r2)) => boolToValue(abstractReferenceEqOps.neq(r1, r2))
    case _ => super.neq(v1, v2)

trait SignJoin extends CoreSignJoin:
  override def combine(lhs: Value, rhs: Value): MaybeChanged[Value] = (lhs, rhs) match
    case (RefValue(r1), RefValue(r2)) => combineAbstractReference.apply(r1, r2).map(RefValue.apply)
    case _ => super.combine(lhs, rhs)

