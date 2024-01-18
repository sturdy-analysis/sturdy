package sturdy.values.objects

class LiftedObjectOps[Addr, Idx, V, CF, O, UV, UO](extractV: V => UV, extractO: O => UO, injectV: UV => V, injectO: UO => O)(using ops: ObjectOps[Addr, Idx, UV, CF, UO]) extends ObjectOps[Addr, Idx, V, CF, O]:
  override def makeObject(addr: Addr, cfs: CF, fields: Seq[(Idx, V)]): O = injectO(ops.makeObject(addr, cfs, fields.map(fv => fv._1 -> extractV(fv._2))))
  override def setField(obj: O, idx: Idx, v: V): Unit = ops.setField(extractO(obj), idx, extractV(v))
  override def getField(obj: O, idx: Idx): V = injectV(ops.getField(extractO(obj), idx))
