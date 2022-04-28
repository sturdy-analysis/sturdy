package sturdy.effect.store

import apron.Texpr1Intern
import sturdy.effect.CMayCompute
import sturdy.effect.CMayCompute.*
import sturdy.effect.NoJoin
import sturdy.values.ints.ApronExpr

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class ApronStore[Addr <: apron.Var](manager: apron.Manager) extends Store[Addr, ApronExpr]:
  override type StoreJoin[A] = NoJoin[A]
  override type StoreJoinComp = Unit

  protected var store: apron.Environment = apron.Environment()
  protected var domain: apron.Abstract1 = apron.Abstract1(manager, store)
  def getDomain(): apron.Abstract1 = domain

  override def read(x: Addr): CMayCompute[ApronExpr] =
    ensureAddress(x)
    Computes(apron.Texpr1VarNode(x))

  override def write(x: Addr, v: ApronExpr): Unit =
    ensureAddress(x)
    val newDomain = domain.assignCopy(manager, x, Texpr1Intern(store, v), null)
    domain.join(manager, newDomain)

  def ensureAddress(x: Addr) =
    if(! store.hasVar(x))
      store =store.add(Array[apron.Var](x), Array[apron.Var]())
      domain.changeEnvironment(manager, store, true)

  override def free(x: Addr): Unit = ???

