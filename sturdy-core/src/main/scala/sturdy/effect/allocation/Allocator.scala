package sturdy.effect.allocation

import sturdy.effect.Effect

trait Allocator[Addr, -Context] extends Effect:
  def alloc(ctx: Context): Addr

  final def apply(ctx: Context): Addr = alloc(ctx)
