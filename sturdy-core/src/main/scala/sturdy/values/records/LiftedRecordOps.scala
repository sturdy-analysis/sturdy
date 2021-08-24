package sturdy.values.records

import sturdy.effect.failure.Failure

class LiftedRecordOps[F, V, R, UV, UR](extractR: R => UR, extractV: V => UV, injectR: UR => R, injectV: UV => V)(using ops: RecordOps[F, UV, UR]) extends RecordOps[F, V, R]:
  override def makeRecord(fields: Seq[(F, V)]): R = injectR(ops.makeRecord(fields.map(fv => fv._1 -> extractV(fv._2))))
  override def lookupRecordField(rec: R, field: F): V = injectV(ops.lookupRecordField(extractR(rec), field))
  override def updateRecordField(rec: R, field: F, newval: V): R = injectR(ops.updateRecordField(extractR(rec), field, extractV(newval)))
