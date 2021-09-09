package sturdy.effect.table

import sturdy.effect.MayCompute

trait Table[Idx,Elem]:
  type TableJoin[A]
  type TableJoinComp

  def tableGet(tabIdx: Int, idx: Idx): MayCompute[Elem, TableJoin, TableJoinComp]
  def tableSet(tabIdx: Int, idx: Idx, elem: Elem): MayCompute[Unit, TableJoin, TableJoinComp]
  
  def addEmptyTable(min: Int, max: Option[Int]): Int

  // missing for new wasm version: tableSize, tableGrow, tableFill, tableCopy, tableInit
