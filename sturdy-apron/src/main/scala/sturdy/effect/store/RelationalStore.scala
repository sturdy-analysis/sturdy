package sturdy.effect.store

import apron.*
import sturdy.apron.{CompareOp, *, given}
import sturdy.data.{*, given}
import sturdy.effect.{*, given}
import sturdy.util.Profiler
import sturdy.values.floating.{*, given}
import sturdy.values.integer.given
import sturdy.values.references.{*, given}
import sturdy.values.{Topped, *, given}

import scala.annotation.tailrec
import scala.collection.immutable.BitSet
import scala.reflect.ClassTag

/**
Example on https://docs.google.com/document/d/1d-o3OSZRHowwXaXAtdW1cN2Day6gtpMqu0Pmk9Q2DuM/edit
 **/
final class RelationalStore
  [
    Context: Ordering: Finite,
    Type : ApronType : Join: Widen,
    PowAddr <: AbstractAddr[PhysicalAddress[Context]],
    Val : Join: Widen
  ]
  (initAddrTrans: Map[Context, RecencyRegion],
   initialAbs1: Abstract1,
   initialNonRelationalStore: Map[PhysicalAddress[Context], Val])
  (using
   manager: Manager,
   relationalValue: StatefullRelationalExprT[Val, PhysicalAddress[Context], Type, RelationalStoreState[Context, Val]]
  )
  extends StoreWithPureOps[PowAddr, Val, WithJoin] with AddressTranslation[Context]:

  val nonRelationalStore: AStoreThreaded[PhysicalAddress[Context], PowAddr, Val] = AStoreThreaded(initialNonRelationalStore)

  var _internalState: State = RelationalStoreState(AddressTranslationState(initAddrTrans), initialAbs1, initialNonRelationalStore)
  var _leftState: State = null
  var _rightState: State = null

  inline def getType(powAddr: PowAddr, state: State = _internalState): JOptionA[Type] =
    getMetaData(powAddr, state).map(_._2)

  inline def getMetaData(phys: PhysicalAddress[Context], state: State): JOptionA[(FloatSpecials, Type)] =
    getMetaData(PowersetAddr(phys).asInstanceOf[PowAddr], state)

  def getMetaData(powAddr: PowAddr, state: State): JOptionA[(FloatSpecials, Type)] =
    for {
      value <- nonRelationalStore.readPure(powAddr, state.nonRelationalStoreState)._1
      metaData <- JOptionA(relationalValue.getMetaData(value))
    } yield metaData

  override def readPure(powAddr: PowAddr, state0: State): (JOptionA[Val],State) = Profiler.addTime("RelationalStore.writePure") {

    // This avoids forgetting newly allocated virtual addresses when joining values
    val snapshotInternal = _internalState
    val snapshotLeft = _leftState
    val snapshotRight = _rightState
    try {
      _internalState = state0
      _leftState = null
      _rightState = null

      val v1 = getMetaData(powAddr, _internalState).flatMap((floatSpecials, tpe) =>
        JOptionA.Some(powAddr.reduce(addr => relationalValue.makeRelationalExpr(ApronExpr.Addr(ApronVar(addr), floatSpecials, tpe))))
      )
      val v2 = powAddr.reduce(addr =>
        JOptionA(_internalState.nonRelationalStoreState.get(addr))
      )

      val result = Join(v1, v2).get

      (result, _internalState)
    } finally {
      _internalState = snapshotInternal
      _leftState = snapshotLeft
      _rightState = snapshotRight
    }
  }

  override def writePure(powAddr: PowAddr, v: Val, state1: State): State =
    relationalValue.getRelationalExprPure(v, state1) match
      case (Some(exp), state2) =>
        writePurePrivate(powAddr, replaceMissingAddrs(exp, state2), state2)
      case (None, _) =>
        state1.modifyNonRelationalState{ st =>
          nonRelationalStore.writePure(powAddr, v, st)
        }

  def writePure(powAddr: PowAddr, physExpr: ApronExpr[PhysicalAddress[Context], Type], state0: State): State =
    writePurePrivate(powAddr, replaceMissingAddrs(physExpr, state0), state0)

  private def writePurePrivate(powAddr: PowAddr, physExpr: ApronExpr[PhysicalAddress[Context], Type], state0: State): State = Profiler.addTime("RelationalStore.writePure") {
    var state = state0
    for (toAddr <- powAddr.iterator) {
      state = writeMetaData(toAddr, physExpr, state)
      val to = ApronVar(toAddr)

      toAddr.recency match
        case Recency.Recent =>
          val hasVariable = extendAbstract1(toAddr, physExpr, state)
          if (hasVariable) {
            state.abs1.assign(manager, to, physExpr.toIntern(state.abs1.getEnvironment), null)
          }
        case Recency.Old =>
          val envBefore = state.abs1.getEnvironment
          val hasVariable = extendAbstract1(toAddr, physExpr, state)
          if (!envBefore.hasVar(to)) {
            if (hasVariable)
              state.abs1.assign(manager, to, physExpr.toIntern(state.abs1.getEnvironment), null)
          } else {
            if (hasVariable) {
              val assigned = state.abs1.assignCopy(manager, to, physExpr.toIntern(state.abs1.getEnvironment), null)
              Profiler.addTime("Abstract1.combine") {
                state.abs1.join(manager, assigned)
              }
            }
          }
        case Recency.Failed => throw new IllegalArgumentException("Cannot assign to physical address on failed branch")
    }
    state
  }

  private def writeMetaData(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type], state1: State): State =
    val (v, state2) = relationalValue.makeRelationalExprPure(ApronExpr.floatConstant(ApronExpr.bottomInterval, expr.floatSpecials, expr._type), state1)
    state2.modifyNonRelationalState(nonRelationalStore.writePure(PowersetAddr(addr).asInstanceOf[PowAddr], v, _))

  private def extendAbstract1(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type], state: State): Boolean =
    if(getBound(expr, state).isBottom) {
      // Don't extend the environment in case the assigned expression is bottom.
      // Such a case happens in case of floating-point special values.
      // Assigning a bottom expression would cause _abstract1 to become bottom, which is unsound.
      false
    } else {
      val variable = ApronVar(addr)
      val tpe = expr._type
      var env = state.abs1.getEnvironment
      if (!env.hasVar(variable)) {
        tpe.apronRepresentation match
          case ApronRepresentation.Int =>
            env = env.add(Array[apron.Var](variable), Array[apron.Var]())
          case ApronRepresentation.Real =>
            env = env.add(Array[apron.Var](), Array[apron.Var](variable))
        state.abs1.changeEnvironment(manager, env, false)
      }
      true
    }

  override def movePure(fromPow: PowAddr, toPow: PowAddr, state0: State): State = Profiler.addTime("RelationalStore.movePure") {
    var state = state0
    if (fromPow.isStrong && fromPow.iterator.size == 1 && toPow.iterator.size == 1) {

      val from = fromPow.iterator.next()
      val to = toPow.iterator.next()

      // This avoids forgetting addresses that get allocated when values in the non-relational store are joined during `movePure`.
      val snapshotInternal = _internalState
      val snapshotLeft = _leftState
      val snapshotRight = _rightState
      try {
        _internalState = state
        _leftState = null
        _rightState = null

        _internalState.nonRelationalStoreState.get(from).foreach { value =>
          val joined = _internalState.nonRelationalStoreState.get(to).map(Join(_, value).get).getOrElse(value)
          _internalState = _internalState.copy(nonRelationalStoreState = _internalState.nonRelationalStoreState - from + (to -> joined))
        }

      } finally {
        state = _internalState
        _internalState = snapshotInternal
        _leftState = snapshotLeft
        _rightState = snapshotRight
      }

      val env = state.abs1.getEnvironment
      (env.hasVar(from), env.hasVar(to)) match
        case (true, true) =>
          state.abs1.fold(manager, Iterable(to,from).map[Var](ApronVar(_)).toArray)
        case (true, false) =>
          state.abs1.rename(manager, Array[Var](ApronVar(from)), Array[Var](ApronVar(to)))
        case (false, true) | (false, false) => // Nothing to do

    } else {
      state = copyPure(fromPow, toPow, state)
      state = freePure(fromPow, state)
    }
    state
  }


  /**
   * Computes an over-approximation of copying addresses from a source to a target.
   * `copy` has a worst-case complexity O(n * m) joins,
   * where n is the number of source addresses
   * and m is the number of target addresses.
   */
  override def copyPure(fromPow: PowAddr, toPow: PowAddr, state0: State): State = Profiler.addTime("RelationalStore.copyPure") {
    var state = state0

    // If `copyPure` is called within a join, virtual addresses are resolved in
    // `_leftState` and `_rightState`. This is wrong.
    // Instead, we resolve these addresses in `state` by setting _internalState.
    val snapshotInternal = _internalState
    val snapshotLeft = _leftState
    val snapshotRight = _rightState
    try {
      _internalState = state
      _leftState = null
      _rightState = null

      for (from <- fromPow.iterator; to <- toPow.iterator) {
        _internalState.nonRelationalStoreState.get(from).foreach { value =>
          val joined = _internalState.nonRelationalStoreState.get(to).map(Join(_, value).get).getOrElse(value)
          _internalState = _internalState.copy(nonRelationalStoreState = _internalState.nonRelationalStoreState + (to -> joined))
        }
      }

      state = _internalState
    } finally {
      _internalState = snapshotInternal
      _leftState = snapshotLeft
      _rightState = snapshotRight
    }

    val env = state.abs1.getEnvironment
    val toSet = toPow.iterator.map(ApronVar(_)).toSet
    // remove `to` addresses, because they don't need to be copied
    val fromSet = fromPow.iterator.map(ApronVar(_)).toSet.diff(toSet)

    for (from <- fromSet; to <- toSet) {

      (env.hasVar(from), env.hasVar(to)) match
        case (true, true) =>
          val (fromSpecials, fromType) = getMetaData(from, state).toOption.get
          val assigned = state.abs1.assignCopy(manager, to, ApronExpr.Addr(from, fromSpecials, fromType).toIntern(env), null)
          state.abs1.join(manager, assigned)
        case (true, false) =>
          state.abs1.expand(manager, from, Array[Var](to))
        case (false, true) | (false, false) =>
      // Nothing to do
    }
    state
  }


  /**
   * Does not actually delete addresses from store.
   * This is important, because addresses may still be referenced from somewhere.
   * Instead, it moves addresses from the relational store into the non-relational store.
   * This reduces the size of the relational store, which improves performance.
   */
  override inline def freePure(powAddr: PowAddr, state: State): State = Profiler.addTime("RelationalStore.free") {
    moveToNonRelationalStore(powAddr, state)
  }

  def moveToNonRelationalStore(powAddr: PowAddr): Unit =
    withInternalState(st => ((),moveToNonRelationalStore(powAddr, st)))

  def moveToNonRelationalStore(powAddr: PowAddr, state0: State): State = Profiler.addTime("RelationalStore.moveToNonRelationalStore") {
    var state = state0
    for (addr <- powAddr.iterator;
         (specials, tpe) <- getMetaData(addr, state).toOption) {

      // Create non-relational value from address
      val iv = getBound(ApronExpr.addr(addr, tpe), state)
      val (newVal, state1) = relationalValue.makeRelationalExprPure(ApronExpr.floatConstant(iv, specials, tpe), state); state = state1

      // Join value into non-relational store.
      val paddr = PowersetAddr(addr).asInstanceOf[PowAddr]
      val oldVal = nonRelationalStore.readPure(paddr, state.nonRelationalStoreState)._1
      val resVal = Join(oldVal, JOptionA.Some(newVal)).get.toOption.get
      state = state.modifyNonRelationalState(st => nonRelationalStore.writePure(paddr, resVal, st))
      val res = nonRelationalStore.read(paddr)
    }

    // Remove addresses from relational abstract domain
    val env = state.abs1.getEnvironment
    val addrArray = powAddr.iterator.map(ApronVar[PhysicalAddress[Context]](_)).filter(env.hasVar(_)).toArray[Var]
    state.abs1.forget(manager, addrArray, false)
    state.abs1.changeEnvironment(manager, env.remove(addrArray), false)
    state
  }

  private def moveUnconstrainedToNonRelationalStore(state: State): State =
    val env = state.abs1.getEnvironment
    val unconstrainedVars = env
      .getVars
      .map { case ApronVar(addr) => addr.asInstanceOf[PhysicalAddress[Any]] }
      .filter { phys => isUnconstrained(PowersetAddr(phys).asInstanceOf[PowAddr], state) }
      .toSet
    moveToNonRelationalStore(PowersetAddr(unconstrainedVars).asInstanceOf[PowAddr], state)

  inline def optimize(state: State): State = Profiler.addTime("RelationalStore.optimize") {
    moveUnconstrainedToNonRelationalStore(state)
  }

  /**
   * An address `x` is unconstrained if any of the following hold:
   *   - `x` has recency Failed
   *   - `x` has an unconstrained value in the non-relational store
   *   - `x` is in the relational abstract domain and its dimension is unconstrained
   */
  def isUnconstrained(powAddr: PowAddr, state0: State = _internalState): Boolean = Profiler.addTime("RelationalStore.isUnconstrained") {
    var state = state0
    powAddr.iterator.forall(x =>
      if(x.recency == Recency.Failed) {
        true
      } else {
        val nonRelVal = nonRelationalStore.readPure(PowersetAddr(x).asInstanceOf[PowAddr], state.nonRelationalStoreState)._1.toOption.getOrElse {
          throw Error(s"No metadata for physical address $x")
        }
        val (nonRelExpr,state1) = relationalValue.getRelationalExprPure(nonRelVal, state)
        state = state1
        val nonRelationalExprUnconstrained = nonRelExpr match {
          case Some(expr) => expr._type.signedTop.cmp(getBound(expr, state)) < 0
          case None => false
        }
        if(nonRelationalExprUnconstrained)
          true
        else
          state.abs1.getEnvironment.hasVar(ApronVar(x)) && state.abs1.isDimensionUnconstrained(manager, ApronVar(x))
      }
    )
  }

  private final class BottomFailure extends SturdyFailure

  def addConstraints(constraints: ApronCons[PhysicalAddress[Context], Type]*): Unit =
    _internalState = addConstraintsPure(_internalState, constraints*)

  def addConstraintsPure(state: State, constraints: ApronCons[PhysicalAddress[Context], Type]*): State = Profiler.addTime("RelationalStore.addConstraintsPure") {
    val resolvedConstraints = constraints
      .map(cons => replaceMissingAddrs(cons, state))
      .filter(cons =>
        // Don't add constraints that have old variables in them.
        // Constraining old variables changes their value non-monotonically, which is unsound.
        if (cons.addrs.exists(physAddr => physAddr.recency == Recency.Old))
          false
        // Comparing NaN to any other number always evaluates to false, e.g. 0 < NaN == false
        // Therefore, adding such constraints to the abstract domain would be unsound.
        // For example, x = 0; y = {1, NaN}; if(x < y) { ... } else { [x = 0, y = NaN] }
        // Adding the negated constraint 0 > {1, NaN} in the else branch causes the abstract domain
        // to become bottom, which is unsound, because x must be 0.
        else if (cons.e1.floatSpecials != FloatSpecials.Bottom || cons.e2.floatSpecials != FloatSpecials.Bottom)
          state.abs1.satisfy(manager, cons.toApron(state.abs1.getEnvironment))
        else
          true
      )

    val cons = resolvedConstraints.map(_.toApron(state.abs1.getEnvironment)).toArray[Tcons1]
    state.abs1.meet(manager, cons)

    // Inequality constraints `x != y` are imprecise on polyhedra.
    // The workaround is to take the join `state[x < y] U state[x > y]` (https://github.com/antoinemine/apron/issues/37)
    val inequalityConstraints = resolvedConstraints.filter { case ApronCons(CompareOp.Neq, _, _) => true; case _ => false }
    if (inequalityConstraints.nonEmpty) {
      val state1 = state.abs1.meetCopy(manager, inequalityConstraints.map { case ApronCons(_, e1, e2) =>
        ApronCons(CompareOp.Lt, e1, e2).toApron(state.abs1.getEnvironment)
      }.toArray[Tcons1])
      state.abs1.meet(manager, inequalityConstraints.map { case ApronCons(_, e1, e2) =>
        ApronCons(CompareOp.Gt, e1, e2).toApron(state.abs1.getEnvironment)
      }.toArray[Tcons1])
      state.abs1.join(manager, state1)
    }

    if (isBottom(state))
      throw new BottomFailure
    else
      state
  }

  def isBottom(state: State): Boolean =
    state.abs1.isBottom(manager)

  def satisfies(cons: ApronCons[PhysicalAddress[Context], Type], state: State = _internalState): Topped[Boolean] = Profiler.addTime("RelationalStore.satisfies") {
    if (!cons.e1.floatSpecials.isBottom || !cons.e2.floatSpecials.isBottom)
      Topped.Top
    else {
      val resolvedCons = replaceMissingAddrs(cons, state)
      if(state.abs1.satisfy(manager, resolvedCons.toApron(state.abs1.getEnvironment)))
        Topped.Actual(true)
      else if(manager.getFlagExactWanted(Manager.FUNID_SAT_LINCONS) && manager.wasExact())
        Topped.Actual(false)
      else
        Topped.Top
    }
  }

  def assert(cons: ApronCons[PhysicalAddress[Context], Type], state: State = _internalState): Topped[Boolean] = Profiler.addTime("RelationalStore.assert") {
    val resolvedCons = replaceMissingAddrs(cons, state)
    val iv1 = getFloatBound(resolvedCons.e1, state)
    val iv2 = getFloatBound(resolvedCons.e2, state)
    assert(resolvedCons.op, iv1, iv2) match
      case res: Topped.Actual[Boolean] => res
      case _: Topped.Top.type =>
        if (!cons.e1.floatSpecials.isBottom || !cons.e2.floatSpecials.isBottom)
          Topped.Top
        else {
          if (state.abs1.meetCopy(manager, resolvedCons.toApron(state.abs1.getEnvironment)).isBottom(manager))
            Topped.Actual(false)
          else if (state.abs1.meetCopy(manager, resolvedCons.negated.toApron(state.abs1.getEnvironment)).isBottom(manager))
            Topped.Actual(true)
          else
            Topped.Top
        }
  }

  @tailrec
  private def assert(op: CompareOp, iv1: sturdy.apron.FloatInterval, iv2: sturdy.apron.FloatInterval): Topped[Boolean] =
    op match
      case CompareOp.Eq =>
        if (!iv1.floatSpecials.nan && !iv2.floatSpecials.nan && iv1.isScalar && iv2.isScalar && iv1.isEqual(iv2))
          Topped.Actual(true)
        else if (iv1.isExactlyNaN || iv2.isExactlyNaN || iv1.sup().cmp(iv2.inf()) < 0 || iv2.sup().cmp(iv1.inf()) < 0)
          Topped.Actual(false)
        else
          Topped.Top

      case CompareOp.Neq =>
        if (iv1.isExactlyNaN || iv2.isExactlyNaN || iv1.sup().cmp(iv2.inf()) < 0 || iv2.sup().cmp(iv1.inf()) < 0)
          Topped.Actual(true)
        else if (!iv1.floatSpecials.nan && !iv2.floatSpecials.nan && (iv1.isScalar && iv2.isScalar && iv1.isEqual(iv2)))
          Topped.Actual(false)
        else
          Topped.Top

      case CompareOp.Le =>
        if (!iv1.floatSpecials.nan && !iv2.floatSpecials.nan && iv1.sup().cmp(iv2.inf()) <= 0)
          Topped.Actual(true)
        else if (iv1.isExactlyNaN || iv2.isExactlyNaN || iv2.sup().cmp(iv1.inf()) < 0)
          Topped.Actual(false)
        else
          Topped.Top

      case CompareOp.Lt =>
        if (!iv1.floatSpecials.nan && !iv2.floatSpecials.nan && iv1.sup().cmp(iv2.inf()) < 0)
          Topped.Actual(true)
        else if (iv1.isExactlyNaN || iv2.isExactlyNaN || iv2.sup().cmp(iv1.inf()) <= 0)
          Topped.Actual(false)
        else
          Topped.Top

      case CompareOp.Ge => assert(CompareOp.Le, iv2, iv1)
      case CompareOp.Gt => assert(CompareOp.Lt, iv2, iv1)

  def getBound(expr: ApronExpr[PhysicalAddress[Context], Type], state: State = _internalState): Interval = Profiler.addTime("RelationalStore.getBound") {
    val env = state.abs1.getEnvironment
    val expr1 = replaceMissingAddrs(expr, state)
    state.abs1.getBound(state.abs1.getCreationManager, expr1.toIntern(env))
  }

  def getFloatBound(expr: ApronExpr[PhysicalAddress[Context], Type], state: State = _internalState): sturdy.apron.FloatInterval =
    val iv = getBound(expr, state)
    new sturdy.apron.FloatInterval(iv.inf, iv.sup, expr.floatSpecials)


  private def replaceMissingAddrs(cons: ApronCons[PhysicalAddress[Context], Type], state: State): ApronCons[PhysicalAddress[Context], Type] =
    if(cons.isConstant)
      cons
    else
      cons.mapExprs(expr => replaceMissingAddrs(expr, state))

  private def replaceMissingAddrs(expr: ApronExpr[PhysicalAddress[Context], Type], state: State): ApronExpr[PhysicalAddress[Context], Type] =
    if(expr.isConstant)
      expr
    else
      expr.mapAddrSame((_var,specials, tpe) => replaceMissingAddrs(_var, specials, tpe, state))

  private def replaceMissingAddrs(_var: ApronVar[PhysicalAddress[Context]], specials: FloatSpecials, tpe: Type, state1: State): ApronExpr[PhysicalAddress[Context], Type] = Profiler.addTime("RelationalStore.replaceMissingAddrs") {
    _var match
      case ApronVar(PhysicalAddress(ctx, Recency.Failed)) => ApronExpr.Constant(ApronExpr.topInterval, specials, tpe)
      case ApronVar(phys) =>
        nonRelationalStore.readPure(PowersetAddr(phys).asInstanceOf[PowAddr], state1.nonRelationalStoreState)._1.toOption match
          case Some(e) =>
            val (optExp,state2) = relationalValue.getRelationalExprPure(e, state1)
            optExp match
              case Some(expr) =>
                if(state2.abs1.getEnvironment.hasVar(_var)) {
                  if (expr.isBottom == Topped.Actual(true))
                    ApronExpr.Addr(_var, specials, tpe)
                  else if (expr.isTop == Topped.Actual(true))
                    expr
                  else
                    ApronExpr.Constant(
                      Join(state2.abs1.getBound(manager, expr.toIntern(state2.abs1.getEnvironment)), state2.abs1.getBound(manager, _var)).get, specials, tpe)
                } else {
                  expr
                }
              case None =>
                if(state2.abs1.getEnvironment.hasVar(_var))
                  ApronExpr.Addr(_var, specials, tpe)
                else {
                  // This case happens if the non-relational store contains a non-bottom value
                  // not bound in the environment of the relational abstract domain.
                  ApronExpr.Constant(ApronExpr.bottomInterval, specials, tpe)
                }
          case None =>
            throw new IllegalArgumentException(s"No metadata for $phys store")
  }

  override type State = RelationalStoreState[Context, Val]

  // It is important to copy abstract1 when getting and setting a state, because
  // RelationalStore mutates abstract1
  override def getState: State =
    _internalState.copy(abs1 = copyAbstract1(_internalState.abs1))

  override def setState(olderState: State): Unit = setState(olderState, widening = true)
  def setState(olderState: State, widening: Boolean): Unit =

      Profiler.addTime("RelationalStore.setState") {
    // Prioritize recent variables from olderState, but do not forget about old variables in _internalState.
    // To do this, we remove all recent variables in olderState from _internalState and then widen _internalState with olderState.

    _internalState = _internalState.modifyNonRelationalState(nonRelationalStoreState =>
      nonRelationalStoreState.filter {
        case (phys@PhysicalAddress(_,Recency.Recent), _) => ! olderState.nonRelationalStoreState.contains(phys)
        case _ => true
      }
    )
    _internalState.abs1.changeEnvironment(manager, _internalState.abs1.getEnvironment.remove(_internalState.abs1.getEnvironment.getVars.filter{
      case v@ApronVar(PhysicalAddress(_,Recency.Recent)) => olderState.abs1.getEnvironment.hasVar(v)
      case _ => false
    }), false)

    // Then widen the `_internalState` into the `olderState`.
    _internalState =
      if(widening)
        widen(olderState, _internalState).get
      else
        join(olderState, _internalState).get

    assertVirtualAddressesIncludedIn(olderState.addressTranslationState, _internalState.addressTranslationState)
  }

  private def assertVirtualAddressesIncludedIn[A](addrTransSmaller: AddressTranslationState[Context], addrTransLarger: AddressTranslationState[Context]): Unit =
    for ((ctx, regionBefore) <- addrTransSmaller.mapping) {
      addrTransLarger.mapping.get(ctx) match
        case Some(regionAfter) =>
          if (!(regionBefore.recent ++ regionBefore.old ++ regionBefore.failed).subsetOf(regionAfter.recent ++ regionAfter.old ++ regionAfter.failed))
            throw Error(s"Address translation forgot virtual addresses: " +
              s"address translation before with [$ctx -> $regionBefore] " +
              s"is not a subset of address translation after with [$ctx -> $regionAfter]")
        case None =>
          throw Error(s"Address translation forgot virtual addresses: " +
            s"address translation before with [$ctx -> $regionBefore] " +
            s"is not a subset of address translation after without a region for $ctx")
    }

  override def setStateNonMonotonically(s: State): Unit =
    _internalState = s.copy(abs1 = copyAbstract1(s.abs1))

  override inline def nullState: State = null

  override inline def setInternalState(state: RelationalStoreState[Context, Val]): Unit =
    _internalState = state

  override inline def withInternalState[A](f: State => (A, State)): A =
    val (res, newInternalState) = f(internalState)
    _internalState = newInternalState
    res

  override inline def modifyInternalState(f: State => State): Unit =
    _internalState = f(internalState)

  override inline def internalStateOption: Option[State] = Option(_internalState)

  override inline def leftState: Option[State] = Option(_leftState)

  override inline def withLeftState[A](f: State => (A, State)): A =
    if (_leftState == null) {
      val (a, s) = f(_internalState)
      _internalState = s
      a
    } else {
      val (a, s) = f(_leftState)
      _leftState = s
      a
    }

  override inline def setLeftState(state: RelationalStoreState[Context, Val]): Unit =
    _leftState = state

  override inline def rightState: Option[State] = Option(_rightState)

  override inline def withRightState[A](f: State => (A, State)): A =
    if (_rightState == null) {
      val (a, s) = f(_internalState)
      _internalState = s
      a
    } else {
      val (a, s) = f(_rightState)
      _rightState = s
      a
    }

  override inline def setRightState(state: RelationalStoreState[Context, Val]): Unit =
    _rightState = state

  override def stateHasAddrTransState: HasAddressTranslationState[Context, State] = new HasAddressTranslationState[Context, State]:
    override def _with[A](state: State)(f: AddressTranslationState[Context] => (A, AddressTranslationState[Context])): (A, State) =
      val (a, newAddressTranslationState) = f(state.addressTranslationState)
      (a, state.copy(addressTranslationState = newAddressTranslationState))


  override def setBottom: Unit =
    _internalState.abs1.meet(manager, Tcons1(_internalState.abs1.getEnvironment, Tcons1.EQ, Texpr1CstNode(DoubleScalar(1))))
    nonRelationalStore.setBottom

  override def join: Join[State] = (s1,s2) => combineRelationalStoreState[Unit,Widening.No](((), s1), ((), s2)).map(_._2)
  override def widen: Widen[State] = (s1,s2) => combineRelationalStoreState[Unit,Widening.Yes](((), s1), ((), s2)).map(_._2)
  override def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = combineRelationalStoreState
  override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = combineRelationalStoreState

  def combineRelationalStoreState[A,W <: Widening](using combineA: Combine[A,W], combineAddrTrans: Combine[AddressTranslationState[Context], W], combineAbs1: Combine[Abstract1,W], combineNonRelStore: Combine[nonRelationalStore.State,W]): Combine[(A,State), W] =
    (s1: (A,State), s2: (A,State)) =>
      Profiler.addTime("RelationalStoreState.combine") {
        val snapshotCurrentState = _internalState
        val snapshotLeftJoin = _leftState
        val snapshotRightJoin = _rightState
        try {
          _internalState = null
          _leftState = s1._2.clone
          _rightState = s2._2.clone

          // Joining A and the non-relational store can have side-effects on `_leftState`, `_rightState`.
          // To avoid loosing these updates, we join the non-relational store a second time.
          val result = for {
            joinedA <- combineA(s1._1, s2._1)
            preJoinedNonRelStore <- combineNonRelStore(_leftState.nonRelationalStoreState, _rightState.nonRelationalStoreState)
            finalJoinedNonRelationalStore <- combineNonRelStore(_leftState.nonRelationalStoreState, _rightState.nonRelationalStoreState)
            joinedAddrTrans <- combineAddrTrans(_leftState.addressTranslationState, _rightState.addressTranslationState)
            joinedAbs1 <- combineAbs1(_leftState.abs1, _rightState.abs1)
            joinedState = RelationalStoreState(joinedAddrTrans, joinedAbs1, finalJoinedNonRelationalStore)
//              optimize(RelationalStoreState(joinedAddrTrans, joinedAbs1, finalJoinedNonRelationalStore))
          } yield (joinedA, joinedState)
          result
        } finally {
          _internalState = snapshotCurrentState
          _leftState = snapshotLeftJoin
          _rightState = snapshotRightJoin
        }
      }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new RelationalStoreComputationJoiner[A])
  private final class RelationalStoreComputationJoiner[A] extends ComputationJoiner[A]:
    private val before = getState
    private var afterFirst: State = _
    private var afterSecond: State = _
    private var snapshotLeftJoin: State = _
    private var snapshotRightJoin: State = _
    private var _retainBoth: Boolean = false

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = getState
      _internalState = before

    override def retainNone(): Unit =
      afterSecond = _internalState
      _internalState = _internalState.withAddressTranslationState(_ =>
        Join(before.addressTranslationState,
          Join(afterFirst.addressTranslationState.failedVirts(before.addressTranslationState),
            afterSecond.addressTranslationState.failedVirts(before.addressTranslationState)).get).get
      )

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      afterSecond = _internalState
      _internalState = afterFirst.withAddressTranslationState(_ =>
        Join(afterFirst.addressTranslationState, afterSecond.addressTranslationState.failedVirts(before.addressTranslationState)).get
      )

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      afterSecond = _internalState
      _internalState = afterSecond.withAddressTranslationState(_ =>
        Join(afterFirst.addressTranslationState.failedVirts(before.addressTranslationState), afterSecond.addressTranslationState).get
      )

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      afterSecond = getState
      snapshotLeftJoin = _leftState
      snapshotRightJoin = _rightState
      _leftState = afterFirst
      _rightState = afterSecond
      _internalState = null
      _retainBoth = true


    override def afterJoin(): Unit = {
      if(_retainBoth) {
        _internalState = join(_leftState, _rightState).get
        _leftState = snapshotLeftJoin
        _rightState = snapshotRightJoin
      }
      assertVirtualAddressesIncludedIn(before.addressTranslationState, _internalState.addressTranslationState)
    }

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    nonRelationalStore.addressIterator(valueIterator)

  override def toString: String =
    if(_internalState != null)
      _internalState.toString
    else
      s"$_leftState ⊔ $_rightState"

