package sturdy.effect.binarymemory

import sturdy.effect.MayCompute

trait Memory[Addr,Bytes,Size]:
  type MemoryJoin[A]
  type MemoryJoinComp
  //final type MemoryJoined[A] = MemoryJoin[A] ?=> A

  //def memRead[A](memIdx: Int, addr: Addr, length: Int, found: Bytes => A, notFound: => A): A
  def memRead(memIdx: Int, addr: Addr, length: Int): MayCompute[Bytes, MemoryJoin, MemoryJoinComp]
  //def memStore[A](memIdx: Int, addr: Addr, bytes: Bytes, ok: => A, notOk: => A): A
  def memStore(memIdx: Int, addr: Addr, bytes: Bytes): MayCompute[Unit, MemoryJoin, MemoryJoinComp]
  def memSize(memIdx: Int): Size
  //def memGrow[A](memIdx: Int, size: Size, ok: Size => A, notOk: => A): A
  def memGrow(memIdx: Int, delta: Size): MayCompute[Size, MemoryJoin, MemoryJoinComp]

  def addEmptyMemory(min: Int, max: Option[Int]): Int