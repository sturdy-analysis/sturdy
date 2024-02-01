package sturdy.effect.store

import apron.*
import sturdy.apron.{Abstract1Join, Abstract1Widen, ApronExpr, ApronVar}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
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
  Context: Ordering,
  Addr <: PhysicalAddress[Context] : Ordering : ClassTag,
  PowAddr <: AbstractAddr[Addr],
  Val : Join]
  (val manager: Manager,
   initialState: Abstract1,
   val getIntVal: Val => Option[ApronExpr[PhysicalAddress[Context]]],
   val makeIntVal: (ApronExpr[PhysicalAddress[Context]], Abstract1) => Val)
  extends Store[PowAddr, Val, WithJoin]:

  override type State = Abstract1
  private var apronState : Abstract1 = initialState

  // TODO: find a better way to copy apronState without changing anything
  def getState : State = apronState.changeEnvironmentCopy(manager, apronState.getEnvironment, false)
  def setState(s : Abstract1) = apronState = s

  def join : Join[State] = implicitly
  def widen : Widen[State] = implicitly

  override def read(powAddr: PowAddr): JOptionA[Val] =
    if(powAddr.isEmpty)
      JOptionA.None()
    else
      powAddr.reduce(addr =>
        if (apronState.getEnvironment().hasVar(ApronVar(addr))) {
          JOptionA.Some(
            makeIntVal(
              ApronExpr._var(PhysicalAddress(addr.ctx, addr.recency)),
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
              apronState.assign(manager, to, exp.toIntern(env), null)
            } else {
              apronState.join(manager, apronState.assignCopy(manager, to, exp.toIntern(env), null))
            }
          )
        }
      case None =>
        throw new NotImplementedError("")

  override def move(fromPow: PowAddr, toPow: PowAddr): Unit =
    // Check for the special case if `fromPow` and `toPow` are singletons to avoid a join on the abstract domain
    if(fromPow.iterator.size == 1 && toPow.iterator.size == 1) {
      val from: Var = ApronVar(fromPow.iterator.next())
      val to: Var = ApronVar(toPow.iterator.next())
      if(apronState.getEnvironment.hasVar(from)){
        if(apronState.getEnvironment.hasVar(to)) {
          apronState.fold(manager, Array(to, from))
        } else {
          apronState.rename(manager, Array(from), Array(to))
        }
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

    val toSet: Set[ApronVar[Addr]] =
      toPow.iterator.map(ApronVar(_)).toSet

    val fromSet: Set[ApronVar[Addr]] =
      fromPow.iterator.map(ApronVar(_)).toSet
        .diff(toSet) // remove `to` addresses, because they don't need to be copied
        .filter(from => env.hasVar(from)) // filter out unbound `from` addresses, because they dont' need to be copied

    for (from <- fromSet; to <- toSet) {
      if (env.hasVar(to)) {
        apronState.join(manager,
          apronState.assignCopy(manager, to, ApronExpr.Var(from).toIntern(env), null))
      } else {
        apronState.expand(manager, from, Array[Var](to))
      }
    }

  override def free(powAddr: PowAddr): Unit =
    if (powAddr.isStrong) {
      powAddr.reduce(addr =>
        val dest = ApronVar(addr)
        val env = apronState.getEnvironment()
        if(env.hasVar(dest)) {
          apronState.forget(manager, dest, false)
          apronState.changeEnvironment(manager, env.remove(Array[Var](dest)), false)
        }
      )
    }

given ApronClosedEquality[Cls]:  ClosedEquality[Cls, apron.Abstract1] with
  def closedEquals(closure1: Cls, a1: apron.Abstract1, closure2: Cls, a2: apron.Abstract1): Boolean =
    a1.isEqual(a1.getCreationManager(), a2)
  def closedHashCode(closure: Cls, a: apron.Abstract1): Int =
    a.hashCode()
