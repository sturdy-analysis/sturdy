package sturdy.language.bytecode.abstractions

import sturdy.effect.store.ManageableAddr
import sturdy.values.{Finite, Join}
import sturdy.values.references.{AbstractAddr, AllocationSiteAddr, PowersetAddr}

// type to represent addresses.
// addresses are somewhat typed, but this is currently not enforced through the type system,
// this may change in the future
// variants try to make use of sturdy_core and/or contain what they replace
enum Addr extends ManageableAddr(true) with AbstractAddr[Addr]{
  case Array(allocationSiteAddr: AllocationSiteAddr)
  case ArrayElement(allocationSiteAddr: AllocationSiteAddr)
  case Object(allocationSiteAddr: AllocationSiteAddr)
  case Field(allocationSiteAddr: AllocationSiteAddr)
  case Static(allocationSiteAddr: AllocationSiteAddr)

  /* uncomment if needed
  def addr: AllocationSiteAddr = this match {
    case Addr.Array(allocationSiteAddr) => allocationSiteAddr
    case Addr.ArrayElement(allocationSiteAddr) => allocationSiteAddr
    case Addr.Object(allocationSiteAddr) => allocationSiteAddr
    case Addr.Field(allocationSiteAddr) => allocationSiteAddr
    case Addr.Static(allocationSiteAddr) => allocationSiteAddr
  }
  */
  
  // implementation without much thought put into it currently
  override def isEmpty: Boolean = false
  override def isStrong: Boolean = false
  override def reduce[A](f: Addr => A)(using Join[A]): A = f(this)
  override def iterator: Iterator[Addr] = Iterator(this)
}

given Finite[Addr] with {}

type AddrSet = PowersetAddr[Addr, Addr]

// TODO:
// - site types
// - change enum variant fields, copy the information currently stored in the respective types
// - use addr and addrset
