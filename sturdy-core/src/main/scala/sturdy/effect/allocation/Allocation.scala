package sturdy.effect.allocation

import sturdy.effect.Effect

trait Allocation[Addr, -Context] extends Effect:
  def apply(ctx: Context): Addr
