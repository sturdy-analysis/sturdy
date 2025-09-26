package sturdy.effect.bytememory

import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import Bytes.*
import sturdy.data
import sturdy.effect.{ComputationJoiner, EffectStack, TrySturdy}
import sturdy.util.Profiler
import sturdy.values.addresses.{AddressLimits, AddressOffset}

import java.nio.ByteOrder
import scala.annotation.tailrec
import scala.collection.AbstractIterator
import scala.collection.immutable.SortedMap
import scala.reflect.ClassTag
import scala.util.boundary
import scala.util.boundary.break

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

trait LanguageSpecificMemOps[Context, Addr, Size]:
  def matchRegion[Val, Timestamp: PartialOrder](addr: Addr, size: Size, mems: Mem[Context, Addr, Timestamp, Val, Size]): Iterator[(PhysicalAddress[Context], MemoryRegion[Addr, Timestamp, Val], AlignedRead)]

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
   memLocAllocator: Allocator[IterableOnce[Context],(Key,Addr)],
   moveMemLoc: Allocator[Context, (Key,Context,Addr)]
  )
  (using
   memOps: LanguageSpecificMemOps[Context, Addr, Size],
   addressOffset: AddressOffset[Addr],
   addressLimits: AddressLimits[Addr, Size, WithJoin],
   sizeIntOps: IntegerOps[Int, Size],
   effectStack: EffectStack
  )
  extends Memory[Key, Addr, Bytes[Val], Size, WithJoin] with VectorClock[Context]:

  type MemoryCtx = Context
  private val pageSize: Size = sizeIntOps.integerLit(ConcreteMemory.pageSize)
  private val maxPages: Size = sizeIntOps.integerLit(ConcreteMemory.maxPageNum)

  var memories: Map[Key, Mem[Context, Addr, Timestamp, Val, Size]] = Map()

  override def read(key: Key, readAddr: Addr, length: Int, align: Int = 1): JOption[WithJoin, Bytes[Val]] =
    Profiler.addTime("AlignedMemory.read") {
      read0(key,readAddr, length, align)
    }

  private def read0(key: Key, readAddr: Addr, length: Int, align: Int = 1): JOption[WithJoin, Bytes[Val]] =
    val mem = memories(key)

    val regionsIterator = memOps.matchRegion(readAddr, sizeIntOps.integerLit(length), mem)

    val topBytes: ReadBytes[Val] = ReadBytes[Val](value = Topped.Top, byteOrder = Topped.Top)
    val readBytes: Bytes[Val] =
      if(! regionsIterator.hasNext)
        mem.fillBytes.get
      else
        regionsIterator
          .map{
            case (_, MemoryRegion(_, _, _, value, Topped.Actual(byteSize), byteOrder), AlignedRead.Aligned) =>
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
          addressLimits.ifAddrLeSize(readAddr, sizeIntOps.sub(sizeIntOps.mul(mem.numPages, pageSize), sizeIntOps.integerLit(length))) {
            readBytes
          }
        } else {
          read0(key = key, readAddr = addressOffset.addOffsetToAddr(byteSize, readAddr), length - byteSize).map { readBytes ++ _ }
        }
      case Topped.Top =>
        JOptionA.NoneSome(ReadBytes[Val](value = Topped.Top, byteOrder = Topped.Top))

  extension[A](iter: Iterator[A])
    def atMost(n: Int, orElse: A): Iterator[A] =
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


  override def write(key: Key, addr: Addr, bytes: Bytes[Val], alignment: Int = 1): JOption[WithJoin, Unit] =
    Profiler.addTime("AlignedMemory.write") {
      write0(key, addr, bytes, alignment)
    }

  private def write0(key: Key, addr: Addr, bytes: Bytes[Val], alignment: Int = 1): JOption[WithJoin, Unit] =
    bytes match
      case StoredBytes((value, byteSize) :: rest, byteOrder) =>
        val Mem(store, fillBytes, numPages, pageLimit) = memories(key)

        addressLimits.ifAddrLeSize(addr, sizeIntOps.sub(sizeIntOps.mul(numPages, pageSize), sizeIntOps.integerLit(byteSize))) {

          var newStore = store
          for (ctx <- memLocAllocator(key, addr)) {
            increment(ctx)
            newStore += PhysicalAddress(ctx, Recency.Recent) -> MemoryRegion(
              startAddr = addr,
              alignment = Topped.Actual(alignment),
              timestamp = this.timestamp,
              value = value,
              byteSize = Topped.Actual(byteSize),
              byteOrder = byteOrder
            )
            for (oldRegion <- Join(store.get(PhysicalAddress(ctx, Recency.Recent)), store.get(PhysicalAddress(ctx, Recency.Old))).get) {
              newStore += PhysicalAddress(ctx, Recency.Old) -> oldRegion
            }
          }
          memories += key -> Mem(newStore, fillBytes, numPages, pageLimit)

          write0(
            key = key,
            addr = addressOffset.addOffsetToAddr(byteSize, addr),
            bytes = StoredBytes(rest, byteOrder)
          )
        }

      case StoredBytes(Nil, byteOrder) =>
        JOptionA.Some(())

      case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bytes")

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

  override def fill(key: Key, addr: Addr, byteAmount: Size, value: Bytes[Val]): JOption[WithJoin, Unit] =
    val Mem(store, fillBytes, numPages, pageLimit) = memories(key)
    addressLimits.ifAddrLeSize(addr, sizeIntOps.sub(sizeIntOps.mul(numPages, pageSize), byteAmount)) {
      memories += key -> Mem(store, Join(fillBytes, Some(value.toReadBytes)).get, numPages, pageLimit)
      ()
    }

  override def init(key: Key, targetAddr0: Addr, sourceAddr0: Addr, byteAmount0: Size, dataBytes: Bytes[Val]): JOption[WithJoin, Unit] =
    Profiler.addTime("AlignedMemory.init") {
      dataBytes match
        case StoredBytes(valueList, byteOrder) =>
          var targetAddr = targetAddr0
          var sourceAddr = sourceAddr0
          var byteAmount = byteAmount0
          var result = JOptionA.Some[Unit](())

          for (atom@(value, byteSize) <- valueList) {
            addressLimits.ifSizeLeLimit(byteAmount, sizeIntOps.integerLit(0)) {
              // if byteAmount is 0, we are done writing.
            } {
              addressLimits.ifAddrLeSize(sourceAddr, sizeIntOps.integerLit(0)) {
                result = Join(result, write(key, targetAddr, StoredBytes(List(atom), byteOrder)).asInstanceOf[JOptionA[Unit]]).get
                targetAddr = addressOffset.addOffsetToAddr(+byteSize, targetAddr)
                byteAmount = sizeIntOps.sub(byteAmount, sizeIntOps.integerLit(byteSize))
              }.orElseAndThen[Unit] {
                // if sourceAddr > 0
                sourceAddr = addressOffset.addOffsetToAddr(-byteSize, sourceAddr)
              } { opt => opt }
            }
          }

          result

        case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bs")
    }

  override def size(key: Key): Size =
    memories(key).numPages

  override def grow(key: Key, deltaPages: Size): JOption[WithJoin, Size] =
    val Mem(addressRanges, fillBytes, numPages, pageLimit) = memories(key)
    val newNumPages = sizeIntOps.add(numPages, deltaPages)

    val (resultPages, returnValue) = addressLimits.ifSizeLeLimit(newNumPages, sizeIntOps.min(maxPages, pageLimit)) {
      (newNumPages, JOptionA.Some(numPages))
    } {
      (numPages, JOptionA.None[Size]())
    }
    memories += key -> Mem(addressRanges, fillBytes, resultPages, pageLimit)

    returnValue

  override def putNew(key: Key, initSize: Size, sizeLimit: Option[Size]): Unit =
    memories += key -> Mem[Context,Addr,Timestamp,Val,Size](SortedMap.empty, None, initSize, sizeLimit.getOrElse(Join(initSize, maxPages).get))

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
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
          ! olderState.state.get(key).map(_.store.contains(physAddr)).getOrElse(false)
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

