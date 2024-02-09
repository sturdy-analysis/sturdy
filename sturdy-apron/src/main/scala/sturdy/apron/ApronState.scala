package sturdy.apron

import apron.Var
import sturdy.effect.EffectStack
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{ApronStore, RecencyStore}
import sturdy.values.Join
import sturdy.values.references.{PhysicalAddress, PowVirtualAddress, PowersetAddr, VirtualAddress, given}

trait ApronState[Addr,Type]:
  def withTempVars[A](types: Type*)(f: PartialFunction[List[Addr],A]): A
  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def ifThenElse[A: Join](condition: ApronCons[Addr,Type])(f: => A)(g: => A): A

trait ApronRecencyState
  [
    Ctx: Ordering,
    Type: ApronType : Join
  ]
  (
    temporaryVariableAllocator: Allocator[Ctx, Type],
    recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], ApronExpr[PhysicalAddress[Ctx],Type]],
    apronStore: ApronStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], ApronExpr[PhysicalAddress[Ctx],Type]]
  ) extends ApronState[VirtualAddress[Ctx], Type]:

  val effectStack = EffectStack(List(recencyStore))

  override def withTempVars[A](types: Type*)(f: PartialFunction[List[VirtualAddress[Ctx]], A]): A =
    val tempVars = types.map { tpe =>
      val ctx = temporaryVariableAllocator(tpe)
      recencyStore.alloc(ctx)
    }.toList
    f(tempVars)

  override def assign(v: VirtualAddress[Ctx], expr: ApronExpr[VirtualAddress[Ctx], Type]): Unit =
    apronStore.write(v.physical, virtToPhys(expr))

  override def ifThenElse[A: Join](condition: ApronCons[VirtualAddress[Ctx], Type])(f: => A)(g: => A): A =
    effectStack.joinComputations(f)(g)

  protected def virtToPhys(exprVirtAddr: ApronExpr[VirtualAddress[Ctx],Type]): ApronExpr[PhysicalAddress[Ctx],Type] =
    // To convert ApronExprVirtAddr to ApronExprPhysAddr, we need to combine virtual addresses
    // that map to two physical addresses {(ctx,recent),(ctx,old)}.
    // Specifically, we join (ctx,recent) into (ctx,old), such that the virtual address
    // can be mapped to {(ctx,old)}.
    val virtRecentOlds = PowVirtualAddress(exprVirtAddr.addrs.filter(virt => virt.physical.addrs.size == 2))
    recencyStore.joinRecentIntoOld(virtRecentOlds)
    exprVirtAddr.mapAddr(
      virt =>
        val physicals = virt.physical
        if (physicals.addrs.size == 1)
          physicals.iterator.next()
        else
          throw IllegalStateException(s"${virt} did not map to a single physical address, but to ${physicals}")
    )