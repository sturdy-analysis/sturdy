package sturdy.values.arrays

import sturdy.data.{JOption, MayJoin}

class LiftedArrayOps[AID, Idx, V, AV, AType, Context, J[_] <: MayJoin[_], UAV, UIdx]
  (extractA: AV => UAV, injectA: UAV => AV, extractIdx: Idx => UIdx, injectIdx: UIdx => Idx)
  (using ops: ArrayOps[AID, UIdx, V, UAV, AType, Context, J]) extends ArrayOps[AID, Idx, V, AV, AType, Context, J]:
  override def makeArray(ctx: Context)(aid: AID, defaultValue: => V, arrayType: AType, arraySize: Idx): AV = injectA(ops.makeArray(ctx)(aid, defaultValue, arrayType, extractIdx(arraySize)))
  override def get(ctx: Context)(array: AV, idx: Idx): JOption[J, V] = ops.get(ctx)(extractA(array), extractIdx(idx))
  override def set(ctx: Context)(array: AV, idx: Idx, v: V): JOption[J, Unit] = ops.set(ctx)(extractA(array), extractIdx(idx), v)
  override def length(ctx: Context)(array: AV): Idx = injectIdx(ops.length(ctx)(extractA(array)))