case class Mem[Ctx, Addr, Timestamp, Val, Size](store: SortedMap[PhysicalAddress[Ctx], MemoryRegion[Addr, Timestamp, Val]],
                                                fillBytes: Option[Bytes[Val]],
                                                numPages: Size,
                                                pageLimit: Size):
  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    store.values.flatMap(_.addressIterator(valueIterator)).iterator ++
      valueIterator(numPages) ++
      valueIterator(pageLimit)

  override def equals(obj: Any): Boolean =
    obj match
      case other: Mem[Ctx, Addr, Timestamp, Val, Size] @unchecked => MapEquals(this.store, other.store) && this.fillBytes.equals(other.fillBytes) && this.numPages.equals(other.numPages) && this.pageLimit.equals(other.pageLimit)
      case _ => false


case class MemoryRegion[Addr, Timestamp, Val](startAddr: Addr, alignment: Topped[Int], timestamp: Timestamp, value: Val, byteSize: Topped[Int], byteOrder: Topped[ByteOrder]):
  def endAddr(using addrOffset: AddressOffset[Addr]): Topped[Addr] =
    byteSize.map(addrOffset.addOffsetToAddr(_, startAddr))

  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    valueIterator(startAddr) ++ valueIterator(value)

  override def equals(obj: Any): Boolean =
    obj match
      case other: MemoryRegion[Addr, Timestamp, Val] @unchecked =>
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
        store <- CombineFiniteKeySortedMap[PhysicalAddress[Context], MemoryRegion[Addr, Timestamp, Val], W](v1.store, v2.store)
        fillBytes <- Combine(v1.fillBytes, v2.fillBytes)
        numPages <- combineSize(v1.numPages, v2.numPages)
        pageLimit <- combineSize(v1.pageLimit, v2.pageLimit)
      } yield (Mem(store, fillBytes, numPages, pageLimit))
      result

