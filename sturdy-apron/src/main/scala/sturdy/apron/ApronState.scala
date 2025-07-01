package sturdy.apron

import apron.{Coeff, Interval, Var}
import gmp.{Mpfr, Mpq}
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyClosure, RecencyStore, RelationalStore}
import sturdy.values.{Changed, Combine, Join, MaybeChanged, Topped, Unchanged, Widen, Widening}
import sturdy.values.booleans.{BooleanOps, given}
import sturdy.values.ordering.{EqOps, given}
import sturdy.values.floating.{*, given}
import sturdy.values.references.{*, given}
import sturdy.data.{*, given}
import sturdy.util.Lazy
import sturdy.values.integer.NumericInterval

import scala.annotation.tailrec
import scala.reflect.ClassTag

trait ApronState[Addr: Ordering: ClassTag,Type]:
  def withTempVars[A](resultType: Type, exprs: ApronExpr[Addr,Type]*)
                     (f: PartialFunction[(Addr, List[ApronExpr[Addr,Type]]),A]): A

  def assignTempVar(expr: ApronExpr[Addr,Type]): ApronExpr[Addr,Type] =
    val tpe = expr._type
    withTempVars(tpe) {
      case (result, _) =>
        assign(result, expr)
        ApronExpr.Addr(result, expr.floatSpecials, tpe)
    }

  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def addConstraints(constraints: ApronCons[Addr,Type]*): Unit
  def addCondition(condition: ApronBool[Addr,Type]): Unit
  def effects: EffectStack
  inline def join[A: Join](f: => A)(g: => A): A =
    effects.joinComputations(f)(g)

  inline def ifThenElse[A: Join](constraint: ApronCons[Addr, Type])(f: => A)(g: => A): A =
    ifThenElse(effects)(constraint)(f)(g)
  inline def ifThenElse[A: Join](effectStack: EffectStack)(constraint: ApronCons[Addr, Type])(f: => A)(g: => A): A =
    ifThenElse(effectStack)(ApronBool.Constraint(constraint))(f)(g)
  inline def ifThenElse[A: Join](condition: ApronBool[Addr, Type])(f: => A)(g: => A): A =
    ifThenElse(effects)(condition)(f)(g)
  def ifThenElse[A: Join](effectStack: EffectStack)(condition: ApronBool[Addr, Type])(f: => A)(g: => A): A =
    getBoolean(condition) match
      case Topped.Actual(true) =>
        addCondition(condition)
        f
      case Topped.Actual(false) =>
        addCondition(condition.negated)
        g
      case Topped.Top =>
        effectStack.joinComputations{
          addCondition(condition)
          f
        } {
          addCondition(condition.negated)
          g
        }


  def join: Join[ApronExpr[Addr, Type]]
  def widen: Widen[ApronExpr[Addr, Type]]

  def getInterval(expr: ApronExpr[Addr, Type]): Interval

  def getFloatInterval(expr: ApronExpr[Addr, Type]): sturdy.apron.FloatInterval

  def getIntInterval(expr: ApronExpr[Addr, Type]): (Int,Int) =
    val (lower,upper) = getBigIntInterval(expr)
    (lower.getOrElse[BigInt](Integer.MIN_VALUE).toInt, upper.getOrElse[BigInt](Integer.MAX_VALUE).toInt)

  def getLongInterval(expr: ApronExpr[Addr, Type]): (Long, Long) =
    val (lower, upper) = getBigIntInterval(expr)
    val inf = lower.getOrElse[BigInt](Long.MinValue).max(Long.MinValue).toLong
    val sup = upper.getOrElse[BigInt](Long.MaxValue).min(Long.MaxValue).toLong
    (inf,sup)

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

  def satisfies(v: ApronCons[Addr,Type]): Topped[Boolean]

  def getBoolean(v: ApronBool[Addr, Type]): Topped[Boolean] =
    v match
      case ApronBool.Constraint(cons) => getBoolean(cons)
      case ApronBool.And(e1, e2) => summon[BooleanOps[Topped[Boolean]]].and(getBoolean(e1), getBoolean(e2))
      case ApronBool.Or(e1, e2)  => summon[BooleanOps[Topped[Boolean]]].or(getBoolean(e1), getBoolean(e2))

  def getBoolean(v: ApronCons[Addr, Type]): Topped[Boolean] =
    getBoolean(v, getFloatInterval(v.e1), getFloatInterval(v.e2))

  private def getBoolean(v: ApronCons[Addr, Type], iv1: sturdy.apron.FloatInterval, iv2: sturdy.apron.FloatInterval): Topped[Boolean] =
    v.op match
      case CompareOp.Eq =>
        if (iv1.isScalar && iv1.floatSpecials.nan || iv2.isScalar && iv2.floatSpecials.nan)
          Topped.Actual(false)
        else if(iv1.floatSpecials.nan || iv2.floatSpecials.nan)
          Topped.Top
        else if (iv1.isScalar && iv2.isScalar && iv1.isEqual(iv2))
          Topped.Actual(true)
        else if (iv1.meet(iv2).isBottom) // no overlap
          Topped.Actual(false)
        else // overlap
          Topped.Top
      case CompareOp.Neq =>
        getBoolean(ApronCons(CompareOp.Eq, v.e1, v.e2), iv1, iv2).map(! _)
      case CompareOp.Lt =>
        if (iv1.isScalar && iv1.floatSpecials.nan || iv2.isScalar && iv2.floatSpecials.nan)
          Topped.Actual(false)
        else if (iv1.floatSpecials.nan || iv2.floatSpecials.nan)
          Topped.Top
        else if (iv1.sup().cmp(iv2.inf()) < 0) // iv1 < iv2
          Topped.Actual(true)
        else if (iv2.sup().cmp(iv1.inf()) <= 0) // iv2 <= iv1
          Topped.Actual(false)
        else // overlap
          Topped.Top
      case CompareOp.Le =>
        if (iv1.isScalar && iv1.floatSpecials.nan || iv2.isScalar && iv2.floatSpecials.nan)
          Topped.Actual(false)
        else if (iv1.floatSpecials.nan || iv2.floatSpecials.nan)
          Topped.Top
        else if (iv1.sup().cmp(iv2.inf()) <= 0) // iv1 <= iv2
          Topped.Actual(true)
        else if (iv2.sup().cmp(iv1.inf()) < 0) // iv2 < iv1
          Topped.Actual(false)
        else
          Topped.Top
      case CompareOp.Gt =>
        getBoolean(ApronCons(CompareOp.Lt, v.e2, v.e1), iv2, iv1)
      case CompareOp.Ge =>
        getBoolean(ApronCons(CompareOp.Le, v.e2, v.e1), iv2, iv1)

  def isUnconstraint(addr: Addr): Boolean
  def makeNonRelational(addr: Addr): Unit

