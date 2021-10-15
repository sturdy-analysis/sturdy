package sturdy.effect.bytememory

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.*
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.Effectful
import sturdy.effect.TrySturdy
import sturdy.fix.*
import sturdy.values.*

import scala.collection.mutable
import scala.collection.IndexedSeqView
import scala.reflect.ClassTag

/** A memory that tracks byte properties `B` for memory accesses via possibly constant addresses `Topped[Int]`.
 */
trait ConstantAddressMemory[Key, B: ClassTag](emptyB: B)(using tb: Top[B], jb: Join[B]) extends Memory[Key, Topped[Int], Seq[B], Topped[Int], WithJoin], Effectful:
  import ConstantAddressMemory.{*, given}

  protected var memories: mutable.Map[Key, Topped[Mem[B]]] = mutable.Map()

  def getMemories: Memories[Key, B] = memories.view.mapValues(_.map(_.cloned)).toMap
  protected def setMemories(s: Memories[Key, B]): Unit =
    memories = mutable.Map() ++ s

  override def memRead(key: Key, addr: Topped[Int], length: Int): OptionA[Seq[B]] =
    (memories(key), addr) match
      case (Topped.Top, _) | (_, Topped.Top) => OptionA.noneSome(Seq.fill[B](length)(Top.top))
      case (Topped.Actual(mem: SizeMem), Topped.Actual(a)) =>
        if (a >=0 && a + length <= mem.size) {
          val readBytes = Seq.fill[B](length)(Top.top)
          if (mem.definite)
            OptionA.some(readBytes)
          else
            OptionA.noneSome(readBytes)
        }
        else
          OptionA.none
      case (Topped.Actual(mem: ByteMem[_]), Topped.Actual(a)) =>
        if (a >=0 && a + length <= mem.size) {
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
      case Topped.Actual(mem: SizeMem) => addr match
        case Topped.Top =>
          // any byte of the memory might be affected, but this memory does not track individual bytes anyway
          OptionA.noneSome(())
        case Topped.Actual(a) =>
          if (a >= 0 && a + bytes.size <= mem.size) {
            OptionA.some(())
          } else {
            OptionA.none
          }
      case Topped.Actual(mem: ByteMem[_]) => addr match
        case Topped.Top =>
          // any byte of the memory might be affected, only track the memory's size from now on
          memories += key -> Topped.Actual(SizeMem(mem.size, mem.sizeLimit, mem.definite))
          OptionA.noneSome(())
        case Topped.Actual(a) =>
          if (a >= 0 && a + bytes.size <= mem.size) {
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
      case Topped.Actual(mem: SizeMem) => delta match
        case Topped.Top =>
          // cannot track size of memory anymore, set the memory to top
          memories += key -> Topped.Top
          OptionA.noneSome(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum <= maxPageNum && mem.sizeLimit.forall(newPageNum <= _)) {
            memories += key -> Topped.Actual(SizeMem(mem.size + d * pageSize, mem.sizeLimit, mem.definite))
            OptionA.some(Topped.Actual(mem.pageNum))
          } else {
            OptionA.none
          }
      case Topped.Actual(mem: ByteMem[_]) => delta match
        case Topped.Top =>
          // cannot track size of memory anymore, set the memory to top
          memories += key -> Topped.Top
          OptionA.noneSome(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum <= maxPageNum && mem.sizeLimit.forall(newPageNum <= _)) {
            val newBytes = mem.bytes.appendedAll(Iterable.fill(d * pageSize)(emptyB))
            memories += key -> Topped.Actual(ByteMem(newBytes, mem.dirty, mem.sizeLimit, mem.definite))
            OptionA.some(Topped.Actual(mem.pageNum))
          } else {
            OptionA.none
          }

  override def addEmptyMemory(key: Key, initSize: Topped[Int], sizeLimit: scala.Option[Topped[Int]]): Unit =
    initSize match
      case Topped.Top => // unknown size
        memories += key ->  Topped.Top
      case Topped.Actual(size) =>
        memories += key -> Topped.Actual(ByteMem(Array.fill[B](size*pageSize)(emptyB), mutable.BitSet(), sizeLimit.flatMap(_.toOption)))

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    var gmemories = mutable.Map() ++ memories.view.mapValues(_.map(_.cloned))
    var fmemories: mutable.Map[Key, Topped[Mem[B]]] = null

    override def inbetween_(): Unit =
      fmemories = memories
      memories = gmemories

    override def retainOnlyFirst_(fRes: TrySturdy[A]): Unit =
      memories = fmemories
      gmemories = null

    override def retainOnlySecond_(gRes: TrySturdy[A]): Unit =
      fmemories = null

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for ((key, fmem) <- fmemories) gmemories.get(key) match
        case Some(gmem) => memories += key -> Join(fmem, gmem).get
        case None => memories += key -> fmem.map(_.toIndefinite)

      val fkeys = fmemories.keySet
      for ((key, gmemOpt) <- gmemories)
        if (!fkeys.contains(key))
          memories += key -> gmemOpt.map(_.toIndefinite)
      fmemories = null
  }

  def memoryIsSound(c: ConcreteMemory[Key])(using Soundness[Byte, B]): IsSound =
    // soundess for memory:
    //  - all concrete memories are present in abstract memories
    //  - all definite abstract memores have a concrete counterpart
    //  - for each key in concrete memories: mems(key) is sound
    val cMemories = c.getMemories
    memories.filterNot{ (key, _) => cMemories.isDefinedAt(key)}.foreachEntry { (k, aMem) => aMem match
      case Topped.Actual(mem) =>
        if (!mem.isIndefinite)
          return IsSound.NotSound(s"Definite memory with key $k not present in concrete memory.")
      case _ =>
    }
    c.getMemories.foreachEntry { (key, cMem) =>
      val aMem = memories.getOrElse(key, { return IsSound.NotSound(s"Key $key not present in constant address memory.") })
      val memSound = memInstanceIsSound(cMem, aMem)
      if (memSound.isNotSound)
        return memSound
    }
    IsSound.Sound


  def memInstanceIsSound(c: ConcreteMemory.Mem, a: Topped[Mem[B]])(using bytesSound: Soundness[Byte, B]): IsSound =
    // - sizes are equal
    // - all locations in concrete memory are approximated by locations in abstract memory
    a match
      case Topped.Top => IsSound.Sound
      case Topped.Actual(SizeMem(size, sizeLimit, _)) =>
        if (c.size == size && c.sizeLimit == sizeLimit)
          IsSound.Sound
        else
          IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide.")
      case Topped.Actual(aMem@ByteMem(bytes, dirty, sizeLimit, _)) =>
        if (c.size != aMem.size || c.sizeLimit != sizeLimit)
          return IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide.")
        c.bytes.zip(bytes).foreach { (cByte, aByte) =>
          val bSound = bytesSound.isSound(cByte, aByte)
          if (bSound.isNotSound)
            return IsSound.NotSound(s"Byte $cByte is not approximated by $aByte.")
        }
        IsSound.Sound


object ConstantAddressMemory:
  type Memories[Key, B] = Map[Key, Topped[Mem[B]]]

  given CombineMem[B, W <: Widening](using j: Combine[B, W]): Combine[Topped[Mem[B]], W] with
    def apply(old: Topped[Mem[B]], now: Topped[Mem[B]]): MaybeChanged[Topped[Mem[B]]] = (old, now) match
      case (Topped.Top, _) | (_, Topped.Top) => Unchanged(Topped.Top)
      case (Topped.Actual(oldMem), Topped.Actual(nowMem)) if oldMem.size != nowMem.size =>
        Changed(Topped.Top)
      case (Topped.Actual(oldMem: SizeMem), Topped.Actual(nowMem: SizeMem)) =>
        Unchanged(Topped.Actual(oldMem))
      case (Topped.Actual(oldMem: SizeMem), Topped.Actual(nowMem: ByteMem[_])) =>
        Unchanged(Topped.Actual(oldMem))
      case (Topped.Actual(oldMem: ByteMem[_]), Topped.Actual(nowMem: SizeMem)) =>
        Changed(Topped.Actual(nowMem))
      case (Topped.Actual(oldMem: ByteMem[_]), Topped.Actual(nowMem: ByteMem[_])) =>
        if (oldMem.dirty.size >= nowMem.dirty.size)
          joinSameSized(oldMem.asInstanceOf[ByteMem[B]], nowMem.asInstanceOf[ByteMem[B]]).map(Topped.Actual.apply)
        else
          joinSameSized(nowMem.asInstanceOf[ByteMem[B]], oldMem.asInstanceOf[ByteMem[B]]).map(Topped.Actual.apply)

    private def joinSameSized(mem1: ByteMem[B], mem2: ByteMem[B]): MaybeChanged[ByteMem[B]] =
      val result = mem1.cloned
      var changed = false
      for (ix <- mem2.dirty) {
        val b = Combine[B, W](result.bytes(ix), mem2.bytes(ix))
        if (b.hasChanged) {
          result.bytes(ix) = b.get
          changed = true
        }
      }
      result.dirty |= mem2.dirty
      MaybeChanged(result, changed)

  sealed trait Mem[+B]:
    def cloned: Mem[B]
    def size: Int
    def toIndefinite: Mem[B]
    def isIndefinite: Boolean

  case class SizeMem(size: Int, sizeLimit: scala.Option[Int], definite: Boolean = true) extends Mem[Nothing]:
    override def cloned = this
    inline def pageNum: Int = (size / pageSize).toInt
    override def toIndefinite: SizeMem = SizeMem(size, sizeLimit, false)
    override def isIndefinite: Boolean = definite

  case class ByteMem[B](bytes: Array[B], dirty: mutable.BitSet, sizeLimit: scala.Option[Int], definite: Boolean = true) extends Mem[B]:

    override def cloned: ByteMem[B] = ByteMem(bytes.clone(), dirty.clone(), sizeLimit)
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt
    override def toIndefinite: ByteMem[B] = ByteMem(bytes, dirty, sizeLimit, false)
    override def isIndefinite: Boolean = definite

    override def toString: String = s"Mem(${bytes.size} bytes, ${dirty.size} dirty addresses, definite=$definite)"

    override def equals(obj: Any): Boolean = obj match
      case that: ByteMem[_] =>
        Array.equals(this.bytes.asInstanceOf[Array[AnyRef]], that.bytes.asInstanceOf[Array[AnyRef]]) &&
          this.dirty == that.dirty &&
          this.sizeLimit == that.sizeLimit &&
          this.definite == that.definite
      case _ => false

    override def hashCode(): Int = this.bytes.toSeq.hashCode * 31 + this.dirty.hashCode * 17 + sizeLimit.hashCode + definite.hashCode

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

