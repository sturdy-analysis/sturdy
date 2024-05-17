package sturdy.effect.bytememory

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.TrySturdy
import sturdy.fix.*
import sturdy.values.*

import scala.collection.mutable
import scala.collection.IndexedSeqView
import scala.collection.immutable.IntMap
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/** A memory that tracks byte properties `B` for memory accesses via possibly constant addresses `Topped[Int]`.
 */
class ConstantAddressMemory[Key, B: ClassTag](emptyB: B)(using tb: Top[B])(using Join[B], Widen[B], Finite[Key]) extends Memory[Key, Topped[Int], Seq[B], Topped[Int], WithJoin], Effect:
  import ConstantAddressMemory.{*, given}

  protected var memories: Map[Key, Mem[B]] = Map()

  override def read(key: Key, addr: Topped[Int], length: Int): JOptionA[Seq[B]] = addr match
    case Topped.Top => JOptionA.noneSome(Seq.fill[B](length)(memories(key).upperBound))
    case Topped.Actual(a) => memories(key).read(a, length)

  override def write(key: Key, addr: Topped[Int], bytes: Seq[B]): JOptionA[Unit] =
    val (newMem, res) = memories(key).store(addr, bytes)
    newMem.foreach(memories += key -> _)
    res

  override def size(key: Key): Topped[Int] = memories(key) match
    case _: TopMem[_] => Topped.Top
    case mem: SizeMem[_] => Topped.Actual(mem.pageNum)
    case mem: ImmutableByteMem[_] => Topped.Actual(mem.pageNum)

  override def grow(key: Key, delta: Topped[Int]): JOptionA[Topped[Int]] =
    val (newMem, res) = memories(key).grow(delta, emptyB)
    newMem.foreach(memories += key -> _)
    res

  override def putNew(key: Key, initSize: Topped[Int], sizeLimit: Option[Topped[Int]]): Unit =
    initSize match
      case Topped.Top => // unknown size
        memories += key ->  TopMem(isDefinite = true, emptyB)
      case Topped.Actual(size) =>
        memories += key -> ImmutableByteMem.ofSize(size * pageSize, sizeLimit.flatMap(_.toOption), emptyB)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ConstantAddressMemoryJoiner[A])
  private class ConstantAddressMemoryJoiner[A] extends ComputationJoiner[A] {
    val snapshot = memories
    var fmemories: Map[Key, Mem[B]] = _

    override def inbetween(fFailed: Boolean): Unit =
      fmemories = memories
      memories = snapshot

    override def retainNone(): Unit =
      memories = snapshot
      fmemories = null

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      memories = fmemories

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for ((key, fmem) <- fmemories) memories.get(key) match
        case Some(gmem) => memories += key -> Join(fmem, gmem).get
        case None => memories += key -> fmem.asIndefinite

      val fkeys = fmemories.keySet
      for ((key, gmemOpt) <- memories)
        if (!fkeys.contains(key))
          memories += key -> gmemOpt.asIndefinite
  }

  override type State = Map[Key, Mem[B]]
  override def getState: State = memories
  override def setState(s: State): Unit = memories = s
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly

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
      case SizeMem(size, sizeLimit , _, _) =>
        if (c.size != size || c.sizeLimit != sizeLimit)
          IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide.")
        else
          concreteInstanceApproximated(c, aMem.upperBound)
      case aMem: ImmutableByteMem[_] =>
        if (c.size != aMem.size || c.sizeLimit != aMem.sizeLimit)
          return IsSound.NotSound(s"Sizes of concrete and abstract memory do not coincide, was \n  $aMem wanted \n  ${(c.size, c.sizeLimit, c.pageNum)}.")
        c.bytes.zip(aMem.bytesIterable).foreach { (cByte, aByte) =>
          val bSound = bytesSound.isSound(cByte, aByte)
          if (bSound.isNotSound) {
            val sound = IsSound.NotSound(s"Byte $cByte is not approximated by $aByte.")
            return sound
          }
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
  given CombineMem[B: ClassTag, W <: Widening](using j: Combine[B, W]): Combine[Mem[B], W] with
    def apply(old: Mem[B], now: Mem[B]): MaybeChanged[Mem[B]] =
      val newUpperBoundChanged = Combine(old.upperBound, now.upperBound)
      val newUpperBound = newUpperBoundChanged.get
      val newDefinite = old.isDefinite && now.isDefinite
      val boundDefChanged = old.isDefinite && !now.isDefinite || newUpperBoundChanged.hasChanged
      (old, now) match
        case (_: TopMem[B], _) =>
          MaybeChanged(TopMem(newDefinite, newUpperBound), boundDefChanged)
        case (_, _: TopMem[B]) =>
          Changed(TopMem(newDefinite, newUpperBound))
        case (old: SizeMem[B], now: SizeMem[B]) =>
          if (old.size == now.size)
            MaybeChanged(SizeMem(old.size, old.sizeLimit, newDefinite, newUpperBound), boundDefChanged)
          else
            Changed(TopMem(newDefinite, newUpperBound))
        case (old: SizeMem[B], now: ImmutableByteMem[B]) =>
          if (old.size == now.size)
            MaybeChanged(SizeMem(old.size, old.sizeLimit, newDefinite, newUpperBound), boundDefChanged)
          else
            Changed(TopMem(newDefinite, newUpperBound))
        case (old: ImmutableByteMem[B], now: SizeMem[B]) =>
          if (old.size == now.size)
            Changed(SizeMem(old.size, old.sizeLimit, newDefinite, newUpperBound))
          else
            Changed(TopMem(newDefinite, newUpperBound))
        case (oldMem: ImmutableByteMem[B], nowMem: ImmutableByteMem[B]) =>
          if (oldMem.size == nowMem.size) {
            val words = JoinIntMap[Word[B], W](using ConstantAddressMemory.CombineWord)(oldMem.words, nowMem.words)
            MaybeChanged(ImmutableByteMem(oldMem.size, oldMem.emptyB, words.get, oldMem.sizeLimit, newDefinite, newUpperBound), words.hasChanged || boundDefChanged)
          } else {
            Changed(TopMem(newDefinite, newUpperBound))
          }

  sealed trait Mem[B: ClassTag]:
    def upperBound: B
    protected def newBound(bs: Iterable[B])(using Join[B]): MaybeChanged[B] =
      var bound = upperBound
      var changed = false
      for (b <- bs)
        Join(bound, b).ifChanged { newBound =>
          bound = newBound
          changed = true
        }
      MaybeChanged(bound, changed)

    def isDefinite: Boolean
    def asIndefinite: Mem[B]

    def read(a: Int, length: Int): JOptionA[Seq[B]]
    def store(addr: Topped[Int], bytes: Seq[B])(using Join[B]): (Option[Mem[B]], JOptionA[Unit])
    def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], JOptionA[Topped[Int]])
    def map[C: ClassTag](f: B => C): Mem[C]

  case class TopMem[B: ClassTag](isDefinite: Boolean, upperBound: B) extends Mem[B]:
    override def asIndefinite: Mem[B] = this.copy(isDefinite = false)
    override def read(a: Int, length: Int): JOptionA[Seq[B]] = JOptionA.noneSome(Seq.fill[B](length)(upperBound))
    override def store(addr: Topped[Int], bytes: Seq[B])(using Join[B]): (Option[Mem[B]], JOptionA[Unit]) =
      val newMem = newBound(bytes) match
        case MaybeChanged.Changed(bound) => Some(TopMem(isDefinite, bound))
        case _ => None
      (newMem, JOptionA.noneSome(()))
    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], JOptionA[Topped[Int]]) = (None, JOptionA.noneSome(Topped.Top))

    override def map[C: ClassTag](f: B => C): Mem[C] = TopMem(isDefinite, f(upperBound))

  case class SizeMem[B: ClassTag](size: Int, sizeLimit: Option[Int], isDefinite: Boolean, upperBound: B) extends Mem[B]:
    override def asIndefinite: Mem[B] = this.copy(isDefinite = false)
    inline def pageNum: Int = size / pageSize
    override def read(a: Int, length: Int): JOptionA[Seq[B]] =
      if (a >=0 && a + length <= size) {
        val readBytes = Seq.fill[B](length)(upperBound)
        if (isDefinite)
          JOptionA.some(readBytes)
        else
          JOptionA.noneSome(readBytes)
      } else {
        JOptionA.none
      }
    override def store(addr: Topped[Int], bytes: Seq[B])(using Join[B]): (Option[Mem[B]], JOptionA[Unit]) = addr match
      case Topped.Top =>
        // any byte of the memory might be affected, but this memory does not track individual bytes anyway
        val newMem = newBound(bytes) match
          case MaybeChanged.Changed(bound) => Some(SizeMem(size, sizeLimit, isDefinite, bound))
          case _ => None
        (newMem, JOptionA.noneSome(()))
      case Topped.Actual(a) =>
        if (a >= 0 && a + bytes.size <= size) {
          val newMem = newBound(bytes) match
            case MaybeChanged.Changed(bound) => Some(SizeMem(size, sizeLimit, isDefinite, bound))
            case _ => None
          (newMem, JOptionA.some(()))
        } else {
          (None, JOptionA.none)
        }
    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], JOptionA[Topped[Int]]) = delta match
      case Topped.Top =>
        // cannot track size of memory anymore, set the memory to top
        (Some(TopMem(isDefinite, upperBound)), JOptionA.noneSome(Topped.Top))
      case Topped.Actual(d) =>
        val newPageNum = pageNum + d
        if (newPageNum <= maxPageNum && sizeLimit.forall(newPageNum <= _)) {
          val newMem = newBound(Iterable.single(emptyB)) match
            case MaybeChanged.Changed(bound) =>
              SizeMem(size + d * pageSize, sizeLimit, isDefinite, bound)
            case _ =>
              SizeMem(size + d * pageSize, sizeLimit, isDefinite, upperBound)
          (Some(newMem), JOptionA.some(Topped.Actual(pageNum)))
        } else {
          (None, JOptionA.none)
        }

    override def map[C: ClassTag](f: B => C): Mem[C] =
      SizeMem(size, sizeLimit, isDefinite, f(upperBound))

  case class Word[B](b1: B, b2: B, b3: B, b4: B):
    def toIterable: Iterable[B] = Iterable(b1, b2, b3, b4)
    def toList: List[B] = List(b1, b2, b3, b4)
    def toVector: Vector[B] = Vector(b1, b2, b3, b4)
    def updated(ix: Int, b: B): Word[B] = ix match
      case 0 => Word(b, b2, b3, b4)
      case 1 => Word(b1, b, b3, b4)
      case 2 => Word(b1, b2, b, b4)
      case 3 => Word(b1, b2, b3, b)
      case _ => throw new IllegalArgumentException
    def map[C](f: B => C): Word[C] = Word(f(b1), f(b2), f(b3), f(b4))

  val WordSize: Int = 4

  given CombineWord[B, W <: Widening](using Combine[B, W]): Combine[Word[B], W] with
    override def apply(v1: Word[B], v2: Word[B]): MaybeChanged[Word[B]] = {
      val b1 = Combine(v1.b1, v2.b1)
      val b2 = Combine(v1.b2, v2.b2)
      val b3 = Combine(v1.b3, v2.b3)
      val b4 = Combine(v1.b4, v2.b4)
      MaybeChanged(Word(b1.get, b2.get, b3.get, b4.get), b1.hasChanged || b2.hasChanged || b3.hasChanged || b4.hasChanged)
    }

  object ImmutableByteMem:
    def ofSize[B: ClassTag](size: Int, sizeLimit: Option[Int], emptyB: B): ImmutableByteMem[B] =
      if (size % WordSize == 0)
        ImmutableByteMem(size, emptyB, IntMap(), sizeLimit, isDefinite = true, emptyB)
      else
        throw new IllegalArgumentException(s"Cannot create ImmutableByteMem of size $size")

  case class ImmutableByteMem[B: ClassTag](size: Int, emptyB: B, words: IntMap[Word[B]], sizeLimit: Option[Int], isDefinite: Boolean, upperBound: B) extends Mem[B]:
    override def asIndefinite: Mem[B] = this.copy(isDefinite = false)
    inline def pageNum: Int = size / pageSize
    def bytesIterable: Iterable[B] =
      val emptyWord = Word(emptyB, emptyB, emptyB, emptyB)
      for (wordAddr <- 0 until (size / 4); b <- words.getOrElse(wordAddr, emptyWord).toIterable)
        yield b
    override def read(a: Int, length: Int): JOptionA[Seq[B]] =
      if (a < 0 || a + length > size)
        return JOptionA.none

      val buf = ListBuffer[B]()

      var count = 0
      var wordAddr = a / WordSize

      // read bytes from `a` until the next full word begins
      val aRemainder = a % WordSize
      var completeWord = aRemainder == 0
      if (aRemainder > 0) {
        val bytes = words.get(wordAddr) match {
          case Some(word) => word.toList
          case None => Iterable.fill(WordSize)(emptyB)
        }
        val end = WordSize.min(aRemainder + length)
        buf ++= bytes.slice(aRemainder, end)
        count += end - aRemainder
        wordAddr += 1
        completeWord = end == WordSize
      }

      // read complete words
      while (completeWord && count + WordSize <= length) {
        words.get(wordAddr) match
          case None =>
            for (_ <- 0 until WordSize)
              buf += emptyB
          case Some(w: Word[B]) =>
            buf ++= w.toIterable
        wordAddr += 1
        count += WordSize
      }

      // read bytes from `wordAddr` until `a + length`
      val remainder = (a + length) % WordSize
      if (completeWord && remainder > 0) {
        val bytes = words.get(wordAddr) match {
          case Some(word) => word.toList
          case None => Iterable.fill(WordSize)(emptyB)
        }
        buf ++= bytes.take(remainder)
        count += remainder
        wordAddr += 1
      }

      if (count != length)
        throw new IllegalStateException(s"Implementation bug, count should be $length but was $count (a=$a)")

      if (isDefinite)
        JOptionA.some(buf.toSeq)
      else
        JOptionA.noneSome(buf.toSeq)



    override def store(addr: Topped[Int], newBytes: Seq[B])(using Join[B]): (Option[Mem[B]], JOptionA[Unit]) = addr match
      case Topped.Top =>
        // any byte of the memory might be affected, only track the memory's size from now on
        val newMem = newBound(newBytes) match
          case MaybeChanged.Changed(bound) =>
            SizeMem(size, sizeLimit, isDefinite, bound)
          case _ =>
            SizeMem(size, sizeLimit, isDefinite, upperBound)
        (Some(newMem), JOptionA.noneSome(()))
      case Topped.Actual(a) =>
        if (a < 0 || a + newBytes.size > size)
          return (None, JOptionA.none)

        val bound = newBound(newBytes).get
        val byteIter = newBytes.iterator
        val length = newBytes.size

        var newWords = words
        val emptyWord = Word(emptyB, emptyB, emptyB, emptyB)

        var wordAddr = a / 4
        // store bytes from `a` until the next full word begins
        val aRemainder = a % WordSize
        var completeWord = aRemainder == 0
        if (aRemainder > 0) {
          var word = words.getOrElse(wordAddr, emptyWord)
          val end = WordSize.min(aRemainder + length)
          for (ix <- aRemainder until end)
            word = word.updated(ix, byteIter.next())
          newWords += wordAddr -> word
          wordAddr += 1
          completeWord = end == WordSize
        }

        // store complete words
        val end = a + length
        while (completeWord && wordAddr * 4 + WordSize <= end) {
          newWords += wordAddr -> Word(byteIter.next(), byteIter.next(), byteIter.next(), byteIter.next())
          wordAddr += 1
        }

        // store bytes from `wordAddr * 4` until `a + length`
        val remainder = end % WordSize
        if (completeWord && remainder > 0) {
          var word = words.getOrElse(wordAddr, emptyWord)
          for (ix <- 0 until remainder)
            word = word.updated(ix, byteIter.next())
          newWords += wordAddr -> word
          wordAddr += 1
        }

        if (byteIter.hasNext)
          throw new IllegalStateException(s"Implementation bug, byteIter should be empty but was ${byteIter.toList}")

        (Some(ImmutableByteMem(size, emptyB, newWords, sizeLimit, isDefinite, bound)), JOptionA.some(()))

    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], JOptionA[Topped[Int]]) = delta match
      case Topped.Top =>
        // cannot track size of memory anymore, set the memory to top
        (Some(TopMem(isDefinite, upperBound)), JOptionA.noneSome(Topped.Top))
      case Topped.Actual(d) =>
        val newPageNum = pageNum + d
        if (newPageNum <= maxPageNum && sizeLimit.forall(newPageNum <= _)) {
          val newMem = ImmutableByteMem(newPageNum * pageSize, emptyB, words, sizeLimit, isDefinite, upperBound)
          (Some(newMem), JOptionA.some(Topped.Actual(pageNum)))
        } else {
          (None, JOptionA.none)
        }

    override def map[C: ClassTag](f: B => C): Mem[C] =
      ImmutableByteMem(size, f(emptyB), words.map((i,w) => (i,w.map(f))), sizeLimit , isDefinite, f(upperBound))

