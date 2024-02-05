package sturdy.values.objects

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.store.Store

class LiftedObjectOps[Addr, Idx, OID, V, CF, O, Site, Mth, J[_] <: MayJoin[_], UV, UO]
  (extractV: V => UV, extractO: O => UO, injectV: UV => V, injectO: UO => O)
  (using ops: ObjectOps[Addr, Idx, OID, UV, CF, UO, Site, Mth, J]) extends ObjectOps[Addr, Idx, OID, V, CF, O, Site, Mth, J]:

  override def makeObject(oid: OID, cfs: CF, vals: Seq[(V, Site)]): O = injectO(ops.makeObject(oid, cfs, vals.map(vs => extractV(vs._1) -> vs._2)))
  override def getField(obj: O, idx: Idx): JOption[J, V] = ops.getField(extractO(obj), idx).map(injectV)
  override def setField(obj: O, idx: Idx, v: V): JOption[J, Unit] = ops.setField(extractO(obj), idx, extractV(v))
  //override def invokeFunction(mth: Mth, args: Seq[V], obj: O)(invoke: (Mth, Seq[V], O) => V): V =
  //  injectV(ops.invokeFunction(mth, args.map(extractV(_)), extractO(obj))(invoke))
