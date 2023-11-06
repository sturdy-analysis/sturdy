package sturdy.language.tip.abstractions

import sturdy.data.WithJoin
import sturdy.language.tip.Interpreter
import sturdy.effect.store.{*, given}
import sturdy.values.{Powerset, Widen, given}
import sturdy.values.references.{*, given}
import sturdy.language.tip.AllocationSite

import reflect.Selectable.reflectiveSelectable

object References:
  def allocationSiteAddr(asite: AllocationSite): AllocationSiteAddr = asite match
    case AllocationSite.Alloc(e) => AllocationSiteAddr.Alloc(e.label)
    case AllocationSite.ParamBinding(fun, p) => AllocationSiteAddr.Variable(s"${fun.name}:$p")
    case AllocationSite.LocalBinding(fun, v) => AllocationSiteAddr.Variable(s"${fun.name}:$v")
    case AllocationSite.Record(r) => AllocationSiteAddr.Alloc(r.label)


  trait AllocationSites extends Interpreter:
    final type Addr = PowersetAddr[AllocationSiteAddr, AllocationSiteAddr]
    final type VRef = AbstractReference[Addr]
    final type Environment = Map[String, Value]
    final type InitStore = Map[AllocationSiteAddr, Value]

    given Widen[VRef] = combineAbstractReference

    def fromAllocationSite(asite: AllocationSite) = Powerset(asite match
      case AllocationSite.Alloc(e) => AllocationSiteAddr.Alloc(e.label)
      case AllocationSite.ParamBinding(fun, p) => AllocationSiteAddr.Variable(s"${fun.name}:$p")
      case AllocationSite.LocalBinding(fun, v) => AllocationSiteAddr.Variable(s"${fun.name}:$v")
      case AllocationSite.Record(r) => AllocationSiteAddr.Alloc(r.label)
    )

    final def topReference(using self: Instance): VRef =
      val addrs = self.store.getState.asInstanceOf[Map[AllocationSiteAddr, _]].keySet
      val aa = PowersetAddr(addrs)
      AbstractReference.NullAddr(aa, false)

  trait AllocationSitesRecency extends Interpreter:
    final type Addr = PowVirtualAddress[AllocationSiteAddr]
    final type VRef = AbstractReference[Addr]
    final type Environment = Map[String, Value]
    final type InitStore = Map[AllocationSiteAddr, Value]

    given Widen[VRef] = combineAbstractReference

    final def topReference(using self: Instance): VRef =
      val store = self.store.asInstanceOf[RecencyStore[AllocationSiteAddr, PowVirtualAddress[AllocationSiteAddr], Value]]
      val knownVirtuals = store.virtualAddresses
      AbstractReference.NullAddr(knownVirtuals, false)