//  case class MutableByteMem[B: ClassTag](bytes: Array[B], dirty: mutable.BitSet, sizeLimit: Option[Int], isDefinite: Boolean, upperBound: B) extends Mem[B]:
//    override def cloned: MutableByteMem[B] = MutableByteMem(bytes.clone(), dirty.clone(), sizeLimit, isDefinite, upperBound)
//    override def asIndefinite: Mem[B] = this.copy(isDefinite = false)
//    inline def size: Int = bytes.length
//    inline def pageNum: Int = size / pageSize
//
//    override def read(a: Int, length: Int): OptionA[Seq[B]] =
//      if (a >=0 && a + length <= bytes.length) {
//        val readBytes = bytes.slice(a, a + length).toSeq
//        if (isDefinite)
//          OptionA.some(readBytes)
//        else
//          OptionA.noneSome(readBytes)
//      }
//      else
//        OptionA.none
//
//    override def store(addr: Topped[Int], newBytes: Seq[B])(using Join[B]): (Option[Mem[B]], OptionA[Unit]) = addr match
//      case Topped.Top =>
//        // any byte of the memory might be affected, only track the memory's size from now on
//        updateBound(newBytes)
//        val newMem = SizeMem(size, sizeLimit, isDefinite, upperBound)
//        (Some(newMem), OptionA.noneSome(()))
//      case Topped.Actual(a) =>
//        if (a >= 0 && a + newBytes.size <= size) {
//          updateBound(newBytes)
//          Array.copy(newBytes.toArray, 0, bytes, a, newBytes.size)
//          dirty.addAll(a until (a + newBytes.size))
//          (None, OptionA.some(()))
//        } else {
//          (None, OptionA.none)
//        }
//
//    override def grow(delta: Topped[Int], emptyB: B)(using Join[B]): (Option[Mem[B]], OptionA[Topped[Int]]) = delta match
//      case Topped.Top =>
//        // cannot track size of memory anymore, set the memory to top
//        (Some(TopMem(isDefinite, upperBound)), OptionA.noneSome(Topped.Top))
//      case Topped.Actual(d) =>
//        val newPageNum = pageNum + d
//        if (newPageNum <= maxPageNum && sizeLimit.forall(newPageNum <= _)) {
//          val newBytes = bytes.appendedAll(Iterable.fill(d * pageSize)(emptyB))
//          val newMem = MutableByteMem(newBytes, dirty, sizeLimit, isDefinite, upperBound)
//          (Some(newMem), OptionA.some(Topped.Actual(pageNum)))
//        } else {
//          (None, OptionA.none)
//        }
//
//    override def toString: String = s"Mem(${bytes.length} bytes, ${dirty.size} dirty addresses, definite=$isDefinite)"
//
//    override def equals(obj: Any): Boolean = obj match
//      case that: MutableByteMem[_] =>
//        Array.equals(this.bytes.asInstanceOf[Array[AnyRef]], that.bytes.asInstanceOf[Array[AnyRef]]) &&
//          this.dirty == that.dirty &&
//          this.sizeLimit == that.sizeLimit &&
//          this.upperBound == that.upperBound &&
//          this.isDefinite == that.isDefinite
//      case _ => false
//
//    override def hashCode(): Int = this.bytes.toSeq.hashCode * 31 + this.dirty.hashCode * 17 + sizeLimit.hashCode + isDefinite.hashCode

  val pageSize: Int = 65536
  val maxPageNum: Int = 65536

