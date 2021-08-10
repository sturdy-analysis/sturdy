package sturdy.effect.binarymemory

trait Memory[Addr,Bytes,Size]:
  //type MemoryJoin[A]
  //final type MemoryJoined[A] = MemoryJoin[A] ?=> A

  def memRead[A](memIdx: Int, addr: Addr, length: Int, found: Bytes => A, notFound: => A): A
  def memStore[A](memIdx: Int, addr: Addr, bytes: Bytes, ok: => A, notOk: => A): A
  def memSize(memIdx: Int): Size
  def memGrow[A](memIdx: Int, size: Size, ok: Size => A, notOk: => A): A