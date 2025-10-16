package sturdy.values.arrays

import sturdy.data.{JOption, MayJoin}

class LiftedArrayOps[AID, Idx, V, AV, AType, Site, Context, J[_] <: MayJoin[_], UAV, UIdx]
  (extractA: AV => UAV, injectA: UAV => AV, extractIdx: Idx => UIdx, injectIdx: UIdx => Idx)
  (using ops: ArrayOps[AID, UIdx, V, UAV, AType, Site, Context, J]) extends ArrayOps[AID, Idx, V, AV, AType, Site, Context, J]:
  override def makeArray(aid: AID, valueSupplier: Int => (V, Site), arrayType: AType, arraySize: Idx): AV = injectA(ops.makeArray(aid, valueSupplier, arrayType, extractIdx(arraySize)))
  override def getVal(ctx: Context)(array: AV, idx: Idx): JOption[J, V] = ops.getVal(ctx)(extractA(array), extractIdx(idx))
  override def setVal(ctx: Context)(array: AV, idx: Idx, v: V): JOption[J, Unit] = ops.setVal(ctx)(extractA(array), extractIdx(idx), v)
  override def arrayLength(ctx: Context)(array: AV): V = ops.arrayLength(ctx)(extractA(array))
