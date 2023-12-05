/*

// CallFrame : names -> VirtualAddress
// RecencyStore : VirtAddr -> PhysAddr
// ApronState : constraints over PhysAddr


// new CallFrame(x, y): 
// CF: x ~> x0; y ~> y0
// RS: x0 ~> xR; y0 ~> yR
// AS: xR = -1, yR = -1
x = rand(0, 10)
// CF.setLocal(x, [0, 10])
//   RS.write(x1, [0, 10])
//     AS.write(xR, [0, 10])
//     RS: x0 ~> xO, x1 ~> xR, y0 ~> yR
//     AS: xO = -1, 0 <= xR <= 10, yR = -1
y = x + 1
// CF.getLocal(x) = ApronExpr.Var(x1)
// CF.setLocal(y, ApronExpr(x1 + 1))
//   RS.write(y1, ApronExpr(x1+1))
// map from ApronExpr[A] to ApronExpr[B] with addressTranslation : A => B? (in ApronStore) 
// just _.physical 

//     AS.write(yR, ApronExpr(x1 + 1)) // xR+1 
// AS:xO =  -1, yO = -1, 0 <= xR <= 10, yR = xR + 1
// RS: x0 ~> xO, x1 ~> xR, y1 ~> yR, y0 ~> yO
print(x) 
// print: x1 <- TODO: precision improvement?
// RS: x1 ~> xR
x = 3
// RS: x0 ~> xO, x1 ~> xO, x2 ~> xR 
// AS: -1 <= xO <= 10, xR = 3, yR <= xO + 1?
if(x == 5) 
  // SpecialBranchingOps 
  // assert(ApronCons(x2 == 5))
  // ApronState: bot, which raises an exception and prunes the branch
if(x >= y)
  // introduce new constraint in ApronState

z = sin(y)
// RS.read(y1)
//   AS.read(yR) = ApronExpr.Constant([...]) <- NOT A VirtAddr, ApronExpr[PhysAddr]. This is a non-relational read, but shouldn't happen much

// ApronExpr.Constant([-1., 1.]) // fetch y as interval, then call sin(y_itv)


// Non-representable operations?
// 1. Convert to intervals (but you need to read)
// 2. 


// TODO: ApronExpr parametric in Virt/Phys Addr


n0 = 0
n1 = inc(n0)  // x = virt#1, xR
n2 = inc(n1)  // x = virt#2, xR
n3 = inc(n2)  // x = virt#3, xR
def inc(x) = {
  global += x
  return x + 1
}





*/

package sturdy.effect.store

import sturdy.{IsSound, Soundness}
import sturdy.data.{JOption, JOptionA, WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.{ComputationJoiner, Stateless, TrySturdy}
import sturdy.values.references.{AbstractAddr, PowersetAddr, PhysicalAddress, joinPowersetAddr}
import sturdy.values.{*, given}
import sturdy.apron.{ApronExpr}// , Apron, ApronAllocationSite,  ApronState, ApronVal, ApronVar}
import apron.Texpr1Intern
import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1VarNode, Texpr1Intern, Var}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag
import math.Ordered.orderingToOrdered

class ApronPAWrap[Context: Ordering](_pa: PhysicalAddress[Context]) extends apron.Var:
  val pa : PhysicalAddress[Context] = _pa// def pa: PhysicalAddress[Context] = pa
  override def clone() : apron.Var = new ApronPAWrap(pa)
  override def compareTo(v : apron.Var) : Int =
    if (!v.isInstanceOf[ApronPAWrap[_]]) { -1 }
    else { 
      val vpa = v.asInstanceOf[ApronPAWrap[Context]]
      val ctxCmp = vpa.pa.ctx.compare(pa.ctx)
      if(ctxCmp == 0) { vpa.pa.isStrong.compare(pa.isStrong) } else { ctxCmp }
    } 
  override def toString: String = s"$pa"

// Plan: 1) ApronStore, 2) tests, 3) ApronCallFrame stuff
class ApronStore[Context: Ordering, V]
  (val apronManager: Manager,  
       getIntVal: V => Option[ApronExpr[ApronPAWrap[Context]]],
       makeIntVal: (ApronExpr[ApronPAWrap[Context]], Abstract1) => V,
       )
  // TODO later: switch to AbstractAddress
  extends Store[ApronPAWrap[Context], V, WithJoin]:

  override type State = Abstract1
  private var apronState : Abstract1 = new Abstract1(apronManager, new Environment())
 
  def getState : State = apronState
  def setState(s : Abstract1) = apronState = s 

  def join : Join[State] = ???
  def widen : Widen[State] = ???


  def read(x: ApronPAWrap[Context]): JOptionA[V] = 
    if (apronState.getEnvironment().hasVar(x)) {
      if (!x.pa.isStrong) throw new NotImplementedError("FIXME: reading weak variable should be different")
      // TODO #3: Which JOption here?
      JOptionA.Some(makeIntVal(ApronExpr.Var(x), apronState))
    } 
    else {
      JOptionA.None()
    }

  def write(x: ApronPAWrap[Context], v : V): Unit =
    getIntVal(v) match 
      case Some(exp) =>
        // TODO add variable if not in
        // TODO #2: can we fix scope or should we assume a function more specific than getIntval?
        var env = apronState.getEnvironment()
        if(!env.hasVar(x)) { 
          env = env.add(Array[apron.Var](x), Array[apron.Var]())
          apronState.changeEnvironment(apronManager, env, false)
        }
        val aexp : apron.Texpr1Intern = exp.toIntern(env)
        val newState = apronState.assignCopy(apronManager, x, aexp, null)
        if (x.pa.isStrong || ! apronState.getEnvironment().hasVar(x))
          apronState = newState
        else 
          // weak update implemented as join
          apronState.join(apronManager, newState)
      case None =>
        throw new NotImplementedError("")

/** 

x = rand(0, 10);
// ApronExpr(Itv[0, 10])
// apronState: 0 <= xR <= 10
// recencyStore: 
//   addrsTranslation: #1 ~> xR
y = x + 1;
  // how to read x?
  // Lookup x in environment: #1 (maybe in the CallFrame?)
  // Recencystore: read #1: 
     // #1 ~> xR
     // ApronStore: read xR:
        // why go back to virtual addresses and do the inverse translation once you write?

// apronState: 0 <= xR <= 10, yR = xR+1
// addrsT: #1 ~> xR, #2 ~> yR
print(y - x)
// addrsTranslation: #1 ~> xR, #2 ~> yR, #3 ~> tmpR
// apronState: 0 <= xR <= 10, yR = xR + 1, tmpR = 1


print(x) // print: xR 
x = 2
// ApronState: xR = 2
...


**/



  def free(x: ApronPAWrap[Context]): Unit = 
    throw new NotImplementedError("free")