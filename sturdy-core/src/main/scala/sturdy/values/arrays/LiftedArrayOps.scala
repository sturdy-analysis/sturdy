package sturdy.values.arrays

import sturdy.data.{JOption, MayJoin}

class LiftedArrayOps[Addr, AID, Idx, V, AV, Site, J[_] <: MayJoin[_], UAV, UIdx]
  (extractA: AV => UAV, injectA: UAV => AV, extractIdx: Idx => UIdx, injectIdx: UIdx => Idx)
  (using ops: ArrayOps[Addr, AID, UIdx, V, UAV, Site, J]) extends ArrayOps[Addr, AID, Idx, V, AV, Site, J]:
  override def makeArray(aid: AID, vals: Seq[(V, Site)]): AV = injectA(ops.makeArray(aid, vals))
  override def getVal(array: AV, idx: Idx): JOption[J, V] = ops.getVal(extractA(array), extractIdx(idx))
  override def setVal(array: AV, idx: Idx, v: V): JOption[J, Unit] = ops.setVal(extractA(array), extractIdx(idx), v)
  override def arrayLength(array: AV): Int = ops.arrayLength(extractA(array))
  override def initArray(size: Idx): Seq[Any] = ops.initArray(extractIdx(size))


