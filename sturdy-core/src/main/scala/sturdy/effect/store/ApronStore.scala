package sturdy.effect.store

import apron.Texpr1Intern
import sturdy.effect.AMayComputeOne
import sturdy.effect.AMayComputeOne.*
import sturdy.effect.JoinComputation
import sturdy.values.JoinValue
import sturdy.values.ints.ApronExpr

import scala.collection.mutable.ListBuffer

trait ApronStore[Addr <: apron.Var](manager: apron.Manager) extends Store[Addr, ApronExpr]:
  override type StoreJoin[A] = JoinValue[A]
  override type StoreJoinComp = JoinComputation

  protected var store: apron.Environment = apron.Environment()
  protected var domain: apron.Abstract1 = apron.Abstract1(manager, store)

  override def read(x: Addr): AMayComputeOne[ApronExpr] = Computes(apron.Texpr1VarNode(x))

  override def write(x: Addr, v: ApronExpr): Unit =
    // Extend this.store with new address?
    domain.assign(manager, x, Texpr1Intern(store, v), domain)

  override def free(x: Addr): Unit = ???

