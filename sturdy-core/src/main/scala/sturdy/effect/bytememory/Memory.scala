package sturdy.effect.bytememory

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effect

/** [[Memory]] describes a mutable memory of bytes, which can be resized. */
trait Memory[Key, Addr, Bytes, Size, J[_] <: MayJoin[_]] extends Effect:
  def read(key: Key, addr: Addr, length: Int): JOption[J, Bytes]
  def write(key: Key, addr: Addr, bytes: Bytes): JOption[J, Unit]
  def size(key: Key): Size
  def grow(key: Key, delta: Size): JOption[J, Size]
  def putNew(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  