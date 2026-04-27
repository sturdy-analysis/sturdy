package sturdy.apron

import apron.{Abstract1, Coeff, DoubleScalar, Interval, Scalar, Var}
import gmp.{Mpfr, Mpq}
import sturdy.apron
import sturdy.apron.ApronExpr.{Addr, Binary, Constant, Unary, addr, booleanLit, intAdd}
import sturdy.effect.{EffectList, EffectStack, SturdyFailure}
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyClosure, RecencyStore, RelationalStore, RelationalStoreState}
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

  def isBottom(expr: ApronExpr[Addr,Type])(using ResolveState): Topped[Boolean]
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
    addConditionToWideningThresholds(condition)
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


  def addConditionToWideningThresholds(condition: ApronBool[Addr,Type]): Unit = {
    val constraints = for(cons <- condition.constraints; c <- Iterable(ApronCons.le(cons.e1, cons.e2), ApronCons.lt(cons.e1, cons.e2), ApronCons.gt(cons.e1, cons.e2), ApronCons.ge(cons.e1, cons.e2))) yield(c)
    addConstraintsToWideningThresholds(constraints)
  }

  def addConstraintsToWideningThresholds(constraints: Iterable[ApronCons[Addr,Type]]): Unit

  def join: Join[ApronExpr[Addr, Type]]
  def widen: Widen[ApronExpr[Addr, Type]]
  def joinBoolExpr: Join[ApronBool[Addr,Type]]
  def widenBoolExpr: Widen[ApronBool[Addr,Type]]

  /**
   * Simplifies expressions into the following normal form:
   *   1. Sorts literals to the right if possible:
   *     - `(lit + e) ~> (e + lit)`
   *     - `(lit * e) ~> (e * lit)`
   *     - `(lit - e) ~> (-(e) + lit)`
   *   2. Converts subtraction to addition:
   *     - `(e - lit) ~> (e + -lit)`
   *   3. Collects constants in additions and multiplications:
   *     - `((e + lit1) + lit2) ~> e + (lit1 + lit2)`
   *     - `((e * lit1) * lit2) ~> e * (lit1 * lit2)`
   */
  def simplify(expr: ApronExpr[Addr, Type])(using ResolveState): ApronExpr[Addr, Type] =
    expr match

      // (lit1 + lit2) ~> |lit1+lit2|
      // (lit1 * lit2) ~> |lit1*lit2|
      case Binary(op: (BinOp.Add.type | BinOp.Mul.type), e1: Constant[Addr,Type], e2: Constant[Addr,Type], rdt, rdd, sp, tpe) =>
        ApronExpr.constant(getInterval(expr), tpe)

      // (lit + e) ~> (e + lit)
      // (lit * e) ~> (e * lit)
      case Binary(op: (BinOp.Add.type | BinOp.Mul.type), e1: Constant[Addr,Type], e2: (ApronExpr.Addr[Addr,Type] | Unary[Addr,Type] | Binary[Addr,Type]), rdt, rdd, sp, tpe) =>
        simplify(Binary(op, simplify(e2), e1, rdt, rdd, sp, tpe))

      // (lit - e) ~> (-(e) + lit)
      case Binary(BinOp.Sub, e1: Constant[Addr,Type], e2: (ApronExpr.Addr[Addr,Type] | Unary[Addr,Type] | Binary[Addr,Type]), rdt, rdd, sp, tpe) =>
        simplify(Binary(BinOp.Add, simplify(ApronExpr.Unary(UnOp.Negate, e2, rdt, rdd, e2.floatSpecials, e2._type)), e1, rdt, rdd, sp, tpe))

      // -(lit) ~> lit.neg()
      case Unary(UnOp.Negate, Constant(coeff, sp2, tpe2), rdt, rdd, sp1, tpe1) =>
        val newCoeff = coeff.copy()
        newCoeff.neg()
        Constant(newCoeff, sp1, tpe1)

      // (e - const) ~> (e + -const)
      case Binary(BinOp.Sub, e1, lit1, rdt, rdd, sp, tpe)
        if lit1.isConstant =>
          simplify(Binary(BinOp.Add, simplify(e1), simplify(ApronExpr.Unary(UnOp.Negate, lit1, rdt, rdd, lit1.floatSpecials, lit1._type)), rdt, rdd, sp, tpe))

      // (e1 + const1) + const2 ~> e1 + (const1 + const2)
      case Binary(BinOp.Add, Binary(BinOp.Add, e1, lit1, rdt1, rdd1, sp1, tpe1), lit2, rdt2, rdd2, sp2, tpe2)
        if (lit1.isConstant && lit2.isConstant && rdt1 == rdt2 && rdd1 == rdd2 && sp1 == sp2 && tpe1 == tpe2) =>
          Binary(BinOp.Add, e1, Binary(BinOp.Add, lit1, lit2, rdt1, rdd1, sp1, tpe1), rdt2, rdd2, sp2, tpe2)

      case _ =>
        expr

  def toNonRelational(expr: ApronExpr[Addr,Type])(using ResolveState): ApronExpr[Addr,Type] =
    ApronExpr.Constant(getInterval(expr), specials = expr.floatSpecials, tpe = expr._type)

  def toNonRelational(cond: ApronBool[Addr,Type])(using ResolveState): ApronBool[Addr,Type] =
    ApronBool.Constant(assert(cond))

  def getInterval(expr: ApronExpr[Addr, Type])(using ResolveState): Interval

  def getFloatInterval(expr: ApronExpr[Addr, Type])(using ResolveState): sturdy.apron.FloatInterval

  inline def getInt(expr: ApronExpr[Addr, Type])(using ResolveState): Option[Int] = {
    val iv = getInterval(expr)
    if(iv.isScalar)
      ApronExpr.toInt(iv.inf())
    else
      None
  }

  inline def getIntInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Int,Int) =
    val iv = getInterval(expr)
    (ApronExpr.toInt(iv.inf()).getOrElse(Int.MinValue), ApronExpr.toInt(iv.sup()).getOrElse(Int.MaxValue))

  inline def getLongInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Long, Long) =
    val iv = getInterval(expr)
    (ApronExpr.toLong(iv.inf()).getOrElse(Long.MinValue), ApronExpr.toLong(iv.sup()).getOrElse(Long.MaxValue))

  def getBigIntInterval(expr: ApronExpr[Addr, Type])(using ResolveState): (Option[BigInt],Option[BigInt]) =
    val iv = getInterval(expr)
    (ApronExpr.toBigInt(iv.inf()), ApronExpr.toBigInt(iv.sup()))

  def assert(v: ApronBool[Addr, Type] | ApronCons[Addr,Type])(using ResolveState): Topped[Boolean]

  inline def isLeq(expr: ApronExpr[Addr,Type], iv: Coeff)(using ResolveState): Topped[Boolean] =
    val tpe = expr._type
    assert(
      ApronBool.And(
        ApronBool.Constraint(ApronCons.le(ApronExpr.constant(iv.inf(), tpe), expr)),
        ApronBool.Constraint(ApronCons.lt(expr, ApronExpr.constant(iv.sup(), tpe)))
      )
    )

  def makeNonRelational(addr: Addr)(using ResolveState): Unit

