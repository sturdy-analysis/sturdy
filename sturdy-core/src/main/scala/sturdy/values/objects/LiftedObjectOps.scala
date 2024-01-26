package sturdy.values.objects

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[Addr, Idx, V, CF, O, Site, J[_] <: MayJoin[_], UV, UO]
  (extractV: V => UV, extractO: O => UO, injectV: UV => V, injectO: UO => O)
  (using ops: ObjectOps[Addr, Idx, UV, CF, UO, Site, J]) extends ObjectOps[Addr, Idx, V, CF, O, Site, J]:

  override def makeObject(cfs: CF, vals: Seq[(V, Site)]): O = injectO(ops.makeObject(cfs, vals.map(vs => extractV(vs._1) -> vs._2)))
  override def getField(obj: O, idx: Idx): JOption[J, V] = ops.getField(extractO(obj), idx).map(injectV)
  override def setField(obj: O, idx: Idx, v: V): JOption[J, Unit] = ops.setField(extractO(obj), idx, extractV(v))
