package sturdy.values.references

import sturdy.effect.failure.Failure
import sturdy.util.Label
import sturdy.values.Structural

enum AllocationSiteRef:
  case Null
  case Addr(a: AllocationSiteAddr)

  override def toString: String = this match
    case Null => "null"
    case Addr(a) => a.toString

enum AllocationSiteAddr:
  case Alloc(lab: Label)
  case Variable(name: String)

  override def toString: String = this match
    case Alloc(l) => s"alloc-$l"
    case Variable(name) => s"&$name"


given AllocationSiteReferenceOps(using f: Failure): ReferenceOps[AllocationSiteAddr, AllocationSiteRef] with
  override def nullValue: AllocationSiteRef = AllocationSiteRef.Null
  override def refValue(addr: AllocationSiteAddr): AllocationSiteRef = AllocationSiteRef.Addr(addr)
  override def refAddr(r: AllocationSiteRef): AllocationSiteAddr = r match
    case AllocationSiteRef.Null => f.fail(NullDereference, "")
    case AllocationSiteRef.Addr(a) => a

given Structural[AllocationSiteRef] with {}
given Structural[AllocationSiteAddr] with {}