class ApronRecencyState
  [
    Ctx: Ordering,
    Type: ApronType : Join: Widen,
    Val: Join: Widen
  ]
  (
    val temporaryVariableAllocator: Allocator[Ctx, Type],
    val combineExpressionAllocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])],
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

  override def addConstraintsToWideningThresholds(constraints: Iterable[ApronCons[VirtualAddress[Ctx], Type]]): Unit =
    relationalStore.modifyInternalState(state0 =>
      var state = state0.clone
      val physConstraints = constraints.map(virtCons =>
        val (physCons,state1) = convertExpr.virtToPhysPure(virtCons, state)
        state = state1
        physCons
      )
      relationalStore.addConstraintsToWideningThresholdsPure(state0, physConstraints.toSeq*)
    )

  override def isBottom(expr: ApronExpr[VirtualAddress[Ctx], Type])(using resolveState: ResolveState): Topped[Boolean] = {
    val state = getResolveState
    if(expr.isBottom == Topped.Actual(true) ||
       expr.addrs.exists(virt =>
         relationalStore.physicalAddresses(virt.ctx, virt.n, state).iterator.exists(phys =>
           !state.abs1.getEnvironment.hasVar(ApronVar(phys))
         ))
    )
      Topped.Actual(true)
    else
      Topped.Top
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
      val snapshotInternal = relationalStore._internalState
      val snapshotLeft = relationalStore._leftState
      val snapshotRight = relationalStore._rightState
      try {
        relationalStore._internalState = state
        relationalStore._leftState = null
        relationalStore._rightState = null
        val (res, nonRelStoreState) = relationalStore.nonRelationalStore.readPure(phys, state.nonRelationalStoreState)
        (res, state.copy(
          addressTranslationState = relationalStore._internalState.addressTranslationState,
          abs1 = relationalStore._internalState.abs1,
          nonRelationalStoreState = nonRelStoreState
        ))
      } finally {
        relationalStore._internalState = snapshotInternal
        relationalStore._leftState = snapshotLeft
        relationalStore._rightState = snapshotRight
      }
    }

  override def getInterval(virtExpr: ApronExpr[VirtualAddress[Ctx], Type])(using ResolveState): Interval =
    if(virtExpr.isConstant) {
      virtExpr match {
        case ApronExpr.Constant(scalar: Scalar, _, _) => Interval(scalar,scalar)
        case ApronExpr.Constant(iv: Interval, _, _) => iv
        case _ => relationalStore.getBound(virtExpr.asInstanceOf[ApronExpr[PhysicalAddress[Ctx], Type]], getResolveState)
      }
    } else {
      val (physExpr, state1) = convertExpr.virtToPhysPure(virtExpr, getResolveState.clone().asInstanceOf)
      relationalStore.getBound(physExpr, state1)
    }

  inline override def getFloatInterval(virtExpr: ApronExpr[VirtualAddress[Ctx], Type])(using ResolveState): apron.FloatInterval =
    if (virtExpr.isConstant) {
      virtExpr match {
        case ApronExpr.Constant(scalar: Scalar, floatSpecials, _) => sturdy.apron.FloatInterval(scalar, scalar, floatSpecials)
        case ApronExpr.Constant(iv: Interval, floatSpecials, _) => sturdy.apron.FloatInterval(iv.inf(), iv.sup(), floatSpecials)
        case _ => relationalStore.getFloatBound(virtExpr.asInstanceOf[ApronExpr[PhysicalAddress[Ctx], Type]], getResolveState)
      }
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

  override def join: Join[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(false, combineExpressionAllocator)
  override def widen: Widen[ApronExpr[VirtualAddress[Ctx], Type]] = combineExpr(true, combineExpressionAllocator)

  def combineExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] = (e1, e2) => Profiler.addTime("ApronState.combineExpr") {
    val e1Simplified = simplify(e1)(using ResolveState.Left)
    val e2Simplified = simplify(e2)(using ResolveState.Right)
    val result = combineExpr0(widen, allocator).apply(e1Simplified, e2Simplified)
    result
  }

  private def combineExpr0[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] =
    case (e1, e2) if (e1 == e2) =>
      Unchanged(e1)

    // (⊥ ⊔ e2) = e2
    case (e1, e2) if (isBottom(e1)(using ResolveState.Left) == Topped.Actual(true)) =>
      for {
        specials <- Join(e1.floatSpecials, e2.floatSpecials)
        tpe <- Join(e1._type, e2._type)
        result = e2.setFloatSpecials(specials).setType(tpe)
        joined <- MaybeChanged(result, !getInterval(e2)(using ResolveState.Right).isBottom)
      } yield(joined)

    // (e1 ⊔ ⊥) = e1
    case (e1, e2) if (isBottom(e2)(using ResolveState.Right) == Topped.Actual(true)) =>
      for {
        specials <- Join(e1.floatSpecials, e2.floatSpecials)
        tpe <- Join(e1._type, e2._type)
        result = e1.setFloatSpecials(specials).setType(tpe)
        joined <- MaybeChanged(result, !getInterval(e1)(using ResolveState.Left).isBottom)
      } yield(joined)

    // (⊤ ⊔ e2) = ⊤
    case (e1, e2) if (e1.isTop == Topped.Actual(true)) =>
      for {
        specials <- Join(e1.floatSpecials, e2.floatSpecials)
        tpe <- Join(e1._type, e2._type)
      } yield(e1.setFloatSpecials(specials).setType(tpe))

    // (e1 ⊔ ⊤) = ⊤
    case (e1, e2) if (e2.isTop == Topped.Actual(true)) =>
      for {
        specials <- Join(e1.floatSpecials, e2.floatSpecials)
        tpe <- Join(e1._type, e2._type)
        result = e2.setFloatSpecials(specials).setType(tpe)
        joined <- MaybeChanged(result, !getInterval(e1)(using ResolveState.Left).isTop)
      } yield(joined)

    case (ApronExpr.Addr(v1, specials1, tpe1), ApronExpr.Addr(v2, specials2, tpe2)) if v1.ctx == v2.ctx =>
      val recencyV1 = relationalStore.recency(v1.ctx, v1.n, relationalStore.leftState.getOrElse(relationalStore.internalState))
      val recencyV2 = relationalStore.recency(v2.ctx, v2.n, relationalStore.rightState.getOrElse(relationalStore.internalState))
      val joinedRecency = Join(recencyV1, recencyV2)
      val joinedType = Join(tpe1, tpe2)
      val joinedSpecials = Join(specials1, specials2)
      MaybeChanged(ApronExpr.Addr(v1, joinedSpecials.get, joinedType.get), joinedSpecials.hasChanged || joinedType.hasChanged)

    // u(e1) ⊔ u(e2) = u(e1 ⊔ e2)
    case (ApronExpr.Unary(op1, e1, rt1, rd1, specials1, tpe1), ApronExpr.Unary(op2, e2, rt2, rd2, specials2, tpe2))
      if(op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2 && structurallyJoinable(e1, e2)) =>
      for {
        eCombined <- combineExpr0(widen, allocator).apply(e1, e2)
        specialsCombined <- Join(specials1, specials2)
      } yield(ApronExpr.Unary(op1, eCombined, rt1, rd1, specialsCombined, tpe1))

    // ((l1 ⊕ r1) ⊔ (l2 ⊕ r2)) = ((l1 ⊔ l2) ⊕ (r1 ⊔ r2)))
    case (ApronExpr.Binary(op1, l1, r1, rt1, rd1, specials1, tpe1), ApronExpr.Binary(op2, l2, r2, rt2, rd2, specials2, tpe2))
      if(op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2 && structurallyJoinable(l1, l2) && structurallyJoinable(r1, r2)) =>
      for {
        lCombined <- combineExpr0(widen, allocator).apply(l1, l2)
        rCombined <- combineExpr0(widen, allocator).apply(r1, r2)
        specialsCombined <- Join(specials1, specials2)
      } yield(ApronExpr.Binary(op1, lCombined, rCombined, rt1, rd1, specialsCombined, tpe1))

//    // (leaf ⊔ (l2 ⊕ r2)) = (leaf ⊕ neutral(⊕)) ⊔ (l2 ⊕ r2) if structurallyJoinable(leaf,l2)
//    // (leaf ⊔ (l2 ⊕ r2)) = (neutral(⊕) ⊕ leaf) ⊔ (l2 ⊕ r2) if structurallyJoinable(leaf,r2)
//    case (e1: (ApronExpr.Constant[VirtualAddress[Ctx], Type] | ApronExpr.Addr[VirtualAddress[Ctx], Type]), e2@ApronExpr.Binary(op, l2, r2, rt2, rd2, specials2, tpe2))
//      if !widen && (op == BinOp.Add || op == BinOp.Sub || op == BinOp.Mul) && (structurallyJoinable(e1,l2) || structurallyJoinable(e1,r2)) =>
//        if(structurallyJoinable(e1,l2))
//          combineExpr0(widen,allocator).apply(ApronExpr.Binary(op, e1, ApronExpr.lit(op.neutralElement, e1.floatSpecials, e1._type), rt2, rd2, specials2, tpe2),e2)
//        else
//          combineExpr0(widen,allocator).apply(ApronExpr.Binary(op, ApronExpr.lit(op.neutralElement, e1.floatSpecials, e1._type), e1, rt2, rd2, specials2, tpe2),e2)
//
//    // ((l1 ⊕ r1) ⊔ leaf) = (l2 ⊕ r2) ⊔ (leaf ⊕ neutral(⊕)) if structurallyJoinable(leaf,l1)
//    // ((l1 ⊕ r1) ⊔ leaf) = (l2 ⊕ r2) ⊔ (neutral(⊕) ⊕ leaf) if structurallyJoinable(leaf,r1)
//    case (e1@ApronExpr.Binary(op, l1, r1, rt1, rd1, specials1, tpe1), e2: (ApronExpr.Constant[VirtualAddress[Ctx], Type] | ApronExpr.Addr[VirtualAddress[Ctx], Type]))
//      if (op == BinOp.Add || op == BinOp.Sub || op == BinOp.Mul) && (structurallyJoinable(l1,e2) || structurallyJoinable(r1,e2)) =>
//        if(structurallyJoinable(l1,e2))
//          combineExpr0(widen,allocator).apply(e1,ApronExpr.Binary(op, e2, ApronExpr.lit(op.neutralElement, e2.floatSpecials, e2._type), rt1, rd1, specials1, tpe1))
//        else
//          combineExpr0(widen,allocator).apply(e1,ApronExpr.Binary(op, ApronExpr.lit(op.neutralElement, e2.floatSpecials, e2._type), e2, rt1, rd1, specials1, tpe1))

    // (const1 ⊔ const2) = interval(const1) ⊔ interval(const2)
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

    case (e1,e2) if containsFailedAddrs(e1)(using ResolveState.Left) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap(
        (joinedSpecials, joinedType) =>
          val ctx = allocator(e1, e2)
          val failedVirt = relationalStore.withLeftState(recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Failed, _))
          val failedExpr = ApronExpr.Addr(failedVirt, joinedSpecials, joinedType)
          val iv1 = getInterval(failedExpr)(using ResolveState.Left)
          val iv2 = getInterval(e2)(using ResolveState.Right)
          MaybeChanged(failedExpr, ! iv2.isLeq(iv1))
      )

    case (e1,e2) if containsFailedAddrs(e2)(using ResolveState.Right) =>
      Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap(
        (joinedSpecials, joinedType) =>
          val ctx = allocator(e1, e2)
          val failedVirt = relationalStore.withRightState(recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Failed, _))
          val failedExpr = ApronExpr.Addr(failedVirt, joinedSpecials, joinedType)
          val iv1 = getInterval(e1)(using ResolveState.Left)
          val iv2 = getInterval(failedExpr)(using ResolveState.Right)
          MaybeChanged(failedExpr, ! iv2.isLeq(iv1))
      )

    case (e1,e2) =>
      val iv1 = getInterval(e1)(using ResolveState.Left)
      val iv2 = getInterval(e2)(using ResolveState.Right)
      if(e1.isConstant && iv1.isLeq(iv2)) {
        MaybeChanged(e2, ! iv2.isLeq(iv1))
      } else if (e2.isConstant && iv2.isLeq(iv1)) {
        Unchanged(e1)
      } else {
        Join((e1.floatSpecials, e1._type), (e2.floatSpecials, e2._type)).flatMap { case (joinedSpecials, joinedType) =>
          val ctx = allocator(e1, e2)
//          if(widen) {
            val result = relationalStore.withLeftState { state1 =>
              val (result, state2) = recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Old, state1)
              val (phys1, state3) = convertExpr.virtToPhysPure(e1, state2)
              (result, relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Old)), phys1, state3))
            }
            relationalStore.withRightState { state1 =>
              val (result, state2) = recencyStore.addressTranslation.allocNoRetire(ctx, PowRecency.Old, state1)
              val (phys2, state3) = convertExpr.virtToPhysPure(e2, state2)
              (result, relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Old)), phys2, state3))
            }
            // Check if expression has grown happens when combining Abstract1
            Unchanged(ApronExpr.Addr(result, joinedSpecials, joinedType))
