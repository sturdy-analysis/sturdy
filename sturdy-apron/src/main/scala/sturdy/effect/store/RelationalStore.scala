
package sturdy.effect.store

import apron.*
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.{*, given}
import sturdy.util.Profiler
import sturdy.values.floating.{*, given}
import sturdy.values.integer.given
import sturdy.values.references.{*, given}
import sturdy.values.{Topped, *, given}

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
  (val manager: Manager,
   initialState: Abstract1,
   initialMetaData: Map[PhysicalAddress[Context], (FloatSpecials,Type)])
  (using
    relationalValue: RelationalExpr[Val, PhysicalAddress[Context], Type]
  )
  extends StoreWithImmutableOps[PowAddr, Val, WithJoin]:

  type MetaData = Map[PhysicalAddress[Context], (FloatSpecials, Type)]

  private var _abstract1: Abstract1 = initialState
  val nonRelationalStore: AStoreThreaded[PhysicalAddress[Context], PowAddr, Val] = AStoreThreaded(Map())
  private var _leftJoin: State = null
  private var _rightJoin: State = null

  inline def abstract1: Abstract1 = _abstract1
  inline def leftJoin: State =
    Option(_leftJoin).getOrElse(RelationalStoreState(_abstract1, nonRelationalStore.getState))
  inline def rightJoin: State =
    Option(_rightJoin).getOrElse(RelationalStoreState(_abstract1, nonRelationalStore.getState))

  inline def getType(powAddr: PowAddr, state: State = getStateNoCopy): JOptionA[Type] =
    getMetaData(powAddr, state).map(_._2)

  inline def getMetaData(phys: PhysicalAddress[Context], state: State): JOptionA[(FloatSpecials, Type)] =
    getMetaData(PowersetAddr(phys).asInstanceOf[PowAddr], state)

  inline def getMetaData(powAddr: PowAddr, state: State): JOptionA[(FloatSpecials, Type)] =
    for {
      value <- nonRelationalStore.read(powAddr, state.nonRelationalStoreState);
      expr <- JOptionA(relationalValue.getRelationalExpr(value))
    } yield((expr.floatSpecials, expr._type))

  override def read(powAddr: PowAddr, state: State): JOptionA[Val] =
    val v1 = getMetaData(powAddr, state).flatMap((floatSpecials, tpe) =>
      JOptionA.Some(powAddr.reduce(addr => relationalValue.makeRelationalExpr(ApronExpr.Addr(ApronVar(addr), floatSpecials, tpe))))
    )
    val v2 = nonRelationalStore.read(powAddr, state.nonRelationalStoreState)
    Join(v1,v2).get

  override def write(powAddr: PowAddr, v: Val, state: State): State =
    relationalValue.getRelationalExpr(v) match
      case Some(exp) =>
        write(powAddr, replaceMissingAddrs(exp, state), state)
      case None =>
        state.withNonRelationalState{ st =>
          nonRelationalStore.write(powAddr, v, st)
        }

  def write(powAddr: PowAddr, physExpr: ApronExpr[PhysicalAddress[Context], Type], state0: State): State =
    var state = state0
    for(toAddr <- powAddr.iterator) {
      writeMetaData(toAddr, physExpr, state)
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

  private def writeMetaData(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type], state: State): State =
    state.withNonRelationalState(
      nonRelationalStore.write(PowersetAddr(addr).asInstanceOf[PowAddr],
        relationalValue.makeRelationalExpr(ApronExpr.floatConstant(ApronExpr.bottomInterval, expr.floatSpecials, expr._type)),
        _
      )
    )

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

  override def move(fromPow: PowAddr, toPow: PowAddr): Unit =
    if (fromPow.isStrong && fromPow.iterator.size == 1 && toPow.iterator.size == 1) {
      nonRelationalStore.move(fromPow, toPow)

      val from = fromPow.iterator.next()
      val to = toPow.iterator.next()

      val env = _abstract1.getEnvironment
      (env.hasVar(from), env.hasVar(to)) match
        case (true, true) =>
          _abstract1.fold(manager, Iterable(to,from).map[Var](ApronVar(_)).toArray)
        case (true, false) =>
          _abstract1.rename(manager, Array[Var](ApronVar(from)), Array[Var](ApronVar(to)))
        case (false, true) | (false, false) => // Nothing to do

    } else {
      copy(fromPow, toPow)
      free(fromPow)
    }


  /**
   * Computes an over-approximation of copying addresses from a source to a target.
   * `copy` has a worst-case complexity O(n * m) joins,
   * where n is the number of source addresses
   * and m is the number of target addresses.
   */
  override def copy(fromPow: PowAddr, toPow: PowAddr, state: State): State =
    nonRelationalStore.copy(fromPow, toPow)

    val env = _abstract1.getEnvironment
    val toSet = toPow.iterator.map(ApronVar(_)).toSet
    // remove `to` addresses, because they don't need to be copied
    val fromSet = fromPow.iterator.map(ApronVar(_)).toSet.diff(toSet)

    for (from <- fromSet; to <- toSet) {

      (env.hasVar(from), env.hasVar(to)) match
        case (true, true) =>
          val (fromSpecials, fromType) = getMetaData(from, state).toOption.get
          val assigned = _abstract1.assignCopy(manager, to, ApronExpr.Addr(from, fromSpecials, fromType).toIntern(env), null)
          _abstract1.join(manager, assigned)
        case (true, false) =>
          _abstract1.expand(manager, from, Array[Var](to))
        case (false, true) | (false, false) =>
          // Nothing to do
    }

  /**
   * Does not actually delete addresses from store.
   * This is important, because addresses may still be referenced from somewhere.
   * Instead, it moves addresses from the relational store into the non-relational store.
   * This reduces the size of the relational store, which improves performance.
   */
  override inline def free(powAddr: PowAddr, state: State): State =
    Profiler.addTime("RelationalStore.free") {
      moveToNonRelationalStore(powAddr)
    }

  def moveToNonRelationalStore(powAddr: PowAddr, state: State): State =
    val env = _abstract1.getEnvironment

    for(addr <- powAddr.iterator;
        (specials, tpe) <- getMetaData(addr).toOption) {

      // Create non-relational value from address
      val iv = getBound(ApronExpr.addr(addr, tpe), getStateNoCopy)
      val newVal = JOptionA.Some(relationalValue.makeRelationalExpr(ApronExpr.floatConstant(iv, specials, tpe)))

      // Join value into non-relational store.
      val paddr = PowersetAddr(addr).asInstanceOf[PowAddr]
      val oldVal = nonRelationalStore.read(paddr)
      val resVal = Join(oldVal, newVal).get.toOption.getOrElse(throw new IllegalStateException(s"no value to write at address $addr to the non-relational store"))
      nonRelationalStore.write(paddr, resVal)
      val res = nonRelationalStore.read(paddr)
    }

    // Remove addresses from relational abstract domain
    val addrArray = powAddr.iterator.map(ApronVar[PhysicalAddress[Context]](_)).filter(env.hasVar(_)).toArray[Var]
    _abstract1.forget(manager, addrArray, false)
    _abstract1.changeEnvironment(manager, env.remove(addrArray), false)

  def isUnconstrained(powAddr: PowAddr): Boolean =
    val env = _abstract1.getEnvironment
    powAddr.iterator.map(ApronVar(_)).forall(x =>
      env.hasVar(x) && _abstract1.isDimensionUnconstrained(manager, x)
    )

  private def moveUnconstrainedToNonRelationalStore(): Unit =
    val env = _abstract1.getEnvironment
    val unconstrainedVars = env
      .getVars
      .map{ case ApronVar(addr) => addr.asInstanceOf[PhysicalAddress[Any]] }
      .filter { phys => isUnconstrained(PowersetAddr(phys).asInstanceOf[PowAddr]) }
      .toSet
    moveToNonRelationalStore(PowersetAddr(unconstrainedVars).asInstanceOf[PowAddr])

  inline def optimize(): Unit =
    moveUnconstrainedToNonRelationalStore()

  private final class BottomFailure extends SturdyFailure

  def addConstraints(constraints: ApronCons[PhysicalAddress[Context], Type]*): Unit =

    val resolvedConstraints = constraints
      .map(cons => replaceMissingAddrs(cons, getStateNoCopy))
      .filter(cons =>
        // Comparing NaN to any other number always evaluates to false, e.g. 0 < NaN == false
        // Therefore, adding such constraints to the abstract domain would be unsound.
        // For example, x = 0; y = {1, NaN}; if(x < y) { ... } else { [x = 0, y = NaN] }
        // Adding the negated constraint 0 > {1, NaN} in the else branch causes the abstract domain
        // to become bottom, which is unsound, because x must be 0.
        if(cons.e1.floatSpecials != FloatSpecials.Bottom || cons.e2.floatSpecials != FloatSpecials.Bottom)
          abstract1.satisfy(manager, cons.toApron(_abstract1.getEnvironment))
        else
          true
      )

    val cons = resolvedConstraints.map(_.toApron(_abstract1.getEnvironment)).toArray[Tcons1]
    this._abstract1.meet(manager, cons)

    // Inequality constraints `x != y` are imprecise on polyhedra.
    // The workaround is to take the join `state[x < y] U state[x > y]` (https://github.com/antoinemine/apron/issues/37)
    val inequalityConstraints = resolvedConstraints.filter { case ApronCons(CompareOp.Neq, _, _) => true; case _ => false }
    if(inequalityConstraints.nonEmpty) {
      val state1 = this._abstract1.meetCopy(manager, inequalityConstraints.map{ case ApronCons(_, e1, e2) =>
        ApronCons(CompareOp.Lt, e1, e2).toApron(this._abstract1.getEnvironment)
      }.toArray[Tcons1])
      this._abstract1.meet(manager, inequalityConstraints.map{ case ApronCons(_, e1, e2) =>
        ApronCons(CompareOp.Gt, e1, e2).toApron(this._abstract1.getEnvironment)
      }.toArray[Tcons1])
      this._abstract1.join(manager, state1)
    }

    if (isBottom)
      throw new BottomFailure

  def isBottom: Boolean =
    this._abstract1.isBottom(manager)

  def satisfies(cons: ApronCons[PhysicalAddress[Context], Type]): Topped[Boolean] =
    val env = _abstract1.getEnvironment
    val resolvedCons = replaceMissingAddrs(cons, getStateNoCopy)
    if(_abstract1.satisfy(manager, resolvedCons.toApron(env)))
      Topped.Actual(true)
    else if(_abstract1.satisfy(manager, resolvedCons.negated.toApron(env)))
      Topped.Actual(false)
    else
      Topped.Top


  def getBound(expr: ApronExpr[PhysicalAddress[Context], Type], state: State = getStateNoCopy): Interval =
    val env = state.abs1.getEnvironment
    val expr1 = replaceMissingAddrs(expr, state)
    val addrs = expr1.addrs
    if(addrs.forall(env.hasVar(_)))
      state.abs1.getBound(state.abs1.getCreationManager, expr1.toIntern(env))
    else
      throw IllegalArgumentException(s"Expression $expr1 contains unbound variables ${addrs.filterNot(env.hasVar(_))}")

  def getFloatBound(expr: ApronExpr[PhysicalAddress[Context], Type], state: State = getStateNoCopy): sturdy.apron.FloatInterval =
    val iv = getBound(expr, state)
    new sturdy.apron.FloatInterval(iv.inf, iv.sup, expr.floatSpecials)


  private def replaceMissingAddrs(cons: ApronCons[PhysicalAddress[Context], Type], state: State): ApronCons[PhysicalAddress[Context], Type] =
    cons.mapExprs(expr => replaceMissingAddrs(expr, state))

  private def replaceMissingAddrs(expr: ApronExpr[PhysicalAddress[Context], Type], state: State): ApronExpr[PhysicalAddress[Context], Type] =
    expr.mapAddrSame((_var,specials, tpe) => replaceMissingAddrs(_var, specials, tpe, state))

  private def replaceMissingAddrs(_var: ApronVar[PhysicalAddress[Context]], specials: FloatSpecials, tpe: Type, state: State): ApronExpr[PhysicalAddress[Context], Type] =
    val env = state.abs1.getEnvironment
    val snapshotNonRelationalState = nonRelationalStore.getState
    try {
      nonRelationalStore.setState(state.nonRelationalStoreState)
      _var match
        case ApronVar(PhysicalAddress(ctx, Recency.Failed)) => ApronExpr.Constant(ApronExpr.topInterval, specials, tpe)
        case ApronVar(phys) =>
          nonRelationalStore.read(PowersetAddr(phys).asInstanceOf[PowAddr]).toOption match
            case Some(e) =>
              relationalValue.getRelationalExpr(e) match
                case Some(expr) =>
                  if(env.hasVar(_var)) {
                    if (expr.isBottom == Topped.Actual(true))
                      ApronExpr.Addr(_var, specials, tpe)
                    else if (expr.isTop == Topped.Actual(true))
                      expr
                    else
                      ApronExpr.Constant(
                        Join(abstract1.getBound(manager, _var), abstract1.getBound(manager, _var)).get, specials, tpe)
                  } else {
                    expr
                  }
                case None => ApronExpr.Addr(_var, specials, tpe)
            case None =>
              throw new IllegalArgumentException(s"No metadata for $phys store")
    } finally {
      nonRelationalStore.setState(snapshotNonRelationalState)
    }


  case class RelationalStoreState(abs1: Abstract1, nonRelationalStoreState: nonRelationalStore.State):
    override def equals(obj: Any): Boolean =
      obj match
        case RelationalStoreState(abs2, nonRel2) =>
          MapEquals(nonRelationalStoreState,nonRel2) && abs1.getEnvironment.isEqual(abs2.getEnvironment) && Profiler.addTime("Abstract1.equals") { abs1.isEqual(manager, abs2) }
        case _ =>
          false
    override def hashCode: Int =
      val abs1Hash = Profiler.addTime("Abstract1.hashCode") { abs1.hashCode(manager) }
      (abs1Hash, nonRelationalStoreState).hashCode()

    override def toString: String = s"RelationalStoreState($hashCode, ${abs1.getEnvironment}, $abs1, $nonRelationalStoreState)"

    def withNonRelationalState[A](f: nonRelationalStore.State => (A,nonRelationalStore.State)): (A, RelationalStoreState) =
      val (res, newNonRelationalStoreState) = f(nonRelationalStoreState)
      (res, copy(nonRelationalStoreState = newNonRelationalStoreState))

  override type State = RelationalStoreState

  // It is important to copy abstract1 when getting and setting a state, because
  // RelationalStore mutates abstract1
  override def getState: State =
    RelationalStoreState(copyAbstract1(_abstract1), nonRelationalStore.getState)

  def getStateNoCopy: State =
    RelationalStoreState(_abstract1, nonRelationalStore.getState)

  override def setState(s: State): Unit =
    // This ensures that old variables are never forgotten.
    setStateNonMonotonically(widen(s, getState).get)

  override def setStateNonMonotonically(s: State): Unit =
    _abstract1 = copyAbstract1(s.abs1)
    nonRelationalStore.setState(s.nonRelationalStoreState)

  def withState[A](state: State)(f: => A): A =
    val snapshotAbs1 = _abstract1
    val snapshotNonRelationalState = nonRelationalStore.getState
    val snapshotLeftJoin = _leftJoin
    val snapshotRightJoin = _rightJoin
    try {
      _abstract1 = state.abs1
      nonRelationalStore.setState(state.nonRelationalStoreState)
      f
    } finally {
      _abstract1 = snapshotAbs1
      nonRelationalStore.setState(snapshotNonRelationalState)
      _leftJoin = snapshotLeftJoin
      _rightJoin = snapshotRightJoin
    }

  override def withInternalState[A](f: State => (A,State)): A =
    val state1 = getStateNoCopy
    val (res, state2) = f(state1)
    _abstract1 = state2.abs1
    nonRelationalStore.setStateNonMonotonically(state2.nonRelationalStoreState)

  override def setBottom: Unit =
    _abstract1.meet(manager, Tcons1(_abstract1.getEnvironment, Tcons1.EQ, Texpr1CstNode(DoubleScalar(1))))
    nonRelationalStore.setBottom

  inline def copyAbstract1(abstract1: Abstract1): Abstract1 =
    Profiler.addTime("Abstract1.copy") {
      if(abstract1 == null)
        abstract1
      else
        new Abstract1(manager, abstract1)
    }

  override def join: Join[State] = (s1,s2) => combineRelationalStoreState[Unit,Widening.No](((), s1), ((), s2)).map(_._2)
  override def widen: Widen[State] = (s1,s2) => combineRelationalStoreState[Unit,Widening.Yes](((), s1), ((), s2)).map(_._2)
  override def joinClosingOver[A](using Join[A]): Join[(A, RelationalStoreState)] = combineRelationalStoreState
  override def widenClosingOver[A](using Widen[A]): Widen[(A, RelationalStoreState)] = combineRelationalStoreState

  def combineRelationalStoreState[A,W <: Widening](using combineA: Combine[A,W], combineTypeEnv: Combine[MetaData,W], combineAbs1: Combine[Abstract1,W], combineNonRelStore: Combine[nonRelationalStore.State,W]): Combine[(A,RelationalStoreState), W] =
    (s1: (A,RelationalStoreState), s2: (A,RelationalStoreState)) =>
      Profiler.addTime("RelationalStoreState.combine") {
        withState(getStateNoCopy) {
          _abstract1 = null
          _leftJoin = RelationalStoreState(copyAbstract1(s1._2.abs1), s1._2.nonRelationalStoreState)
          _rightJoin = RelationalStoreState(copyAbstract1(s2._2.abs1), s2._2.nonRelationalStoreState)

          // Joining A and the non-relational store can have side-effects on `this.nonRelationalStore`, `this.leftJoin`, and `this.rightJoin`.
          // To avoid loosing these updates, we join the non-relational store a second time.
          for {
            joinedA <- combineA(s1._1, s2._1)
            preJoinedNonRelStore <- combineNonRelStore(s1._2.nonRelationalStoreState, s2._2.nonRelationalStoreState)
            finalJoinedNonRelationalStore <- combineNonRelStore(preJoinedNonRelStore, this.nonRelationalStore.getState)
            joinedAbs1 <- combineAbs1(_leftJoin.abs1, _rightJoin.abs1)
          } yield((joinedA, RelationalStoreState(joinedAbs1, finalJoinedNonRelationalStore)))
        }
      }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new RelationalStoreComputationJoiner[A])
  private final class RelationalStoreComputationJoiner[A] extends ComputationJoiner[A]:
    val before = getState
    var afterFirst: State = _
    var afterSecond: State = _
    var snapshotLeftJoin: State = _
    var snapshotRightJoin: State = _

    override def inbetween(fFailed: Boolean): Unit =
      afterFirst = getState
      setStateNonMonotonically(before)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      setStateNonMonotonically(afterFirst)

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      afterSecond = getState
      snapshotLeftJoin = _leftJoin
      snapshotRightJoin = _rightJoin
      _leftJoin = afterFirst
      _rightJoin = afterSecond
      _abstract1 = null

    override def retainNone(): Unit = {}

    override def afterJoin(): Unit =
      if(afterFirst != null && afterSecond != null) {
        setStateNonMonotonically(join(_leftJoin, _rightJoin).get)
        _leftJoin = snapshotLeftJoin
        _rightJoin = snapshotRightJoin
      }

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    nonRelationalStore.addressIterator(valueIterator)

  override def toString: String =
    if(_abstract1 != null)
      _abstract1.toString
    else
      s"$leftJoin ⊔ $rightJoin"