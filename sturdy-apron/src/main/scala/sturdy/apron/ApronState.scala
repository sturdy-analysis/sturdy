package sturdy.apron

import apron.{Abstract1, Coeff, Interval, Var}
import gmp.{Mpfr, Mpq}
import sturdy.apron
import sturdy.apron.ApronExpr.{addr, booleanLit}
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyClosure, RecencyStore, RelationalStore}
import sturdy.values.{Widen, *, given}
import sturdy.values.booleans.{BooleanOps, given}
import sturdy.values.ordering.{EqOps, given}
import sturdy.values.floating.{*, given}
import sturdy.values.references.{*, given}
import sturdy.data.{*, given}
import sturdy.util.Lazy
import sturdy.values.integer.{IntervalRange, NumericInterval}

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

  def alloc(ctx: Any): Addr

  def assign(v: Addr, expr: ApronExpr[Addr,Type]): Unit
  def addConstraints(constraints: ApronCons[Addr,Type]*): Unit
  def addCondition(condition: ApronBool[Addr,Type]): Unit

  def isUnconstrained(expr: ApronExpr[Addr,Type]): Boolean
  def isUnconstrained(cons: ApronCons[Addr,Type]): Boolean
  def isUnconstrained(cond: ApronBool[Addr,Type]): Boolean

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

  def toNonRelational(expr: ApronExpr[Addr,Type]): ApronExpr[Addr,Type] =
    ApronExpr.Constant(getInterval(expr), specials = expr.floatSpecials, tpe = expr._type)

  def toNonRelational(cond: ApronBool[Addr,Type]): ApronBool[Addr,Type] =
    ApronBool.Constant(getBoolean(cond))

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
      case ApronBool.Constant(b) => b
      case ApronBool.And(e1, e2) => summon[BooleanOps[Topped[Boolean]]].and(getBoolean(e1), getBoolean(e2))
      case ApronBool.Or(e1, e2)  => summon[BooleanOps[Topped[Boolean]]].or(getBoolean(e1), getBoolean(e2))

  def getBoolean(cond: ApronCons[Addr, Type]): Topped[Boolean] = {
    val specials1 = cond.e1.floatSpecials
    val specials2 = cond.e2.floatSpecials
    cond.op match
      case CompareOp.Eq if(!specials1.isBottom || !specials2.isBottom) => Topped.Top
      case CompareOp.Neq if(specials1.nan || specials2.nan || (specials1.isLeq(specials2) && !specials1.isBottom) || (specials2.isLeq(specials1) && !specials2.isBottom)) => Topped.Top
      case CompareOp.Le | CompareOp.Lt if(specials1.nan || specials2.nan || specials1.negZero || specials1.posInfinity || specials2.negZero || specials2.negInfinity) => Topped.Top
      case CompareOp.Ge | CompareOp.Gt if(specials1.nan || specials2.nan || specials1.negZero || specials1.negInfinity || specials2.negZero || specials2.posInfinity) => Topped.Top
      case _ =>
        (satisfies(cond),satisfies(cond.negated)) match
          case (Topped.Actual(true), Topped.Actual(false)) =>
             Topped.Actual(true)
          case (Topped.Actual(false), Topped.Actual(true)) =>
            Topped.Actual(false)
          case (Topped.Actual(false), Topped.Actual(false)) =>
            addCondition(ApronBool.Constant(Topped.Actual(false))); throw Error();
          case (Topped.Actual(true), Topped.Actual(true)) | (Topped.Top, _) | (_, Topped.Top) =>
            Topped.Top
  }

  def makeNonRelational(addr: Addr): Unit

