package sturdy.apron

import apron.{Coeff, Interval, Var}
import gmp.{Mpfr, Mpq}
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyClosure, RecencyStore, RelationalStore}
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
  def effects: EffectStack
  def join[A: Join](f: => A)(g: => A): A =
    effects.joinComputations(f)(g)

  def ifThenElse[A: Join](condition: ApronCons[Addr, Type])(f: => A)(g: => A): A =
    ifThenElse(effects)(condition)(f)(g)
  def ifThenElse[A: Join](effectStack: EffectStack)(condition: ApronCons[Addr, Type])(f: => A)(g: => A): A =
    getBoolean(condition) match
      case Topped.Actual(true) =>
        addConstraints(condition)
        f
      case Topped.Actual(false) =>
        addConstraints(condition.negated)
        g
      case Topped.Top =>
        effectStack.joinComputations{
          addConstraints(condition)
          f
        } {
          addConstraints(condition.negated)
          g
        }

  def join: Join[ApronExpr[Addr, Type]]
  def widen: Widen[ApronExpr[Addr, Type]]

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
        if (iv1.isEqual(iv2) && iv1.inf.isEqual(iv1.sup))
          Topped.Actual(true)
        else if (IntervalLattice.meet(iv1, iv2).isBottom) // no overlap
          Topped.Actual(false)
        else // overlap
          Topped.Top
      case CompareOp.Neq =>
        if (iv1.isEqual(iv2) && iv1.inf.isEqual(iv1.sup))
          Topped.Actual(false)
        else if (IntervalLattice.meet(iv1, iv2).isBottom) // no overlap
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
        getBoolean(ApronCons(CompareOp.Lt, v.e2, v.e1), iv2, iv1)
      case CompareOp.Ge =>
        getBoolean(ApronCons(CompareOp.Le, v.e2, v.e1), iv2, iv1)

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
  extends ApronState[VirtualAddress[Ctx], Type]:

  val effectStack: EffectStack = EffectStack(recencyStore)
  val convertExpr: ApronExprConverter[Ctx, Type, Val] = ApronExprConverter(recencyStore, relationalStore)

  inline def unapply: (RecencyStore[Ctx, PowVirtualAddress[Ctx], Val], RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]) =
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
    relationalStore.write(v.physical,
      relationalStore.makeRelationalVal(
        convertExpr.virtToPhys(expr)))

  inline override def addConstraints(constraints: ApronCons[VirtualAddress[Ctx], Type]*): Unit =
    relationalStore.addConstraints(constraints.flatMap(convertExpr.virtToPhys(_))*)

  inline override def getInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
    relationalStore.getBound(convertExpr.virtToPhys(expr))

  override def effects: EffectStack = effectStack

  override def join: Join[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(false)
  override def widen: Widen[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(true)

  private def combineExpr[W <: Widening](widen: Boolean): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] = {
    case (ApronExpr.Addr(v1, tpe1), ApronExpr.Addr(v2, tpe2)) if v1.ctx == v2.ctx =>
      val addrTrans = v1.addressTrans
      val region1 = addrTrans.region(v1.ctx).get
      val recency1 = addrTrans.recency(v1.ctx, v1.n)
      val recency2 = addrTrans.recency(v2.ctx, v2.n)
      val joined = Join[(PowRecency,Type)]((recency1,tpe1), (recency2,tpe2))
      if (joined.get._1 == PowRecency.RecentOld)
        addrTrans.mapping += v1.ctx -> RecencyRegion(region1.recent + v1.n, region1.old + v1.n)
      joined.map(
        (_, tpe) => ApronExpr.Addr(v1, tpe)
      )
    case (e1, e2) if (e1 == e2) =>
      Unchanged(e1)
    case (e1, e2) if(!widen && e1.isConstant && e2.isConstant) =>
      val iv1 = getInterval(e1)
      val iv2 = getInterval(e2)
      Join[(Coeff,Type)]((iv1,e1._type), (iv2, e2._type)).map(
        (iv,tpe) => ApronExpr.constant(iv,tpe)
      )
    case (e1,e2) =>
      val joinedType = Join(e1._type, e2._type)
      val ctx = temporaryVariableAllocator(joinedType.get)
      val result = recencyStore.addressTranslation.allocOld(ctx)
      val resultExpr = ApronExpr.addr(result, joinedType.get)
      val iv1 = getInterval(e1)
      assign(result, e1)
      assign(result, e2)
      val iv2 = getInterval(resultExpr)
      // The check if the result has changed, happens on the abstract domain
      // in the recency closure join
      MaybeChanged(resultExpr, ! iv2.isLeq(iv1))
  }


  override def toString: String =
    relationalStore.abstract1.toString