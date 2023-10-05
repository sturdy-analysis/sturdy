package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values.references.{AbstractAddr, PowersetAddr, joinPowersetAddr}
import sturdy.values.{*, given}
import sturdy.apron.{Apron, ApronAllocationSite, ApronExpr, ApronState, ApronVal, ApronVar}
import apron.Texpr1Intern
import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1VarNode, Texpr1Intern, Var}


import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag

// class ApronVar[Context] extends apron.Var, sturdy.effect.store.PowPhysicalAddress[Context]:
//   def compareTo(var : ApronVar[Context]) : int = ...


class ApronRecencyStore[Context, Virt <: AbstractAddr[VirtualAddress[Context]], V]
  (val apronManager : Manager)
  (using initStore: Store[PowPhysicalAddress[Context], V, WithJoin])
  (using Join[V], Widen[V], Finite[Context])
  // missing argument for parameter initStore to massage correctly?
  extends RecencyStore[Context, Virt, V]:
  private var apronState : Abstract1 = new Abstract1(apronManager, new Environment())

  override def read(vs: Virt): JOption[WithJoin, V] =
    val pa = virtToPhys(vs)
    // TODO: make apron.Var compatible with sturdy.effect.store.PowPhysicalAddress[Context]
    val pa_av : apron.Var = pa
    // Keep ApronExpr to maintain relationality? simplify?
    makeIntVal(ApronExpr.Var(pa_av))

  override def write(vs: Virt, v: V): Unit =
    val pa  = virtToPhys(vs)
    val pa_av : apron.Var = pa
    val av : apron.Texpr1Intern = ApronExpr.toIntern(v)
    val newState = apronState.assignCopy(apronManager, pa_av, av, null)
    if (pa.isStrong)
      apronState = newState
    else 
      // weak update implemented as join
      apronState.join(apronManager, newState)

  def alloc(ctx: Context): VirtualAddress[Context] =
    val fresh = getNext()
    mostRecent.get(ctx) match
      case Some(mostRecentVirts) =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, virtToPhys)
        addressTranslation += virt.identifier -> PowRecency.Recent
        val pa_recent_av : apron.Var = PowersetAddr(PhysicalAddress(ctx, Recency.Recent))
        val pa_old_av : apron.Var = PowersetAddr(PhysicalAddress(ctx, Recency.Old))
        // fold recent into old
        apronState.fold(apronManager, Array(pa_old_av, pa_recent_av))
        // add recent again
        val new_env = apronState.getEnvironment().add(Array(pa_recent_av), null)
        apronState.changeEnvironment(apronManager, new_env, false)
        for(mostRecentVirt <- mostRecentVirts)
          addressTranslation += (ctx,mostRecentVirt) -> PowRecency.Old
        virt
      case None =>
        mostRecent += ctx -> Powerset(fresh)
        val virt = VirtualAddress(ctx, fresh, virtToPhys)
        addressTranslation += virt.identifier -> PowRecency.Recent
        val pa_av : apron.Var = virtToPhys(virt)
        // add recent
        val new_env = apronState.getEnvironment().add(Array(pa_av), null)
        apronState.changeEnvironment(apronManager, new_env, false)
        virt
