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
import sturdy.util.{Lazy, Profiler}
import sturdy.values.integer.{IntervalRange, NumericInterval}

import scala.annotation.tailrec
import scala.reflect.ClassTag

enum ResolveState:
  case Internal
  case Left
  case Right

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
  def addConstraints(constraints: ApronCons[Addr,Type]*)(using ResolveState): Unit
  def addCondition(condition: ApronBool[Addr,Type])(using ResolveState): Unit

  def isUnconstrained(expr: ApronExpr[Addr,Type] | ApronCons[Addr,Type] | ApronBool[Addr,Type])(using ResolveState): Boolean

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
    given resolveState: ResolveState = ResolveState.Internal
    assert(condition) match
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

  def toNonRelational(expr: ApronExpr[Addr,Type])(using ResolveState): ApronExpr[Addr,Type] =
    ApronExpr.Constant(getInterval(expr), specials = expr.floatSpecials, tpe = expr._type)

  def toNonRelational(cond: ApronBool[Addr,Type])(using ResolveState): ApronBool[Addr,Type] =
    ApronBool.Constant(assert(cond))

  def getInterval(expr: ApronExpr[Addr, Type])(using ResolveState): Interval

  def getFloatInterval(expr: ApronExpr[Addr, Type])(using ResolveState): sturdy.apron.FloatInterval

  def getIntInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Int,Int) =
    val (lower,upper) = getBigIntInterval(expr)
    (lower.getOrElse[BigInt](Integer.MIN_VALUE).toInt, upper.getOrElse[BigInt](Integer.MAX_VALUE).toInt)

  def getLongInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Long, Long) =
    val (lower, upper) = getBigIntInterval(expr)
    val inf = lower.getOrElse[BigInt](Long.MinValue).max(Long.MinValue).toLong
    val sup = upper.getOrElse[BigInt](Long.MaxValue).min(Long.MaxValue).toLong
    (inf,sup)

  def getBigIntInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Option[BigInt],Option[BigInt]) =
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

  def getDoubleInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Double, Double) =
    val iv = getInterval(expr)
    val lower: Array[Double] = Array(0.0)
    iv.inf().toDouble(lower, Mpfr.RNDZ)
    val upper: Array[Double] = Array(0.0)
    iv.inf().toDouble(upper, Mpfr.RNDZ)
    (lower(0), upper(0))

  def assert(v: ApronBool[Addr, Type] | ApronCons[Addr,Type])(using ResolveState): Topped[Boolean]

  def makeNonRelational(addr: Addr)(using ResolveState): Unit

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

  inline override def addConstraints(constraints: ApronCons[VirtualAddress[Ctx], Type]*)(using ResolveState): Unit =
    val physConstraints = constraints.map(constraint => withState(convertExpr.virtToPhysPure(constraint, _)))
    modifyState(relationalStore.addConstraintsPure(_,physConstraints*))

  override def addCondition(condition: ApronBool[VirtualAddress[Ctx], Type])(using ResolveState): Unit =
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

  override def isUnconstrained(expr: ApronExpr[VirtualAddress[Ctx], Type] | ApronCons[VirtualAddress[Ctx], Type] | ApronBool[VirtualAddress[Ctx], Type])(using ResolveState): Boolean =
    expr match
      case ApronExpr.Addr(ApronVar(virtAddr), _, _) =>
        relationalStore.isUnconstrained(virtAddr.physical, relationalStore.internalState)
      case ApronExpr.Constant(coeff, _, tpe) =>
        tpe.signedTop.cmp(coeff) < 0
      case ApronExpr.Unary(_, e, _, _, _, _) => isUnconstrained(e)
      case ApronExpr.Binary(_, e1, e2, _, _, _, _) => isUnconstrained(e1) || isUnconstrained(e2)
      case ApronCons(_, e1, e2) => isUnconstrained(e1) || isUnconstrained(e2)
      case ApronBool.Constraint(cons) => isUnconstrained(cons)
      case ApronBool.Constant(toppedBool) => toppedBool.isTop
      case ApronBool.And(e1, e2) => isUnconstrained(e1) || isUnconstrained(e2)
      case ApronBool.Or(e1, e2) => isUnconstrained(e1) || isUnconstrained(e2)

  def getNonRelationalValue(addr: VirtualAddress[Ctx])(using ResolveState): JOptionA[Val] =
    withState{ state =>
      val phys = relationalStore.physicalAddresses(addr.ctx, addr.n, state)
      state.withNonRelationalState(relationalStore.nonRelationalStore.readPure(phys, _))
    }

  override def getInterval(virtExpr: ApronExpr[VirtualAddress[Ctx], Type])(using ResolveState): Interval =
    if(virtExpr.isConstant) {
      relationalStore.getBound(virtExpr.asInstanceOf[ApronExpr[PhysicalAddress[Ctx], Type]], getResolveState)
    } else {
      val (physExpr, state1) = convertExpr.virtToPhysPure(virtExpr, getResolveState.clone().asInstanceOf)
      relationalStore.getBound(physExpr, state1)
    }

  inline override def getFloatInterval(virtExpr: ApronExpr[VirtualAddress[Ctx], Type])(using ResolveState): apron.FloatInterval =
    if (virtExpr.isConstant) {
      relationalStore.getFloatBound(virtExpr.asInstanceOf[ApronExpr[PhysicalAddress[Ctx], Type]], getResolveState)
    } else {
      val (physExpr,state1) = convertExpr.virtToPhysPure(virtExpr, getResolveState.clone.asInstanceOf)
      relationalStore.getFloatBound(physExpr, state1)
    }

  override def assert(v: ApronBool[VirtualAddress[Ctx], Type] | ApronCons[VirtualAddress[Ctx], Type])(using ResolveState): Topped[Boolean] =
    v match
      case cons: ApronCons[VirtualAddress[Ctx], Type] =>
        if (cons.isConstant) {
          relationalStore.assert(cons.asInstanceOf[ApronCons[PhysicalAddress[Ctx], Type]], getResolveState)
        } else {
          val (physCons, state1) = convertExpr.virtToPhysPure(cons, getResolveState.clone())
          relationalStore.assert(physCons, state1)
        }
      case ApronBool.Constraint(cons) => assert(cons)
      case ApronBool.Constant(b) => b
      case ApronBool.And(e1, e2) =>
        assert(e1) match
          case _false@Topped.Actual(false) => _false
          case Topped.Actual(true) => assert(e2)
          case _top@Topped.Top => assert(e2) match
            case _false@Topped.Actual(false) => _false
            case _ => _top
      case ApronBool.Or(e1, e2) =>
        assert(e1) match
          case _true@Topped.Actual(true) => _true
          case Topped.Actual(false) => assert(e2)
          case _top@Topped.Top => assert(e2) match
            case _true@Topped.Actual(true) => _true
            case _ => _top

  override def effects: EffectStack = effectStack

  override def join: Join[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(false, temporaryVariableAllocator)
  override def widen: Widen[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(true, temporaryVariableAllocator)

  def combineExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, Type]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] = Profiler.addTime("ApronState.combineExpr") {
    case (e1, e2) if (e1 == e2) =>
      Unchanged(e1)
    case (e1, e2) if (getInterval(e1)(using ResolveState.Left).isBottom) =>
      Join[(FloatSpecials, Type)]((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).map((specials, tpe) =>
        e2.setFloatSpecials(specials).setType(tpe)
      )
    case (e1, e2) if (getInterval(e2)(using ResolveState.Right).isBottom) =>
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
      val iv1 = getInterval(e1)(using ResolveState.Left)
      val iv2 = getInterval(e2)(using ResolveState.Right)
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
          val iv1 = getInterval(failedExpr)(using ResolveState.Left)
          val iv2 = getInterval(e2)(using ResolveState.Right)
          MaybeChanged(failedExpr, ! iv2.isLeq(iv1))
      )
    case (e1,e2) if containsFailedAddrs(e2, relationalStore.rightState.getOrElse(relationalStore.internalState)) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap(
        (joinedSpecials, joinedType) =>
          val ctx = allocator(joinedType)
          val failedVirt = relationalStore.withRightState(recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Failed, _))
          val failedExpr = ApronExpr.Addr(failedVirt, joinedSpecials, joinedType)
          val iv1 = getInterval(e1)(using ResolveState.Left)
          val iv2 = getInterval(failedExpr)(using ResolveState.Right)
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
        // Check if expression has grown happens when combining Abstract1
        Unchanged(ApronExpr.Addr(result, joinedSpecials, joinedType))
      }
  }

  override def makeNonRelational(virtualAddress: VirtualAddress[Ctx])(using ResolveState): Unit =
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

  inline private def withState[A](using resolveState: ResolveState)(f: relationalStore.State => (A, relationalStore.State)): A =
    resolveState match
      case ResolveState.Internal => relationalStore.withInternalState(f)
      case ResolveState.Left => relationalStore.withLeftState(f)
      case ResolveState.Right => relationalStore.withRightState(f)

  inline private def modifyState(using resolveState: ResolveState)(f: relationalStore.State => relationalStore.State): Unit =
    withState(state => ((), f(state)))

  inline private def getResolveState(using resolveState: ResolveState): relationalStore.State =
    resolveState match
      case ResolveState.Internal => relationalStore.internalState
      case ResolveState.Right => relationalStore.rightState.getOrElse(relationalStore.internalState)
      case ResolveState.Left => relationalStore.leftState.getOrElse(relationalStore.internalState)

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
      val iv1 = getInterval(e1)(using ResolveState.Left)
      val iv2 = getInterval(e2)(using ResolveState.Right)
      for {
        iv <- if(widen) Widen(iv1,iv2) else Join(iv1,iv2);
        floatSpecials <- if(widen) Widen(e1.floatSpecials,e2.floatSpecials) else Join(e1.floatSpecials,e2.floatSpecials)
        tpe <- if(widen) Widen(e1._type,e2._type) else Join(e1._type,e2._type)
      } yield(ApronExpr.floatConstant(iv, floatSpecials, tpe))


given ApronExprIntervalRange[Addr, Type: ApronType](using apronState: ApronState[Addr, Type]): IntervalRange[ApronExpr[Addr,Type]] with
  var tpe: Type = _

  override def range(expr: ApronExpr[Addr, Type]): Option[Range] =
    tpe = expr._type
    IntervalRange[Interval].range(apronState.getInterval(expr)(using ResolveState.Internal))

  override def fromInt(l: Int, h: Int): ApronExpr[Addr, Type] = ApronExpr.interval(l, h, tpe)

  override def fromTop(t: Topped[Int]): ApronExpr[Addr, Type] =
    t match
      case Topped.Actual(x) => ApronExpr.interval(x, x, tpe)
      case Topped.Top => ApronExpr.top(tpe)