package sturdy.values.references

import sturdy.util.Label

enum AllocationSiteAddr:
  case Null
  case Alloc(lab: Label)
  case Variable(name: String)

  override def toString: String = this match
    case Null => "null"
    case Alloc(l) => s"alloc-$l"
    case Variable(name) => s"#$name"


given AllocationSiteReferenceOps: ReferenceOps[AllocationSiteAddr, AllocationSiteAddr] with
  override def nullValue: AllocationSiteAddr = AllocationSiteAddr.Null
  override def refAddr(v: AllocationSiteAddr): AllocationSiteAddr = v
  override def refValue(addr: AllocationSiteAddr): AllocationSiteAddr = addr

