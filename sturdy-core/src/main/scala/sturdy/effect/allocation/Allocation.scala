package sturdy.effect.allocation

import sturdy.effect.Effect

/** Allocates addresses for a given context.
 * Contexts can for example be the allocation site of the variable. */
trait Allocation[Addr, -Context] extends Effect:
  def apply(ctx: Context): Addr
