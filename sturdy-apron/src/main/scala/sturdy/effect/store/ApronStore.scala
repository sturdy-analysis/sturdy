package sturdy.effect.store

import apron.*
import sturdy.apron.{Abstract1Join, Abstract1Widen, ApronCons, ApronExpr, ApronType, ApronVar, Representation}
import sturdy.data.{*, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
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
final class ApronStore[
  Context: Ordering: Finite,
  Type : ApronType : Join: Widen,
  PowAddr <: AbstractAddr[PhysicalAddress[Context]],
  Val : Join]
  (val manager: Manager,
   initialState: Abstract1,
   initialTypeEnv: Map[PhysicalAddress[Context], Type],
   val getIntVal: Val => Option[ApronExpr[PhysicalAddress[Context], Type]],
   val makeIntVal: (ApronExpr[PhysicalAddress[Context], Type], Abstract1) => Val)
  extends Store[PowAddr, Val, WithJoin]:

  type TypeEnv = Map[PhysicalAddress[Context], Type]

  private var apronState : Abstract1 = initialState
  private var typeEnv: TypeEnv = initialTypeEnv

  def getType(powAddr: PowAddr): Type =
    powAddr.reduce(typeEnv(_))

  override def read(powAddr: PowAddr): JOptionA[Val] =
    if(powAddr.isEmpty)
      JOptionA.None()
    else
      powAddr.reduce(addr =>
        val vAddr = ApronVar(addr)
        if (apronState.getEnvironment().hasVar(vAddr)) {
          JOptionA.Some(
            makeIntVal(
              ApronExpr.Addr(vAddr, typeEnv(addr)),
              apronState))
        }
        else {
          JOptionA.None()
        }
      )

  override def write(powAddr: PowAddr, v : Val): Unit =
    getIntVal(v) match
      case Some(exp) =>
        if(powAddr.isEmpty) {
          // nothing
        } else if (powAddr.isStrong) {
          powAddr.reduce(toAddr =>
            addAddrToEnvs(toAddr, exp)
            val to = ApronVar(toAddr)
            val aexp : apron.Texpr1Intern = exp.toIntern(apronState.getEnvironment)
            apronState.assign(manager, to, aexp, null)
          )
        } else /* if(powAddr.isWeak) */ {
          // weak update implemented as join
          powAddr.reduce(toAddr =>
            addAddrToEnvs(toAddr, exp)
            val to = ApronVar(toAddr)
            if (! apronState.getEnvironment.hasVar(to)) {
              apronState.assign(manager, to, exp.toIntern(apronState.getEnvironment), null)
            } else {
              apronState.join(manager,
                apronState.assignCopy(manager, to, exp.toIntern(apronState.getEnvironment), null))
            }
          )
        }
      case None =>
        throw new NotImplementedError("")

  override def move(fromPow: PowAddr, toPow: PowAddr): Unit =
    if (fromPow.isStrong && fromPow.iterator.size == 1 && toPow.iterator.size == 1) {
      val from = fromPow.iterator.next()
      val to = toPow.iterator.next()

      (typeEnv.get(from), typeEnv.get(to)) match
        case (Some(fromType), Some(toType)) =>
          typeEnv += to -> Join(toType, fromType).get
          typeEnv -= from
          apronState.fold(manager, Iterable(to,from).map[Var](ApronVar(_)).toArray)
        case (Some(fromType), None) =>
          typeEnv += to -> fromType
          typeEnv -= from
          apronState.rename(manager, Array[Var](ApronVar(from)), Array[Var](ApronVar(to)))
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
    val env = apronState.getEnvironment
    val toSet = toPow.iterator.map(ApronVar(_)).toSet
    // remove `to` addresses, because they don't need to be copied
    val fromSet = fromPow.iterator.map(ApronVar(_)).toSet.diff(toSet)

    for (from <- fromSet; to <- toSet) {
      (typeEnv.get(from.addr), typeEnv.get(to.addr)) match
        case (Some(fromType), Some(toType)) =>
          if(fromType.representation == toType.representation) {
            typeEnv += to.addr -> Join(typeEnv(to.addr), typeEnv(from.addr)).get
            apronState.join(manager,
              apronState.assignCopy(manager, to, ApronExpr.Addr(from, typeEnv(from.addr)).toIntern(env), null))
          } else {
            throw new IllegalStateException(
              s"Cannot copy address ${from.addr} of type ${fromType} represented by ${fromType.representation}" +
                s"to address ${to.addr} of type ${toType} represented by ${toType.representation}")
          }

        case (Some(fromType), None) =>
          typeEnv += to.addr -> typeEnv(from.addr)
          apronState.expand(manager, from, Array[Var](to))

        case (None, Some(_)) | (None, None) =>
          // from is not bound. There is nothing to do.
    }

  override def free(powAddr: PowAddr): Unit =
    if (powAddr.isStrong) {
      powAddr.reduce(addr =>
        val dest = ApronVar(addr)
        val env = apronState.getEnvironment()
        if(env.hasVar(dest)) {
          typeEnv -= dest.addr
//          apronState.forget(manager, dest, false)
          apronState.changeEnvironment(manager, env.remove(Array[Var](dest)), false)
        }
      )
    }

  def addConstraint(constraint: ApronCons[PhysicalAddress[Context], Type]): Unit =
    val constraints: Array[Tcons1] = constraint.toApron(apronState.getEnvironment).toArray
    apronState.meet(manager, constraints)

  def getBound(expr: ApronExpr[PhysicalAddress[Context], Type]): Interval =
    apronState.getBound(apronState.getCreationManager, expr.toIntern(apronState.getEnvironment))

  private def addAddrToEnvs(addr: PhysicalAddress[Context], expr: ApronExpr[PhysicalAddress[Context], Type]) =
    var env = apronState.getEnvironment()
    val variable = ApronVar(addr)
    val tpe = expr._type
    if (!env.hasVar(variable)) {
      tpe.representation match
        case Representation.Int =>
          env = env.add(Array[apron.Var](variable), Array[apron.Var]())
        case Representation.Real =>
          env = env.add(Array[apron.Var](), Array[apron.Var](variable))
      apronState.changeEnvironment(manager, env, false)
      typeEnv += addr -> tpe
    } else {
      if (typeEnv(addr).representation == tpe.representation) {
        typeEnv += addr -> Join(typeEnv(addr), tpe).get
      } else {
        throw new IllegalStateException(
          s"Cannot assign ${expr} of type ${tpe} represented by ${tpe.representation}" +
            s"to address ${addr} of type ${typeEnv(addr)} represented by ${typeEnv(addr).representation}")
      }
    }

  def isBottom: Boolean =
    apronState.isBottom(manager)

  override type State = (TypeEnv, Abstract1)

  // TODO: find a better way to copy apronState without changing anything
  def getState: State = (typeEnv, apronState.changeEnvironmentCopy(manager, apronState.getEnvironment, false))

  def setState(s: State) =
    typeEnv = s._1
    apronState = s._2

  def join: Join[State] = implicitly

  def widen: Widen[State] = implicitly

given ApronClosedEquality[Cls, Context, Type]:  ClosedEquality[Cls, (Map[PhysicalAddress[Context],Type], apron.Abstract1)] with
  def closedEquals(closure1: Cls, a1: (Map[PhysicalAddress[Context], Type], apron.Abstract1), closure2: Cls, a2: (Map[PhysicalAddress[Context], Type], apron.Abstract1)): Boolean =
    val (typeEnv1, abstract1) = a1
    val (typeEnv2, abstract2) = a2
    typeEnv1 == typeEnv2 && abstract1.isEqual(abstract1.getCreationManager(), abstract2)

  def closedHashCode(closure: Cls, a: (Map[PhysicalAddress[Context], Type], apron.Abstract1)): Int =
    a.hashCode()
