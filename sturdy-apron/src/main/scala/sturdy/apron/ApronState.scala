package sturdy.apron

import apron.{Interval, Var}
import gmp.{Mpfr, Mpq}
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.apron.Strictness.Strict
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.{Combine, Join, MaybeChanged, Topped, Unchanged, Widen, Widening}
import sturdy.values.booleans.BooleanOps
import sturdy.values.references.{PhysicalAddress, PowRecency, PowVirtualAddress, PowersetAddr, Recency, RecencyRegion, VirtualAddress, given}
import sturdy.data.{*, given}

import scala.annotation.tailrec
import scala.reflect.ClassTag

trait ApronState[Addr: Ordering: ClassTag,Type]:
  def withTempVars[A](resultType: Type, exprs: ApronExpr[Addr,Type]*)
                     (f: PartialFunction[(Addr, List[ApronExpr[Addr,Type]]),A]): A
  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def addConstraints(constraints: ApronCons[Addr,Type]*): Unit
  def satisfies(constraints: ApronCons[Addr,Type]*): Boolean
  def join[A: Join](f: => A)(g: => A): A
  def join: Join[ApronExpr[Addr,Type]]
  def widen: Widen[ApronExpr[Addr,Type]]
  def ifThenElse[A: Join](condition: ApronCons[Addr, Type])(f: => A)(g: => A): A =
    getBoolean(condition) match
      case Topped.Actual(true) =>
        addConstraints(condition)
        f
      case Topped.Actual(false) =>
        addConstraints(condition.negated)
        g
      case Topped.Top =>
        join {
          addConstraints(condition)
          f
        } {
          addConstraints(condition.negated)
          g
        }

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

  def getBoolean(v: ApronCons[Addr, Type]): Topped[Boolean] =
    getBoolean(v, getInterval(v.e1), getInterval(v.e2))

  @tailrec
  private def getBoolean(v: ApronCons[Addr, Type], iv1: Interval, iv2: Interval): Topped[Boolean] =
    v.op match
      case CompareOp.Eq =>
        if (iv1.isEqual(iv2))
          Topped.Actual(true)
        else if (meet(iv1, iv2).isBottom) // no overlap
          Topped.Actual(false)
        else // overlap
          Topped.Top
      case CompareOp.Neq =>
        if (iv1.isEqual(iv2))
          Topped.Actual(false)
        else if (meet(iv1, iv2).isBottom) // no overlap
          Topped.Actual(true)
        else // overlap
          Topped.Top
      case CompareOp.Lt =>
        if (iv1.sup.cmp(iv2.inf) < 0) // iv1 < iv2
          Topped.Actual(true)
        else if (iv2.sup.cmp(iv1.inf) <= 0) // iv2 <= iv2
          Topped.Actual(false)
        else // overlap
          Topped.Top
      case CompareOp.Le =>
        if (iv1.sup.cmp(iv2.inf) <= 0) // iv1 <= iv2
          Topped.Actual(true)
        else if (iv2.sup.cmp(iv1.inf) < 0) // iv2 < iv2
          Topped.Actual(false)
        else
          Topped.Top
      case CompareOp.Gt =>
        getBoolean(ApronCons(CompareOp.Lt, v.e2, v.e1, v.tpe), iv2, iv1)
      case CompareOp.Ge =>
        getBoolean(ApronCons(CompareOp.Le, v.e2, v.e1, v.tpe), iv2, iv1)

  private def meet(iv1: Interval, iv2: Interval): Interval =
    val res = Interval()
    if (iv1.sup.cmp(iv2.inf) < 0 || iv2.sup.cmp(iv1.inf) < 0) // no overlap
      res.setBottom()
    else
      if (iv1.inf.cmp(iv2.inf) >= 0)
        res.setInf(iv1.inf)
      else
        res.setInf(iv2.inf)
      if (iv1.sup.cmp(iv2.sup) <= 0)
        res.setInf(iv1.sup)
      else
        res.setInf(iv2.sup)
    res


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

  override def addConstraints(constraints: ApronCons[VirtualAddress[Ctx], Type]*): Unit =
    relationalStore.addConstraints(constraints.map(convertExpr.virtToPhys)*)

  override def satisfies(constraints: ApronCons[VirtualAddress[Ctx], Type]*): Boolean =
    relationalStore.satisfies(constraints.map(convertExpr.virtToPhys)*)

  override def getInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
    relationalStore.getBound(convertExpr.virtToPhys(expr))

  override def join[A: Join](f: => A)(g: => A): A =
    effectStack.joinComputations {
      f
    } {
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
    joinedState.map(_ => ApronExpr.addr(result, resultType))


  override def toString: String =
    relationalStore.abstract1.toString