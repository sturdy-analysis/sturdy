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

  protected var memories: mutable.Map[Key, Mem[B]] = mutable.Map()

  def getMemories: Memories[Key, B] = memories.view.mapValues(_.cloned).toMap
  protected def setMemories(s: Memories[Key, B]): Unit =
    memories = mutable.Map() ++ s

  private def joinSeq(s: Seq[B]): B =
    var res = emptyB
    s.foreach { b =>
      res = jb(res, b).get
    }
    res

  override def memRead(key: Key, addr: Topped[Int], length: Int): OptionA[Seq[B]] =
    (memories(key), addr) match
      case (mem: ValueMem[_], _) => OptionA.noneSome(Seq.fill[B](length)(mem.upperBound))
      case (mem: SizeMem[_], Topped.Top) =>
        OptionA.noneSome(Seq.fill[B](length)(mem.upperBound))
      case (mem: SizeMem[_], Topped.Actual(a)) =>
        if (a >=0 && a + length <= mem.size) {
          val readBytes = Seq.fill[B](length)(mem.upperBound)
          if (mem.definite)
            OptionA.some(readBytes)
          else
            OptionA.noneSome(readBytes)
        }
        else
          OptionA.none
      case (mem: ByteMem[_], Topped.Top) =>
        OptionA.noneSome(Seq.fill[B](length)(mem.upperBound))
      case (mem: ByteMem[_], Topped.Actual(a)) =>
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
      case mem: ValueMem[_] =>
        mem.upperBound = jb(mem.upperBound, joinSeq(bytes)).get
        OptionA.noneSome(())
      case mem: SizeMem[_] => addr match
        case Topped.Top =>
          mem.upperBound = jb(mem.upperBound, joinSeq(bytes)).get
          // any byte of the memory might be affected, but this memory does not track individual bytes anyway
          OptionA.noneSome(())
        case Topped.Actual(a) =>
          if (a >= 0 && a + bytes.size <= mem.size) {
            mem.upperBound = jb(mem.upperBound, joinSeq(bytes)).get
            OptionA.some(())
          } else {
            OptionA.none
          }
      case mem: ByteMem[_] => addr match
        case Topped.Top =>
          // any byte of the memory might be affected, only track the memory's size from now on
          val newUpperBound = jb(mem.upperBound, joinSeq(bytes)).get
          memories += key -> SizeMem(mem.size, mem.sizeLimit, newUpperBound, mem.definite)
          OptionA.noneSome(())
        case Topped.Actual(a) =>
          if (a >= 0 && a + bytes.size <= mem.size) {
            mem.upperBound = jb(mem.upperBound, joinSeq(bytes)).get
            Array.copy(bytes.toArray, 0, mem.bytes, a, bytes.size)
            mem.dirty.addAll(a until (a + bytes.size))
            OptionA.some(())
          } else {
            OptionA.none
          }

  override def memSize(key: Key): Topped[Int] = memories(key) match
    case _: ValueMem[_] => Topped.Top
    case mem: SizedMem[_] => Topped.Actual(mem.size / pageSize)

  override def memGrow(key: Key, delta: Topped[Int]): OptionA[Topped[Int]] =
    memories(key) match
      case _: ValueMem[_] => OptionA.noneSome(Topped.Top)
      case mem: SizeMem[_] => delta match
        case Topped.Top =>
          // cannot track size of memory anymore, set the memory to top
          memories += key -> ValueMem(mem.upperBound, mem.definite)
          OptionA.noneSome(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum <= maxPageNum && mem.sizeLimit.forall(newPageNum <= _)) {
            val newUpperBound = jb(mem.upperBound, emptyB).get
            memories += key -> SizeMem(mem.size + d * pageSize, mem.sizeLimit, newUpperBound, mem.definite)
            OptionA.some(Topped.Actual(mem.pageNum))
          } else {
            OptionA.none
          }
      case mem: ByteMem[_] => delta match
        case Topped.Top =>
          // cannot track size of memory anymore, set the memory to top
          memories += key -> ValueMem(mem.upperBound, mem.definite)
          OptionA.noneSome(Topped.Top)
        case Topped.Actual(d) =>
          val newPageNum = mem.pageNum + d
          if (newPageNum <= maxPageNum && mem.sizeLimit.forall(newPageNum <= _)) {
            val newBytes = mem.bytes.appendedAll(Iterable.fill(d * pageSize)(emptyB))
            memories += key -> ByteMem(newBytes, mem.dirty, mem.sizeLimit, mem.upperBound, mem.definite)
            OptionA.some(Topped.Actual(mem.pageNum))
          } else {
            OptionA.none
          }

  override def addEmptyMemory(key: Key, initSize: Topped[Int], sizeLimit: scala.Option[Topped[Int]]): Unit =
    initSize match
      case Topped.Top => // unknown size
        memories += key ->  ValueMem(emptyB)
      case Topped.Actual(size) =>
        memories += key -> ByteMem(Array.fill[B](size*pageSize)(emptyB), mutable.BitSet(), sizeLimit.flatMap(_.toOption), emptyB)

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ConstantAddressMemoryJoiner[A]
  class ConstantAddressMemoryJoiner[A] extends ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    var snapshot = mutable.Map() ++ memories.view.mapValues(_.cloned)
    var fmemories: mutable.Map[Key, Mem[B]] = null

    override def inbetween_(): Unit =
      fmemories = memories
      memories = mutable.Map() ++ snapshot.view.mapValues(_.cloned)

    override def retainNone_(): Unit =
      memories = snapshot
      fmemories = null

    override def retainFirst_(fRes: TrySturdy[A]): Unit =
      memories = fmemories

    override def retainSecond_(gRes: TrySturdy[A]): Unit =
      fmemories = null

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for ((key, fmem) <- fmemories) memories.get(key) match
        case Some(gmem) => memories += key -> Join(fmem, gmem).get
        case None => memories += key -> fmem.toIndefinite

      val fkeys = fmemories.keySet
      for ((key, gmemOpt) <- memories)
        if (!fkeys.contains(key))
          memories += key -> gmemOpt.toIndefinite
      fmemories = null
  }

  def memoryIsSound(c: ConcreteMemory[Key])(using Soundness[Byte, B]): IsSound =
    // soundess for memory:
    //  - all concrete memories are present in abstract memories
    //  - all definite abstract memores have a concrete counterpart
    //  - for each key in concrete memories: mems(key) is sound
    val cMemories = c.getMemories
    memories.filterNot{ (key, _) => cMemories.isDefinedAt(key)}.foreachEntry { (k, aMem) =>
      if (aMem.isDefinite)
        return IsSound.NotSound(s"Definite memory with key $k not present in concrete memory.")
    }
    cMemories.foreachEntry { (key, cMem) =>
      val aMem = memories.getOrElse(key, { return IsSound.NotSound(s"Key $key not present in constant address memory.") })
      val memSound = memInstanceIsSound(cMem, aMem)
      if (memSound.isNotSound)
        return memSound
    }
    IsSound.Sound


  def memInstanceIsSound(c: ConcreteMemory.Mem, a: Mem[B])(using bytesSound: Soundness[Byte, B]): IsSound =
    // - sizes are equal
    // - all locations in concrete memory are approximated by locations in abstract memory
    a match
      case mem: ValueMem[B] =>
        concreteInstanceApproximated(c, mem.upperBound)
      case SizeMem(size, sizeLimit, upperBound , _) =>
        if (c.size != size || c.sizeLimit != sizeLimit)
          IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide.")
        else
          concreteInstanceApproximated(c, upperBound)
      case aMem@ByteMem(bytes, dirty, sizeLimit, _, _) =>
        if (c.size != aMem.size || c.sizeLimit != sizeLimit)
          return IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide.")
        c.bytes.zip(bytes).foreach { (cByte, aByte) =>
          val bSound = bytesSound.isSound(cByte, aByte)
          if (bSound.isNotSound)
            return IsSound.NotSound(s"Byte $cByte is not approximated by $aByte.")
        }
        IsSound.Sound


  def concreteInstanceApproximated(c: ConcreteMemory.Mem, b: B)(using bytesSound: Soundness[Byte, B]): IsSound =
    c.bytes.foreach { cByte =>
      val bSound = bytesSound.isSound(cByte, b)
      if (bSound.isNotSound)
        return IsSound.NotSound(s"Byte $cByte is not approximated by $b.")
    }
    IsSound.Sound

