package sturdy.effect.bytememory

import sturdy.data.*
import sturdy.effect.Stateless
import sturdy.values.Top

class TopMemory[Key, Addr, Bytes, Size](using Top[Size], Top[Bytes]) extends Memory[Key, Addr, Bytes, Size, WithJoin], Stateless:
  override def read(key: Key, addr: Addr, length: Int): JOptionA[Bytes] =
    JOptionA.noneSome(Top.top[Bytes])

  override def write(key: Key, addr: Addr, bytes: Bytes): JOptionA[Unit] =
    JOptionA.noneSome(())

  override def grow(key: Key, delta: Size): JOptionA[Size] =
    JOptionA.noneSome(Top.top[Size])
    
  override def fill(key: Key, addr: Addr, size: Size, value: Bytes): JOptionA[Unit] =
    JOptionA.noneSome(())
    
  override def copy(key: Key, srcAddr: Addr, dstAddr: Addr, size: Size): JOptionA[Unit] =
    JOptionA.noneSome(())

  override def init(key: Key, tableAddr: Addr, dataAddr: Addr, size: Size, dataBytes: Bytes): JOption[WithJoin, Unit] = 
    JOptionA.noneSome(())

  override def size(key: Key): Size = Top.top[Size]

  override def putNew(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit = {}
