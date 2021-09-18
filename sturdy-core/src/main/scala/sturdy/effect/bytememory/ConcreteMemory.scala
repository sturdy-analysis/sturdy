package sturdy.effect.bytememory

import sturdy.data.*

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.mutable


trait ConcreteMemory[Key] extends Memory[Key, Int, ByteBuffer, Int]:
  import ConcreteMemory.*
  
  override type MemoryJoin[A] = NoJoin[A]

  protected val memories: mutable.Map[Key, Mem] = mutable.Map()

  override def memRead(key: Key, addr: Int, length: Int): OptionC[ByteBuffer] =
    val mem = memories(key)
    if (addr + length < mem.size)
      val buf = ByteBuffer.wrap(mem.bytes, addr, length)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      OptionC.Some(buf)
    else
      OptionC.none

  override def memStore(key: Key, addr: Int, buf: ByteBuffer): OptionC[Unit] =
    val mem = memories(key)
    val length = buf.capacity()
    if (addr + length < mem.size) {
      buf.get(mem.bytes, addr, length)
      OptionC.Some(())
    } else {
      OptionC.none
    }

  override def memSize(key: Key): Int =
    memories(key).size / pageSize

  override def memGrow(key: Key, delta: Int): OptionC[Int] =
    val mem = memories(key)
    val newPageNum = mem.pageNum + delta
    if (newPageNum < maxPageNum && mem.sizeLimit.forall(newPageNum < _)) {
      val newBytes = Array.ofDim[Byte](mem.size + delta * pageSize)
      Array.copy(mem.bytes, 0, newBytes, 0, mem.size)
      memories(key) = Mem(newBytes, mem.sizeLimit)
      OptionC.Some(mem.pageNum)
    } else {
      OptionC.none
    }


  override def addEmptyMemory(key: Key, initSize: Int, sizeLimit: scala.Option[Int]): Unit =
    memories(key) = Mem(Array.ofDim[Byte](initSize*pageSize), sizeLimit)


object ConcreteMemory:
  case class Mem(bytes: Array[Byte], sizeLimit: scala.Option[Int]):
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536
