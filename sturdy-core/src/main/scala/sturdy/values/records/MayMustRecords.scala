package sturdy.values.records

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.values.{JoinValue, joinMayMust, MayMust, PartialOrder, mayMustPO}

import scala.collection.immutable

case class ARecord[F, V](m: Map[F, MayMust[V]]):
  def partitionMustMay: (Iterable[(F, V)], Iterable[(F, V)]) = m.partitionMap {
    case (f, MayMust.Must(v)) => Left(f -> v)
    case (f, MayMust.May(v)) => Right(f -> v)
  }

given mayMustRecordOps[F, V] (using Failure, JoinValue[V])(using j: JoinComputation): RecordOps[F, V, ARecord[F, V]] with
  override def makeRecord(fields: Seq[(F, V)]): ARecord[F, V] =
    var rec = Map[F, MayMust[V]]()
    for ((field, v) <- fields)
      rec += field -> MayMust.Must(v)
    ARecord(rec)
  override def lookupRecordField(rec: ARecord[F, V], field: F): V = rec.m.get(field) match
    case None => UnboundRecordField(field).failedLookup(rec)
    case Some(MayMust.Must(v)) => v
    case Some(MayMust.May(v)) => j.joinComputations(v)(UnboundRecordField(field).failedLookup(rec))
  override def updateRecordField(rec: ARecord[F, V], field: F, newval: V): ARecord[F, V] = rec.m.get(field) match
    case None => UnboundRecordField(field).failedUpdate(rec)
    case Some(MayMust.Must(v)) => ARecord(rec.m + (field -> MayMust.Must(newval)))
    case Some(MayMust.May(v)) => j.joinComputations(ARecord(rec.m + (field -> MayMust.Must(newval))))(UnboundRecordField(field).failedUpdate(rec))

given joinARecord[F, V](using j: JoinValue[V]): JoinValue[ARecord[F, V]] with
  override def joinValues(rec1: ARecord[F, V], rec2: ARecord[F, V]): ARecord[F, V] =
    var joined =  rec1.m
    for ((f, v2) <- rec2.m)
      joined.get(f) match
        case None => joined += f -> MayMust.May(v2.get)
        case Some(v1) =>
          val joinedV = joinMayMust.joinValues(v1, v2)
          joined += f -> joinedV
    ARecord(joined)

given arecordPartialOrder[F, V](using PartialOrder[V]): PartialOrder[ARecord[F, V]] with
  override def lteq(rec1: ARecord[F, V], rec2: ARecord[F, V]): Boolean =
    // records are comparable only if they contain the exact same fields
    if (rec1.m.size != rec2.m.size)
      return false
    for ((f, v1) <- rec1.m) {
      val v2 = rec2.m.get(f).getOrElse(return false)
      if (!mayMustPO.lteq(v1, v2))
        return false
    }
    true
