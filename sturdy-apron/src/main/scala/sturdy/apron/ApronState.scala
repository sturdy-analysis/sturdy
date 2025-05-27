package sturdy.apron

import apron.{Interval, Var}
import gmp.{Mpfr, Mpq}
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.{Combine, Join, MaybeChanged, Unchanged, Widen, Widening}
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
  def join: Join[ApronExpr[Addr,Type]]
  def widen: Widen[ApronExpr[Addr,Type]]
  def ifThenElse[A: Join](condition: ApronCons[Addr, Type])(f: => A)(g: => A): A
  def getInterval(expr: ApronExpr[Addr, Type]): Interval
  def getIntInterval(expr: ApronExpr[Addr, Type]): (Int,Int) =
    val (lower,upper) = getBigIntInterval(expr)
    (lower.getOrElse[BigInt](Integer.MIN_VALUE).toInt, upper.getOrElse[BigInt](Integer.MAX_VALUE).toInt)

  def getLongInterval(expr: ApronExpr[Addr, Type]): (Long, Long) =
    val (lower, upper) = getBigIntInterval(expr)
    (lower.getOrElse[BigInt](Long.MinValue).toLong, upper.getOrElse[BigInt](Long.MaxValue).toLong)

  def getBigIntInterval(expr: ApronExpr[Addr, Type]): (Option[BigInt],Option[BigInt]) =
    val iv = getInterval(expr)
    val lower =
      if (iv.inf().isInfty() != 0)
        None
      else
        val mpq = Mpq()
        iv.inf().toMpq(mpq, 0)
        Some(BigInt(mpq.getNum.bigIntegerValue().divide(mpq.getDen.bigIntegerValue())))

    val upper =
      if (iv.sup().isInfty() != 0)
        None
      else
        val mpq = Mpq()
        iv.sup().toMpq(mpq, 0)
        Some(BigInt(mpq.getNum.bigIntegerValue().divide(mpq.getDen.bigIntegerValue())))

    (lower, upper)

  def getDoubleInterval(expr: ApronExpr[Addr, Type]): (Double, Double) =
    val iv = getInterval(expr)
    val lower: Array[Double] = Array(0.0)
    iv.inf().toDouble(lower, Mpfr.RNDZ)
    val upper: Array[Double] = Array(0.0)
    iv.inf().toDouble(upper, Mpfr.RNDZ)
    (lower(0), upper(0))

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
  )
  (using
    effectStack: EffectStack
  )
  extends ApronState[VirtualAddress[Ctx], Type]:

  val convertExpr = ApronExprConverter(recencyStore, relationalStore)

  def unapply: (RecencyStore[Ctx, PowVirtualAddress[Ctx], Val], RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]) =
    (recencyStore, relationalStore)

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

  override def getInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
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

  override def join: Join[ApronExpr[VirtualAddress[Ctx], Type]] = {
    // The first two special cases avoid allocating a new temporary address
    case (e1,e2) if (e1 == e2) => Unchanged(e1)
//    case (e1@ApronExpr.Addr(ApronVar(addr1),tpe1), e2@ApronExpr.Addr(ApronVar(addr2),tpe2)) if(addr1.ctx == addr2.ctx && tpe1 == tpe2) =>
//      val iv1 = getBound(e1)
//      val virt = recencyStore.addressTranslation.allocRecentOld(addr1.ctx)
//      val joinedExpr = ApronExpr.addr(virt, tpe1)
//      val iv3 = getBound(joinedExpr)
//      MaybeChanged(joinedExpr, iv3.isLeq(iv1))
    // The general case allocates a new temporary address
    case (e1,e2) =>
      combineExpr(recencyStore.widen)(e1, e2)
  }

  override def widen: Widen[ApronExpr[VirtualAddress[Ctx], Type]] = {
    case (e1,e2) if (e1 == e2) => Unchanged(e1)
    case (e1,e2) => combineExpr(recencyStore.widen)(e1, e2)
  }

  private def combineExpr[W <: Widening](combineStore: Combine[recencyStore.State, W]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] = (e1, e2) =>
    val resultType = Join(e1._type, e2._type).get
    val ctx = temporaryVariableAllocator(resultType)
    val result = recencyStore.addressTranslation.allocOld(ctx)
    val state1 = recencyStore.getState
    assign(result, e1)
    val state2 = recencyStore.getState
    recencyStore.setState(state1)
    assign(result, e2)
    val state3 = recencyStore.getState
    val joinedState = combineStore(state2, state3)
    recencyStore.setState(joinedState.get)
    if(joinedState.hasChanged)
      println("Changed")
    joinedState.map(_ => ApronExpr.addr(result, resultType))

