package sturdy.effect.store

import apron.*
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{*, given}
import sturdy.util.Profiler
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import sturdy.{IsSound, Soundness}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.mutable
import math.Ordered.orderingToOrdered
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
    relationalValue: RelationalValue[Val, PhysicalAddress[Context], Type]
  )
  extends Store[PowAddr, Val, WithJoin]:

  type MetaData = Map[PhysicalAddress[Context], (FloatSpecials, Type)]

  private var _abstract1 : Abstract1 = initialState
  private var metaData: MetaData = initialMetaData
  val nonRelationalStore: AStoreThreaded[PhysicalAddress[Context], PowAddr, Val] = AStoreThreaded(Map())

  inline def abstract1: Abstract1 = _abstract1

  inline def getType(powAddr: PowAddr): JOptionA[Type] =
    getMetaData(powAddr).map(_._2)

  inline def getMetaData(powAddr: PowAddr): JOptionA[(FloatSpecials, Type)] =
    powAddr.reduce(addr => JOptionA(metaData.get(addr)))

  override def read(powAddr: PowAddr): JOptionA[Val] =
    if(powAddr.isEmpty)
      JOptionA.None()
    else {
      val v1 = powAddr.reduce(addr =>
        metaData.get(addr) match
          case Some((floatSpecials, tpe)) =>
            JOptionA.Some(relationalValue.makeRelationalVal(ApronExpr.Addr(ApronVar(addr), floatSpecials, tpe)))
          case None =>
            JOptionA.None()
      )
      val v2 = nonRelationalStore.read(powAddr)
      (v1,v2) match
        case (JOptionA.None(), _) => v2
        case (_, JOptionA.None()) => v1
        case (_,_) => Join(v1,v2).get
    }

  override def write(powAddr: PowAddr, v: Val): Unit =
    relationalValue.getRelationalVal(v) match
      case Some(exp) => write(powAddr, exp)
      case None => nonRelationalStore.write(powAddr, v)

  private def write(powAddr: PowAddr, physExpr: ApronExpr[PhysicalAddress[Context], Type]): Unit =
    if (powAddr.isEmpty) {
      // nothing
    } else if (powAddr.isStrong) {
      for (toAddr <- powAddr.iterator) {
        writeMetaData(toAddr, physExpr)
        val to = ApronVar(toAddr)
        val env = _abstract1.getEnvironment
        if(env.hasVar(to))
          _abstract1.assign(manager, to, physExpr.toIntern(_abstract1.getEnvironment), null)
      }
    } else /* if(powAddr.isWeak) */ {
      // weak update implemented as join
      for (toAddr <- powAddr.iterator) {
        val to = ApronVar(toAddr)
        writeMetaData(toAddr, physExpr)
        val env = _abstract1.getEnvironment
        if(env.hasVar(to) && physExpr.addrs.forall(env.hasVar(_))) {
          val assigned = _abstract1.assignCopy(manager, to, physExpr.toIntern(_abstract1.getEnvironment), null)
          Profiler.addTime("Abstract1.combine") {
            _abstract1.join(manager, assigned)
          }
        }
      }
    }

  override def move(fromPow: PowAddr, toPow: PowAddr): Unit =
    nonRelationalStore.move(fromPow, toPow)

    if (fromPow.isStrong && fromPow.iterator.size == 1 && toPow.iterator.size == 1) {
      val from = fromPow.iterator.next()
      val to = toPow.iterator.next()

      (metaData.get(from), metaData.get(to)) match
        case (Some(fromMetaData), Some(toMetaData)) =>
          metaData += to -> Join(toMetaData, fromMetaData).get
          metaData -= from
        case (Some(fromMetaData), None) =>
          metaData += to -> fromMetaData
          metaData -= from
        case (None, Some(_)) | (None, None) => // Nothing to do

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
      (metaData.get(from.addr), metaData.get(to.addr)) match
        case (Some(fromMetaData@(fromSpecials,fromType)), Some(toMetaData@(toSpecials,toType))) =>
          if(fromType.apronRepresentation == toType.apronRepresentation) {
            metaData += to.addr -> Join(toMetaData, fromMetaData).get
          } else {
            throw new IllegalStateException(
              s"Cannot copy address ${from.addr} of type ${fromType} represented by ${fromType.apronRepresentation}" +
                s"to address ${to.addr} of type ${toType} represented by ${toType.apronRepresentation}")
          }

        case (Some(fromType), None) =>
          metaData += to.addr -> metaData(from.addr)

        case (None, Some(_)) | (None, None) =>
          // from is not bound. There is nothing to do.

      (env.hasVar(from), env.hasVar(to)) match
        case (true, true) =>
          val (fromSpecials, fromType) = metaData(from)
          val assigned = _abstract1.assignCopy(manager, to, ApronExpr.Addr(from, fromSpecials, fromType).toIntern(env), null)
          _abstract1.join(manager, assigned)
        case (true, false) =>
          _abstract1.expand(manager, from, Array[Var](to))
        case (false, true) | (false, false) =>
          // Nothing to do
    }

  override def free(powAddr: PowAddr): Unit =
    nonRelationalStore.free(powAddr)

    for(addr <- powAddr.iterator) {
      val dest = ApronVar(addr)
      val env = _abstract1.getEnvironment()
      metaData -= dest.addr
      if (env.hasVar(dest)) {
        _abstract1.forget(manager, dest, false)
        _abstract1.changeEnvironment(manager, env.remove(Array[Var](dest)), false)
      }
    }

  private final class BottomFailure extends SturdyFailure

  def addConstraints(constraints: ApronCons[PhysicalAddress[Context], Type]*): Unit =
    val cons = constraints.map(_.toApron(_abstract1.getEnvironment)).toArray[Tcons1]
    this._abstract1.meet(manager, cons)
    if (this._abstract1.isBottom(manager) && constraints.forall(cons => cons.e1.floatSpecials.isBottom && cons.e2.floatSpecials.isBottom))
      throw new BottomFailure

  def satisfies(constraints: ApronCons[PhysicalAddress[Context], Type]*): Boolean =
    constraints.forall(cons =>
      _abstract1.satisfy(manager, cons.toApron(_abstract1.getEnvironment))
    )

  def getBound(expr: ApronExpr[PhysicalAddress[Context], Type]): Interval =
    val env = _abstract1.getEnvironment
    val addrs = expr.addrs
    if(addrs.forall(env.hasVar(_)))
      _abstract1.getBound(_abstract1.getCreationManager, expr.toIntern(_abstract1.getEnvironment))
    else if(addrs.forall(metaData.contains(_)))
      ApronExpr.bottomInterval
    else
      throw IllegalArgumentException(s"Expression $expr contains unbound variables ${addrs.filterNot(metaData.contains(_))}")

  def getFloatBound(expr: ApronExpr[PhysicalAddress[Context], Type]): sturdy.apron.FloatInterval =
    val iv = getBound(expr)
    new sturdy.apron.FloatInterval(iv.inf, iv.sup, expr.floatSpecials)

  private def writeMetaData(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type]): Unit =
    val variable = ApronVar(addr)
    val tpe = expr._type
    val specials = expr.floatSpecials
    if (!metaData.contains(variable)) {
      metaData += addr -> (specials,tpe)
    } else {
      val addrMetaData@(addrSpecials,addrType) = metaData(addr)
      if (addrType.apronRepresentation == tpe.apronRepresentation) {
        if (addr.recency == Recency.Recent)
          metaData += addr -> addrMetaData
        else
          metaData += addr -> Join(addrMetaData, (specials,tpe)).get
      } else {
        throw new IllegalStateException(
          s"Cannot assign ${expr} of type ${tpe} represented by ${tpe.apronRepresentation} " +
            s"to address ${addr} of type ${addrType} represented by ${addrType.apronRepresentation}")
      }
    }

    var env = _abstract1.getEnvironment
    if(!env.hasVar(variable)) {
      tpe.apronRepresentation match
        case ApronRepresentation.Int =>
          env = env.add(Array[apron.Var](variable), Array[apron.Var]())
        case ApronRepresentation.Real =>
          val bounds = getBound(expr)
          if (bounds.isBottom && specials != FloatSpecials.Bottom) {
            // Don't add variable to environment
          } else {
            env = env.add(Array[apron.Var](), Array[apron.Var](variable))
          }
      _abstract1.changeEnvironment(manager, env, false)
    } else {
      if (addr.recency == Recency.Recent && tpe.apronRepresentation == ApronRepresentation.Real && specials != FloatSpecials.Bottom && getBound(expr).isBottom) {
        env = env.remove(Array[apron.Var](variable))
        _abstract1.changeEnvironment(manager, env, false)
      }
    }


  case class RelationalStoreState(metaData: MetaData, abs1: Abstract1, nonRelationalStoreState: nonRelationalStore.State):
    override def equals(obj: Any): Boolean =
      obj match
        case RelationalStoreState(tenv2, abs2, nonRel2) =>
          metaData.equals(tenv2) && MapEquals(nonRelationalStoreState,nonRel2) && Profiler.addTime("Abstract1.equals") { abs1.isEqual(manager, abs2) }
        case _ =>
          false
    override def hashCode: Int =
      val abs1Hash = Profiler.addTime("Abstract1.hashCode") { abs1.hashCode(manager) }
      (metaData, abs1Hash, nonRelationalStoreState).hashCode()

    override def toString: String = s"RelationalStoreState($hashCode, $metaData, $abs1, $nonRelationalStoreState)"

  override type State = RelationalStoreState

  // It is important to copy abstract1 when getting and setting a state, because
  // RelationalStore mutates abstract1
  override def getState: State =
    RelationalStoreState(metaData, copyAbstract1(_abstract1), nonRelationalStore.getState)
  override def setState(s: State): Unit =
    metaData = s.metaData
    _abstract1 = copyAbstract1(s.abs1)
    nonRelationalStore.setState(s.nonRelationalStoreState)

  inline def copyAbstract1(abstract1: Abstract1): Abstract1 =
    Profiler.addTime("Abstract1.copy") {
      new Abstract1(manager, abstract1)
    }

  override def join: Join[State] = combineRelationalStoreState
  override def widen: Widen[State] = combineRelationalStoreState

  def combineRelationalStoreState[W <: Widening](using combineTypeEnv: Combine[MetaData,W], combineAbs1: Combine[Abstract1,W], combineNonRelStore: Combine[nonRelationalStore.State,W]): Combine[RelationalStoreState, W] =
    (s1: RelationalStoreState, s2: RelationalStoreState) =>
      val state = getState
      val snapshotTypeEnv = state.metaData
      val snapshotAbs1 = state.abs1
      try {
        val joinedTypeEnv = combineTypeEnv(s1.metaData, s2.metaData)
        val joinedAbs1 = combineAbs1(s1.abs1, s2.abs1)
        metaData = joinedTypeEnv.get
        _abstract1 = copyAbstract1(joinedAbs1.get)
        val joinedNonRelationalStore = combineNonRelStore(s1.nonRelationalStoreState, s2.nonRelationalStoreState)
        MaybeChanged(
          RelationalStoreState(metaData, copyAbstract1(_abstract1), joinedNonRelationalStore.get),
          joinedTypeEnv.hasChanged || joinedAbs1.hasChanged || joinedNonRelationalStore.hasChanged
        )
      } finally {
        metaData = snapshotTypeEnv
        _abstract1 = snapshotAbs1
      }


  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    nonRelationalStore.addressIterator(valueIterator)