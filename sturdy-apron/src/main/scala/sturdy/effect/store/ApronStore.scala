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
import sturdy.values.references.{AbstractAddr, PhysicalAddress, PowersetAddr, Recency, joinPowersetAddr, given}
import sturdy.values.{*, given}
import sturdy.apron.{ApronExpr, Abstract1Join, Abstract1Widen}
import apron.Texpr1Intern
import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1Intern, Texpr1VarNode, Var}

import scala.collection.immutable.{HashMap, IntMap}
import scala.collection.{MapView, mutable}
import scala.reflect.ClassTag
import math.Ordered.orderingToOrdered

implicit def convertToApron[Context : Ordering](x: PhysicalAddress[Context]) : ApronPhysicalAddress[Context] =
  ApronPhysicalAddress(x.ctx, x.recency)

implicit def convertFromApron[Context : Ordering](x: ApronPhysicalAddress[Context]) : PhysicalAddress[Context] =
  PhysicalAddress(x.ctx, x.recency)



case class ApronPhysicalAddress[Context: Ordering](ctx: Context, recency: Recency)
  extends apron.Var
  with AbstractAddr[ApronPhysicalAddress[Context]]:

  override def isEmpty: Boolean = false
  override def isStrong: Boolean = recency == Recency.Recent
  override def reduce[A](f: ApronPhysicalAddress[Context] => A)(using Join[A]): A = f(this)
  override def clone(): ApronPhysicalAddress[Context] = this // We don't need to clone since PhysicalAddress is immutable

  override def compareTo(v: apron.Var): Int =
    v match
      case other: ApronPhysicalAddress[Context] => physicalAddressOrdering[Context].compare(this, other)
      case _ => -1

  override def toString: String = s"${ctx}_${recency}"

given physicalAddressOrdering[Context: Ordering]: Ordering[ApronPhysicalAddress[Context]] =
  Ordering.by[ApronPhysicalAddress[Context], (Context, Recency)](addr => (addr.ctx, addr.recency))

given PhysicalAddressToApronVar[Context: Ordering]: Conversion[PhysicalAddress[Context], ApronPhysicalAddress[Context]] with
  override def apply(addr: PhysicalAddress[Context]): ApronPhysicalAddress[Context] = ApronPhysicalAddress[Context](addr.ctx, addr.recency)



/**

Example on https://docs.google.com/document/d/1d-o3OSZRHowwXaXAtdW1cN2Day6gtpMqu0Pmk9Q2DuM/edit
 
 **/

// Plan: 1) ApronStore, 2) tests, 3) ApronCallFrame stuff
// Restrict type to PowPhysicalAddress and enable automatic conversion?
class ApronStore[
  Context: Ordering,
  //Addr <: apron.Var,
  Addr <: PhysicalAddress[Context], 
  PowAddr <: AbstractAddr[Addr],
  V]
  (val apronManager: Manager,
       initialState: Abstract1,
       getIntVal: V => Option[ApronExpr[ApronPhysicalAddress[Context]]],
       makeIntVal: (ApronExpr[ApronPhysicalAddress[Context]], Abstract1) => V,
       )
  (using Join[V])
  extends Store[PowAddr, V, WithJoin]:


  override type State = Abstract1
  private var apronState : Abstract1 = initialState

  def getState : State = apronState
  def setState(s : Abstract1) = apronState = s 
 
  def join : Join[State] = implicitly
  def widen : Widen[State] = implicitly

  def read(powAddr: PowAddr): JOptionA[V] =
    if(powAddr.isEmpty)
      JOptionA.None()
    else
      powAddr.reduce(addr =>
        if (apronState.getEnvironment().hasVar(addr)) {
          JOptionA.Some(makeIntVal(
            ApronExpr.Var(
            (ApronPhysicalAddress(addr.ctx, addr.recency))), apronState))
        }
        else {
          JOptionA.None()
        }
      )

  def write(powAddr: PowAddr, v : V): Unit =
    getIntVal(v) match 
      case Some(exp) =>
        if(powAddr.isEmpty) {
          // nothing
        } else if (powAddr.isStrong) {
          powAddr.reduce(addr =>
            var env = apronState.getEnvironment()
            if (!env.hasVar(addr)) {
              env = env.add(Array[apron.Var](addr), Array[apron.Var]())
              apronState.changeEnvironment(apronManager, env, false)
            }
            val aexp : apron.Texpr1Intern = exp.toIntern(env)
            apronState = apronState.assignCopy(apronManager, addr, aexp, null)
          )
        } else /* if(powAddr.isWeak) */ {
          powAddr.reduce(addr =>
            var env = apronState.getEnvironment()
            if (!env.hasVar(addr)) {
              env = env.add(Array[apron.Var](addr), Array[apron.Var]())
              apronState.changeEnvironment(apronManager, env, false)
            }
            val aexp: apron.Texpr1Intern = exp.toIntern(env)
            // weak update implemented as join
            apronState.join(apronManager, apronState.assignCopy(apronManager, addr, aexp, null))
          )
        }
      case None =>
        throw new NotImplementedError("")

  def free(x: PowAddr): Unit =
    throw new NotImplementedError("free")


// TODO write explicit, simple, join on ApronExpressions
given JoinApronExpr[Addr <: apron.Var](using abstract1: Abstract1): Join[ApronExpr[Addr]] with
  def apply(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): MaybeChanged[ApronExpr[Addr]] =
    throw NotImplementedError()

given WidenApronExpr[Addr <: apron.Var](using abstract1: Abstract1): Widen[ApronExpr[Addr]] with
  def apply(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): MaybeChanged[ApronExpr[Addr]] =
    throw NotImplementedError()

given ApronClosedEquality[Cls]:  ClosedEquality[Cls, apron.Abstract1] with
  def closedEquals(closure1: Cls, a1: apron.Abstract1, closure2: Cls, a2: apron.Abstract1): Boolean =
    a1.isEqual(a1.getCreationManager(), a2)
  def closedHashCode(closure: Cls, a: apron.Abstract1): Int =
    a.hashCode()