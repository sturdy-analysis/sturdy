package sturdy.values.references

import sturdy.effect.failure.Failure
import sturdy.effect.store.ManageableAddr
import sturdy.util.Label
import sturdy.values.Structural

enum AllocationSiteRef:
  case Null
  case Addr(a: AllocationSiteAddr)

  override def toString: String = this match
    case Null => "null"
    case Addr(a) => a.toString

sealed trait AllocationSiteAddr extends ManageableAddr:
  override def toString: String = this match
    case AllocationSiteAddr.Alloc(l) => s"alloc-$l"
    case AllocationSiteAddr.Variable(name) => s"&$name"
  def unmanaged: AllocationSiteAddr = this match
    case a: AllocationSiteAddr.Alloc => a
    case AllocationSiteAddr.Variable(name) => AllocationSiteAddr.Variable(name)(false)

object AllocationSiteAddr:
  case class Alloc(lab: Label)(managed: Boolean) extends AllocationSiteAddr with ManageableAddr(managed)
  case class Variable(name: String)(managed: Boolean) extends AllocationSiteAddr with ManageableAddr(managed)



given AllocationSiteReferenceOps(using f: Failure): ReferenceOps[AllocationSiteAddr, AllocationSiteRef] with
  override def nullValue: AllocationSiteRef = AllocationSiteRef.Null
  override def refValue(addr: AllocationSiteAddr): AllocationSiteRef = AllocationSiteRef.Addr(addr.unmanaged)
  override def refAddr(r: AllocationSiteRef): AllocationSiteAddr = r match
    case AllocationSiteRef.Null => f.fail(NullDereference, "")
    case AllocationSiteRef.Addr(a) => a

given Structural[AllocationSiteRef] with {}
given Structural[AllocationSiteAddr] with {}
