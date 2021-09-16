package sturdy.effect.binarymemory

import sturdy.effect.MayCompute

trait Memory[Addr,Bytes,Size]:
  type MemoryJoin[A]

  def memRead(memIdx: Int, addr: Addr, length: Int): MayCompute[MemoryJoin, Bytes]
  def memStore(memIdx: Int, addr: Addr, bytes: Bytes): MayCompute[MemoryJoin, Unit]
  def memSize(memIdx: Int): Size
  def memGrow(memIdx: Int, delta: Size): MayCompute[MemoryJoin, Size]

  def addEmptyMemory(min: Int, max: Option[Int]): Int