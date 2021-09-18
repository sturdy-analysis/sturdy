package sturdy.effect.bytememory

import sturdy.data.Option

trait Memory[Key,Addr,Bytes,Size]:
  type MemoryJoin[A]

  def memRead(key: Key, addr: Addr, length: Int): Option[MemoryJoin, Bytes]
  def memStore(key: Key, addr: Addr, bytes: Bytes): Option[MemoryJoin, Unit]
  def memSize(key: Key): Size
  def memGrow(key: Key, delta: Size): Option[MemoryJoin, Size]

  def addEmptyMemory(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  