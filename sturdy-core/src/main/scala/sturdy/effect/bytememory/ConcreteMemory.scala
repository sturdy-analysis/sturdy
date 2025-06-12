package sturdy.effect.bytememory

import sturdy.data.*
import sturdy.effect.Concrete

import scala.collection.mutable


class ConcreteMemory[Key] extends Memory[Key, Int, Seq[Byte], Int, NoJoin], Concrete:
  import ConcreteMemory.*
  
  protected val memories: mutable.Map[Key, Mem] = mutable.Map()
  
  def getMemories: Map[Key, Mem] = memories.toMap

  override def read(key: Key, addr: Int, length: Int): JOptionC[Seq[Byte]] =
    val mem = memories(key)
    // since collections are indexed by signed integers, we have to check here for addr >= 0
    // this means our maximum memory size is half of the allowed size in wasm
    val end = addr + length
    if (addr >= 0 && end <= mem.size)
      JOptionC.Some(mem.bytes.slice(addr, end).toSeq)
    else
      JOptionC.none

  override def write(key: Key, addr: Int, bytes: Seq[Byte]): JOptionC[Unit] =
    val mem = memories(key)
    if (addr >= 0 && addr + bytes.size <= mem.size) {
      var i = addr
      for (b <- bytes) {
        mem.bytes(i) = b
        i += 1
      }
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  override def size(key: Key): Int =
    memories(key).size / pageSize

  override def grow(key: Key, delta: Int): JOptionC[Int] =
    val mem = memories(key)
    val newPageNum = mem.pageNum + delta
    if (newPageNum <= maxPageNum && mem.sizeLimit.forall(newPageNum <= _)) {
      val newBytes = Array.ofDim[Byte](mem.size + delta * pageSize)
      Array.copy(mem.bytes, 0, newBytes, 0, mem.size)
      memories(key) = Mem(newBytes, mem.sizeLimit)
      JOptionC.Some(mem.pageNum)
    } else {
      JOptionC.none
    }

  override def fill(key: Key, addr: Int, size: Int, value: Seq[Byte]): JOptionC[Unit] =
    val mem = memories(key)
    if (addr >= 0 && addr + size <= mem.size && value.size == 1 && size >= 0) {
      for (i <- 0 until size) {
        mem.bytes(addr + i) = value.head
      }
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  override def copy(key: Key, srcAddr: Int, dstAddr: Int, size: Int): JOptionC[Unit] =
    val mem = memories(key)
    if (srcAddr >= 0 && dstAddr >= 0 && srcAddr + size <= mem.size && dstAddr + size <= mem.size && size >= 0) {
      Array.copy(mem.bytes, srcAddr, mem.bytes, dstAddr, size)
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  override def init(key: Key, tableAddr: Int, dataAddr: Int, byteAmount: Int, dataBytes: Seq[Byte]): JOption[NoJoin, Unit] =
    var mem = memories(key)
    if (tableAddr >= 0 && dataAddr >= 0 && byteAmount >= 0) {
      if (!mem.sizeLimit.forall(lim => tableAddr + byteAmount <= lim * pageSize && dataAddr + byteAmount <= lim * pageSize)) {
        return JOptionC.none
      }
      val newBytes = Array.ofDim[Byte](math.max(tableAddr + byteAmount, mem.size))
      Array.copy(mem.bytes, 0, newBytes, 0, mem.size)
      mem = Mem(newBytes, mem.sizeLimit)
      for (i <- 0 until byteAmount) {
        mem.bytes(tableAddr + i) = dataBytes(dataAddr + i)
      }
      memories(key) = mem
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  override def putNew(key: Key, initSize: Int, sizeLimit: scala.Option[Int]): Unit =
    memories(key) = Mem(Array.ofDim[Byte](initSize*pageSize), sizeLimit)


object ConcreteMemory:
  case class Mem(bytes: Array[Byte], sizeLimit: scala.Option[Int]):
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536
