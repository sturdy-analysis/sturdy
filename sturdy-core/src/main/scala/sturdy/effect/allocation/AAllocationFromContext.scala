package sturdy.effect.allocation

import sturdy.effect.Stateless
import sturdy.values.Abstractly

import scala.collection.immutable.IntMap

class AAllocationFromContext[Context, Addr](addr: Context => Addr) extends Allocation[Addr, Context], Stateless:
  override def apply(ctx: Context): Addr = addr(ctx)

class AllocationContextAbstractly[Addr, Context](c: CAllocationIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