object ConstantAddressMemory:
  type Memories[Key, B] = Map[Key, Mem[B]]

  given CombineMem[B, W <: Widening](using j: Combine[B, W]): Combine[Mem[B], W] with
    def apply(old: Mem[B], now: Mem[B]): MaybeChanged[Mem[B]] = (old, now) match
      case (oldMem@ValueMem(upperBound, definite), nowMem) =>
        val newUpperBound = j(upperBound, nowMem.upperBound).get
        val newDefinite = definite && nowMem.isDefinite
        if (newUpperBound != upperBound || newDefinite != definite)
          Changed(ValueMem(newUpperBound, newDefinite))
        else
          Unchanged(oldMem)
      case (oldMem, nowMem@ValueMem(upperBound1, definite1)) =>
        Changed(ValueMem(j(oldMem.upperBound, upperBound1).get, oldMem.isDefinite && definite1))
      case (oldMem: SizedMem[B], nowMem: SizedMem[B]) if oldMem.size != nowMem.size =>
        Changed(ValueMem(j(oldMem.upperBound, nowMem.upperBound).get, oldMem.isDefinite && nowMem.isDefinite))
      case (oldMem: SizeMem[B], nowMem: SizedMem[B]) =>
        val newUpperBound = j(oldMem.upperBound, nowMem.upperBound).get
        val newDefinite = oldMem.isDefinite && nowMem.isDefinite
        if (newUpperBound != oldMem.upperBound || newDefinite != oldMem.isDefinite)
          Changed(SizeMem(oldMem.size, oldMem.sizeLimit, newUpperBound, newDefinite))
        else
          Unchanged(oldMem)
      case (oldMem: ByteMem[B], nowMem: SizeMem[B]) =>
        Changed(SizeMem(oldMem.size, oldMem.sizeLimit, j(oldMem.upperBound, nowMem.upperBound).get, oldMem.isDefinite && nowMem.isDefinite))
      case (oldMem: ByteMem[B], nowMem: ByteMem[B]) =>
        if (oldMem.dirty.size >= nowMem.dirty.size)
          joinByteMemSameSized(oldMem, nowMem)
        else
          joinByteMemSameSized(nowMem, oldMem)

    private def joinByteMemSameSized(mem1: ByteMem[B], mem2: ByteMem[B]): MaybeChanged[Mem[B]] =
      // TODO join definite?
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

  sealed trait Mem[B]:
    def cloned: Mem[B]
    //def size: Int
    def upperBound: B
    def toIndefinite: Mem[B]
    def isDefinite: Boolean

  sealed trait SizedMem[B] extends Mem[B]:
    def size: Int

  case class ValueMem[B](var upperBound: B, definite: Boolean = true) extends Mem[B]:
    override def cloned: ValueMem[B] = this
    override def toIndefinite: ValueMem[B] = ValueMem(upperBound, false)
    override def isDefinite: Boolean = definite


  case class SizeMem[B](size: Int, sizeLimit: scala.Option[Int], var upperBound: B, definite: Boolean = true) extends SizedMem[B]:
    override def cloned = this
    inline def pageNum: Int = (size / pageSize).toInt
    override def toIndefinite: SizeMem[B] = SizeMem(size, sizeLimit, upperBound, false)
    override def isDefinite: Boolean = definite

  case class ByteMem[B](bytes: Array[B], dirty: mutable.BitSet, sizeLimit: scala.Option[Int], var upperBound: B, definite: Boolean = true) extends SizedMem[B]:
    override def cloned: ByteMem[B] = ByteMem(bytes.clone(), dirty.clone(), sizeLimit, upperBound, definite)
    inline def size = bytes.length
    inline def pageNum: Int = (size / pageSize).toInt
    override def toIndefinite: ByteMem[B] = ByteMem(bytes, dirty, sizeLimit, upperBound, false)
    override def isDefinite: Boolean = definite

    override def toString: String = s"Mem(${bytes.size} bytes, ${dirty.size} dirty addresses, definite=$definite)"

    override def equals(obj: Any): Boolean = obj match
      case that: ByteMem[_] =>
        Array.equals(this.bytes.asInstanceOf[Array[AnyRef]], that.bytes.asInstanceOf[Array[AnyRef]]) &&
          this.dirty == that.dirty &&
          this.sizeLimit == that.sizeLimit &&
          this.upperBound == that.upperBound &&
          this.definite == that.definite
      case _ => false

    override def hashCode(): Int = this.bytes.toSeq.hashCode * 31 + this.dirty.hashCode * 17 + sizeLimit.hashCode + definite.hashCode

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

