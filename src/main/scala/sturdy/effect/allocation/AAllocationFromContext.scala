package sturdy.effect.allocation

trait AAllocationFromContext[Addr, Context](addr: Context => Addr) extends Allocation[Addr, Context] {
  override def alloc(ctx: Context): Addr = addr(ctx)
}
