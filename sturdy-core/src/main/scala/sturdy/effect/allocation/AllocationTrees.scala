package sturdy.effect.allocation

import sturdy.effect.Stateless
import sturdy.values.Indent
import sturdy.values.{TreeBuffer, Tree}

case class AllocationTree[Context](ctx: Context) extends Tree:
  override def prettyPrint(using Indent): String = s"alloc $ctx"

class AllocationTrees[-Context](using buf: TreeBuffer) extends Allocation[Tree, Context], Stateless:
  override def apply(ctx: Context): Tree = buf += AllocationTree(ctx)
