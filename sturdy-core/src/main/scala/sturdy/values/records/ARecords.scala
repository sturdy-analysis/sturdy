package sturdy.values.records

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.util.*
import sturdy.values.*
import sturdy.values.booleans.{BooleanBranching, BooleanSelection}
import sturdy.values.relational.EqOps

import reflect.Selectable.reflectiveSelectable

enum ARecord[F, V]:
  case Top()
  case Map(m: Predef.Map[F, V])

given ARecordOps[F, V](using Failure, Join[V], Top[V])(using j: EffectStack): RecordOps[F, V, ARecord[F, V]] with
  override def makeRecord(fields: Seq[(F, V)]): ARecord[F, V] =
    ARecord.Map(fields.toMap)
  override def lookupRecordField(rec: ARecord[F, V], field: F): V = rec match
    case ARecord.Top() => j.joinWithFailure(Top.top)(UnboundRecordField(field).failedLookup(rec))
    case ARecord.Map(m) => m.get(field) match
      case None => UnboundRecordField(field).failedLookup(rec)
      case Some(v) => v
  override def updateRecordField(rec: ARecord[F, V], field: F, newval: V): ARecord[F, V] = rec match
    case ARecord.Top() =>
      given Lazy[Join[V]] = lazily(implicitly)
      j.joinWithFailure(ARecord.Top())(UnboundRecordField(field).failedLookup(rec))
    case ARecord.Map(m) => m.get(field) match
      case None => UnboundRecordField(field).failedUpdate(rec)
      case Some(_) => ARecord.Map(m + (field -> newval))

given CombineARecord[F, V, W <: Widening](using l: Lazy[Combine[V, W]]): Combine[ARecord[F, V], W] with
  override def apply(rec1: ARecord[F, V], rec2: ARecord[F, V]): MaybeChanged[ARecord[F, V]] = (rec1, rec2) match
    case (ARecord.Top(), _ ) => Unchanged(rec1)
    case (_, ARecord.Top()) => Changed(rec2)
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      if (m1.size != m2.size)
        return Changed(ARecord.Top())
      var joined =  m1
      var changed = false
      for ((f, v2) <- m2)
        joined.get(f) match
          case None => return Changed(ARecord.Top())
          case Some(v1) =>
            l.force(v1, v2).ifChanged { changedV =>
              joined += f -> changedV
              changed = true
            }
      MaybeChanged(ARecord.Map(joined), changed)

  override def lteq(x: ARecord[F, V], y: ARecord[F, V]): Boolean = (x,y) match
    case (_,ARecord.Top()) => true
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      m1.keySet == m2.keySet &&
      m1.forall((k1,v1) =>
        l.force.lteq(v1, m2(k1))
      )

given FiniteARecord[F, V](using Finite[F], Lazy[Finite[V]]): Finite[ARecord[F, V]] with {}

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
        val v2 = m2.getOrElse(f, return false)
        if (!PartialOrder[V](using force).lteq(v1, v2))
          return false
      }
      true

given ARecordEqOps[F, V, B](using Lazy[EqOps[V, B]], BooleanSelection[B, Topped[Boolean]]): EqOps[ARecord[F, V], Topped[Boolean]] with
  override def equ(rec1: ARecord[F, V], rec2: ARecord[F, V]): Topped[Boolean] = (rec1, rec2) match
    case (ARecord.Top(), _) | (_, ARecord.Top()) => Topped.Top
    case (ARecord.Map(m1), ARecord.Map(m2)) =>
      // rec1 and rec2 must have the same entries or they are incomparable
      if (m1.size != m2.size)
        return Topped.Actual(false)

      for ((f, v1) <- m1) {
        val v2 = m2.getOrElse(f, return Topped.Actual(false))
        val b = EqOps.equ(v1, v2)(using force)
        BooleanSelection(b, Topped.Actual(true), Topped.Actual(false)) match
          case Topped.Top => return Topped.Top
          case Topped.Actual(false) => return Topped.Actual(false)
          case _ => // nothing
      }
      Topped.Actual(true)

  override def neq(v1: ARecord[F, V], v2: ARecord[F, V]): Topped[Boolean] = equ(v1, v2).map(!_)