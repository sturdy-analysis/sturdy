package sturdy.values.objects

import sturdy.effect.failure.Failure
import sturdy.values.records.UnboundRecordField

import scala.collection.mutable

trait ObjectOps[Addr, F, V, CF, O]:
  def makeObject(addr: Addr, fields: Seq[(F, V)], cfs: CF): O
  def getField(obj: O, idx: F): V
  def setField(obj: O, idx: F, v: V): Unit

given concreteObjectsOps[Addr, F, V, CF](using Failure): ObjectOps[Addr, F, V, CF, (Addr, CF, scala.collection.mutable.Map[F, V])] with

  override def makeObject(addr: Addr, fields: Seq[(F, V)], cfs: CF): (Addr, CF, scala.collection.mutable.Map[F, V]) =
    (addr, cfs, scala.collection.mutable.Map() ++ fields.toMap)

  override def getField(obj: (Addr, CF, mutable.Map[F, V]), idx: F): V =
    obj._3.getOrElse(idx, UnboundRecordField(idx).failedLookup(obj._3))

  override def setField(obj: (Addr, CF, mutable.Map[F, V]), idx: F, v: V): Unit =
    obj._3.map(idx => v)







