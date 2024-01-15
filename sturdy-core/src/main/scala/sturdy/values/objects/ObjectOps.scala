package sturdy.values.objects

import sturdy.effect.failure.Failure
import sturdy.values.records.UnboundRecordField

import scala.collection.mutable

trait ObjectOps[Addr, Idx, V, CF, O]:
  def makeObject(addr: Addr, fields: Seq[(Idx, V)], cfs: CF): O
  def getField(obj: O, idx: Idx): V
  def setField(obj: O, idx: Idx, v: V): Unit

given ConcreteObjectOps[Addr, Idx, V, CF](using Failure): ObjectOps[Addr, Idx, V, CF, (Addr, CF, scala.collection.mutable.Map[Idx, V])] with

  override def makeObject(addr: Addr, fields: Seq[(Idx, V)], cfs: CF): (Addr, CF, scala.collection.mutable.Map[Idx, V]) =
    (addr, cfs, scala.collection.mutable.Map() ++ fields.toMap)

  override def getField(obj: (Addr, CF, mutable.Map[Idx, V]), idx: Idx): V =
    obj._3.getOrElse(idx, UnboundRecordField(idx).failedLookup(obj._3))

  override def setField(obj: (Addr, CF, mutable.Map[Idx, V]), idx: Idx, v: V): Unit =
    obj._3.map(idx => v)







