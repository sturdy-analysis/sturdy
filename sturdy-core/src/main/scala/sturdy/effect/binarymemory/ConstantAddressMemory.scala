package sturdy.effect.binarymemory

import sturdy.effect.Effectful
import sturdy.effect.Join
import sturdy.effect.MayCompute
import sturdy.effect.MayComputeOne
import sturdy.values.JoinValue
import sturdy.values.Top
import sturdy.values.Topped

import scala.collection.IndexedSeqView
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/** A memory that tracks byte properties `B` for memory accesses via possibly constant addresses `Topped[Int]`. */
trait ConstantAddressMemory[B: ClassTag](using Top[B]) extends Memory[Topped[Int], IndexedSeqView[B], Topped[Int]], Effectful:
  import ConstantAddressMemory.*

  override type MemoryJoin[A] = Join[A]

  protected val memories: ArrayBuffer[Topped[Mem[B]]] = ArrayBuffer.empty

  override def memRead(memIdx: Int, addr: Topped[Int], length: Int): MayComputeOne[IndexedSeqView[B]] =
    (memories(memIdx), addr) match
      case (Topped.Top, _) | (_, Topped.Top) => MayComputeOne.MaybeComputes(Array.fill[B](length)(Top.top).view)
      case (Topped.Actual(mem), Topped.Actual(a)) =>
        if (a + length < mem.size)
          MayComputeOne.Computes(mem.bytes.view.slice(a, a + length))
        else
          MayComputeOne.ComputesNot()

  override def memStore(memIdx: Int, addr: Topped[Int], bytes: IndexedSeqView[B]): MayComputeOne[Unit] =
    memories(memIdx) match
      case Topped.Top => MayComputeOne.MaybeComputes(())
      case Topped.Actual(mem) => addr match
        case Topped.Top =>
          // any byte of the memory might be affected, set the memory to top
          memories(memIdx) = Topped.Top
          MayComputeOne.MaybeComputes(())
        case Topped.Actual(a) =>
          if (a + bytes.size < mem.size) {
            Array.copy(bytes, 0, mem.bytes, a, bytes.size)
            MayComputeOne.Computes(())
          } else {
            MayComputeOne.ComputesNot()
          }

  override def memSize(memIdx: Int): Topped[Int] =
    memories(memIdx).map(_.size / pageSize)

  override def memGrow(memIdx: Int, delta: Topped[Int]): MayComputeOne[Topped[Int]] =
    memories(memIdx) match
      case Topped.Top => MayComputeOne.MaybeComputes(Topped.Top)
      case Topped.Actual(mem) => delta match
        case Topped.Top =>
          // cannot track size of memory anymore, set the memory to top
          memories(memIdx) = Topped.Top
          MayComputeOne.MaybeComputes(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum < maxPageNum && mem.sizeLimit.forall(newPageNum < _)) {
            val newBytes = Array.ofDim[B](mem.size + d * pageSize)
            Array.copy(mem.bytes, 0, newBytes, 0, mem.size)
            memories(memIdx) = Topped.Actual(Mem(newBytes, mem.sizeLimit))
            MayComputeOne.Computes(Topped.Actual(mem.pageNum))
          } else {
            MayComputeOne.ComputesNot()
          }

  override def addEmptyMemory(min: Int, max: Option[Int]): Int =
    val mix = memories.length
    memories += Topped.Actual(Mem(Array.ofDim[B](min), max))
    mix

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    // TODO implement
    ???

object ConstantAddressMemory:
  case class Mem[B](bytes: Array[B], sizeLimit: Option[Int]):
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

