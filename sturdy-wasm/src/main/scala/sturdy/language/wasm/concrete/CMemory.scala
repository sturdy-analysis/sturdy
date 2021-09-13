package sturdy.language.wasm.concrete

import sturdy.effect.binarymemory.Memory
import sturdy.effect.{CMayCompute, MayCompute, NoJoin}

import java.nio.ByteBuffer
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object CMemory:
  case class Mem(min: Int, max: Option[Int], var size: Long, memory: mutable.HashMap[Long,Byte]):
    def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

import CMemory.*

trait CMemory extends Memory[Long, ByteBuffer, Int]:
  override type MemoryJoin[A] = NoJoin[A]
  override type MemoryJoinComp = Unit

  protected val memories: ArrayBuffer[Mem] = ArrayBuffer.empty

  override def memRead(memIdx: Int, addr: Long, length: Int): CMayCompute[ByteBuffer] =
    val Mem(_,_,size,mem) = memories(memIdx)
    val upper = addr + length
    if (upper < size)
      val buf = Interpreter.allocByteBuffer(length)
      Vector.range(addr,upper).zipWithIndex.foreach {
        (addr,i) => buf.put(i,mem.getOrElse[Byte](addr, 0x00))
      }
      CMayCompute.Computes(buf)
    else
      CMayCompute.ComputesNot()


  override def memStore(memIdx: Int, addr: Long, bytes: ByteBuffer): CMayCompute[Unit] =
    val arr = bytes.array()
    val Mem(_,_,size,mem) = memories(memIdx)
    val upper: Long = addr + arr.length
    if (upper < size)
      arr.zipWithIndex.foreach { (byte,i) =>
        mem(addr+i) = byte
      }
      CMayCompute.Computes(())
    else
      CMayCompute.ComputesNot()

  override def memSize(memIdx: Int): Int =
    (memories(memIdx).size / pageSize).toInt

  override def memGrow(memIdx: Int, delta: Int): MayCompute[Int, NoJoin, Unit] =
    val mem = memories(memIdx)
    val upper = mem.pageNum + delta
    if (upper > maxPageNum || mem.max.exists(_ < upper))
      return CMayCompute.ComputesNot()
    val newSize = mem.size + delta*pageSize
    val oldPageNum = mem.pageNum
    mem.size = newSize
    CMayCompute.Computes(oldPageNum)

  override def addEmptyMemory(min: Int, max: Option[Int]): Int =
    memories += Mem(min, max, min*pageSize, mutable.HashMap())
    memories.length
