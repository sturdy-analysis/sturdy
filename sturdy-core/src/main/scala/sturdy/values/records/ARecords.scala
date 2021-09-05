package sturdy.values.records

import sturdy.effect.JoinComputation
import sturdy.effect.failure.Failure
import sturdy.fix.Widening
import sturdy.util.{*, given}
import sturdy.values.{JoinValue, MayMust, PartialOrder, Top, joinMayMust, mayMustPO}
import sturdy.values.relational.EqOps
import sturdy.values.Topped

import reflect.Selectable.reflectiveSelectable

enum ARecord[F, V]:
  case Top()
  case Map(m: Predef.Map[F, V])

given ARecordOps[F, V](using Failure, JoinValue[V], Top[V])(using j: JoinComputation): RecordOps[F, V, ARecord[F, V]] with
  override def makeRecord(fields: Seq[(F, V)]): ARecord[F, V] =
    ARecord.Map(fields.toMap)
  override def lookupRecordField(rec: ARecord[F, V], field: F): V = rec match
    case ARecord.Top() => j.joinComputations(Top.top)(UnboundRecordField(field).failedLookup(rec))
    case ARecord.Map(m) => m.get(field) match
      case None => UnboundRecordField(field).failedLookup(rec)
      case Some(v) => v
  override def updateRecordField(rec: ARecord[F, V], field: F, newval: V): ARecord[F, V] = rec match
    case ARecord.Top() =>
      given Lazy[JoinValue[V]] = implicitly
      j.joinComputations(ARecord.Top())(UnboundRecordField(field).failedLookup(rec))
    case ARecord.Map(m) => m.get(field) match
      case None => UnboundRecordField(field).failedUpdate(rec)
      case Some(_) => ARecord.Map(m + (field -> newval))

given ARecordJoin[F, V](using Lazy[JoinValue[V]]): JoinValue[ARecord[F, V]] with
  override def joinValues(rec1: ARecord[F, V], rec2: ARecord[F, V]): ARecord[F, V] = (rec1, rec2) match
    case (ARecord.Top(), _ ) | (_, ARecord.Top()) => ARecord.Top()
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      if (m1.size != m2.size)
        return ARecord.Top()
      var joined =  m1
      for ((f, v2) <- m2)
        joined.get(f) match
          case None => return ARecord.Top()
          case Some(v1) =>
            val joinedV = JoinValue.join(v1, v2)
            joined += f -> joinedV
      ARecord.Map(joined)

given ARecordWidening[F, V](using Lazy[Widening[V]]): Widening[ARecord[F, V]] with
  override def widen(rec1: ARecord[F, V], rec2: ARecord[F, V]): ARecord[F, V] = (rec1, rec2) match
    case (ARecord.Top(), _ ) | (_, ARecord.Top()) => ARecord.Top()
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      if (m1.size != m2.size)
        return ARecord.Top()
      var joined =  m1
      for ((f, v2) <- m2)
        joined.get(f) match
          case None => return ARecord.Top()
          case Some(v1) =>
            val joinedV = Widening.widen(v1, v2)
            joined += f -> joinedV
      ARecord.Map(joined)

given ARecordPartialOrder[F, V](using Lazy[PartialOrder[V]]): PartialOrder[ARecord[F, V]] with
  override def lteq(rec1: ARecord[F, V], rec2: ARecord[F, V]): Boolean = (rec1, rec2) match
    case (_, ARecord.Top()) => true
    case (ARecord.Top(), _) => false
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      // rec1 and rec2 must have the same entries or they are incomparable
      if (m1.size != m2.size)
        return false
      // all entries e1 of rec1 have a corresponding e2 in rec2 that s.t. e1 <= e2
      for ((f, v1) <- m1) {
        val v2 = m2.get(f).getOrElse(return false)
        if (!PartialOrder[V].lteq(v1, v2))
          return false
      }
      true

given ARecordEqOps[F, V, B <: {def asBoolean: Topped[Boolean]}](using Lazy[EqOps[V, B]]): EqOps[ARecord[F, V], Topped[Boolean]] with
  override def equ(rec1: ARecord[F, V], rec2: ARecord[F, V]): Topped[Boolean] = (rec1, rec2) match
    case (ARecord.Top(), _) | (_, ARecord.Top()) => Topped.Top
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      // rec1 and rec2 must have the same entries or they are incomparable
      if (m1.size != m2.size)
        return Topped.Actual(false)

      for ((f, v1) <- m1) {
        val v2 = m2.get(f).getOrElse(return Topped.Actual(false))
        EqOps.equ(v1, v2).asBoolean match
          case Topped.Top => return Topped.Top
          case Topped.Actual(false) => return Topped.Actual(false)
          case _ => // nothing
      }
      Topped.Actual(true)

  override def neq(v1: ARecord[F, V], v2: ARecord[F, V]): Topped[Boolean] = equ(v1, v2).map(!_)