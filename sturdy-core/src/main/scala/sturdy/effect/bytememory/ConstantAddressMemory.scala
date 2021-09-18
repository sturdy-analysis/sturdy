package sturdy.effect.bytememory

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.JoinValue
import sturdy.values.Top
import sturdy.values.Topped

import scala.collection.IndexedSeqView
import scala.collection.mutable
import scala.reflect.ClassTag


/** A memory that tracks byte properties `B` for memory accesses via possibly constant addresses `Topped[Int]`. */
trait ConstantAddressMemory[Key, B: ClassTag](using Top[B]) extends Memory[Key, Topped[Int], IndexedSeqView[B], Topped[Int]], Effectful:
  import ConstantAddressMemory.*

  override type MemoryJoin[A] = Join[A]

  protected val memories: mutable.Map[Key, Topped[Mem[B]]] = mutable.Map()

  override def memRead(key: Key, addr: Topped[Int], length: Int): OptionA[IndexedSeqView[B]] =
    (memories(key), addr) match
      case (Topped.Top, _) | (_, Topped.Top) => OptionA.noneSome(Array.fill[B](length)(Top.top).view)
      case (Topped.Actual(mem), Topped.Actual(a)) =>
        if (a + length < mem.size)
          OptionA.some(mem.bytes.view.slice(a, a + length))
        else
          OptionA.none

  override def memStore(key: Key, addr: Topped[Int], bytes: IndexedSeqView[B]): OptionA[Unit] =
    memories(key) match
      case Topped.Top => OptionA.noneSome(())
      case Topped.Actual(mem) => addr match
        case Topped.Top =>
          // any byte of the memory might be affected, set the memory to top
          memories(key) = Topped.Top
          OptionA.noneSome(())
        case Topped.Actual(a) =>
          if (a + bytes.size < mem.size) {
            Array.copy(bytes, 0, mem.bytes, a, bytes.size)
            OptionA.some(())
          } else {
            OptionA.none
          }

  override def memSize(key: Key): Topped[Int] =
    memories(key).map(_.size / pageSize)

  override def memGrow(key: Key, delta: Topped[Int]): OptionA[Topped[Int]] =
    memories(key) match
      case Topped.Top => OptionA.noneSome(Topped.Top)
      case Topped.Actual(mem) => delta match
        case Topped.Top =>
          // cannot track size of memory anymore, set the memory to top
          memories(key) = Topped.Top
          OptionA.noneSome(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum < maxPageNum && mem.sizeLimit.forall(newPageNum < _)) {
            val newBytes = Array.ofDim[B](mem.size + d * pageSize)
            Array.copy(mem.bytes, 0, newBytes, 0, mem.size)
            memories(key) = Topped.Actual(Mem(newBytes, mem.sizeLimit))
            OptionA.some(Topped.Actual(mem.pageNum))
          } else {
            OptionA.none
          }

  override def addEmptyMemory(key: Key, initSize: Topped[Int], sizeLimit: scala.Option[Topped[Int]]): Unit =
    initSize match
      case Topped.Top => // unknown size
        memories(key) = Topped.Top
      case Topped.Actual(size) =>
        memories(key) = Topped.Actual(Mem(Array.ofDim[B](size), sizeLimit.flatMap(_.toOption)))

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    // TODO implement
    ???

object ConstantAddressMemory:
  case class Mem[B](bytes: Array[B], sizeLimit: scala.Option[Int]):
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

