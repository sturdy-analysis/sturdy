package sturdy.values.records

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.ordering.EqOps

import scala.collection.immutable

case class MayMustRecord[F, V](m: Map[F, MayMust[V]]):
  def partitionMustMay: (Iterable[(F, V)], Iterable[(F, V)]) = m.partitionMap {
    case (f, MayMust.Must(v)) => Left(f -> v)
    case (f, MayMust.May(v)) => Right(f -> v)
  }

given mayMustRecordOps[F, V](using Failure, Join[V])(using j: EffectStack): RecordOps[F, V, MayMustRecord[F, V]] with
  override def makeRecord(fields: Seq[(F, V)]): MayMustRecord[F, V] =
    var rec = Map[F, MayMust[V]]()
    for ((field, v) <- fields)
      rec += field -> MayMust.Must(v)
    MayMustRecord(rec)
  override def lookupRecordField(rec: MayMustRecord[F, V], field: F): V = rec.m.get(field) match
    case None => UnboundRecordField(field).failedLookup(rec)
    case Some(MayMust.Must(v)) => v
    case Some(MayMust.May(v)) => j.joinWithFailure(v)(UnboundRecordField(field).failedLookup(rec))
  override def updateRecordField(rec: MayMustRecord[F, V], field: F, newval: V): MayMustRecord[F, V] = rec.m.get(field) match
    case None => UnboundRecordField(field).failedUpdate(rec)
    case Some(MayMust.Must(v)) => MayMustRecord(rec.m + (field -> MayMust.Must(newval)))
    case Some(MayMust.May(v)) => j.joinWithFailure(MayMustRecord(rec.m + (field -> MayMust.Must(newval))))(UnboundRecordField(field).failedUpdate(rec))

given CombineMayMustRecord[F, V, W <: Widening](using j: Combine[V, W]): Combine[MayMustRecord[F, V], W] with
  override def apply(rec1: MayMustRecord[F, V], rec2: MayMustRecord[F, V]): MaybeChanged[MayMustRecord[F, V]] =
    var joined =  rec1.m
    var changed = false
    for ((f, v2) <- rec2.m)
      joined.get(f) match
        case None =>
          joined += f -> MayMust.May(v2.get)
          changed = true
        case Some(v1) =>
          Combine[MayMust[V], W](v1, v2).ifChanged { changedV =>
            joined += f -> changedV
            changed = true
          }
    MaybeChanged(MayMustRecord(joined), changed)

given MayMustRecordPO[F, V](using PartialOrder[V]): PartialOrder[MayMustRecord[F, V]] with
  override def lteq(rec1: MayMustRecord[F, V], rec2: MayMustRecord[F, V]): Boolean =
    // all must entries of rec2 have a must entry in rec1
    for ((f, v1) <- rec2.m; if v1.isMust)
      if (!rec1.m.get(f).getOrElse(return false).isMust)
        return false
    // all entries e1 of rec1 have a corresponding e2 in rec2 that s.t. e1 <= e2
    for ((f, v1) <- rec1.m) {
      val v2 = rec2.m.get(f).getOrElse(return false)
      if (!mayMustPO.lteq(v1, v2))
        return false
    }
    true
