package sturdy.effect.bytememory

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effectful

trait Memory[Key, Addr, Bytes, Size, J[_] <: MayJoin[_]] extends Effectful:
  def read(key: Key, addr: Addr, length: Int): JOption[J, Bytes]
  def write(key: Key, addr: Addr, bytes: Bytes): JOption[J, Unit]
  def size(key: Key): Size
  def grow(key: Key, delta: Size): JOption[J, Size]
  def putNew(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  