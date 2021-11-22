package sturdy.effect.bytememory

import sturdy.data.*
import sturdy.effect.Stateless
import sturdy.values.Top

class TopMemory[Key, Addr, Bytes, Size](using Top[Size], Top[Bytes]) extends Memory[Key, Addr, Bytes, Size, WithJoin], Stateless:
  override def memRead(key: Key, addr: Addr, length: Int): JOptionA[Bytes] =
    JOptionA.noneSome(Top.top[Bytes])

  override def memWrite(key: Key, addr: Addr, bytes: Bytes): JOptionA[Unit] =
    JOptionA.noneSome(())

  override def memGrow(key: Key, delta: Size): JOptionA[Size] =
    JOptionA.noneSome(Top.top[Size])

  override def memSize(key: Key): Size = Top.top[Size]

  override def addEmptyMemory(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit = {}
