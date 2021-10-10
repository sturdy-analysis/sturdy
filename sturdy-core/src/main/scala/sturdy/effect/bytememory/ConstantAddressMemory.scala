package sturdy.effect.bytememory

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.fix.*
import sturdy.values.*
import sturdy.values.Widening

import scala.collection.mutable
import scala.collection.IndexedSeqView
import scala.reflect.ClassTag

/** A memory that tracks byte properties `B` for memory accesses via possibly constant addresses `Topped[Int]`.
 */
trait ConstantAddressMemory[Key, B: ClassTag](emptyB: B)(using tb: Top[B], jb: Join[B]) extends Memory[Key, Topped[Int], Seq[B], Topped[Int], WithJoin], Effectful:
  import ConstantAddressMemory.{*, given}

  protected var memories: mutable.Map[Key, Topped[Mem[B]]] = mutable.Map()
  
  def getMemories: Memories[Key, B] = memories.view.mapValues(_.map(_.clone())).toMap
  protected def setMemories(s: Memories[Key, B]): Unit =
    memories = mutable.Map() ++ s

  override def memRead(key: Key, addr: Topped[Int], length: Int): OptionA[Seq[B]] =
    (memories(key), addr) match
      case (Topped.Top, _) | (_, Topped.Top) => OptionA.noneSome(Seq.fill[B](length)(Top.top))
      case (Topped.Actual(mem), Topped.Actual(a)) =>
        if (a + length < mem.size) {
          val readBytes = mem.bytes.slice(a, a + length).toSeq
          if (mem.definite)
            OptionA.some(readBytes)
          else
            OptionA.noneSome(readBytes)
        }
        else
          OptionA.none

  override def memStore(key: Key, addr: Topped[Int], bytes: Seq[B]): OptionA[Unit] =
    memories(key) match
      case Topped.Top => OptionA.noneSome(())
      case Topped.Actual(mem) => addr match
        case Topped.Top =>
          // any byte of the memory might be affected, set the memory to top
          //memories += key -> Topped.Top
          mem.bytes.map(_ => tb)
          mem.dirty.addAll(0 until mem.size)
          OptionA.noneSome(())
        case Topped.Actual(a) =>
          if (a + bytes.size < mem.size) {
            Array.copy(bytes.toArray, 0, mem.bytes, a, bytes.size)
            mem.dirty.addAll(a until (a + bytes.size))
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
          memories += key -> Topped.Top
          OptionA.noneSome(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum < maxPageNum && mem.sizeLimit.forall(newPageNum < _)) {
            val newBytes = mem.bytes.appendedAll(Iterable.fill(d * pageSize)(emptyB))
            memories += key -> Topped.Actual(Mem(newBytes, mem.dirty, mem.sizeLimit))
            OptionA.some(Topped.Actual(mem.pageNum))
          } else {
            OptionA.none
          }

  override def addEmptyMemory(key: Key, initSize: Topped[Int], sizeLimit: scala.Option[Topped[Int]]): Unit =
    initSize match
      case Topped.Top => // unknown size
        memories += key ->  Topped.Top
      case Topped.Actual(size) =>
        memories += key -> Topped.Actual(Mem(Array.fill[B](size*pageSize)(emptyB), mutable.BitSet(), sizeLimit.flatMap(_.toOption)))

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    // clones all mutable Mem
    val gmemories = mutable.Map() ++ memories.view.mapValues(_.map(_.clone()))
    super.joinComputations(f) {
      val fmemories = memories
      memories = gmemories
      try g finally {
        for ((key, fmem) <- fmemories) gmemories.get(key) match
          case Some(gmem) => memories += key -> Join(fmem, gmem)
          case None => memories += key -> fmem.map(_.copy(definite = false))

        val fkeys = fmemories.keySet
        for ((key, gmemOpt) <- gmemories)
          if (!fkeys.contains(key))
            memories += key -> gmemOpt.map(_.copy(definite = false))
      }
    }


object ConstantAddressMemory:
  type Memories[Key, B] = Map[Key, Topped[Mem[B]]]

  given CombineMem[B, W <: Widening](using j: Combine[B, W]): Combine[Topped[Mem[B]], W] with
    def apply(old: Topped[Mem[B]], now: Topped[Mem[B]]): Topped[Mem[B]] = (old, now) match
      case (Topped.Top, _) | (_, Topped.Top) => Topped.Top
      case (Topped.Actual(oldMem), Topped.Actual(nowMem)) =>
        if (oldMem.bytes.length != nowMem.bytes.length)
          Topped.Top
        else if (oldMem.dirty.size >= nowMem.dirty.size)
          Topped.Actual(joinSameSized(oldMem, nowMem))
        else
          Topped.Actual(joinSameSized(nowMem, oldMem))

    private def joinSameSized(mem1: Mem[B], mem2: Mem[B]): Mem[B] =
      val result = mem1.clone()
      for (ix <- mem2.dirty)
        result.bytes(ix) = Combine[B, W](result.bytes(ix), mem2.bytes(ix))
      result.dirty |= mem2.dirty
      result

  case class Mem[B](bytes: Array[B], dirty: mutable.BitSet, sizeLimit: scala.Option[Int], definite: Boolean = true):

    override def clone(): Mem[B] = Mem(bytes.clone(), dirty.clone(), sizeLimit)
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt

    override def toString: String = s"Mem(bytesHash=${bytes.toSeq.hashCode()}, dirtyHash=${dirty.hashCode()}, $definite)"

    override def equals(obj: Any): Boolean = obj match
      case that: Mem[_] =>
        Array.equals(this.bytes.asInstanceOf[Array[AnyRef]], that.bytes.asInstanceOf[Array[AnyRef]]) &&
          this.dirty == that.dirty &&
          this.sizeLimit == that.sizeLimit &&
          this.definite == that.definite
      case _ => false

    override def hashCode(): Int = this.bytes.toSeq.hashCode * 31 + this.dirty.hashCode * 17 + sizeLimit.hashCode + definite.hashCode

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

