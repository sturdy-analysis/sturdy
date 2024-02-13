package sturdy.apron

import apron.{Interval, Var}
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.effect.{EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{ApronStore, RecencyStore}
import sturdy.values.Join
import sturdy.values.booleans.BooleanOps
import sturdy.values.references.{PhysicalAddress, PowVirtualAddress, PowersetAddr, VirtualAddress, given}
import sturdy.data.{*,given}

import scala.reflect.ClassTag

trait ApronState[Addr,Type]:
  def withTempVars[A](resultType: Type, exprs: ApronExpr[Addr,Type]*)
                     (f: PartialFunction[(Addr, List[ApronExpr[Addr,Type]]),A]): A
  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def addConstraint(constraint: ApronCons[Addr,Type]): Unit
  def getBound(expr: ApronExpr[Addr,Type]): Interval
  def join[A: Join](f: => A)(g: => A): A
  def ifThenElse[A: Join](condition: ApronCons[Addr, Type])(f: => A)(g: => A): A

object ApronState:
  def comparison[Addr: Ordering : ClassTag, Type]
    (op: (ApronExpr[Addr, Type], ApronExpr[Addr, Type]) => ApronCons[Addr, Type],
     v1: ApronExpr[Addr, Type],
     v2: ApronExpr[Addr, Type],
     resultType: Type)
    (using
     apronState: ApronState[Addr, Type],
     typeBooleanOps: BooleanOps[Type]
    ): ApronExpr[Addr, Type] =
      apronState.withTempVars(resultType, v1, v2) {
        case (result, List(x, y)) =>
          apronState.ifThenElse(op(x, y)) {
            apronState.assign(result, booleanLit(true))
          } {
            apronState.assign(result, booleanLit(false))
          }
          addr(result, resultType)
      }

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

  override def withTempVars[A](resultType: Type, exprs: ApronExpr[VirtualAddress[Ctx], Type]*)(f: PartialFunction[(VirtualAddress[Ctx],List[ApronExpr[VirtualAddress[Ctx], Type]]), A]): A =
    val tempVars = exprs.map ( expr =>
      if(expr.addrs.isEmpty) {
        val tpe = expr._type
        val ctx = temporaryVariableAllocator(tpe)
        val addr = recencyStore.alloc(ctx)
        assign(addr, expr)
        ApronExpr.addr(addr, tpe)
      } else {
        expr
      }
    ).toList

    val resultCtx = temporaryVariableAllocator(resultType)
    val resultAddr = recencyStore.alloc(resultCtx)

    f(resultAddr, tempVars)

  override def assign(v: VirtualAddress[Ctx], expr: ApronExpr[VirtualAddress[Ctx], Type]): Unit =
    apronStore.write(v.physical, virtToPhys(expr))

  override def addConstraint(constraint: ApronCons[VirtualAddress[Ctx], Type]) =
    apronStore.addConstraint(virtToPhys(constraint))

  override def getBound(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
    apronStore.getBound(virtToPhys(expr))

  override def join[A: Join](f: => A)(g: => A): A =
    effectStack.joinComputations {
      f
    } {
      g
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