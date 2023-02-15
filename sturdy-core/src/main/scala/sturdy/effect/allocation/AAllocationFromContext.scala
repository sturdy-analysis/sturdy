package sturdy.effect.allocation

import sturdy.effect.Stateless
import sturdy.values.Abstractly

import scala.collection.immutable.IntMap

class AAllocationFromContext[Site, Addr](addr: Site => Addr) extends Allocation[Addr, Site], Stateless:
  override def apply(site: Site): Addr = addr(site)

class AllocationContextAbstractly[Addr, Site](c: CAllocationIntIncrement[Site], addr: Site => Addr) extends Abstractly[Int, Addr]:
  private val addressContexts: IntMap[Site] = IntMap.from(c.getAddressContexts)
  override def apply(caddr: Int): Addr = addr(addressContexts(caddr))
