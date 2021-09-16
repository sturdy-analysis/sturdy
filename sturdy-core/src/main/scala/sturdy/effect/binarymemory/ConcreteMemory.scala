package sturdy.effect.binarymemory

import sturdy.effect.MayComputeConcrete
import sturdy.effect.MayCompute
import sturdy.effect.MayComputeConcrete
import sturdy.effect.MayComputeConcrete
import sturdy.effect.NoJoin

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


trait ConcreteMemory extends Memory[Int, ByteBuffer, Int]:
  import ConcreteMemory.*
  
  override type MemoryJoin[A] = NoJoin[A]

  protected val memories: ArrayBuffer[Mem] = ArrayBuffer.empty

  override def memRead(memIdx: Int, addr: Int, length: Int): MayComputeConcrete[ByteBuffer] =
    val mem = memories(memIdx)
    if (addr + length < mem.size)
      val buf = ByteBuffer.wrap(mem.bytes, addr, length)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      MayComputeConcrete.Computes(buf)
    else
      MayComputeConcrete.ComputesNot()

  override def memStore(memIdx: Int, addr: Int, buf: ByteBuffer): MayComputeConcrete[Unit] =
    val mem = memories(memIdx)
    val length = buf.capacity()
    if (addr + length < mem.size) {
      buf.get(mem.bytes, addr, length)
      MayComputeConcrete.Computes(())
    } else {
      MayComputeConcrete.ComputesNot()
    }

  override def memSize(memIdx: Int): Int =
    memories(memIdx).size / pageSize

  override def memGrow(memIdx: Int, delta: Int): MayComputeConcrete[Int] =
    val mem = memories(memIdx)
    val newPageNum = mem.pageNum + delta
    if (newPageNum < maxPageNum && mem.sizeLimit.forall(newPageNum < _)) {
      val newBytes = Array.ofDim[Byte](mem.size + delta * pageSize)
      Array.copy(mem.bytes, 0, newBytes, 0, mem.size)
      memories(memIdx) = Mem(newBytes, mem.sizeLimit)
      MayComputeConcrete.Computes(mem.pageNum)
    } else {
      MayComputeConcrete.ComputesNot()
    }


  override def addEmptyMemory(min: Int, max: Option[Int]): Int =
    val mix = memories.length
    memories += Mem(Array.ofDim[Byte](min*pageSize), max)
    mix


object ConcreteMemory:
  case class Mem(bytes: Array[Byte], sizeLimit: Option[Int]):
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536
