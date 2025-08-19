package sturdy.effect.bytememory

import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import Bytes.*
import sturdy.data
import sturdy.effect.EffectStack
import sturdy.util.Profiler
import sturdy.values.addresses.{AddressLimits, AddressOffset}

import java.nio.ByteOrder
import scala.annotation.tailrec
import scala.collection.immutable.SortedMap
import scala.reflect.ClassTag

/**
 * Indicates if the read address aligns with the stored address, i.e., if storedAddr = readAddr.
 */
enum AlignedRead:
  case Aligned
  case Unaligned
  case MaybeAligned

  def toToppedBoolean: Topped[Boolean] =
    this match
      case Aligned => Topped.Actual(true)
      case Unaligned => Topped.Actual(false)
      case MaybeAligned => Topped.Top

trait MatchRegions[Context, Addr, Size]:
  def apply[Val](addr: Addr, mems: Mem[Context, Addr, Size, Val]): IterableOnce[(MemoryRegion[Addr, Val], AlignedRead)]

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
   memLocAllocator: Allocator[IterableOnce[Context],(Key,Addr,Val)]
  )
  (using
   matchRegions: MatchRegions[Context, Addr, Size],
   addressOffset: AddressOffset[Addr],
   addressLimits: AddressLimits[Addr, Size, WithJoin],
   sizeIntOps: IntegerOps[Int, Size],
   effectStack: EffectStack
  )
  extends Memory[Key, Addr, Bytes[Val], Size, WithJoin]:

  type MemoryCtx = Context
  private val pageSize: Size = sizeIntOps.integerLit(ConcreteMemory.pageSize)
  private val maxPages: Size = sizeIntOps.integerLit(ConcreteMemory.maxPageNum)

  var memories: Map[Key, Mem[Context, Addr, Size, Val]] = Map()

  override def read(key: Key, readAddr: Addr, length: Int): JOption[WithJoin, Bytes[Val]] =
    val mem = memories(key)

    val regions = matchRegions(readAddr, mem).iterator

    val readBytes: Bytes[Val] =
      if(! regions.hasNext)
        defaultBytes
      else
        regions
          .map{
            case (MemoryRegion(_, value, Topped.Actual(byteSize), byteOrder), AlignedRead.Aligned) =>
              ReadBytes[Val](
                value = Topped.Actual(List((value,byteSize))),
                byteOrder = byteOrder
              )
            case _ =>
              ReadBytes[Val](
                value = Topped.Top,
                byteOrder = Topped.Top
              )
          }
          .reduce(Join(_,_).get)

    readBytes.byteSize match
      case Topped.Actual(byteSize) =>
        if(length - byteSize <= 0) {
          addressLimits.ifAddrLeSize(readAddr, sizeIntOps.sub(sizeIntOps.mul(mem.numPages, pageSize), sizeIntOps.integerLit(length))) {
            readBytes
          }
        } else {
          read(key = key, readAddr = readAddr, length - byteSize).map { readBytes ++ _ }
        }
      case Topped.Top =>
        JOptionA.NoneSome(ReadBytes[Val](value = Topped.Top, byteOrder = Topped.Top))

  @tailrec
  override def write(key: Key, addr: Addr, bytes: Bytes[Val]): JOption[WithJoin, Unit] =
    bytes match
      case StoredBytes((value, byteSize) :: rest, byteOrder) =>
        val Mem(store, numPages, pageLimit) = memories(key)

        val newRegion = MemoryRegion(
          startAddr = addr,
          value = value,
          byteSize = Topped.Actual(byteSize),
          byteOrder = byteOrder
        )

        var newStore = store
        for (ctx <- memLocAllocator(key, addr, value)) {
          newStore += PhysicalAddress(ctx, Recency.Recent) -> newRegion
          for (oldRegion <- Join(store.get(PhysicalAddress(ctx, Recency.Recent)), store.get(PhysicalAddress(ctx, Recency.Old))).get) {
            newStore += PhysicalAddress(ctx, Recency.Old) -> oldRegion
          }
        }
        memories += key -> Mem(newStore, numPages, pageLimit)

        if(rest.isEmpty) {
          addressLimits.ifAddrLeSize(addr, sizeIntOps.sub(sizeIntOps.mul(numPages, pageSize), sizeIntOps.integerLit(byteSize))) {
            ()
          }
        } else {
          write(
            key = key,
            addr = addressOffset.addOffsetToAddr(byteSize, addr),
            bytes = StoredBytes(rest, byteOrder)
          )
        }

      case StoredBytes(Nil, byteOrder) =>
        JOptionA.Some(())

      case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bytes")

  override def copy(key: Key, srcAddr: Addr, dstAddr: Addr, byteAmount: Size): JOptionA[Unit] = ???
  override def fill(key: Key, addr: Addr, byteAmount: Size, value: Bytes[Val]): JOptionA[Unit] = ???
  override def init(key: Key, targetAddr: Addr, sourceAddr: Addr, byteAmount: Size, dataBytes: Bytes[Val]): JOption[WithJoin, Unit] =
    dataBytes match
      case StoredBytes(valueList, byteOrder) =>
        addressLimits.ifSizeLeLimit(byteAmount, sizeIntOps.integerLit(0)) {
          // if byteAmount is 0, we are done writing.
          JOptionA.Some(())
        } {
          addressLimits.ifAddrLeSize(sourceAddr, sizeIntOps.integerLit(0)) {
            if(valueList.isEmpty) {
              throw IllegalArgumentException(s"Memory.Init: Need to write $byteAmount of bytes, but ran out of bytes to write: $dataBytes")
            } else {
              val atom@(value, byteSize) = valueList.head

              write(key, targetAddr, StoredBytes(List(atom), byteOrder)).flatMap(_ =>
                init(
                  key = key,
                  targetAddr = addressOffset.addOffsetToAddr(+byteSize, targetAddr),
                  sourceAddr = sourceAddr,
                  byteAmount = sizeIntOps.sub(byteAmount, sizeIntOps.integerLit(byteSize)),
                  dataBytes = StoredBytes(valueList.tail, byteOrder)
                )
              ).asInstanceOf[JOptionA[Unit]]
            }
          }.orElseAndThen[JOptionA[Unit]] {
            // if sourceAddr > 0
            if (valueList.isEmpty) {
              throw IllegalArgumentException(s"Memory.Init: Need to write $byteAmount of bytes, but ran out of bytes to write: $dataBytes")
            } else {
              val atom@(_, byteSize) = valueList.head
              init(
                key = key,
                targetAddr = targetAddr,
                sourceAddr = addressOffset.addOffsetToAddr(-byteSize, sourceAddr),
                byteAmount = byteAmount,
                dataBytes = StoredBytes(valueList.tail, byteOrder)
              ).asInstanceOf[JOptionA[Unit]]
            }
          } { opt => opt }
        }

      case bs: ReadBytes[Val] => throw IllegalArgumentException(s"Expected StoredBytes, but got $bs")

  override def size(key: Key): Size =
    memories(key).numPages

  override def grow(key: Key, deltaPages: Size): JOption[WithJoin, Size] =
    val Mem(addressRanges, numPages, pageLimit) = memories(key)
    val newNumPages = sizeIntOps.add(numPages, deltaPages)

    val (resultPages, returnValue) = addressLimits.ifSizeLeLimit(numPages, sizeIntOps.min(maxPages, pageLimit)) {
      (newNumPages, JOptionA.Some(numPages))
    } {
      (numPages, JOptionA.None[Size]())
    }
    memories += key -> Mem(addressRanges, resultPages, pageLimit)

    returnValue

  override def putNew(key: Key, initSize: Size, sizeLimit: Option[Size]): Unit =
    memories += key -> Mem[Context,Addr,Size,Val](SortedMap.empty, initSize, sizeLimit.getOrElse(Join(sizeIntOps.integerLit(0), maxPages).get))

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    memories.values.flatMap(_.addressIterator(valueIterator)).iterator

  case class RelationalMemoryState(state: Map[Key, Mem[Context, Addr, Size, Val]]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: RelationalMemoryState => MapEquals(this.state, other.state)
        case _ => false


  override type State = RelationalMemoryState

  override def getState: State = RelationalMemoryState(memories)

  override def setState(st: State): Unit = memories = st.state

  override def join: Join[State] = (s1: State,s2:State) => Profiler.addTime("RelationalMemoryState.combine") { Join(s1.state,s2.state).map(RelationalMemoryState(_)) }
  override def widen: Widen[State] = (s1: State,s2:State) => Profiler.addTime("RelationalMemoryState.combine") { Widen(s1.state,s2.state).map(RelationalMemoryState(_)) }

case class Mem[Ctx, Addr, Size, Val](store: SortedMap[PhysicalAddress[Ctx], MemoryRegion[Addr, Val]],
                                     numPages: Size,
                                     pageLimit: Size):
  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    store.values.flatMap(_.addressIterator(valueIterator)).iterator ++
      valueIterator(numPages) ++
      valueIterator(pageLimit)

  override def equals(obj: Any): Boolean =
    obj match
      case other: Mem[Ctx, Addr, Size, Val] @unchecked => MapEquals(this.store, other.store) && this.numPages.equals(other.numPages) && this.pageLimit == other.pageLimit
      case _ => false

case class MemoryRegion[Addr, Val](startAddr: Addr, value: Val, byteSize: Topped[Int], byteOrder: Topped[ByteOrder]):
  def endAddr(using addrIntOps: IntegerOps[Int,Addr], joinAddr: Join[Addr]): Addr =
    byteSize match
      case Topped.Actual(bs) => addrIntOps.add(startAddr, addrIntOps.integerLit(bs))
      case Topped.Top => addrIntOps.add(startAddr, joinAddr(addrIntOps.integerLit(0), addrIntOps.integerLit(Int.MaxValue)).get)

  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    valueIterator(startAddr) ++ valueIterator(value)

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

  override def toString: String =
    this match
      case bytes: StoredBytes[Val] => s"${bytes.value}"
      case bytes: ReadBytes[Val] => s"${bytes.value}"

given CombineMem[Context: Finite, Addr, Size, Val, W <: Widening](using combineAddr: Combine[Addr,W], combineSize: Combine[Size,W], combineVal: Combine[Val,W]): Combine[Mem[Context, Addr, Size, Val], W] with
  override def apply(v1: Mem[Context, Addr, Size, Val], v2: Mem[Context, Addr, Size, Val]): MaybeChanged[Mem[Context, Addr, Size, Val]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
      for {
        store <- CombineFiniteKeySortedMap[PhysicalAddress[Context], MemoryRegion[Addr, Val], W](v1.store, v2.store)
        numPages <- combineSize(v1.numPages, v2.numPages)
        pageLimit <- combineSize(v1.pageLimit, v2.pageLimit)
      } yield (Mem(store, numPages, pageLimit))

given CombineRegion[Addr, Val, W <: Widening](using combineAddr: Combine[Addr, W], combineVal: Combine[Val,W]): Combine[MemoryRegion[Addr, Val], W] with
  override def apply(v1: MemoryRegion[Addr, Val], v2: MemoryRegion[Addr, Val]): MaybeChanged[MemoryRegion[Addr, Val]] =
    if (v1 eq v2)
      Unchanged(v1)
    else
      for {
        startAddr <- combineAddr(v1.startAddr, v2.startAddr)
        value <- combineVal(v1.value, v2.value)
        byteSize <- Join(v1.byteSize, v2.byteSize)
        byteOrder <- Join(v1.byteOrder, v2.byteOrder)
      } yield (MemoryRegion(startAddr, value, byteSize, byteOrder))

given CombineBytes[Addr, Val, W <: Widening](using combineVal: Combine[Val, W]): Combine[Bytes[Val], W] with
  override def apply(v1: Bytes[Val], v2: Bytes[Val]): MaybeChanged[Bytes[Val]] =
    (v1, v2) match
      case (_,_) if(v1 eq v2) => Unchanged(v1)
      case (ReadBytes(val1, byteOrder1), ReadBytes(val2, byteOrder2)) =>
        for {
          value <- combineListOfValues(val1, val2)
          byteOrder <- Join(byteOrder1, byteOrder2)
        } yield (ReadBytes(value, byteOrder))
      case _ => throw IllegalArgumentException(s"Can only join ReadBytes, but got ($v1, $v2)")

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
