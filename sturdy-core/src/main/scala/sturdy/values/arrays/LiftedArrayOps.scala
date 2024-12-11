package sturdy.values.arrays

import sturdy.data.{JOption, MayJoin}

class LiftedArrayOps[AID, Idx, V, AV, AType, Site, J[_] <: MayJoin[_], UAV, UIdx]
  (extractA: AV => UAV, injectA: UAV => AV, extractIdx: Idx => UIdx, injectIdx: UIdx => Idx)
  (using ops: ArrayOps[AID, UIdx, V, UAV, AType, Site, J]) extends ArrayOps[AID, Idx, V, AV, AType, Site, J]:
  override def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType, arraySize: V): AV = injectA(ops.makeArray(aid, vals, arrayType, arraySize))
  override def getVal(array: AV, idx: Idx): JOption[J, V] = ops.getVal(extractA(array), extractIdx(idx))
  override def setVal(array: AV, idx: Idx, v: V): JOption[J, Unit] = ops.setVal(extractA(array), extractIdx(idx), v)
  override def arrayLength(array: AV): V = ops.arrayLength(extractA(array))
  override def initArray(size: Idx): Seq[Any] = ops.initArray(extractIdx(size))
  override def arraycopy(src: AV, srcPos: Idx, dest: AV, destPos: Idx, length: Idx): JOption[J, Unit] =
    ops.arraycopy(extractA(src), extractIdx(srcPos), extractA(dest), extractIdx(destPos), extractIdx(length))
  override def getArray(array: AV): Seq[JOption[J, V]] = ops.getArray(extractA(array))

  override def printString(letters: Seq[Idx]): Unit = ops.printString(letters.map(elem => extractIdx(elem)))