class ApronRecencyState
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
    StatelessRelationalExpr[Val, VirtualAddress[Ctx], Type]
  )
  extends ApronState[VirtualAddress[Ctx], Type]:

  import relationalStore.given

  val effectStack: EffectStack = EffectStack(recencyStore)
  val convertExpr: ApronExprConverter[Ctx, Type, Val] = ApronExprConverter(recencyStore, relationalStore)
  given lazyConvertExpr: Lazy[ApronExprConverter[Ctx, Type, Val]] = Lazy(convertExpr)
  val relationalValue: StatefullRelationalExprT[Val, PhysicalAddress[Ctx], Type, lazyConvertExpr.value.State] = RelationalValueApronExprPhysicalAddress[Val, Ctx, Type].asInstanceOf

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

  override def alloc(ctx: Any): VirtualAddress[Ctx] =
    recencyStore.alloc(ctx.asInstanceOf[Ctx])

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
      case ApronBool.Constant(Topped.Actual(false)) =>
        relationalStore.setBottom
        relationalStore.addConstraints()
      case ApronBool.Constant(Topped.Actual(true)) | ApronBool.Constant(Topped.Top) => {}
      case ApronBool.And(e1, e2) => addCondition(e1); addCondition(e2)
      case ApronBool.Or(e1, e2) =>
        effectStack.joinComputations {
          addCondition(e1)
        } {
          addCondition(e2)
        }

  override def isUnconstrained(expr: ApronExpr[VirtualAddress[Ctx], Type]): Boolean =
    expr match
      case ApronExpr.Addr(ApronVar(virtAddr), _, _) =>
        relationalStore.isUnconstrained(virtAddr.physical, relationalStore.internalState)
      case ApronExpr.Constant(coeff, _, tpe) =>
        tpe.signedBounds.cmp(coeff) < 0
      case ApronExpr.Unary(_, e, _, _, _, _) => isUnconstrained(e)
      case ApronExpr.Binary(_, e1, e2, _, _, _, _) => isUnconstrained(e1) || isUnconstrained(e2)

  override def isUnconstrained(cons: ApronCons[VirtualAddress[Ctx], Type]): Boolean =
    isUnconstrained(cons.e1) || isUnconstrained(cons.e2)

  override def isUnconstrained(cons: ApronBool[VirtualAddress[Ctx], Type]): Boolean =
    cons match
      case ApronBool.Constraint(cons) => isUnconstrained(cons)
      case ApronBool.Constant(toppedBool) => toppedBool.isTop
      case ApronBool.And(e1, e2) => isUnconstrained(e1) || isUnconstrained(e2)
      case ApronBool.Or(e1, e2) => isUnconstrained(e1) || isUnconstrained(e2)

  override def getInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): Interval = getInterval(state = relationalStore.internalState, expr)
  def getInterval(state: relationalStore.State, virtExpr: ApronExpr[VirtualAddress[Ctx], Type]): Interval = {
    // purposefully throws away changes to the state as they lower precision.
    val (physExpr,state1) = convertExpr.virtToPhysPure(virtExpr, state.clone().asInstanceOf)
    relationalStore.getBound(physExpr, state1)
  }

  inline override def getFloatInterval(expr: ApronExpr[VirtualAddress[Ctx], Type]): apron.FloatInterval =
    getFloatInterval(relationalStore.internalState, expr)
  def getFloatInterval(state: relationalStore.State, virtExpr: ApronExpr[VirtualAddress[Ctx], Type]): sturdy.apron.FloatInterval =
    val (physExpr,state1) = convertExpr.virtToPhysPure(virtExpr, state.clone.asInstanceOf)
    relationalStore.getFloatBound(physExpr, state1)

  inline override def satisfies(v: ApronCons[VirtualAddress[Ctx], Type]): Topped[Boolean] =
    convertExpr.virtToPhys(v).map(relationalStore.satisfies(_)).getOrElse(Topped.Top)

  override def effects: EffectStack = effectStack

  override def join: Join[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(false, temporaryVariableAllocator)
  override def widen: Widen[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(true, temporaryVariableAllocator)

  def combineExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, Type]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] = {
    case (e1, e2) if (e1 == e2) =>
      Unchanged(e1)
    case (e1, e2) if (getInterval(relationalStore.leftState.getOrElse(relationalStore.internalState), e1).isBottom) =>
      Join[(FloatSpecials, Type)]((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).map((specials, tpe) =>
        e2.setFloatSpecials(specials).setType(tpe)
      )
    case (e1, e2) if (getInterval(relationalStore.rightState.getOrElse(relationalStore.internalState), e2).isBottom) =>
      Join[(FloatSpecials, Type)]((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).map((specials, tpe) =>
        e1.setFloatSpecials(specials).setType(tpe)
      )
    case (ApronExpr.Addr(v1, specials1, tpe1), ApronExpr.Addr(v2, specials2, tpe2)) if v1.ctx == v2.ctx =>
      val recencyV1 = relationalStore.recency(v1.ctx, v1.n, relationalStore.leftState.getOrElse(relationalStore.internalState))
      val recencyV2 = relationalStore.recency(v2.ctx, v2.n, relationalStore.rightState.getOrElse(relationalStore.internalState))
      val joinedRecency = Join(recencyV1, recencyV2)
      val joinedType = Join(tpe1, tpe2)
      val joinedSpecials = Join(specials1, specials2)
      MaybeChanged(ApronExpr.Addr(v1, joinedSpecials.get, joinedType.get), joinedSpecials.hasChanged || joinedType.hasChanged)
    case (e1, e2) if(e1.isConstant && e2.isConstant) =>
      val iv1 = getInterval(relationalStore.leftState.getOrElse(relationalStore.internalState), e1)
      val iv2 = getInterval(relationalStore.rightState.getOrElse(relationalStore.internalState), e2)
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
    case (e1,e2) if containsFailedAddrs(e1, relationalStore.leftState.getOrElse(relationalStore.internalState)) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap(
        (joinedSpecials, joinedType) =>
          val ctx = allocator(joinedType)
          val failedVirt = relationalStore.withLeftState(recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Failed, _))
          val failedExpr = ApronExpr.Addr(failedVirt, joinedSpecials, joinedType)
          val iv1 = getInterval(relationalStore.leftState.getOrElse(relationalStore.internalState), failedExpr)
          val iv2 = getInterval(relationalStore.rightState.getOrElse(relationalStore.internalState), e2)
          MaybeChanged(failedExpr, ! iv2.isLeq(iv1))
      )
    case (e1,e2) if containsFailedAddrs(e2, relationalStore.rightState.getOrElse(relationalStore.internalState)) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap(
        (joinedSpecials, joinedType) =>
          val ctx = allocator(joinedType)
          val failedVirt = relationalStore.withRightState(recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Failed, _))
          val failedExpr = ApronExpr.Addr(failedVirt, joinedSpecials, joinedType)
          val iv1 = getInterval(relationalStore.leftState.getOrElse(relationalStore.internalState), e1)
          val iv2 = getInterval(relationalStore.rightState.getOrElse(relationalStore.internalState), failedExpr)
          MaybeChanged(failedExpr, ! iv2.isLeq(iv1))
      )
    case (e1,e2) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap { case (joinedSpecials, joinedType) =>
        val ctx = allocator(joinedType)
        val result = relationalStore.withLeftState { state1 =>
          val (phys1, state2) = convertExpr.virtToPhysPure(e1, state1)
          val (result, state3) = recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Old, state2)
          (result, relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Old)), phys1, state3))
        }
        relationalStore.withRightState { state1 =>
          val (phys2, state2) = convertExpr.virtToPhysPure(e2, state1)
          val (result, state3) = recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Old, state2)
          (result, relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Old)), phys2, state3))
        }
