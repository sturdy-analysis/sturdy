package sturdy.effect.bytememory

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effectful

trait Memory[Key, Addr, Bytes, Size, J[_] <: MayJoin[_]] extends Effectful:

  def memRead(key: Key, addr: Addr, length: Int): JOption[J, Bytes]
  def memWrite(key: Key, addr: Addr, bytes: Bytes): JOption[J, Unit]
  def memSize(key: Key): Size
  def memGrow(key: Key, delta: Size): JOption[J, Size]

  def addEmptyMemory(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  