//          } else {
//            if (relationalStore._leftState != null && relationalStore._rightState != null) {
//              val result = relationalStore.withLeftState { state1 =>
//                val (result, state2) = recencyStore.allocPure(ctx, state1.asInstanceOf[recencyStore.State])
//                val (phys1, state3) = convertExpr.virtToPhysPure(e1, state2.asInstanceOf[relationalStore.State])
//                (result, relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Recent)), phys1, state3))
//              }
//              relationalStore.withRightState { state1 =>
//                val (result, state2) = recencyStore.allocPure(ctx, state1.asInstanceOf[recencyStore.State])
//                val (phys2, state3) = convertExpr.virtToPhysPure(e2, state2.asInstanceOf[relationalStore.State])
//                (result, relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Recent)), phys2, state3))
//              }
//              Unchanged(ApronExpr.Addr(result, joinedSpecials, joinedType))
//            } else {
//              val result = relationalStore.withInternalState { state1 =>
//                val (result, state2) = recencyStore.allocPure(ctx, state1.asInstanceOf[recencyStore.State])
//                val (phys1, state3) = convertExpr.virtToPhysPure(e1, state2.asInstanceOf[relationalStore.State].clone())
//                val state4 = relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Recent)), phys1, state3)
//                val (phys2, state5) = convertExpr.virtToPhysPure(e2, state2.asInstanceOf[relationalStore.State])
//                val state6 = relationalStore.writePure(PowersetAddr(PhysicalAddress(ctx, Recency.Recent)), phys2, state5)
//                (result, if (widen) relationalStore.widen(state5, state6).get else relationalStore.join(state5, state6).get)
//              }
//              Unchanged(ApronExpr.Addr(result, joinedSpecials, joinedType))
//            }
//          }
        }
      }

  override def joinBoolExpr: Join[ApronBool[VirtualAddress[Ctx], Type]] = combineBoolExpr(widen = false, combineExpressionAllocator)
  override def widenBoolExpr: Widen[ApronBool[VirtualAddress[Ctx], Type]] = combineBoolExpr(widen = true, combineExpressionAllocator)

  private def combineBoolExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])]): Combine[ApronBool[VirtualAddress[Ctx], Type], W] = {
    case (e1,e2) if e1 eq e2 => Unchanged(e1)
    case (ApronBool.Constant(b1), ApronBool.Constant(b2)) =>
      for {
        b <- Join(b1, b2)
      } yield (ApronBool.Constant(b))
    case (ApronBool.Constraint(ApronCons(op1, l1, r1)), ApronBool.Constraint(ApronCons(op2, l2, r2))) if op1 == op2 =>
      for {
        l <- combineExpr(widen, allocator)(l1, l2)
        r <- combineExpr(widen, allocator)(r1, r2)
      } yield (ApronBool.Constraint(ApronCons(op1, l, r)))
    case (ApronBool.And(l1, r1), ApronBool.And(l2, r2)) if structurallyJoinable(l1, l2) && structurallyJoinable(r1, r2) =>
      for {
        l <- combineBoolExpr(widen, allocator)(l1, l2)
        r <- combineBoolExpr(widen, allocator)(r1, r2)
      } yield (ApronBool.And(l,r))
    case (ApronBool.Or(l1, r1), ApronBool.Or(l2, r2)) if structurallyJoinable(l1, l2) && structurallyJoinable(r1, r2) =>
      for {
        l <- combineBoolExpr(widen, allocator)(l1, l2)
        r <- combineBoolExpr(widen, allocator)(r1, r2)
      } yield (ApronBool.Or(l, r))
    case (e1, e2) =>
      for {
        b <- Join(assert(e1)(using ResolveState.Left), assert(e2)(using ResolveState.Right))
      } yield (ApronBool.Constant(b))
  }

  def joinAddrsInto(addrs: PowVirtualAddress[Ctx], into: VirtualAddress[Ctx]): Unit =
    relationalStore.copy(new PowersetAddr(addrs.physicalAddresses), into.physical)

  override def makeNonRelational(virtualAddress: VirtualAddress[Ctx])(using ResolveState): Unit =
    relationalStore.moveToNonRelationalStore(virtualAddress.physical)

  private def containsFailedAddrs(expr: ApronExpr[VirtualAddress[Ctx], Type])(using ResolveState): Boolean =
    expr.addrs.exists(virt => recencyStore.addressTranslation.recency(virt.ctx, virt.n, getResolveState) == PowRecency.Failed)

  private def structurallyEq(e1: ApronExpr[VirtualAddress[Ctx], Type], e2: ApronExpr[VirtualAddress[Ctx], Type]): Boolean =
    (e1, e2) match
      case (ApronExpr.Addr(v1, specials1, tpe1), ApronExpr.Addr(v2, specials2, tpe2)) => v1.ctx == v2.ctx && tpe1 == tpe2
      case (ApronExpr.Constant(_, _, tpe1), ApronExpr.Constant(_, _, tpe2)) => tpe1 == tpe2
      case (ApronExpr.Unary(op1, e1, rt1, rd1, _, tpe1), ApronExpr.Unary(op2, e2, rt2, rd2, _, tpe2)) =>
        op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2
      case (ApronExpr.Binary(op1, l1, r1, rt1, rd1, _, tpe1), ApronExpr.Binary(op2, l2, r2, rt2, rd2, _, tpe2)) =>
        op1 == op2 && rt1 == rt2 && rd1 == rd2 && tpe1 == tpe2 && structurallyEq(r1, r2) && structurallyEq(l1, l2)
      case (_,_) => false

  private def structurallyJoinable(e1: ApronExpr[VirtualAddress[Ctx], Type], e2: ApronExpr[VirtualAddress[Ctx], Type]): Boolean = (e1, e2) match {
    case _ if(e1.isConstant && e2.isConstant || structurallyEq(e1, e2)) => true
//    case (_: (ApronExpr.Addr[VirtualAddress[Ctx], Type] | ApronExpr.Constant[VirtualAddress[Ctx], Type]), ApronExpr.Binary(op2, l2, r2, rt2, rd2, _, tpe2)) if(op2 == BinOp.Add || op2 == BinOp.Sub || op2 == BinOp.Mul) =>
//      e1._type == tpe2 && (structurallyJoinable(e1, l2) || structurallyJoinable(e1, r2))
//    case (ApronExpr.Binary(op1, l1, r1, rt1, rd1, _, tpe1), _: (ApronExpr.Addr[VirtualAddress[Ctx], Type] | ApronExpr.Constant[VirtualAddress[Ctx], Type])) if(op1 == BinOp.Add || op1 == BinOp.Sub || op1 == BinOp.Mul) =>
//      tpe1 == e2._type && (structurallyJoinable(l1, e2) || structurallyJoinable(r1, e2))
    case _ => false
  }

  private def structurallyEq(e1: ApronBool[VirtualAddress[Ctx], Type], e2: ApronBool[VirtualAddress[Ctx], Type]): Boolean = (e1, e2) match {
    case (ApronBool.Constraint(ApronCons(op1, _, _)), ApronBool.Constraint(ApronCons(op2, _, _))) => op1 == op2
    case (ApronBool.And(l1, r1), ApronBool.And(l2, r2)) => structurallyEq(l1, l2) && structurallyEq(r1, r2)
    case (ApronBool.Or(l1, r1), ApronBool.Or(l2, r2)) => structurallyEq(l1, l2) && structurallyEq(r1, r2)
    case (ApronBool.Constant(_), ApronBool.Constant(_)) => true
    case _ => false
  }

  private inline def structurallyJoinable(e1: ApronBool[VirtualAddress[Ctx], Type], e2: ApronBool[VirtualAddress[Ctx], Type]): Boolean =
    structurallyEq(e1, e2)

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

  def toStringWithRenaming(renaming: Map[String, String]): String =
    var result = relationalStore.toString
    for((from,to) <- renaming) {
      result = result.replaceAll(from, to)
    }
    result

class NonRelationalApronState[Ctx: Ordering, Type: ApronType: Join: Widen, Val: Join: Widen]
  (
    temporaryVariableAllocator: Allocator[Ctx, Type],
    combineExpressionAllocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])],
    recencyStore: RecencyStore[Ctx, PowVirtualAddress[Ctx], Val],
    relationalStore: RelationalStore[Ctx, Type, PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]], Val]
  )(using
    StatelessRelationalExpr[Val, VirtualAddress[Ctx], Type]
  )
  extends ApronRecencyState[Ctx, Type, Val](temporaryVariableAllocator, combineExpressionAllocator, recencyStore, relationalStore):
  
  override def combineExpr[W <: Widening](widen: Boolean, allocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type], ApronExpr[VirtualAddress[Ctx],Type])]): Combine[ApronExpr[VirtualAddress[Ctx], Type], W] =
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