//        if(isUnconstraint(result))
//          makeNonRelational(result)
        // Check if expression has grown happens when combining Abstract1
        Unchanged(ApronExpr.Addr(result, joinedSpecials, joinedType))
      }
  }

  override def makeNonRelational(virtualAddress: VirtualAddress[Ctx]): Unit =
    relationalStore.moveToNonRelationalStore(virtualAddress.physical)

  private def containsFailedAddrs(expr: ApronExpr[VirtualAddress[Ctx], Type], state: relationalStore.State): Boolean =
    expr.addrs.exists(virt => recencyStore.addressTranslation.recency(virt.ctx, virt.n, state) == PowRecency.Failed)

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
    relationalStore.toString

class NonRelationalApronState[Ctx: Ordering, Type: ApronType: Join: Widen, Val: Join: Widen]
  (
    temporaryVariableAllocator: Allocator[Ctx, Type],
    recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], Val],
    relationalStore: RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]
  )(using
    StatelessRelationalExpr[Val, VirtualAddress[Ctx], Type]
  )
  extends ApronRecencyState[Ctx, Type, Val](temporaryVariableAllocator, recencyStore, relationalStore):
  override def combineExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, Type]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] =
    (e1: ApronExpr[VirtualAddress[Ctx],Type], e2: ApronExpr[VirtualAddress[Ctx],Type]) =>
      val iv1 = getInterval(relationalStore.leftState.getOrElse(relationalStore.internalState), e1)
      val iv2 = getInterval(relationalStore.rightState.getOrElse(relationalStore.internalState), e2)
      for {
        iv <- if(widen) Widen(iv1,iv2) else Join(iv1,iv2);
        floatSpecials <- if(widen) Widen(e1.floatSpecials,e2.floatSpecials) else Join(e1.floatSpecials,e2.floatSpecials)
        tpe <- if(widen) Widen(e1._type,e2._type) else Join(e1._type,e2._type)
      } yield(ApronExpr.floatConstant(iv, floatSpecials, tpe))


given ApronExprIntervalRange[Addr, Type: ApronType](using apronState: ApronState[Addr, Type]): IntervalRange[ApronExpr[Addr,Type]] with
  var tpe: Type = _

  override def range(expr: ApronExpr[Addr, Type]): Option[Range] =
    tpe = expr._type
    IntervalRange[Interval].range(apronState.getInterval(expr))

  override def fromInt(l: Int, h: Int): ApronExpr[Addr, Type] = ApronExpr.interval(l, h, tpe)

  override def fromTop(t: Topped[Int]): ApronExpr[Addr, Type] =
    t match
      case Topped.Actual(x) => ApronExpr.interval(x, x, tpe)
      case Topped.Top => ApronExpr.top(tpe)