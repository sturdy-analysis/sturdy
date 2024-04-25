package sturdy.effect.store

import apron.*
import sturdy.apron.{Abstract1Join, Abstract1Widen, ApronCons, ApronExpr, ApronRepresentation, ApronType, ApronVar, CompareOp}
import sturdy.data.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, EffectStack, Stateless, SturdyFailure, TrySturdy}
import sturdy.values.integer.IntegerOps
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
trait RelationalStore
  [
    Context: Ordering: Finite,
    Type : ApronType : Join: Widen,
    PowAddr <: AbstractAddr[PhysicalAddress[Context]],
    Val : Join: Widen
  ]
  (val manager: Manager,
   initialState: Abstract1,
   initialTypeEnv: Map[PhysicalAddress[Context], Type])
  extends Store[PowAddr, Val, WithJoin]:

  def getRelationalVal(v: Val): Option[ApronExpr[PhysicalAddress[Context], Type]]
  def makeRelationalVal(expr: ApronExpr[PhysicalAddress[Context], Type]): Val

  type TypeEnv = Map[PhysicalAddress[Context], Type]

  private var _abstract1 : Abstract1 = initialState
  private var typeEnv: TypeEnv = initialTypeEnv
  val nonRelationalStore: AStoreThreaded[PhysicalAddress[Context], PowAddr, Val] = AStoreThreaded(Map())

  def abstract1: Abstract1 = _abstract1

  def getType(powAddr: PowAddr): JOptionA[Type] =
    powAddr.reduce(addr => JOptionA(typeEnv.get(addr)))

  override def read(powAddr: PowAddr): JOptionA[Val] =
    if(powAddr.isEmpty)
      JOptionA.None()
    else {
      val v1 = powAddr.reduce(addr =>
        val vAddr = ApronVar(addr)
        if (_abstract1.getEnvironment().hasVar(vAddr)) {
          JOptionA.Some(makeRelationalVal(ApronExpr.Addr(vAddr, typeEnv(addr))))
        } else {
          JOptionA.None()
        }
      )
      val v2 = nonRelationalStore.read(powAddr)
      (v1,v2) match
        case (JOptionA.None(), _) => v2
        case (_, JOptionA.None()) => v1
        case (_,_) => Join(v1,v2).get
    }

  override def write(powAddr: PowAddr, v : Val): Unit =
    getRelationalVal(v) match
      case Some(exp) =>
        if(powAddr.isEmpty) {
          // nothing
        } else if (powAddr.isStrong) {
          powAddr.reduce(toAddr =>
            addAddrToEnvs(toAddr, exp)
            val to = ApronVar(toAddr)
            val env = _abstract1.getEnvironment
            assert(env.hasVar(to), s"environment ${env} does not have variable ${to}")
            val aexp : apron.Texpr1Intern = exp.toIntern(_abstract1.getEnvironment)
            _abstract1.assign(manager, to, aexp, null)
          )
        } else /* if(powAddr.isWeak) */ {
          // weak update implemented as join
          powAddr.reduce(toAddr =>
            addAddrToEnvs(toAddr, exp)
            val to = ApronVar(toAddr)
            if (! _abstract1.getEnvironment.hasVar(to)) {
              _abstract1.assign(manager, to, exp.toIntern(_abstract1.getEnvironment), null)
            } else {
              _abstract1.join(manager,
                _abstract1.assignCopy(manager, to, exp.toIntern(_abstract1.getEnvironment), null))
            }
          )
        }
      case None =>
        nonRelationalStore.write(powAddr, v)

  override def move(fromPow: PowAddr, toPow: PowAddr): Unit =
    nonRelationalStore.move(fromPow, toPow)

    if (fromPow.isStrong && fromPow.iterator.size == 1 && toPow.iterator.size == 1) {
      val from = fromPow.iterator.next()
      val to = toPow.iterator.next()

      (typeEnv.get(from), typeEnv.get(to)) match
        case (Some(fromType), Some(toType)) =>
          typeEnv += to -> Join(toType, fromType).get
          typeEnv -= from
          _abstract1.fold(manager, Iterable(to,from).map[Var](ApronVar(_)).toArray)
        case (Some(fromType), None) =>
          typeEnv += to -> fromType
          typeEnv -= from
          _abstract1.rename(manager, Array[Var](ApronVar(from)), Array[Var](ApronVar(to)))
        case (None, Some(_)) | (None, None) => // Nothing to do
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
      (typeEnv.get(from.addr), typeEnv.get(to.addr)) match
        case (Some(fromType), Some(toType)) =>
          if(fromType.apronRepresentation == toType.apronRepresentation) {
            typeEnv += to.addr -> Join(typeEnv(to.addr), typeEnv(from.addr)).get
            _abstract1.join(manager,
              _abstract1.assignCopy(manager, to, ApronExpr.Addr(from, typeEnv(from.addr)).toIntern(env), null))
          } else {
            throw new IllegalStateException(
              s"Cannot copy address ${from.addr} of type ${fromType} represented by ${fromType.apronRepresentation}" +
                s"to address ${to.addr} of type ${toType} represented by ${toType.apronRepresentation}")
          }

        case (Some(fromType), None) =>
          typeEnv += to.addr -> typeEnv(from.addr)
          _abstract1.expand(manager, from, Array[Var](to))

        case (None, Some(_)) | (None, None) =>
          // from is not bound. There is nothing to do.
    }

  override def free(powAddr: PowAddr): Unit =
    nonRelationalStore.free(powAddr)

    if (powAddr.isStrong) {
      powAddr.reduce(addr =>
        val dest = ApronVar(addr)
        val env = _abstract1.getEnvironment()
        if(env.hasVar(dest)) {
          typeEnv -= dest.addr
//          _abstract1.forget(manager, dest, false)
          _abstract1.changeEnvironment(manager, env.remove(Array[Var](dest)), false)
        }
      )
    }

  class BottomFailure extends SturdyFailure

  def addConstraint(constraint: ApronCons[PhysicalAddress[Context], Type]): Unit =
    val constraints: Array[Tcons1] = Array(constraint.toApron(_abstract1.getEnvironment))
    this._abstract1.meet(manager, constraints)
    if (this._abstract1.isBottom(manager))
      throw new BottomFailure

  def getBound(expr: ApronExpr[PhysicalAddress[Context], Type]): Interval =
    _abstract1.getBound(_abstract1.getCreationManager, expr.toIntern(_abstract1.getEnvironment))

  private def addAddrToEnvs(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type]) =
    var env = _abstract1.getEnvironment()
    val variable = ApronVar(addr)
    val tpe = expr._type
    if (!env.hasVar(variable)) {
      tpe.apronRepresentation match
        case ApronRepresentation.Int =>
          env = env.add(Array[apron.Var](variable), Array[apron.Var]())
        case ApronRepresentation.Real =>
          env = env.add(Array[apron.Var](), Array[apron.Var](variable))
      _abstract1.changeEnvironment(manager, env, false)
      typeEnv += addr -> tpe
    } else {
      if (typeEnv(addr).apronRepresentation == tpe.apronRepresentation) {
        typeEnv += addr -> Join(typeEnv(addr), tpe).get
      } else {
        throw new IllegalStateException(
          s"Cannot assign ${expr} of type ${tpe} represented by ${tpe.apronRepresentation}" +
            s"to address ${addr} of type ${typeEnv(addr)} represented by ${typeEnv(addr).apronRepresentation}")
      }
    }

  def isBottom: Boolean =
    _abstract1.isBottom(manager)

  case class RelationalStoreState(tenv: TypeEnv, abs1: Abstract1, nonRelationalStoreState: nonRelationalStore.State):
    override def equals(obj: Any): Boolean =
      obj match
        case RelationalStoreState(tenv2, abs2, nonRel2) =>
          tenv.equals(tenv2) && abs1.isEqual(manager, abs2) && MapEquals(nonRelationalStoreState,nonRel2)
        case _ =>
          false
    override def hashCode(): Int = (tenv, abs1.hashCode(manager), nonRelationalStoreState).hashCode()

  override type State = RelationalStoreState

  // It is important to copy abstract1 when getting and setting a state, because
  // RelationalStore mutates abstract1
  override def getState: State =
    RelationalStoreState(typeEnv, copyAbstract1(_abstract1), nonRelationalStore.getState)
  override def setState(s: State) =
    typeEnv = s.tenv
    _abstract1 = copyAbstract1(s.abs1)
    nonRelationalStore.setState(s.nonRelationalStoreState)

  def copyAbstract1(abstract1: Abstract1): Abstract1 = new Abstract1(manager, abstract1)

  override def join: Join[State] = combineRelationalStoreState
  override def widen: Widen[State] = combineRelationalStoreState

  def combineRelationalStoreState[W <: Widening](using combineTypeEnv: Combine[TypeEnv,W], combineAbs1: Combine[Abstract1,W], combineNonRelStore: Combine[nonRelationalStore.State,W]): Combine[RelationalStoreState, W] =
    (s1: RelationalStoreState, s2: RelationalStoreState) =>
      val joinedTypeEnv = combineTypeEnv(s1.tenv, s2.tenv)
      val joinedAbs1 = combineAbs1(s1.abs1, s2.abs1)
      val joinedNonRelationalStore = combineNonRelStore(s1.nonRelationalStoreState, s2.nonRelationalStoreState)
      val res = MaybeChanged(
        RelationalStoreState(joinedTypeEnv.get, joinedAbs1.get, joinedNonRelationalStore.get),
        joinedTypeEnv.hasChanged || joinedAbs1.hasChanged || joinedNonRelationalStore.hasChanged
      )
      res