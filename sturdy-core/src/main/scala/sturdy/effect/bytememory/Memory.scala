package sturdy.effect.bytememory

import sturdy.data.Option

trait Memory[Key, Addr, Bytes, Size, MayJoin[_]]:

  def memRead(key: Key, addr: Addr, length: Int): Option[MayJoin, Bytes]
  def memStore(key: Key, addr: Addr, bytes: Bytes): Option[MayJoin, Unit]
  def memSize(key: Key): Size
  def memGrow(key: Key, delta: Size): Option[MayJoin, Size]

  def addEmptyMemory(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  