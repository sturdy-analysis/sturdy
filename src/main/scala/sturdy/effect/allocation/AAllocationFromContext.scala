package sturdy.effect.allocation

trait AAllocationFromContext[Context, Addr](addr: Context => Addr) extends Allocation[Addr, Context] {
  override def alloc(ctx: Context): Addr = addr(ctx)
}
