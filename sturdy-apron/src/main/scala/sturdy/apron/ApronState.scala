package sturdy.apron

import apron.{Interval, Var}
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.{Join, Widen}
import sturdy.values.booleans.BooleanOps
import sturdy.values.references.{PhysicalAddress, PowRecency, PowVirtualAddress, PowersetAddr, Recency, RecencyRegion, VirtualAddress, given}
import sturdy.data.{*, given}

import scala.reflect.ClassTag

trait ApronState[Addr,Type]:
  def withTempVars[A](resultType: Type, exprs: ApronExpr[Addr,Type]*)
                     (f: PartialFunction[(Addr, List[ApronExpr[Addr,Type]]),A]): A
  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def addConstraint(constraint: ApronCons[Addr,Type]): Unit
  def join[A: Join](f: => A)(g: => A): A
  def ifThenElse[A: Join](condition: ApronCons[Addr, Type])(f: => A)(g: => A): A
  def getBound(expr: ApronExpr[Addr, Type]): Interval
  def getIntBound(expr: ApronExpr[Addr, Type]): (Int,Int) =
    val iv = getBound(expr)
    val d = Array[Double](0)
    val lower =
      if (iv.inf().isInfty() != 0)
        Integer.MIN_VALUE
      else
        iv.inf().toDouble(d, 0)
        d(0).intValue()

    val upper =
      if (iv.sup().isInfty() != 0)
        Integer.MAX_VALUE
      else
        iv.sup().toDouble(d, 0)
        d(0).intValue()

    (lower, upper)

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

final class ApronRecencyState
  [
    Ctx: Ordering,
    Type: ApronType : Join,
    Val: Join: Widen
  ]
  (
    temporaryVariableAllocator: Allocator[Ctx, Type],
    val recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], Val],
    val relationalStore: RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]
  ) extends ApronState[VirtualAddress[Ctx], Type]:

  val effectStack = EffectStack(EffectList(recencyStore, recencyStore.getAddressTranslation))
  val convertExpr = ApronExprConverter(recencyStore, relationalStore)

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
    try {
      relationalStore.write(v.physical, relationalStore.makeRelationalVal(convertExpr.virtToPhys(expr)))
    } catch {
      case e: IllegalArgumentException =>
        e.printStackTrace()
    }

  override def addConstraint(constraint: ApronCons[VirtualAddress[Ctx], Type]): Unit =
    relationalStore.addConstraint(convertExpr.virtToPhys(constraint))

  override def getBound(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
    relationalStore.getBound(convertExpr.virtToPhys(expr))

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