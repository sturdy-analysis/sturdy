package sturdy.effect.binarymemory

import sturdy.effect.MayCompute

trait Memory[Addr,Bytes,Size]:
  type MemoryJoin[A]
  type MemoryJoinComp

  def memRead(memIdx: Int, addr: Addr, length: Int): MayCompute[Bytes, MemoryJoin, MemoryJoinComp]
  def memStore(memIdx: Int, addr: Addr, bytes: Bytes): MayCompute[Unit, MemoryJoin, MemoryJoinComp]
  def memSize(memIdx: Int): Size
  def memGrow(memIdx: Int, delta: Size): MayCompute[Size, MemoryJoin, MemoryJoinComp]

  def addEmptyMemory(min: Int, max: Option[Int]): Int