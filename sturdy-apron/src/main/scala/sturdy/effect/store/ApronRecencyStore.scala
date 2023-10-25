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
import sturdy.effect.store.PhysicalAddress

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag


// Plan: 1) ApronStore, 2) tests, 3) ApronCallFrame stuff
class ApronStore[Context, V]
  (val apronManager: Manager,  
       getIntVal: V => Option[ApronExpr[Context]],
       makeIntVal: ApronExpr[Context] => V,
       )
  extends Store[PhysicalAddress[Context], V, WithJoin]:

  private var apronState : Abstract1 = new Abstract1(apronManager, new Environment())
 
  def read(x: PhysicalAddress[Context]): JOptionA[V] = 
    // FIXME: reading weak variable should be different
    if (apronState.getEnvironment().hasVar(x)) {
      // Which JOption here?

      // TODO: x:PhysicalAddress, ApronVar requires a VirtualAddress
      JOptionA.Some(makeIntVal(ApronExpr.Var(x)))
    } 
    else {
      JOptionA.None()
    }

  def write(x: PhysicalAddress[Context], v : V): Unit =
    getIntVal(v) match 
      case Some(exp) =>
        val aexp : apron.Texpr1Intern = exp.toIntern(null) // TODO: fix scope
        val newState = apronState.assignCopy(apronManager, x, aexp, null)
        if (x.isStrong || ! apronState.getEnvironment().hasVar(x))
          apronState = newState
        else 
          // weak update implemented as join
          apronState.join(apronManager, newState)
      case None =>
        throw new NotImplementedError("")


  def free(x: PhysicalAddress[Context]): Unit = throw new NotImplementedError("free")