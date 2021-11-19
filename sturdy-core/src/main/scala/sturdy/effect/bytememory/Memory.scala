package sturdy.effect.bytememory

import sturdy.data.JOption

trait Memory[Key, Addr, Bytes, Size, MayJoin[_]]:

  def memRead(key: Key, addr: Addr, length: Int): JOption[MayJoin, Bytes]
  def memStore(key: Key, addr: Addr, bytes: Bytes): JOption[MayJoin, Unit]
  def memSize(key: Key): Size
  def memGrow(key: Key, delta: Size): JOption[MayJoin, Size]

  def addEmptyMemory(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  