package sturdy.language.tip.abstractions

import sturdy.data.WithJoin
import sturdy.language.tip.Interpreter
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.values.Powerset
import sturdy.values.references.AllocationSiteRef
import sturdy.values.references.AllocationSiteAddr
import sturdy.language.tip.AllocationSite

import reflect.Selectable.reflectiveSelectable

object References:
  trait AllocationSites extends Interpreter:
    final type Addr = Powerset[AllocationSiteAddr]
    final type VRef = Powerset[AllocationSiteRef]
    final type Environment = Map[String, Value]
    final type Store = Map[AllocationSiteAddr, Value]

    def fromAllocationSite(asite: AllocationSite): Addr = Powerset(asite match
      case AllocationSite.Alloc(e) => AllocationSiteAddr.Alloc(e.label)(true)
      case AllocationSite.ParamBinding(fun, p) => AllocationSiteAddr.Variable(s"${fun.name}:$p")(true)
      case AllocationSite.LocalBinding(fun, v) => AllocationSiteAddr.Variable(s"${fun.name}:$v")(true)
      case AllocationSite.Record(r) => AllocationSiteAddr.Alloc(r.label)(true)
    )

    final def topReference(using self: Instance): Powerset[AllocationSiteRef] =
      val addrs = self.store.getState.asInstanceOf[Map[AllocationSiteAddr, _]].keySet
      Powerset(addrs.map(AllocationSiteRef.Addr.apply) + AllocationSiteRef.Null)
