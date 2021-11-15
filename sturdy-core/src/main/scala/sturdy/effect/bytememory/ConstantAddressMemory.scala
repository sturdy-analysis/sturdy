package sturdy.effect.bytememory

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{OptionA, WithJoin}
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

  override def memRead(key: Key, addr: Topped[Int], length: Int): OptionA[Seq[B]] = addr match
    case Topped.Top => OptionA.noneSome(Seq.fill[B](length)(memories(key).upperBound))
    case Topped.Actual(a) => memories(key).read(a, length)

  override def memStore(key: Key, addr: Topped[Int], bytes: Seq[B]): OptionA[Unit] =
    val (newMem, res) = memories(key).store(addr, bytes)
    newMem.foreach(memories(key) = _)
    res

  override def memSize(key: Key): Topped[Int] = memories(key) match
    case _: TopMem[_] => Topped.Top
    case mem: SizeMem[_] => Topped.Actual(mem.pageNum)
    case mem: ByteMem[_] => Topped.Actual(mem.pageNum)

  override def memGrow(key: Key, delta: Topped[Int]): OptionA[Topped[Int]] =
    val (newMem, res) = memories(key).grow(delta, emptyB)
    newMem.foreach(memories(key) = _)
    res

  override def addEmptyMemory(key: Key, initSize: Topped[Int], sizeLimit: scala.Option[Topped[Int]]): Unit =
    initSize match
      case Topped.Top => // unknown size
        memories += key ->  TopMem(isDefinite = true)(emptyB)
      case Topped.Actual(size) =>
        memories += key -> ByteMem(Array.fill[B](size*pageSize)(emptyB), mutable.BitSet(), sizeLimit.flatMap(_.toOption), isDefinite = true)(emptyB)

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
        case None => memories += key -> fmem.asIndefinite

      val fkeys = fmemories.keySet
      for ((key, gmemOpt) <- memories)
        if (!fkeys.contains(key))
          memories += key -> gmemOpt.asIndefinite
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


  def memInstanceIsSound(c: ConcreteMemory.Mem, aMem: Mem[B])(using bytesSound: Soundness[Byte, B]): IsSound =
    // - sizes are equal
    // - all locations in concrete memory are approximated by locations in abstract memory
    aMem match
      case _: TopMem[B] =>
        concreteInstanceApproximated(c, aMem.upperBound)
      case SizeMem(size, sizeLimit , _) =>
        if (c.size != size || c.sizeLimit != sizeLimit)
          IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide.")
        else
          concreteInstanceApproximated(c, aMem.upperBound)
      case aMem@ByteMem(bytes, dirty, sizeLimit, _) =>
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

  given CombineMem[B: ClassTag, W <: Widening](using j: Combine[B, W]): Combine[Mem[B], W] with
    def apply(old: Mem[B], now: Mem[B]): MaybeChanged[Mem[B]] =
      val newUpperBoundChanged = Combine(old.upperBound, now.upperBound)
      val newUpperBound = newUpperBoundChanged.get
      val newDefinite = old.isDefinite && now.isDefinite
      val boundDefChanged = old.isDefinite && !now.isDefinite || newUpperBoundChanged.hasChanged
      (old, now) match
        case (_: TopMem[B], _) =>
          MaybeChanged(TopMem(newDefinite)(newUpperBound), boundDefChanged)
        case (_, _: TopMem[B]) =>
          Changed(TopMem(newDefinite)(newUpperBound))
        case (old: SizeMem[B], now: SizeMem[B]) =>
          if (old.size == now.size)
            MaybeChanged(SizeMem(old.size, old.sizeLimit, newDefinite)(newUpperBound), boundDefChanged)
          else
            Changed(TopMem(newDefinite)(newUpperBound))
        case (old: SizeMem[B], now: ByteMem[B]) =>
          if (old.size == now.size)
            MaybeChanged(SizeMem(old.size, old.sizeLimit, newDefinite)(newUpperBound), boundDefChanged)
          else
            Changed(TopMem(newDefinite)(newUpperBound))
        case (old: ByteMem[B], now: SizeMem[B]) =>
          if (old.size == now.size)
            Changed(SizeMem(old.size, old.sizeLimit, newDefinite)(newUpperBound))
          else
            Changed(TopMem(newDefinite)(newUpperBound))
        case (oldMem: ByteMem[B], nowMem: ByteMem[B]) =>
          if (oldMem.dirty.size >= nowMem.dirty.size)
            joinByteMemSameSized(oldMem, nowMem, newDefinite, newUpperBound, boundDefChanged)
          else
            joinByteMemSameSized(nowMem, oldMem, newDefinite, newUpperBound, boundDefChanged)

    private def joinByteMemSameSized(mem1: ByteMem[B], mem2: ByteMem[B], newDefinite: Boolean, newUpperBound: B, boundDefChanged: Boolean): MaybeChanged[Mem[B]] =
      val result = ByteMem(mem1.bytes.clone(), mem1.dirty.clone(), mem1.sizeLimit, newDefinite)(newUpperBound)
      var changed = boundDefChanged
      for (ix <- mem2.dirty) {
        val b = Combine[B, W](result.bytes(ix), mem2.bytes(ix))
        if (b.hasChanged) {
          result.bytes(ix) = b.get
          changed = true
        }
      }
      result.dirty |= mem2.dirty
      MaybeChanged(result, changed)

  sealed trait Mem[B: ClassTag](bound: B):
    def cloned: Mem[B]

    protected var _upperBound: B = bound
    def upperBound: B = _upperBound
    inline protected def updateBound(bs: Iterable[B])(using Join[B]) =
      for (b <- bs)
        _upperBound = Join(_upperBound, b).get

    def isDefinite: Boolean
    def asIndefinite: Mem[B] = this match
      case TopMem(_) => TopMem(isDefinite = false)(_upperBound)
      case SizeMem(size, sizeLimit, _) => SizeMem(size, sizeLimit, isDefinite = false)(_upperBound)
      case ByteMem(bytes, dirty, sizeLimit, _) => ByteMem(bytes, dirty, sizeLimit, isDefinite = false)(_upperBound)

    def read(a: Int, length: Int): OptionA[Seq[B]]
    def store(addr: Topped[Int], bytes: Seq[B])(using Join[B]): (Option[Mem[B]], OptionA[Unit])
    def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], OptionA[Topped[Int]])

  case class TopMem[B: ClassTag](isDefinite: Boolean)(bound: B) extends Mem[B](bound):
    override def cloned: TopMem[B] = this
    override def read(a: Int, length: Int): OptionA[Seq[B]] = OptionA.noneSome(Seq.fill[B](length)(upperBound))
    override def store(addr: Topped[Int], bytes: Seq[B])(using Join[B]): (Option[Mem[B]], OptionA[Unit]) =
      updateBound(bytes)
      (None, OptionA.noneSome(()))
    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], OptionA[Topped[Int]]) = (None, OptionA.noneSome(Topped.Top))


  case class SizeMem[B: ClassTag](size: Int, sizeLimit: scala.Option[Int], isDefinite: Boolean)(bound: B) extends Mem[B](bound):
    override def cloned: Mem[B] = this
    inline def pageNum: Int = size / pageSize
    override def read(a: Int, length: Int): OptionA[Seq[B]] =
      if (a >=0 && a + length <= size) {
        val readBytes = Seq.fill[B](length)(upperBound)
        if (isDefinite)
          OptionA.some(readBytes)
        else
          OptionA.noneSome(readBytes)
      } else {
        OptionA.none
      }
    override def store(addr: Topped[Int], bytes: Seq[B])(using Join[B]): (Option[Mem[B]], OptionA[Unit]) = addr match
      case Topped.Top =>
        // any byte of the memory might be affected, but this memory does not track individual bytes anyway
        updateBound(bytes)
        (None, OptionA.noneSome(()))
      case Topped.Actual(a) =>
        if (a >= 0 && a + bytes.size <= size) {
          updateBound(bytes)
          (None, OptionA.some(()))
        } else {
          (None, OptionA.none)
        }
    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], OptionA[Topped[Int]]) = delta match
      case Topped.Top =>
        // cannot track size of memory anymore, set the memory to top
        (Some(TopMem(isDefinite)(upperBound)), OptionA.noneSome(Topped.Top))
      case Topped.Actual(d) =>
        val newPageNum = pageNum + d
        if (newPageNum <= maxPageNum && sizeLimit.forall(newPageNum <= _)) {
          updateBound(Iterable.single(emptyB))
          val newMem = SizeMem(size + d * pageSize, sizeLimit, isDefinite)(upperBound)
          (Some(newMem), OptionA.some(Topped.Actual(pageNum)))
        } else {
          (None, OptionA.none)
        }


  case class ByteMem[B: ClassTag](bytes: Array[B], dirty: mutable.BitSet, sizeLimit: scala.Option[Int], isDefinite: Boolean)(bound: B) extends Mem[B](bound):
    override def cloned: ByteMem[B] = ByteMem(bytes.clone(), dirty.clone(), sizeLimit, isDefinite)(upperBound)
    inline def size: Int = bytes.length
    inline def pageNum: Int = size / pageSize

    override def read(a: Int, length: Int): OptionA[Seq[B]] =
      if (a >=0 && a + length <= bytes.length) {
        val readBytes = bytes.slice(a, a + length).toSeq
        if (isDefinite)
          OptionA.some(readBytes)
        else
          OptionA.noneSome(readBytes)
      }
      else
        OptionA.none

    override def store(addr: Topped[Int], newBytes: Seq[B])(using Join[B]): (Option[Mem[B]], OptionA[Unit]) = addr match
      case Topped.Top =>
        // any byte of the memory might be affected, only track the memory's size from now on
        updateBound(newBytes)
        val newMem = SizeMem(size, sizeLimit, isDefinite)(upperBound)
        (Some(newMem), OptionA.noneSome(()))
      case Topped.Actual(a) =>
        if (a >= 0 && a + newBytes.size <= size) {
          updateBound(newBytes)
          Array.copy(newBytes.toArray, 0, bytes, a, newBytes.size)
          dirty.addAll(a until (a + newBytes.size))
          (None, OptionA.some(()))
        } else {
          (None, OptionA.none)
        }

    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], OptionA[Topped[Int]]) = delta match
      case Topped.Top =>
        // cannot track size of memory anymore, set the memory to top
        (Some(TopMem(isDefinite)(upperBound)), OptionA.noneSome(Topped.Top))
      case Topped.Actual(d) =>
        val newPageNum = pageNum + d
        if (newPageNum <= maxPageNum && sizeLimit.forall(newPageNum <= _)) {
          val newBytes = bytes.appendedAll(Iterable.fill(d * pageSize)(emptyB))
          val newMem = ByteMem(newBytes, dirty, sizeLimit, isDefinite)(upperBound)
          (Some(newMem), OptionA.some(Topped.Actual(pageNum)))
        } else {
          (None, OptionA.none)
        }

    override def toString: String = s"Mem(${bytes.length} bytes, ${dirty.size} dirty addresses, definite=$isDefinite)"

    override def equals(obj: Any): Boolean = obj match
      case that: ByteMem[_] =>
        Array.equals(this.bytes.asInstanceOf[Array[AnyRef]], that.bytes.asInstanceOf[Array[AnyRef]]) &&
          this.dirty == that.dirty &&
          this.sizeLimit == that.sizeLimit &&
          this.upperBound == that.upperBound &&
          this.isDefinite == that.isDefinite
      case _ => false

    override def hashCode(): Int = this.bytes.toSeq.hashCode * 31 + this.dirty.hashCode * 17 + sizeLimit.hashCode + isDefinite.hashCode

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