inline def copyAbstract1(abstract1: Abstract1): Abstract1 = Profiler.addTime("Abstract1.copy") {
  if (abstract1 == null)
    abstract1
  else
    new Abstract1(abstract1.getCreationManager, abstract1)
}

case class RelationalStoreState[Context,Value](addressTranslationState: AddressTranslationState[Context], abs1: Abstract1, nonRelationalStoreState: Map[PhysicalAddress[Context], Value]):
  override def equals(obj: Any): Boolean =  Profiler.addTime("RelationalStoreState.equals") {
    obj match
      case RelationalStoreState(addrTrans2, abs2, nonRel2) =>
        addressTranslationState == addrTrans2 &&
          MapEquals(nonRelationalStoreState, nonRel2.asInstanceOf[Map[PhysicalAddress[Context], Value]], _ == _) &&
          abs1.getEnvironment.isEqual(abs2.getEnvironment) &&
          abs1.isEqual(abs1.getCreationManager, abs2)
      case _ =>
        false
    }

  lazy val _hashCode = Profiler.addTime("RelationalStoreState.hashCode") {
    val abs1Hash = abs1.hashCode(abs1.getCreationManager)
    (abs1Hash, nonRelationalStoreState).hashCode()
  }
  override def hashCode: Int = _hashCode

  override def toString: String = s"RelationalStoreState($hashCode, $addressTranslationState, ${abs1.getEnvironment}, $abs1, $nonRelationalStoreState)"

  inline def withNonRelationalState[A](f: Map[PhysicalAddress[Context], Value] => (A,Map[PhysicalAddress[Context], Value])): (A,RelationalStoreState[Context,Value]) =
    val (res,newNonRelationalStoreState) = f(nonRelationalStoreState)
    (res,copy(nonRelationalStoreState = newNonRelationalStoreState))

  inline def modifyNonRelationalState(f: Map[PhysicalAddress[Context], Value] => Map[PhysicalAddress[Context], Value]): RelationalStoreState[Context, Value] =
    withNonRelationalState(st => ((),f(st)))._2

  inline def withAddressTranslationState(f: AddressTranslationState[Context] => AddressTranslationState[Context]) =
    val newAddressTranslationState = f(addressTranslationState)
    copy(addressTranslationState = newAddressTranslationState)

  override def clone(): RelationalStoreState[Context, Value] =
    RelationalStoreState(addressTranslationState, copyAbstract1(abs1), nonRelationalStoreState)