package sturdy.effect.allocation

import sturdy.effect.Stateless
import sturdy.values.Abstractly

import scala.collection.immutable.IntMap

class AAllocatorFromContext[Context, Addr](addr: Context => Addr) extends Allocator[Addr, Context], Stateless:
  override def alloc(ctx: Context): Addr = addr(ctx)

class AllocationContextAbstractly[Addr, Context](c: CAllocatorIntIncrement[Context], addr: Context => Addr) extends Abstractly[(Context,Int), Addr]:
  override def apply(caddr: (Context,Int)): Addr = addr(caddr._1)
