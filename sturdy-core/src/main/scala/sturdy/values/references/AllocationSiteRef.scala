package sturdy.values.references

import sturdy.effect.failure.Failure
import sturdy.effect.store.ManageableAddr
import sturdy.util.Label
import sturdy.values.Finite
import sturdy.values.Structural
import sturdy.values.relational.EqOps
import sturdy.values.Topped

enum AllocationSiteRef:
  case Null
  case Addr(a: AllocationSiteAddr)

  override def toString: String = this match
    case Null => "null"
    case Addr(a) => a.toString

sealed trait AllocationSiteAddr extends ManageableAddr:
  override def toString: String = this match
    case AllocationSiteAddr.Alloc(l) => s"alloc-$l"
    case AllocationSiteAddr.AllocRelative(l, name) => s"alloc-$l-$name"
    case AllocationSiteAddr.Variable(name) => s"&$name"
  def unmanaged: AllocationSiteAddr = this match
    case AllocationSiteAddr.Alloc(l) => AllocationSiteAddr.Alloc(l)(false)
    case AllocationSiteAddr.AllocRelative(l, name) => AllocationSiteAddr.AllocRelative(l, name)(false)
    case AllocationSiteAddr.Variable(name) => AllocationSiteAddr.Variable(name)(false)

object AllocationSiteAddr:
  case class Alloc(lab: Label)(managed: Boolean) extends AllocationSiteAddr with ManageableAddr(managed)
  case class AllocRelative(lab: Label, name: String)(managed: Boolean) extends AllocationSiteAddr with ManageableAddr(managed)
  case class Variable(name: String)(managed: Boolean) extends AllocationSiteAddr with ManageableAddr(managed)

given Finite[AllocationSiteRef] with {}
given Finite[AllocationSiteAddr] with {}

given AllocationSiteReferenceOps(using f: Failure): ReferenceOps[AllocationSiteAddr, AllocationSiteRef] with
  override def mkNullRef: AllocationSiteRef = AllocationSiteRef.Null
  override def mkManagedRef(trg: AllocationSiteAddr): AllocationSiteRef = AllocationSiteRef.Addr(trg)
  override def mkRef(trg: AllocationSiteAddr): AllocationSiteRef = AllocationSiteRef.Addr(trg.unmanaged)
  override def deref(r: AllocationSiteRef): AllocationSiteAddr = r match
    case AllocationSiteRef.Null => f.fail(NullDereference, "")
    case AllocationSiteRef.Addr(a) => a
  
given EqOps[AllocationSiteRef, Topped[Boolean]] with
  override def equ(v1: AllocationSiteRef, v2: AllocationSiteRef): Topped[Boolean] = (v1, v2) match
    case (AllocationSiteRef.Null, AllocationSiteRef.Null) => Topped.Actual(true)
    case (AllocationSiteRef.Addr(a1), AllocationSiteRef.Addr(a2)) =>
      if (a1 == a2)
        Topped.Top
      else
        Topped.Actual(false)
    case _ => Topped.Actual(false)
  override def neq(v1: AllocationSiteRef, v2: AllocationSiteRef): Topped[Boolean] = (v1, v2) match
    case (AllocationSiteRef.Null, AllocationSiteRef.Null) => Topped.Actual(false)
    case (AllocationSiteRef.Addr(a1), AllocationSiteRef.Addr(a2)) =>
      if (a1 == a2)
        Topped.Top
      else
        Topped.Actual(true)
    case _ => Topped.Actual(true)
