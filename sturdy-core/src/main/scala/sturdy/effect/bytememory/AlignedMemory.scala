package sturdy.effect.bytememory

import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import Bytes.*
import sturdy.data
import sturdy.effect.EffectStack
import sturdy.util.Profiler
import sturdy.values.addresses.{AddressOffset, AddressLimits}

import java.nio.ByteOrder
import scala.collection.AbstractIterator
import scala.collection.immutable.SortedMap
import scala.reflect.ClassTag

/**
 * Indicates if the read address aligns with the stored address, i.e., if storedAddr = readAddr.
 */
enum AlignedRead:
  case Aligned
  case MaybeAligned

  def toToppedBoolean: Topped[Boolean] =
    this match
      case Aligned => Topped.Actual(true)
      case MaybeAligned => Topped.Top

trait LanguageSpecificMemOps[Context, Addr, Size, Val]:
  def matchRegion[Timestamp: PartialOrder](addr: Addr, size: Size, alignment: Int, mems: Mem[Context, Addr, Timestamp, Val, Size]): Iterable[(PhysicalAddress[Context], MemoryRegion[Addr, Size, Timestamp, Val], AlignedRead)]
  def isSummaryRegion[Timestamp: PartialOrder](ctx: Context, newRegion: MemoryRegion[Addr, Size, Timestamp, Val]): Boolean
  def normalizeStartAddrAndSize(ctx: Context, startAddr: Addr, byteSize: Size): (Addr,Size)

trait SizeOps[Size]:
  def fromInt(size: Int): Size
  def add(size1: Size, size2: Size): Size
  def sub(size1: Size, size2: Size): Size
  def mul(size1: Size, size2: Size): Size
  def min(size1: Size, size2: Size): Size
  def toTopped(size: Size): Topped[Int]

enum ByteMemoryAllocationContext:
  case Write
  case Copy
  case Fill
  case Init

