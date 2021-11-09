package sturdy.effect.bytememory

import sturdy.data.*
import sturdy.values.Top

trait TopMemory[Key, Addr, Bytes, Size](using Top[Size], Top[Bytes]) extends Memory[Key, Addr, Bytes, Size, WithJoin]:
  override def memRead(key: Key, addr: Addr, length: Int): OptionA[Bytes] =
    OptionA.noneSome(Top.top[Bytes])

  override def memStore(key: Key, addr: Addr, bytes: Bytes): OptionA[Unit] =
    OptionA.noneSome(())

  override def memGrow(key: Key, delta: Size): OptionA[Size] =
    OptionA.noneSome(Top.top[Size])

  override def memSize(key: Key): Size = Top.top[Size]

  override def addEmptyMemory(key: Key, initSize: Size, sizeLimit: scala.Option[Size]): Unit = {}
