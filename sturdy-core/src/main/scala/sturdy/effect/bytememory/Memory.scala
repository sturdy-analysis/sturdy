package sturdy.effect.bytememory

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.Effect

/** [[Memory]] describes a mutable memory of bytes, which can be resized. */
trait Memory[Key, Addr, Bytes, Size, J[_] <: MayJoin[_]] extends Effect:
  def read(key: Key, addr: Addr, length: Int, align: Int = 1): JOption[J, Bytes]
  def write(key: Key, addr: Addr, bytes: Bytes, align: Int = 1): JOption[J, Unit]
  def size(key: Key): Size
  def grow(key: Key, delta: Size): JOption[J, Size]
  def fill(key: Key, addr: Addr, byteAmount: Size, value: Bytes): JOption[J, Unit]
  def copy(key: Key, srcAddr: Addr, dstAddr: Addr, byteAmount: Size): JOption[J, Unit]
  def init(key: Key, tableAddr: Addr, dataAddr: Addr, byteAmount: Size, dataBytes: Bytes): JOption[J, Unit]
  def putNew(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit
  