final class ApronRecencyState
  [
    Ctx: Ordering,
    Type: ApronType : Join: Widen,
    Val: Join: Widen
  ]
  (
    temporaryVariableAllocator: Allocator[Ctx, Type],
    val recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], Val],
    val relationalStore: RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]
  )(using
    RelationalExpr[Val, VirtualAddress[Ctx], Type]
  )
  extends ApronState[VirtualAddress[Ctx], Type]:

  val effectStack: EffectStack = EffectStack(recencyStore)
  val convertExpr: ApronExprConverter[Ctx, Type, Val] = ApronExprConverter(recencyStore, relationalStore)
  given Lazy[ApronExprConverter[Ctx, Type, Val]] = Lazy(convertExpr)
  val relationalValue: RelationalExpr[Val, PhysicalAddress[Ctx], Type] = implicitly

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
      relationalValue.makeRelationalExpr(
        convertExpr.virtToPhys(expr)))

  inline override def addConstraints(constraints: ApronCons[VirtualAddress[Ctx], Type]*): Unit =
    val physConstraints = constraints.flatMap(convertExpr.virtToPhys(_))
    relationalStore.addConstraints(physConstraints*)

  override def addCondition(condition: ApronBool[VirtualAddress[Ctx], Type]): Unit =
    condition match
      case ApronBool.Constraint(constraint) => addConstraints(constraint)
      case ApronBool.And(e1, e2) => addCondition(e1); addCondition(e2)
      case ApronBool.Or(e1, e2) =>
        effectStack.joinComputations {
          addCondition(e1)
        } {
          addCondition(e2)
        }

  inline override def getInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval =
    relationalStore.getBound(convertExpr.virtToPhys(expr))

  inline override def getFloatInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): sturdy.apron.FloatInterval =
    relationalStore.getFloatBound(convertExpr.virtToPhys(expr))

  inline override def satisfies(v: ApronCons[VirtualAddress[Ctx], Type]): Topped[Boolean] =
    convertExpr.virtToPhys(v).map(relationalStore.satisfies).getOrElse(Topped.Top)

  override def effects: EffectStack = effectStack

  override def join: Join[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(false, temporaryVariableAllocator)
  override def widen: Widen[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(true, temporaryVariableAllocator)

  def combineExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, Type]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] = {
    case (e1, e2) if (e1 == e2) =>
      Unchanged(e1)
    case (e1, e2) if (getInterval(e1).isBottom) =>
      Join[(FloatSpecials, Type)]((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).map((specials, tpe) =>
        e2.setFloatSpecials(specials).setType(tpe)
      )
    case (e1, e2) if (getInterval(e2).isBottom) =>
      Join[(FloatSpecials, Type)]((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).map((specials, tpe) =>
        e1.setFloatSpecials(specials).setType(tpe)
      )
    case (ApronExpr.Addr(v1, specials1, tpe1), ApronExpr.Addr(v2, specials2, tpe2)) if v1.ctx == v2.ctx =>
      val joinedRecency = Join(v1.recency, v2.recency)
      val joinedType = Join(tpe1, tpe2)
      val joinedSpecials = Join(specials1, specials2)
      val virt: VirtualAddress[Ctx] = if(joinedRecency.hasChanged) {
        recencyStore.addressTranslation.allocNoRetire(v1.ctx, joinedRecency.get)
      } else {
        v1
      }
      MaybeChanged(ApronExpr.Addr(ApronVar(virt), joinedSpecials.get, joinedType.get), joinedSpecials.hasChanged || joinedType.hasChanged)
    case (e1, e2) if(e1.isConstant && e2.isConstant) =>
      val iv1 = getInterval(e1)
      val iv2 = getInterval(e2)
      if(widen)
        Widen[(Interval, FloatSpecials, Type)]((iv1, e1.floatSpecials, e1._type), (iv2, e2.floatSpecials, e2._type)).map(
          ApronExpr.Constant(_, _, _)
        )
      else
        Join[(Interval, FloatSpecials, Type)]((iv1, e1.floatSpecials, e1._type), (iv2, e2.floatSpecials, e2._type)).map(
            ApronExpr.Constant(_, _, _)
        )
    case (ApronExpr.Unary(op1, e1, rt1, rd1, specials1, tpe1), ApronExpr.Unary(op2, e2, rt2, rd2, specials2, tpe2)) if(op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2 && structurallyJoinable(e1, e2)) =>
      for {
        eCombined <- combineExpr(widen, allocator).apply(e1, e2)
        specialsCombined <- Join(specials1, specials2)
      } yield(ApronExpr.Unary(op1, eCombined, rt1, rd1, specialsCombined, tpe1))
    case (ApronExpr.Binary(op1, l1, r1, rt1, rd1, specials1, tpe1), ApronExpr.Binary(op2, l2, r2, rt2, rd2, specials2, tpe2)) if(op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2 && structurallyJoinable(l1, l2) && structurallyJoinable(r1, r2)) =>
      for {
        lCombined <- combineExpr(widen, allocator).apply(l1, l2)
        rCombined <- combineExpr(widen, allocator).apply(r1, r2)
        specialsCombined <- Join(specials1, specials2)
      } yield(ApronExpr.Binary(op1, lCombined, rCombined, rt1, rd1, specialsCombined, tpe1))
    case (e1,e2) if containsFailedAddrs(recencyStore.addressTranslation.mapping, e1) || containsFailedAddrs(recencyStore.addressTranslation.otherMapping.getOrElse(recencyStore.addressTranslation.mapping), e2) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap(
        (joinedSpecials, joinedType) =>
          val iv1 = getInterval(e1)
          val ctx = allocator(joinedType)
          val failedVirt = recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Failed)
          val failedExpr = ApronExpr.Addr(failedVirt, joinedSpecials, joinedType)
          val iv2 = getInterval(failedExpr)
          MaybeChanged(failedExpr, ! iv2.isLeq(iv1))
      )
    case (e1,e2) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap { case (joinedSpecials, joinedType) =>
        val ctx = allocator(joinedType)
        val result = recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Old)
        assign(result, e1)
        assign(result, e2)
        // The check if the value has grown happens on the abstract domain
        Unchanged(ApronExpr.Addr(result, joinedSpecials, joinedType))
      }
  }

  override def isUnconstraint(virtualAddress: VirtualAddress[Ctx]): Boolean =
    relationalStore.isUnconstrained(virtualAddress.physical)

  override def makeNonRelational(virtualAddress: VirtualAddress[Ctx]): Unit =
    relationalStore.moveToNonRelationalStore(virtualAddress.physical)

  private def containsFailedAddrs(mapping: Map[Ctx, RecencyRegion], expr: ApronExpr[VirtualAddress[Ctx], Type]): Boolean =
    expr.addrs.exists(virt => recencyStore.addressTranslation.recency(mapping, virt.ctx, virt.n) == PowRecency.Failed)

  private def structuralEq(e1: ApronExpr[VirtualAddress[Ctx], Type], e2: ApronExpr[VirtualAddress[Ctx], Type]): Boolean =
    (e1, e2) match
      case (ApronExpr.Addr(v1, specials1, tpe1), ApronExpr.Addr(v2, specials2, tpe2)) => v1.ctx == v2.ctx && tpe1 == tpe2
      case (ApronExpr.Constant(_, _, tpe1), ApronExpr.Constant(_, _, tpe2)) => tpe1 == tpe2
      case (ApronExpr.Unary(op1, e1, rt1, rd1, _, tpe1), ApronExpr.Unary(op2, e2, rt2, rd2, _, tpe2)) =>
        op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2
      case (ApronExpr.Binary(op1, l1, r1, rt1, rd1, _, tpe1), ApronExpr.Binary(op2, l2, r2, rt2, rd2, _, tpe2)) =>
        op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2 && structuralEq(r1, r2) && structuralEq(l1, l2)
      case (_,_) => false

  private inline def structurallyJoinable(e1: ApronExpr[VirtualAddress[Ctx], Type], e2: ApronExpr[VirtualAddress[Ctx], Type]): Boolean =
    e1.isConstant && e2.isConstant || structuralEq(e1, e2)

  override def toString: String =
    relationalStore.abstract1.toString