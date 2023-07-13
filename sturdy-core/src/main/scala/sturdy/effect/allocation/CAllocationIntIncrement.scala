package sturdy.effect.allocation

import sturdy.effect.Concrete

import scala.collection.mutable

class CAllocationIntIncrement[Context] extends Allocation[(Context,Int), Context], Concrete:
  private val next: mutable.Map[Context, Int] = mutable.Map()

  override def apply(ctx: Context): (Context,Int) =
    next.get(ctx) match
      case Some(n) =>
        next += ctx -> (n+1)
        (ctx,n)
      case None =>
        next += ctx -> 2
        (ctx,1)
