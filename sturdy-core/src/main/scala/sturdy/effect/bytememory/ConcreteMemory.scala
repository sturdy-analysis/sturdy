package sturdy.effect.bytememory

import sturdy.data.*

import scala.collection.mutable


class ConcreteMemory[Key] extends Memory[Key, Int, Seq[Byte], Int, NoJoin]:
  import ConcreteMemory.*
  
  protected val memories: mutable.Map[Key, Mem] = mutable.Map()

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


  override def putNew(key: Key, initSize: Int, sizeLimit: scala.Option[Int]): Unit =
    memories(key) = Mem(Array.ofDim[Byte](initSize*pageSize), sizeLimit)

  override type State = Map[Key, Mem]
  override def getState: Map[Key, Mem] = memories.view.mapValues(m => m.copy(bytes = m.bytes.clone())).toMap
  override def setState(s: Map[Key, Mem]): Unit =
    memories.clear()
    memories ++= s

object ConcreteMemory:
  case class Mem(bytes: Array[Byte], sizeLimit: scala.Option[Int]):
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536
