package sturdy.language.tip_xdai.record.concrete

import sturdy.data.MayJoin.NoJoin
import sturdy.language.tip_xdai.core.*
import sturdy.language.tip_xdai.record.{Field, GenericInterpreter}
import sturdy.language.tip_xdai.references.concrete.ConcreteAddr
import sturdy.values.records.{RecordOps, UnboundRecordField}

trait ConcreteInterpreter extends GenericInterpreter[Value, ConcreteAddr, NoJoin]:
  private def unlift(v: Value): RecordValue = v match
    case r: RecordValue => r
    case _ => failure(TypeError, s"Expected Record but got $this")

  override val recOps: RecordOps[Field, Value, Value] = new RecordOps[Field, Value, Value]:
    override def makeRecord(fields: Seq[(Field, Value)]): Value = RecordValue(fields.toMap)
    override def lookupRecordField(rec: Value, field: Field): Value =
      unlift(rec).value.getOrElse(field, UnboundRecordField(field).failedLookup(rec))
    override def updateRecordField(rec: Value, field: Field, newval: Value): Value =
      val m = unlift(rec).value
      val updated = m + (field -> newval)
      if (m.size == updated.size)
        RecordValue(updated)
      else
        UnboundRecordField(field).failedUpdate(rec)
