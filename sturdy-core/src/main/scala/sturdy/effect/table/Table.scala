package sturdy.effect.table

import sturdy.effect.MayCompute

trait Table[Idx,Elem]:
  type TableJoin[A]

  def tableGet(tabIdx: Int, idx: Idx): MayCompute[TableJoin, Elem]
  def tableSet(tabIdx: Int, idx: Idx, elem: Elem): MayCompute[TableJoin, Unit]
  
  def addEmptyTable(min: Int, max: Option[Int]): Int

  // missing for new wasm version: tableSize, tableGrow, tableFill, tableCopy, tableInit
