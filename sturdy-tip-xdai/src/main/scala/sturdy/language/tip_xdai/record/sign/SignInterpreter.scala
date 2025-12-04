package sturdy.language.tip_xdai.record.sign

import sturdy.data.WithJoin
import sturdy.language.tip_xdai.record.{Field, GenericInterpreter}
import sturdy.language.tip_xdai.core.abstractions.BoolValue
import sturdy.language.tip_xdai.core.{TypeError, Value}
import sturdy.language.tip_xdai.record.sign.RecordValue
import sturdy.language.tip_xdai.references.sign.AbstractSignAddr
import sturdy.values.Topped
import sturdy.values.records.{RecordOps, UnboundRecordField}


trait SignInterpreter extends GenericInterpreter[Value, AbstractSignAddr, WithJoin]:
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