given CombineRegion[Addr, Timestamp, Val, W <: Widening](using combineAddr: Combine[Addr, W], joinTimestamp: Join[Timestamp], combineVal: Combine[Val,W]): Combine[MemoryRegion[Addr, Timestamp, Val], W] with
  override def apply(v1: MemoryRegion[Addr, Timestamp, Val], v2: MemoryRegion[Addr, Timestamp, Val]): MaybeChanged[MemoryRegion[Addr, Timestamp, Val]] =
    if (v1 eq v2)
      Unchanged(v1)
    else
      val result = for {
        startAddr <- combineAddr(v1.startAddr, v2.startAddr)
        alignment <- Join(v1.alignment, v2.alignment)
        value <- combineVal(v1.value, v2.value)
        byteSize <- Join(v1.byteSize, v2.byteSize)
        byteOrder <- Join(v1.byteOrder, v2.byteOrder)
      } yield (MemoryRegion(startAddr, alignment, joinTimestamp(v1.timestamp, v2.timestamp).get, value, byteSize, byteOrder))
      result

given CombineBytes[Addr, Val, W <: Widening](using combineVal: Combine[Val, W]): Combine[Bytes[Val], W] with
  override def apply(v1: Bytes[Val], v2: Bytes[Val]): MaybeChanged[Bytes[Val]] =
    (v1, v2) match
      case (_,_) if(v1 eq v2) => Unchanged(v1)
      case (ReadBytes(val1, byteOrder1), ReadBytes(val2, byteOrder2)) =>
        for {
          value <- combineListOfValues(val1, val2)
          byteOrder <- Join(byteOrder1, byteOrder2)
        } yield (ReadBytes(value, byteOrder))
      case _ => throw CannotJoinException(s"Can only join ReadBytes, but got ($v1, $v2)")

  def combineListOfValues(v1: Topped[List[(Val,Int)]], v2: Topped[List[(Val,Int)]]): MaybeChanged[Topped[List[(Val,Int)]]] =
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
