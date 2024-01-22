package sturdy.values.objects

import sturdy.data.{MayJoin, NoJoin}
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation

import scala.collection.mutable

enum ObjectFailure extends FailureKind:
  case UnboundField

enum AllocationSite:
  case Object

import ObjectFailure.*

trait ObjectOps[Addr, Idx, V, CF, J[_] <: MayJoin[_], O]:
  def makeObject(cfs: CF, store: Store[Idx, V, J]): O
  def getField(obj: O, idx: Idx): V
  def setField(obj: O, idx: Idx, v: V): Unit

given ConcreteObjectOps[Addr, Idx, V, CF, J[_] <: MayJoin[_]](using f: Failure, alloc: Allocation[Addr, AllocationSite]): ObjectOps[Addr, Idx, V, CF, J, (Addr, CF, Store[Idx, V, J])] with
  override def makeObject(cfs: CF, store: Store[Idx, V, J]): (Addr, CF, Store[Idx, V, J]) =
    val addr = alloc(AllocationSite.Object)
    (addr, cfs, store)
  override def getField(obj: (Addr, CF, Store[Idx, V, J]), idx: Idx): V =
    obj._3.read(idx).getOrElse(f(UnboundField, idx.toString))

  override def setField(obj: (Addr, CF, Store[Idx, V, J]), idx: Idx, v: V): Unit =
    obj._3.write(idx, v)