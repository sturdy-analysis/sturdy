package sturdy.effect.store

import apron.*
import sturdy.apron.{Abstract1Join, Abstract1Widen, ApronExpr, ApronVar}
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
  Type : Join: Widen,
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
              ApronExpr.Var(vAddr, typeEnv(addr)),
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
          powAddr.reduce(addr =>
            val to = ApronVar(addr)
            var env = apronState.getEnvironment()
            if (!env.hasVar(to)) {
              env = env.add(Array[apron.Var](to), Array[apron.Var]())
              apronState.changeEnvironment(manager, env, false)
              typeEnv += to.addr -> exp._type
            }
            val aexp : apron.Texpr1Intern = exp.toIntern(env)
            apronState.assign(manager, to, aexp, null)
          )
        } else /* if(powAddr.isWeak) */ {
          // weak update implemented as join
          powAddr.reduce(addr =>
            val to = ApronVar(addr)
            var env = apronState.getEnvironment()
            if (!env.hasVar(to)) {
              env = env.add(Array[apron.Var](to), Array[apron.Var]())
              apronState.changeEnvironment(manager, env, false)
              typeEnv += to.addr -> exp._type
              apronState.assign(manager, to, exp.toIntern(env), null)
            } else {
              typeEnv += to.addr -> Join(typeEnv(to.addr), exp._type).get
              apronState.join(manager, apronState.assignCopy(manager, to, exp.toIntern(env), null))
            }
          )
        }
      case None =>
        throw new NotImplementedError("")

  override def move(fromPow: PowAddr, toPow: PowAddr): Unit =
    // Check for the special case if `fromPow` and `toPow` are singletons to avoid a join on the abstract domain
    if(fromPow.iterator.size == 1 && toPow.iterator.size == 1) {
      val from = ApronVar(fromPow.iterator.next())
      val to = ApronVar(toPow.iterator.next())
      if(apronState.getEnvironment.hasVar(from)){
        if(apronState.getEnvironment.hasVar(to)) {
          typeEnv += to.addr -> Join(typeEnv(to.addr), typeEnv(from.addr)).get
          apronState.fold(manager, Array[Var](to, from))
        } else {
          typeEnv += to.addr -> typeEnv(from.addr)
          apronState.rename(manager, Array[Var](from), Array[Var](to))
        }
        typeEnv -= from.addr
      } else {
        // Address `from` is not bound in `apronState`. In this case `apronState` is not changed.
      }
    } else {
      throw new NotImplementedError("Handle the case where multiple variables are moved")
//      toPow.iterator.foreach {
//        to =>
//          val foldVars = (Iterator(to) ++ fromPow.iterator).map(addr => convertToApron(addr): Var).toArray
//          apronState.join(apronManager, apronState.foldCopy(apronManager, foldVars))
//      }
    }

  /**
   * Computes an over-approximation of copying addresses from a source to a target.
   * `copy` has a worst-case complexity O(n * m) joins,
   * where n is the number of source addresses
   * and m is the number of target addresses.
   */
  override def copy(fromPow: PowAddr, toPow: PowAddr): Unit =
    val env = apronState.getEnvironment

    val toSet =
      toPow.iterator.map(ApronVar(_)).toSet

    val fromSet =
      fromPow.iterator
        .map(ApronVar(_))
        .filter(from => env.hasVar(from)) // filter out unbound `from` addresses, because they dont' need to be copied
        .toSet
        .diff(toSet) // remove `to` addresses, because they don't need to be copied

    for (from <- fromSet; to <- toSet) {
      if (env.hasVar(to)) {
        typeEnv += to.addr -> Join(typeEnv(to.addr), typeEnv(from.addr)).get
        apronState.join(manager,
          apronState.assignCopy(manager, to, ApronExpr.Var(from, typeEnv(from.addr)).toIntern(env), null))
      } else {
        typeEnv += to.addr -> typeEnv(from.addr)
        apronState.expand(manager, from, Array[Var](to))
      }
    }

  override def free(powAddr: PowAddr): Unit =
    if (powAddr.isStrong) {
      powAddr.reduce(addr =>
        val dest = ApronVar(addr)
        val env = apronState.getEnvironment()
        if(env.hasVar(dest)) {
          typeEnv -= dest.addr
          apronState.forget(manager, dest, false)
          apronState.changeEnvironment(manager, env.remove(Array[Var](dest)), false)
        }
      )
    }

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
