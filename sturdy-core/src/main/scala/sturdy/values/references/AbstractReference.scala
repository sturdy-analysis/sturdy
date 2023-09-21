package sturdy.values.references

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.ordering.EqOps
import sturdy.values.{Combine, Finite, MaybeChanged, PartialOrder, Structural, Topped, Widen, Widening}

enum AbstractReference[+Addr]:
  case Null
  case Addr(a: Addr, definitelyManaged: Boolean)
  case NullAddr(a: Addr, definitelyManaged: Boolean)

  def isManaged: Boolean = this match
    case Null => true
    case Addr(_, m) => m
    case NullAddr(_, m) => m

given abstractReferenceOps[Addr] (using f: Failure, effects: EffectStack): ReferenceOps[Addr, AbstractReference[Addr]] with
  def nullValue: AbstractReference[Addr] = AbstractReference.Null
  def refValue(addr: Addr): AbstractReference[Addr] = AbstractReference.Addr(addr, true)
  def unmanagedRefValue(addr: Addr): AbstractReference[Addr] = AbstractReference.Addr(addr, false)
  def refAddr(v: AbstractReference[Addr]): Addr = v match
    case AbstractReference.Null => f.fail(NullDereference, s"Cannot dereference $v")
    case AbstractReference.Addr(a, _) => a
    case AbstractReference.NullAddr(a, _) => effects.joinWithFailure(a)(f.fail(NullDereference, s"Cannot dereference $v"))

given abstractReferenceStructural[A](using Structural[A]): Structural[AbstractReference[A]] with {}
given abstractReferenceFinite[A](using Finite[A]): Finite[AbstractReference[A]] with {}
given abstractReferencePO[A](using po: PartialOrder[A]): PartialOrder[AbstractReference[A]] with
  import AbstractReference.*
  private inline def managedLteq(m1: Boolean, m2: Boolean): Boolean = m1 || !m2
  override def lteq(x: AbstractReference[A], y: AbstractReference[A]): Boolean = (x, y) match
    case (Null, Null) => true
    case (Null, NullAddr(_, _)) => true
    case (Addr(a1, m1), Addr(a2, m2)) => managedLteq(m1, m2) && po.lteq(a1, a2)
    case (Addr(a1, m1), NullAddr(a2, m2)) => managedLteq(m1, m2) && po.lteq(a1, a2)
    case (NullAddr(a1, m1), NullAddr(a2, m2)) => managedLteq(m1, m2) && po.lteq(a1, a2)
    case _ => false

given combineAbstractReference[Addr, W <: Widening](using Combine[Addr, W]): Combine[AbstractReference[Addr], W] with
  import AbstractReference.*

  private def combine(a1: Addr, m1: Boolean, a2: Addr, m2: Boolean): MaybeChanged[(Addr, Boolean)] = {
    Combine(a1, a2) match
      case MaybeChanged.Changed(a) => MaybeChanged.Changed((a, m1 && m2))
      case MaybeChanged.Unchanged(a) =>
        val m = m1 && m2
        MaybeChanged((a, m), m1 != m)
  }
  override def apply(v1: AbstractReference[Addr], v2: AbstractReference[Addr]): MaybeChanged[AbstractReference[Addr]] =
    (v1, v2) match
      case (Null, Null) => MaybeChanged.Unchanged(Null)
      case (Null, Addr(a2, m2)) => MaybeChanged.Changed(NullAddr(a2, m2))
      case (Null, NullAddr(_, _)) => MaybeChanged.Changed(v2)
      case (Addr(a1,m1), Null) => MaybeChanged.Changed(NullAddr(a1, m1))
      case (Addr(a1,m1), Addr(a2, m2)) => combine(a1, m1, a2, m2).map(Addr.apply)
      case (Addr(a1,m1), NullAddr(a2,m2)) => MaybeChanged.Changed(NullAddr(Combine(a1,a2).get, m1 && m2))
      case (NullAddr(a1, m1), Null) => MaybeChanged.Unchanged(NullAddr(a1, m1))
      case (NullAddr(a1, m1), Addr(a2, m2)) => combine(a1, m1, a2, m2).map(NullAddr.apply)
      case (NullAddr(a1, m1), NullAddr(a2, m2)) => combine(a1, m1, a2, m2).map(NullAddr.apply)

given abstractReferenceEqOps[Addr](using eqA: EqOps[Addr, Topped[Boolean]]) : EqOps[AbstractReference[Addr], Topped[Boolean]] with
  override def equ(v1: AbstractReference[Addr], v2: AbstractReference[Addr]): Topped[Boolean] = (v1, v2) match
    case (AbstractReference.Null, AbstractReference.Null) => Topped.Actual(true)
    case (AbstractReference.Null, AbstractReference.Addr(_, _)) => Topped.Actual(false)
    case (AbstractReference.Null, AbstractReference.NullAddr(_, _)) => Topped.Top
    case (AbstractReference.Addr(_, _), AbstractReference.Null) => Topped.Actual(false)
    case (AbstractReference.Addr(a1,_), AbstractReference.Addr(a2,_)) => eqA.equ(a1, a2)
    case (AbstractReference.Addr(a1,_), AbstractReference.NullAddr(a2,_)) =>
      if (eqA.equ(a1, a2) == Topped.Actual(false))
        Topped.Actual(false)
      else
        Topped.Top
    case (AbstractReference.NullAddr(_, _), AbstractReference.Null) => Topped.Top
    case (AbstractReference.NullAddr(a1, _), AbstractReference.Addr(a2, _)) =>
      if (eqA.equ(a1, a2) == Topped.Actual(false))
        Topped.Actual(false)
      else
        Topped.Top
    case (AbstractReference.NullAddr(a1, _), AbstractReference.NullAddr(a2, _)) => Topped.Top

  override def neq(v1: AbstractReference[Addr], v2: AbstractReference[Addr]): Topped[Boolean] = equ(v1, v2).map(!_)