final class AlignedMemory
  [
    Key: Finite,
    Context: Ordering: Finite,
    Addr: Join: Widen,
    Val: ClassTag: Join: Widen,
    Size: Join: Widen
  ]
  (val
   defaultBytes: ReadBytes[Val],
   memLocAllocator: Allocator[IterableOnce[Context],(ByteMemoryAllocationContext,Key,Addr)]
  )
  (using
   memOps: LanguageSpecificMemOps[Context, Addr, Size, Val],
   addressOffset: AddressOffset[Addr],
   addressLimits: AddressLimits[Addr, Size, WithJoin],
   sizeOps: SizeOps[Size],
   effectStack: EffectStack
  )
  extends Memory[Key, Addr, Bytes[Val], Size, WithJoin] with VectorClock[Context]:

  type MemoryCtx = Context
  private val pageSize: Size = sizeOps.fromInt(ConcreteMemory.pageSize)
  private val maxPages: Size = sizeOps.fromInt(ConcreteMemory.maxPageNum)
  var zeroAddr: Option[Addr] = None
  var zeroValue: Option[Val] = None

  var memories: Map[Key, Mem[Context, Addr, Timestamp, Val, Size]] = Map()

  override def read(key: Key, readAddr: Addr, length: Int, align: Int = 0): JOption[WithJoin, Bytes[Val]] =
    Profiler.addTime("AlignedMemory.read") {
      val result = read0(key,readAddr, length, align)
      result
    }

  private def read0(key: Key, readAddr: Addr, length: Int, align: Int = 0): JOption[WithJoin, Bytes[Val]] =
    val mem = memories(key)

    val matchingRegions = memOps.matchRegion(readAddr, sizeOps.fromInt(length), align, mem)

    if(matchingRegions.isEmpty) {
      JOptionA.None()
    } else if(matchingRegions.exists { case (_, _, AlignedRead.MaybeAligned) => true; case (_, region, _) if(region.elementByteSize.isTop) => true; case _ => false }) {
      JOptionA.NoneSome(ReadBytes[Val](value = Topped.Top, byteOrder = Topped.Top))
    } else {
      val minElementByteSize = matchingRegions.map((_, region, _) => region.elementByteSize.get).min
      val (joinedValue,joinedByteOrder) = matchingRegions.map((_, region, _) => (region.value,region.byteOrder)).reduce(Join(_,_).get)
      val readBytes = ReadBytes[Val](
        value = Topped.Actual(List((joinedValue, minElementByteSize))),
        byteOrder = joinedByteOrder
      )
      if (length - minElementByteSize <= 0) {
        addressLimits.ifAddrLeSize(readAddr, sizeOps.sub(sizeOps.mul(mem.numPages, pageSize), sizeOps.fromInt(length))) {
          readBytes
        }
      } else {
        read0(key = key, readAddr = addressOffset.addOffsetToAddr(minElementByteSize, readAddr), length - minElementByteSize).map {
          readBytes ++ _
        }
      }
    }

  override def write(key: Key, addr: Addr, bytes: Bytes[Val], alignment: Int = 0): JOption[WithJoin, Unit] =
    Profiler.addTime("AlignedMemory.write") {
      write0(key, addr, bytes, alignment = alignment)
    }

  private def write0(key: Key, addr: Addr, bytes: Bytes[Val], allocationCtx: ByteMemoryAllocationContext = ByteMemoryAllocationContext.Write, alignment: Int = 0): JOption[WithJoin, Unit] =
    bytes match
      case StoredBytes((value, byteSize) :: rest, byteOrder) =>
        val Mem(store0, numPages, pageLimit) = memories(key)

        addressLimits.ifAddrLeSize(addr, sizeOps.sub(sizeOps.mul(numPages, pageSize), sizeOps.fromInt(byteSize))) {

          var store = store0
          for (ctx <- memLocAllocator(allocationCtx, key, addr)) {
            increment(ctx)
            val (normalizedStartAddr,normalizeSize) = memOps.normalizeStartAddrAndSize(ctx, addr, sizeOps.fromInt(byteSize))
            val newRegion = MemoryRegion(
              startAddr = normalizedStartAddr,
              alignment = Powerset(alignment),
              elementByteSize = Topped.Actual(byteSize),
              regionByteSize = normalizeSize,
              timestamp = this.timestamp,
              value = value,
              byteOrder = byteOrder
            )
            store = writeRegion(ctx, newRegion, store)
          }
          memories += key -> Mem(store, numPages, pageLimit)

          write0(
            key = key,
            addr = addressOffset.addOffsetToAddr(byteSize, addr),
            bytes = StoredBytes(rest, byteOrder)
          )
        }

      case StoredBytes(Nil, byteOrder) =>
        JOptionA.Some(())

      case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bytes")


  private def writeRegion(ctx: Context, newRegion: MemoryRegion[Addr, Size, Timestamp, Val], store0: SortedMap[PhysicalAddress[Context], MemoryRegion[Addr, Size, Timestamp, Val]]): SortedMap[PhysicalAddress[Context], MemoryRegion[Addr, Size, Timestamp, Val]] = {
    var store = store0
    val recentRegion = store.get(PhysicalAddress(ctx, Recency.Recent))
    val oldRegion = store.get(PhysicalAddress(ctx, Recency.Old))
    if (memOps.isSummaryRegion(ctx, newRegion)) {
      // If the region represents multiple concrete memory addresses, join everything into old.
      val joinedRegion = Join(Join(oldRegion, recentRegion).get, Some(newRegion)).get.get
      store += PhysicalAddress(ctx, Recency.Old) -> joinedRegion
      store -= PhysicalAddress(ctx, Recency.Recent)
    } else {
      // If the region represents a single concrete memory addresses,
      // retire the previous recent address and assign to the new recent address.
      val joinedRegion = Join(recentRegion, oldRegion).get
      for (region <- joinedRegion) {
        store += PhysicalAddress(ctx, Recency.Old) -> region
      }
      store += PhysicalAddress(ctx, Recency.Recent) -> newRegion
    }
    store
  }

  override def copy(key: Key, srcAddr: Addr, dstAddr: Addr, byteAmount: Size): JOptionA[Unit] =
    val mem = memories(key)
    var store = mem.store
    for((physAddr, region,_) <- memOps.matchRegion(srcAddr, byteAmount, alignment=0, mem)) {
      val destAddr = addressOffset.moveAddress(region.startAddr, srcOffset = srcAddr, dstOffset = dstAddr)
      for (destCtx <- memLocAllocator(ByteMemoryAllocationContext.Copy, key, destAddr)) {
        store = writeRegion(destCtx, region.copy (startAddr = destAddr), store)
      }
    }
    memories += key -> mem.copy(store = store)
    JOptionA.NoneSome(())

  override def fill(key: Key, startAddr: Addr, byteAmount: Size, bytes: Bytes[Val]): JOption[WithJoin, Unit] =
    bytes match
      case StoredBytes(values, byteOrder) =>
        val Mem(store0,  numPages, pageLimit) = memories(key)
        var store = store0

        // Save zero address and zero value needed for future memory.grow
        addressLimits.ifAddrLeSize(startAddr, sizeOps.fromInt(0)) {
          zeroAddr = Some(startAddr)
          zeroValue = Some(values.head._1)
        }

        addressLimits.ifAddrLeSize(startAddr, sizeOps.sub(sizeOps.mul(numPages, pageSize), byteAmount)) {
          val (joinedValue, joinedElementByteSize) = values.map((value,elementByteSize) => (value, Topped.Actual(elementByteSize))).reduce(Join(_, _).get)
          for (ctx <- memLocAllocator(ByteMemoryAllocationContext.Fill, key, startAddr)) {
            increment(ctx)
            val (normalizedStartAddr, normalizedSize) = memOps.normalizeStartAddrAndSize(ctx, startAddr, byteAmount)
            val newRegion = MemoryRegion(
              startAddr = normalizedStartAddr,
              alignment = Powerset(0),
              elementByteSize = joinedElementByteSize,
              regionByteSize = normalizedSize,
              value = joinedValue,
              byteOrder = byteOrder,
              timestamp = this.timestamp
            )
            store = writeRegion(ctx, newRegion, store)
          }

          memories += key -> Mem(store, numPages, pageLimit)
          ()
        }

      case ReadBytes(_,_) => throw IllegalArgumentException(s"Expected StoredBytes, but got $bytes")

  override def init(key: Key, targetAddr: Addr, sourceAddr: Addr, byteAmount0: Size, dataBytes: Bytes[Val]): JOption[WithJoin, Unit] =
    Profiler.addTime("AlignedMemory.init") {
      dataBytes match
        case StoredBytes(valueList, byteOrder) =>
          val Mem(store0,  numPages, pageLimit) = memories(key)
          var store = store0

          var offset = 0
          var byteAmount = byteAmount0
          var result = JOptionA.Some[Unit](())

          for (atom@(value, regionSize) <- valueList) {
            addressLimits.ifSizeLeLimit(byteAmount, sizeOps.fromInt(0)) {
              // if byteAmount is 0, we are done writing.
            } {
              val effectiveSourceAddr = addressOffset.addOffsetToBaseAddr(-offset, sourceAddr)
              addressLimits.ifAddrLeSize(effectiveSourceAddr, sizeOps.fromInt(0)) {
                val effectiveTargetAddr = addressOffset.addOffsetToBaseAddr(offset, targetAddr)

                for (ctx <- memLocAllocator(ByteMemoryAllocationContext.Init, key, effectiveTargetAddr)) {
                  increment(ctx)
                  val newRegion = MemoryRegion(
                    startAddr = effectiveTargetAddr,
                    alignment = Powerset(0),
                    elementByteSize = Topped.Actual(1),
                    regionByteSize = sizeOps.fromInt(regionSize),
                    value = value,
                    byteOrder = byteOrder,
                    timestamp = this.timestamp
                  )
                  store = writeRegion(ctx, newRegion, store)
                }

                offset += regionSize
                byteAmount = sizeOps.sub(byteAmount, sizeOps.fromInt(regionSize))
              }
              ()
            }
          }

          memories += key -> Mem(store, numPages, pageLimit)

          result

        case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bs")
    }

  override def size(key: Key): Size =
    memories(key).numPages

  override def grow(key: Key, deltaPages: Size): JOption[WithJoin, Size] =
    val Mem(store0, numPages, pageLimit) = memories(key)
    var store = store0

    val newNumPages = sizeOps.add(numPages, deltaPages)
    val startAddr = addressLimits.addSizeToAddr(sizeOps.mul(numPages, pageSize), zeroAddr.getOrElse(throw IllegalStateException("No zero address saved.")))

    val (resultPages, returnValue) = addressLimits.ifSizeLeLimit(newNumPages, sizeOps.min(maxPages, pageLimit)) {
      for (ctx <- memLocAllocator(ByteMemoryAllocationContext.Fill, key, startAddr)) {
        increment(ctx)
        val newRegion = MemoryRegion(
          startAddr = startAddr,
          alignment = Powerset(0),
          elementByteSize = Topped.Actual(1),
          regionByteSize = sizeOps.mul(deltaPages, pageSize),
          value = zeroValue.getOrElse(throw IllegalStateException("No zero value saved.")),
          byteOrder = Topped.Actual(ByteOrder.LITTLE_ENDIAN),
          timestamp = this.timestamp
        )
        store = writeRegion(ctx, newRegion, store)
      }
      (newNumPages, JOptionA.Some(numPages))
    } {
      (numPages, JOptionA.None[Size]())
    }
    memories += key -> Mem(store, resultPages, pageLimit)

    returnValue

  override def putNew(key: Key, initSize: Size, sizeLimit: Option[Size]): Unit =
    memories += key -> Mem[Context,Addr,Timestamp,Val,Size](SortedMap.empty, initSize, sizeLimit.getOrElse(Join(initSize, maxPages).get))

  override def addressIterator[_Addr: ClassTag](valueIterator: Any => Iterator[_Addr]): Iterator[_Addr] =
    memories.values.flatMap(_.addressIterator(valueIterator)).iterator

  case class AlignedMemoryState(state: Map[Key, Mem[Context, Addr, Timestamp, Val, Size]]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: AlignedMemoryState => MapEquals(this.state, other.state)
        case _ => false

    override def toString: String = s"AlignedMemoryState($hashCode, $state)"


  override type State = AlignedMemoryState

  override def getState: State = AlignedMemoryState(memories)

  override def setState(olderState: State): Unit = Profiler.addTime("AlignedMemory.setState") {
    memories = memories.map { (key,mem) =>
      (key,mem.copy(store = mem.store.filter {
        case (physAddr@PhysicalAddress(_,Recency.Recent), _) =>
          !olderState.state.get(key).exists(_.store.contains(physAddr))
        case _ => true
      }))
    }
    memories = widen(olderState, getState).get.state
  }

  override def setStateNonMonotonically(st: AlignedMemoryState): Unit =
    memories = st.state

  override def join: Join[State] = (s1: State,s2:State) => Profiler.addTime("AlignedMemory.combine") { Join(s1.state,s2.state).map(AlignedMemoryState(_)) }
  override def widen: Widen[State] = (s1: State,s2:State) => Profiler.addTime("AlignedMemory.combine") { Widen(s1.state,s2.state).map(AlignedMemoryState(_)) }

  override def setBottom: Unit =
    memories = Map()

  private object ConstantSize:
    def unapply(size: Size): Option[Int] =
      sizeOps.toTopped(size).toOption

case class Mem[Ctx, Addr, Timestamp, Val, Size](store: SortedMap[PhysicalAddress[Ctx], MemoryRegion[Addr, Size, Timestamp, Val]],
                                                numPages: Size,
                                                pageLimit: Size):
  def addressIterator[_Addr: ClassTag](valueIterator: Any => Iterator[_Addr]): Iterator[_Addr] =
    store.values.flatMap(_.addressIterator(valueIterator)).iterator ++
      valueIterator(numPages) ++
      valueIterator(pageLimit)

  override def equals(obj: Any): Boolean =
    obj match
      case other: Mem[Ctx, Addr, Timestamp, Val, Size] @unchecked => MapEquals(this.store, other.store) && this.numPages.equals(other.numPages) && this.pageLimit.equals(other.pageLimit)
      case _ => false


case class MemoryRegion[Addr, Size, Timestamp, Val](startAddr: Addr, alignment: Powerset[Int], elementByteSize: Topped[Int], regionByteSize: Size, timestamp: Timestamp, value: Val, byteOrder: Topped[ByteOrder]):
  def addressIterator[_Addr: ClassTag](valueIterator: Any => Iterator[_Addr]): Iterator[_Addr] =
    valueIterator(startAddr) ++ valueIterator(value)

  override def equals(obj: Any): Boolean =
    obj match
      case other: MemoryRegion[Addr, Size, Timestamp, Val] @unchecked =>
        // Does not compare timestamp to avoid non-termination. Timestamp lattice is infinite and without widening.
        this.startAddr == other.startAddr &&
          this.alignment == other.alignment &&
          this.elementByteSize == other.elementByteSize &&
          this.value == other.value &&
          this.regionByteSize == other.regionByteSize &&
          this.byteOrder == other.byteOrder
      case _ => false

  override def hashCode(): Int =
    (this.startAddr, this.alignment, this.value, this.elementByteSize, this.regionByteSize, this.byteOrder).hashCode()

enum Bytes[Val]:
  case StoredBytes(value: List[(Val,Int)], byteOrder: Topped[ByteOrder])
  case ReadBytes(value: Topped[List[(Val,Int)]], byteOrder: Topped[ByteOrder])

  def byteSize: Topped[Int] =
    this match
      case StoredBytes(valueArray, _) => Topped.Actual(valueArray.map(_._2).sum)
      case ReadBytes(Topped.Actual(valueArray), _) => Topped.Actual(valueArray.map(_._2).sum)
      case _ => Topped.Top

  def ++(other: Bytes[Val]): Bytes[Val] =
    (this, other) match
      case (ReadBytes(Topped.Actual(valueList1), byteOrder1), ReadBytes(Topped.Actual(valueList2), byteOrder2)) =>
        ReadBytes(Topped.Actual(valueList1 ++ valueList2), Join(byteOrder1, byteOrder2).get)
      case (_, _) =>
        ReadBytes(Topped.Top, Topped.Top)

  def toReadBytes: Bytes.ReadBytes[Val] =
    this match
      case bytes: ReadBytes[Val] @unchecked => bytes
      case bytes: StoredBytes[Val] => ReadBytes(Topped.Actual(bytes.value), bytes.byteOrder)

  override def toString: String =
    this match
      case bytes: StoredBytes[Val] => s"${bytes.value}"
      case bytes: ReadBytes[Val] => s"${bytes.value}"

given CombineMem[Context: Finite, Addr, Timestamp, Val, Size, W <: Widening](using combineAddr: Combine[Addr,W], joinTimestamp: Join[Timestamp], combineVal: Combine[Val,W],  combineSize: Combine[Size,W]): Combine[Mem[Context, Addr, Timestamp, Val, Size], W] with
  override def apply(v1: Mem[Context, Addr, Timestamp, Val, Size], v2: Mem[Context, Addr, Timestamp, Val, Size]): MaybeChanged[Mem[Context, Addr, Timestamp, Val, Size]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
      val result = for {
        store <- CombineFiniteKeySortedMap[PhysicalAddress[Context], MemoryRegion[Addr, Size, Timestamp, Val], W](v1.store, v2.store)
        numPages <- combineSize(v1.numPages, v2.numPages)
        pageLimit <- combineSize(v1.pageLimit, v2.pageLimit)
      } yield Mem(store, numPages, pageLimit)
      result

given CombineRegion[Addr, Size, Timestamp, Val, W <: Widening](using combineAddr: Combine[Addr, W], combineSize: Combine[Size,W], joinTimestamp: Join[Timestamp], combineVal: Combine[Val,W]): Combine[MemoryRegion[Addr, Size, Timestamp, Val], W] with
  override def apply(v1: MemoryRegion[Addr, Size, Timestamp, Val], v2: MemoryRegion[Addr, Size, Timestamp, Val]): MaybeChanged[MemoryRegion[Addr, Size, Timestamp, Val]] =
    if (v1 eq v2)
      Unchanged(v1)
    else
      for {
        startAddr <- combineAddr(v1.startAddr, v2.startAddr)
        alignment <- Join(v1.alignment, v2.alignment)
        elementByteSize <- Join(v1.elementByteSize, v2.elementByteSize)
        regionByteSize <- combineSize(v1.regionByteSize, v2.regionByteSize)
        value <- combineVal(v1.value, v2.value)
        byteOrder <- Join(v1.byteOrder, v2.byteOrder)
      } yield MemoryRegion(startAddr, alignment, elementByteSize, regionByteSize, joinTimestamp(v1.timestamp, v2.timestamp).get, value, byteOrder)

given CombineBytes[Addr, Val, W <: Widening](using combineVal: Combine[Val, W]): Combine[Bytes[Val], W] with
  override def apply(v1: Bytes[Val], v2: Bytes[Val]): MaybeChanged[Bytes[Val]] =
    (v1, v2) match
      case (_,_) if v1 eq v2 => Unchanged(v1)
      case (ReadBytes(val1, byteOrder1), ReadBytes(val2, byteOrder2)) =>
        for {
          value <- combineListOfValues(val1, val2)
          byteOrder <- Join(byteOrder1, byteOrder2)
        } yield ReadBytes(value, byteOrder)
      case _ => throw CannotJoinException(s"Can only join ReadBytes, but got ($v1, $v2)")

  private def combineListOfValues(v1: Topped[List[(Val,Int)]], v2: Topped[List[(Val,Int)]]): MaybeChanged[Topped[List[(Val,Int)]]] =
    (v1, v2) match
      case (Topped.Top, _) => Unchanged(v1)
      case (_, Topped.Top) => Changed(v2)
      case (Topped.Actual(l1), Topped.Actual(l2)) =>
        if(l1.length != l2.length)
          Changed(Topped.Top)
        else if(! l1.zip(l2).forall{case ((_,byteSize1), (_,byteSize2)) => byteSize1 == byteSize2})
          Changed(Topped.Top)
        else
          var changed = false
          val joined = l1.zip(l2).map {
            case ((b1, byteSize1), (b2,_)) =>
              val joinedVals = Combine(b1,b2)
              changed |= joinedVals.hasChanged
              (joinedVals.get,byteSize1)
          }
          MaybeChanged(Topped.Actual(joined), changed)
