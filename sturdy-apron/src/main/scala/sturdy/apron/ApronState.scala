package sturdy.apron

import apron.{Interval, Var}
import sturdy.effect.{EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{ApronStore, RecencyStore}
import sturdy.values.Join
import sturdy.values.references.{PhysicalAddress, PowVirtualAddress, PowersetAddr, VirtualAddress, given}

trait ApronState[Addr,Type]:
  def withTempVars[A](types: Type*)(f: PartialFunction[List[Addr],A]): A
  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def addConstraint(constraint: ApronCons[Addr,Type]): Unit
  def getBound(expr: ApronExpr[Addr,Type]): Interval
  def join[A: Join](f: => A)(g: => A): A
  def ifThenElse[A: Join](condition: ApronCons[Addr, Type])(f: => A)(g: => A): A

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

  override def addConstraint(constraint: ApronCons[VirtualAddress[Ctx], Type]): Unit =
    apronStore.addConstraint(virtToPhys(constraint))

  override def getBound(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
    apronStore.getBound(virtToPhys(expr))

  class BottomFailure extends SturdyFailure

  override def join[A: Join](f: => A)(g: => A): A =
    effectStack.joinComputations {
      val result = f
      if (apronStore.isBottom) throw BottomFailure()
      result
    } {
      val result = g
      if (apronStore.isBottom) throw BottomFailure()
      result
    }

  override def ifThenElse[A: Join](condition: ApronCons[VirtualAddress[Ctx], Type])(f: => A)(g: => A): A =
    join {
      addConstraint(condition)
      f
    } {
      addConstraint(condition.negated)
      g
    }


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

  protected  def virtToPhys(constrVirtAddr: ApronCons[VirtualAddress[Ctx], Type]): ApronCons[PhysicalAddress[Ctx],Type] =
    val virtRecentOlds = PowVirtualAddress(constrVirtAddr.addrs.filter(virt => virt.physical.addrs.size == 2))
    recencyStore.joinRecentIntoOld(virtRecentOlds)
    constrVirtAddr.mapAddr(
      virt =>
        val physicals = virt.physical
        if (physicals.addrs.size == 1)
          physicals.iterator.next()
        else
          throw IllegalStateException(s"${virt} did not map to a single physical address, but to ${physicals}")
    )