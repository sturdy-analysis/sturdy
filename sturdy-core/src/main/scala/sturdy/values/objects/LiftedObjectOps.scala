package sturdy.values.objects

import sturdy.data.MayJoin
import sturdy.effect.store.Store

class LiftedObjectOps[Addr, Idx, V, CF, J[_] <: MayJoin[_], O, UV, UO](extractV: V => UV, extractO: O => UO, injectV: UV => V, injectO: UO => O, extractStore: Store[Idx, V, J] => Store[Idx, UV, J])(using ops: ObjectOps[Addr, Idx, UV, CF, J, UO]) extends ObjectOps[Addr, Idx, V, CF, J, O]:
  override def makeObject(cfs: CF, store: Store[Idx, V, J]): O = injectO(ops.makeObject(cfs, extractStore(store)))
  override def setField(obj: O, idx: Idx, v: V): Unit = ops.setField(extractO(obj), idx, extractV(v))
  override def getField(obj: O, idx: Idx): V = injectV(ops.getField(extractO(obj), idx))
