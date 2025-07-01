
package sturdy.effect.store

import apron.*
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.{*, given}
import sturdy.util.Profiler
import sturdy.values.floating.{*, given}
import sturdy.values.integer.given
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

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
  extends Store[PowAddr, Val, WithJoin]:

  type MetaData = Map[PhysicalAddress[Context], (FloatSpecials, Type)]

  private var _abstract1 : Abstract1 = initialState
  val nonRelationalStore: AStoreThreaded[PhysicalAddress[Context], PowAddr, Val] = AStoreThreaded(Map())

  inline def abstract1: Abstract1 = _abstract1

  inline def getType(powAddr: PowAddr): JOptionA[Type] =
    getMetaData(powAddr).map(_._2)

  inline def getMetaData(phys: PhysicalAddress[Context]): JOptionA[(FloatSpecials, Type)] =
    getMetaData(PowersetAddr(phys).asInstanceOf[PowAddr])

  inline def getMetaData(powAddr: PowAddr): JOptionA[(FloatSpecials, Type)] =
    for {
      value <- nonRelationalStore.read(powAddr);
      expr <- JOptionA(relationalValue.getRelationalExpr(value))
    } yield((expr.floatSpecials, expr._type))

  override def read(powAddr: PowAddr): JOptionA[Val] =
    val v1 = getMetaData(powAddr).flatMap((floatSpecials, tpe) =>
      JOptionA.Some(powAddr.reduce(addr => relationalValue.makeRelationalExpr(ApronExpr.Addr(ApronVar(addr), floatSpecials, tpe))))
    )
    val v2 = nonRelationalStore.read(powAddr)
    Join(v1,v2).get

  override def write(powAddr: PowAddr, v: Val): Unit =
    relationalValue.getRelationalExpr(v) match
      case Some(exp) => write(powAddr, replaceMissingAddrs(exp))
      case None => nonRelationalStore.write(powAddr, v)

  private def write(powAddr: PowAddr, physExpr: ApronExpr[PhysicalAddress[Context], Type]): Unit =
    for(toAddr <- powAddr.iterator) {
      writeMetaData(toAddr, physExpr)
      val to = ApronVar(toAddr)

      toAddr.recency match
        case Recency.Recent =>
          val hasVariable = extendAbstract1(toAddr, physExpr)
          if (hasVariable) {
            _abstract1.assign(manager, to, physExpr.toIntern(_abstract1.getEnvironment), null)
          }
        case Recency.Old =>
          val to = ApronVar(toAddr)
          writeMetaData(toAddr, physExpr)
          val envBefore = _abstract1.getEnvironment
          val hasVariable = extendAbstract1(toAddr, physExpr)
          if (!envBefore.hasVar(to)) {
            if (hasVariable)
              _abstract1.assign(manager, to, physExpr.toIntern(_abstract1.getEnvironment), null)
          } else {
            if (hasVariable) {
              val assigned = _abstract1.assignCopy(manager, to, physExpr.toIntern(_abstract1.getEnvironment), null)
              Profiler.addTime("Abstract1.combine") {
                _abstract1.join(manager, assigned)
              }
            }
          }
        case Recency.Failed => throw new IllegalArgumentException("Cannot assign to physical address on failed branch")
    }

  private def writeMetaData(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type]): Unit =
    nonRelationalStore.write(PowersetAddr(addr).asInstanceOf[PowAddr],
      relationalValue.makeRelationalExpr(ApronExpr.floatConstant(ApronExpr.bottomInterval, expr.floatSpecials, expr._type)))

  private def extendAbstract1(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type]): Boolean =
    if(getBound(expr).isBottom) {
      // Don't extend the environment in case the assigned expression is bottom.
      // Such a case happens in case of floating-point special values.
      // Assigning a bottom expression would cause _abstract1 to become bottom, which is unsound.
      false
    } else {
      val variable = ApronVar(addr)
      val tpe = expr._type
      var env = _abstract1.getEnvironment
      if (!env.hasVar(variable)) {
        tpe.apronRepresentation match
          case ApronRepresentation.Int =>
            env = env.add(Array[apron.Var](variable), Array[apron.Var]())
          case ApronRepresentation.Real =>
            env = env.add(Array[apron.Var](), Array[apron.Var](variable))
        _abstract1.changeEnvironment(manager, env, false)
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
  override def copy(fromPow: PowAddr, toPow: PowAddr): Unit =
    nonRelationalStore.copy(fromPow, toPow)

    val env = _abstract1.getEnvironment
    val toSet = toPow.iterator.map(ApronVar(_)).toSet
    // remove `to` addresses, because they don't need to be copied
    val fromSet = fromPow.iterator.map(ApronVar(_)).toSet.diff(toSet)

    for (from <- fromSet; to <- toSet) {

      (env.hasVar(from), env.hasVar(to)) match
        case (true, true) =>
          val (fromSpecials, fromType) = getMetaData(from).toOption.get
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
  override inline def free(powAddr: PowAddr): Unit =
    Profiler.addTime("RelationalStore.free") {
      moveToNonRelationalStore(powAddr)
    }

  def moveToNonRelationalStore(powAddr: PowAddr): Unit =
    val env = _abstract1.getEnvironment

    for (addr <- powAddr.iterator;
         (specials, tpe) <- getMetaData(addr).toOption.iterator;
         if (env.hasVar(ApronVar(addr)))) {

      // Create non-relational value from address
      val iv = getBound(ApronExpr.addr(addr, tpe))
      val newVal = JOptionA.Some(relationalValue.makeRelationalExpr(ApronExpr.floatConstant(iv, specials, tpe)))

      // Join value into non-relational store.
      val paddr = PowersetAddr(addr).asInstanceOf[PowAddr]
      val oldVal = nonRelationalStore.read(paddr)
      for (resVal <- Join(oldVal, newVal).get.toOption) {
        nonRelationalStore.write(paddr, resVal)
      }
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
    val unconstrained = env.getVars.map(_.addr.asInstanceOf[PhysicalAddress[Context]]).filter { phys =>
      isUnconstrained(PowersetAddr(phys).asInstanceOf[PowAddr])
    }.toSet
    moveToNonRelationalStore(PowersetAddr(unconstrained).asInstanceOf[PowAddr])

  inline def optimize(): Unit =
    moveUnconstrainedToNonRelationalStore()

  private final class BottomFailure extends SturdyFailure

  def addConstraints(constraints: ApronCons[PhysicalAddress[Context], Type]*): Unit =
    val resolvedConstraints = constraints.map(replaceMissingAddrs)

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
    val resolvedCons = replaceMissingAddrs(cons)
    if(_abstract1.satisfy(manager, resolvedCons.toApron(_abstract1.getEnvironment)))
      Topped.Actual(true)
    else if(_abstract1.satisfy(manager, resolvedCons.negated.toApron(_abstract1.getEnvironment)))
      Topped.Actual(false)
    else
      Topped.Top

  def getBound(expr: ApronExpr[PhysicalAddress[Context], Type]): Interval =
    val env = _abstract1.getEnvironment
    val expr1 = replaceMissingAddrs(expr)
    val addrs = expr1.addrs
    if(addrs.forall(env.hasVar(_)))
      _abstract1.getBound(_abstract1.getCreationManager, expr1.toIntern(_abstract1.getEnvironment))
    else
      throw IllegalArgumentException(s"Expression $expr1 contains unbound variables ${addrs.filterNot(env.hasVar(_))}")

  def getFloatBound(expr: ApronExpr[PhysicalAddress[Context], Type]): sturdy.apron.FloatInterval =
    val iv = getBound(expr)
    new sturdy.apron.FloatInterval(iv.inf, iv.sup, expr.floatSpecials)


  private def replaceMissingAddrs(cons: ApronCons[PhysicalAddress[Context], Type]): ApronCons[PhysicalAddress[Context], Type] =
    cons.mapExprs(replaceMissingAddrs)

  private def replaceMissingAddrs(expr: ApronExpr[PhysicalAddress[Context], Type]): ApronExpr[PhysicalAddress[Context], Type] =
    expr.mapAddrSame(replaceMissingAddrs)

  private def replaceMissingAddrs(_var: ApronVar[PhysicalAddress[Context]], specials: FloatSpecials, tpe: Type): ApronExpr[PhysicalAddress[Context], Type] =
    _var match
      case ApronVar(PhysicalAddress(ctx, Recency.Failed)) => ApronExpr.Constant(ApronExpr.topInterval, specials, tpe)
      case ApronVar(phys) if(!_abstract1.getEnvironment.hasVar(_var) && getMetaData(phys).toOption.isDefined) =>
        readApronExprFromNonRelationalStore(phys).get
      case _ => ApronExpr.Addr(_var, specials, tpe)

  private def readApronExprFromNonRelationalStore(phys: PhysicalAddress[Context]): Option[ApronExpr[PhysicalAddress[Context], Type]] =
    nonRelationalStore.read(PowersetAddr(phys).asInstanceOf[PowAddr]).toOption.flatMap(relationalValue.getRelationalExpr)

  case class RelationalStoreState(abs1: Abstract1, nonRelationalStoreState: nonRelationalStore.State):
    override def equals(obj: Any): Boolean =
      obj match
        case RelationalStoreState(abs2, nonRel2) =>
          MapEquals(nonRelationalStoreState,nonRel2) && Profiler.addTime("Abstract1.equals") { abs1.isEqual(manager, abs2) }
        case _ =>
          false
    override def hashCode: Int =
      val abs1Hash = Profiler.addTime("Abstract1.hashCode") { abs1.hashCode(manager) }
      (abs1Hash, nonRelationalStoreState).hashCode()

    override def toString: String = s"RelationalStoreState($hashCode, $abs1, $nonRelationalStoreState)"

  override type State = RelationalStoreState

  // It is important to copy abstract1 when getting and setting a state, because
  // RelationalStore mutates abstract1
  override def getState: State =
    RelationalStoreState(copyAbstract1(_abstract1), nonRelationalStore.getState)
  override def setState(s: State): Unit =
    _abstract1 = copyAbstract1(s.abs1)
    nonRelationalStore.setState(s.nonRelationalStoreState)

  override def setBottom: Unit =
    _abstract1 = Abstract1(manager, new Environment())
    nonRelationalStore.setBottom

  inline def copyAbstract1(abstract1: Abstract1): Abstract1 =
    Profiler.addTime("Abstract1.copy") {
      new Abstract1(manager, abstract1)
    }

  override def join: Join[State] = combineRelationalStoreState
  override def widen: Widen[State] = combineRelationalStoreState

  def combineRelationalStoreState[W <: Widening](using combineTypeEnv: Combine[MetaData,W], combineAbs1: Combine[Abstract1,W], combineNonRelStore: Combine[nonRelationalStore.State,W]): Combine[RelationalStoreState, W] =
    (s1: RelationalStoreState, s2: RelationalStoreState) =>
      Profiler.addTime("RelationalStoreState.combine") {
        val state = getState
        val snapshotAbs1 = state.abs1
        val snapshotNonRelStore = state.nonRelationalStoreState
        try {
          val joinedAbs1 = combineAbs1(s1.abs1, s2.abs1)
          _abstract1 = copyAbstract1(joinedAbs1.get)
          val joinedNonRelationalStore = combineNonRelStore(s1.nonRelationalStoreState, s2.nonRelationalStoreState)
          nonRelationalStore.setState(joinedNonRelationalStore.get)
//          optimize()
          MaybeChanged(
            RelationalStoreState(copyAbstract1(_abstract1), nonRelationalStore.getState),
            joinedAbs1.hasChanged || joinedNonRelationalStore.hasChanged
          )
        } finally {
          _abstract1 = snapshotAbs1
          nonRelationalStore.setState(snapshotNonRelStore)
        }
        }

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    nonRelationalStore.addressIterator(valueIterator)

  override def toString: String = _abstract1.toString