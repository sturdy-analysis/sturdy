package sturdy.values.objects

import sturdy.effect.failure.{Failure, FailureKind}

import scala.collection.mutable

case class UnboundField[F](field: F) extends FailureKind:
  def failedLookup[R](rec: R)(using f: Failure) = f.fail(this, s"while reading $rec")
  def failedUpdate[R](rec: R)(using f: Failure) = f.fail(this, s"while updating $rec")

trait ObjectOps[Addr, Idx, V, CF, O]:
  def makeObject(addr: Addr, cfs: CF, fields: Seq[(Idx, V)]): O
  def getField(obj: O, idx: Idx): V
  def setField(obj: O, idx: Idx, v: V): Unit

given ConcreteObjectOps[Addr, Idx, V, CF](using Failure): ObjectOps[Addr, Idx, V, CF, (Addr, CF, scala.collection.mutable.Map[Idx, V])] with

  override def makeObject(addr: Addr, cfs: CF, fields: Seq[(Idx, V)]): (Addr, CF, scala.collection.mutable.Map[Idx, V]) =
    (addr, cfs, scala.collection.mutable.Map() ++ fields.toMap)

  override def getField(obj: (Addr, CF, mutable.Map[Idx, V]), idx: Idx): V =
    obj._3.getOrElse(idx, UnboundField(idx).failedLookup(obj._3))

  override def setField(obj: (Addr, CF, mutable.Map[Idx, V]), idx: Idx, v: V): Unit =
    obj._3.map(idx => v)







