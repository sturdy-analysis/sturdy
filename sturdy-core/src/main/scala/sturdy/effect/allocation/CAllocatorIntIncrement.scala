package sturdy.effect.allocation

import sturdy.effect.Concrete

import scala.collection.mutable

class CAllocatorIntIncrement[Context] extends Allocator[(Context,Int), Context], Concrete:
  private val next: mutable.Map[Context, Int] = mutable.Map()

  override def alloc(ctx: Context): (Context,Int) =
    next.get(ctx) match
      case Some(n) =>
        next += ctx -> (n+1)
        (ctx,n)
      case None =>
        next += ctx -> 2
        (ctx,1)
