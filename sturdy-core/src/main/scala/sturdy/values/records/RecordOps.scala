package sturdy.values.records

import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

case class UnboundRecordField[F](field: F) extends FailureKind:
  def failedLookup[R](rec: R)(using f: Failure) = f.fail(this, s"while reading $rec")
  def failedUpdate[R](rec: R)(using f: Failure) = f.fail(this, s"while updating $rec")

trait RecordOps[F, V, R]:
  def makeRecord(fields: Seq[(F, V)]): R
  def lookupRecordField(rec: R, field: F): V
  def updateRecordField(rec: R, field: F, newval: V): R

given concreteRecordOps[F, V](using Failure): RecordOps[F, V, Map[F, V]] with
  override def makeRecord(fields: Seq[(F, V)]): Map[F, V] = fields.toMap
  override def lookupRecordField(rec: Map[F, V], field: F): V = rec.getOrElse(field, UnboundRecordField(field).failedLookup(rec))
    override def updateRecordField(rec: Map[F, V], field: F, newval: V): Map[F, V] =
    val updated = rec + (field -> newval)
    if (rec.size == updated.size)
      updated
    else
      UnboundRecordField(field).failedUpdate(rec)
