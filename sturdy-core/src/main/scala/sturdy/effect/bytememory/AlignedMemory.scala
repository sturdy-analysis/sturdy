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
  def matchRegion[Timestamp: PartialOrder](addr: Addr, size: Size, mems: Mem[Context, Addr, Timestamp, Val, Size]): Iterator[(PhysicalAddress[Context], MemoryRegion[Addr, Size, Timestamp, Val], AlignedRead)]
  def isSummaryRegion[Timestamp: PartialOrder](ctx: Context, newRegion: MemoryRegion[Addr, Size, Timestamp, Val]): Boolean
  def knownStartAddrAndSize(ctx: Context, startAddr: Addr, byteSize: Int): (Addr,Size)

trait SizeOps[Size]:
  def fromInt(size: Int): Size
  def add(size1: Size, size2: Size): Size
  def sub(size1: Size, size2: Size): Size
  def mul(size1: Size, size2: Size): Size
  def min(size1: Size, size2: Size): Size
  def toTopped(size: Size): Topped[Int]

enum ByteMemoryAllocationContext:
  case Write
  case Fill

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
   memLocAllocator: Allocator[IterableOnce[Context],(ByteMemoryAllocationContext,Key,Addr)],
   moveMemLoc: Allocator[Context, (Key,Context,Addr)]
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

  var memories: Map[Key, Mem[Context, Addr, Timestamp, Val, Size]] = Map()

  override def read(key: Key, readAddr: Addr, length: Int, align: Int = 1): JOption[WithJoin, Bytes[Val]] =
    Profiler.addTime("AlignedMemory.read") {
      read0(key,readAddr, length, align)
    }

  private def read0(key: Key, readAddr: Addr, length: Int, align: Int = 1): JOption[WithJoin, Bytes[Val]] =
    val mem = memories(key)

    val regionsIterator = memOps.matchRegion(readAddr, sizeOps.fromInt(length), mem)

    val topBytes: ReadBytes[Val] = ReadBytes[Val](value = Topped.Top, byteOrder = Topped.Top)
    val readBytes: Bytes[Val] =
      regionsIterator
        .map{
          case (_, MemoryRegion(_, _, ConstantSize(byteSize), _, value, byteOrder), AlignedRead.Aligned) =>
            ReadBytes[Val](
              value = Topped.Actual(List((value,byteSize))),
              byteOrder = byteOrder
            )
          case b =>
            topBytes
        }
        .atMost(10, orElse = topBytes)
        .reduce(Join(_,_).get)

    readBytes.byteSize match
      case Topped.Actual(byteSize) =>
        if(length - byteSize <= 0) {
          addressLimits.ifAddrLeSize(readAddr, sizeOps.sub(sizeOps.mul(mem.numPages, pageSize), sizeOps.fromInt(length))) {
            readBytes
          }
        } else {
          read0(key = key, readAddr = addressOffset.addOffsetToAddr(byteSize, readAddr), length - byteSize).map { readBytes ++ _ }
        }
      case Topped.Top =>
        JOptionA.NoneSome(ReadBytes[Val](value = Topped.Top, byteOrder = Topped.Top))

  extension[A](iter: Iterator[A])
    private def atMost(n: Int, orElse: A): Iterator[A] =
      new AbstractIterator[A]:
        var taken = 0
        override def next(): A =
          if(taken < n) {
            taken += 1
            iter.next()
          } else {
            taken += 1
            orElse
          }

        override def hasNext: Boolean = iter.hasNext && taken <= n


  override def write(key: Key, addr: Addr, bytes: Bytes[Val], alignment: Int = 0): JOption[WithJoin, Unit] =
    Profiler.addTime("AlignedMemory.write") {
      write0(key, addr, bytes, alignment)
    }

  private def write0(key: Key, addr: Addr, bytes: Bytes[Val], alignment: Int = 0): JOption[WithJoin, Unit] =
    bytes match
      case StoredBytes((value, byteSize) :: rest, byteOrder) =>
        val Mem(store0, numPages, pageLimit) = memories(key)

        addressLimits.ifAddrLeSize(addr, sizeOps.sub(sizeOps.mul(numPages, pageSize), sizeOps.fromInt(byteSize))) {

          var store = store0
          for (ctx <- memLocAllocator(ByteMemoryAllocationContext.Write, key, addr)) {
            increment(ctx)
            val (knownStartAddr,knownSize) = memOps.knownStartAddrAndSize(ctx, addr, byteSize)
            val newRegion = MemoryRegion(
              startAddr = knownStartAddr,
              alignment = Powerset(alignment),
              byteSize = knownSize,
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
    for((physAddr, region,_) <- memOps.matchRegion(srcAddr, byteAmount, mem)) {
      val destAddr = addressOffset.moveAddress(region.startAddr, srcOffset = srcAddr, dstOffset = dstAddr)
      val destCtx = moveMemLoc(key, physAddr.ctx, destAddr)
      store += PhysicalAddress(destCtx, Recency.Recent) -> Join(store.get(PhysicalAddress(destCtx, Recency.Recent)), Some(region.copy(startAddr = destAddr))).get.get
      store += PhysicalAddress(destCtx, Recency.Old) -> Join(store.get(PhysicalAddress(destCtx, Recency.Old)), Some(region.copy(startAddr = destAddr))).get.get
    }
    memories += key -> mem.copy(store = store)
    JOptionA.NoneSome(())

  override def fill(key: Key, addr: Addr, byteAmount: Size, bytes: Bytes[Val]): JOption[WithJoin, Unit] =
    bytes match
      case StoredBytes(values, byteOrder) =>
        val Mem(store0,  numPages, pageLimit) = memories(key)
        var store = store0
        addressLimits.ifAddrLeSize(addr, sizeOps.sub(sizeOps.mul(numPages, pageSize), byteAmount)) {
          val joinedValue = values.map(_._1).reduce(Join(_, _).get)
          val joinedByteSize = values.map((_,i) => Powerset(i)).reduce(Join(_,_).get).set
          val size = Join(sizeOps.fromInt(joinedByteSize.min), sizeOps.fromInt(joinedByteSize.max)).get
          val startAddr = Join(addr, addressLimits.addSizeToAddr(byteAmount, addr)).get
          for (ctx <- memLocAllocator(ByteMemoryAllocationContext.Fill, key, startAddr)) {
            increment(ctx)
            val newRegion = MemoryRegion(
              startAddr = startAddr,
              alignment = Powerset(0),
              byteSize = size,
              timestamp = this.timestamp,
              value = joinedValue,
              byteOrder = byteOrder
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
          var offset = 0
          var byteAmount = byteAmount0
          var result = JOptionA.Some[Unit](())

          for (atom@(value, byteSize) <- valueList) {
            addressLimits.ifSizeLeLimit(byteAmount, sizeOps.fromInt(0)) {
              // if byteAmount is 0, we are done writing.
            } {
              val effectiveSourceAddr = addressOffset.addOffsetToAddr(-offset, sourceAddr)
              addressLimits.ifAddrLeSize(effectiveSourceAddr, sizeOps.fromInt(0)) {
                val effectiveTargetAddr = addressOffset.addOffsetToAddr(offset, targetAddr)
                result = Join(result, write(key, effectiveTargetAddr, StoredBytes(List(atom), byteOrder)).asInstanceOf[JOptionA[Unit]]).get

                offset += byteSize
                byteAmount = sizeOps.sub(byteAmount, sizeOps.fromInt(byteSize))
              }
              ()
            }
          }

          result

        case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bs")
    }

  override def size(key: Key): Size =
    memories(key).numPages

  override def grow(key: Key, deltaPages: Size): JOption[WithJoin, Size] =
    val Mem(addressRanges, numPages, pageLimit) = memories(key)
    val newNumPages = sizeOps.add(numPages, deltaPages)

    val (resultPages, returnValue) = addressLimits.ifSizeLeLimit(newNumPages, sizeOps.min(maxPages, pageLimit)) {
      (newNumPages, JOptionA.Some(numPages))
    } {
      (numPages, JOptionA.None[Size]())
    }
    memories += key -> Mem(addressRanges, resultPages, pageLimit)

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


case class MemoryRegion[Addr, Size, Timestamp, Val](startAddr: Addr, alignment: Powerset[Int], byteSize: Size, timestamp: Timestamp, value: Val, byteOrder: Topped[ByteOrder]):
  def addressIterator[_Addr: ClassTag](valueIterator: Any => Iterator[_Addr]): Iterator[_Addr] =
    valueIterator(startAddr) ++ valueIterator(value)

  override def equals(obj: Any): Boolean =
    obj match
      case other: MemoryRegion[Addr, Size, Timestamp, Val] @unchecked =>
        // Does not compare timestamp to avoid non-termination. Timestamp lattice is infinite and without widening.
        this.startAddr == other.startAddr && this.alignment == other.alignment && this.value == other.value && this.byteSize == other.byteSize && this.byteOrder == other.byteOrder
      case _ => false

  override def hashCode(): Int =
    (this.startAddr, this.alignment, this.value, this.byteSize, this.byteOrder).hashCode()

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
      val result = for {
        startAddr <- combineAddr(v1.startAddr, v2.startAddr)
        alignment <- Join(v1.alignment, v2.alignment)
        byteSize <- combineSize(v1.byteSize, v2.byteSize)
        value <- combineVal(v1.value, v2.value)
        byteOrder <- Join(v1.byteOrder, v2.byteOrder)
      } yield MemoryRegion(startAddr, alignment, byteSize, joinTimestamp(v1.timestamp, v2.timestamp).get, value, byteOrder)
      result

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
