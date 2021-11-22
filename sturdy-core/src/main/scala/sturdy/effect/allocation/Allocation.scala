package sturdy.effect.allocation

import sturdy.effect.Effectful

trait Allocation[Addr, -Context] extends Effectful:
  def apply(ctx: Context): Addr
