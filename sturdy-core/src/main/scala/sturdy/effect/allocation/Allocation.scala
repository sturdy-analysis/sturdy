package sturdy.effect.allocation

trait Allocation[Addr, -Context]:
  def alloc(ctx: Context): Addr
