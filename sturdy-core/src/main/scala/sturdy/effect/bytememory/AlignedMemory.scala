package sturdy.effect.bytememory

import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import Bytes.*
import sturdy.data
import sturdy.util.Profiler

import java.nio.ByteOrder
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

trait AlignedMemoryAddress[Context, Addr, Size]:
  def ifAddrLeSize[A: Join](addr: Addr, size: Size)(f: => A): JOptionA[A]
  def matchingRegions[Val](addr: Addr, mems: Mem[Context, Addr, Size, Val]): IterableOnce[(MemoryRegion[Addr, Val], AlignedRead)]

trait AlignedMemorySize[Size]:
  def ifSizeLeLimit[A: Join](size: Size, limit: Size)(ifTrue: => A)(ifFalse: => A): A

final class AlignedMemory
  [
    Key: Finite,
    Context: Ordering: Finite,
    Addr: Join: Widen,
    Val: Join: Widen,
    Size: Join: Widen
  ]
  (val
   defaultBytes: Bytes[Addr,Val],
   memLocAllocator: Allocator[IterableOnce[Context],(Key,Addr,Bytes[Addr,Val])]
  )
  (using
   memoryAddr: AlignedMemoryAddress[Context, Addr, Size],
   memorySize: AlignedMemorySize[Size],
   sizeIntOps: IntegerOps[Int, Size]
  )
  extends Memory[Key, Addr, Bytes[Addr,Val], Size, WithJoin]:

  type MemoryCtx = Context
  private val pageSize: Size = sizeIntOps.integerLit(ConcreteMemory.pageSize)
  private val maxPages: Size = sizeIntOps.integerLit(ConcreteMemory.maxPageNum)

  var memories: Map[Key, Mem[Context, Addr, Size, Val]] = Map()

  override def read(key: Key, readAddr: Addr, length: Int): JOptionA[Bytes[Addr,Val]] =
    val mem = memories(key)
    val regions = memoryAddr.matchingRegions(readAddr, mem).iterator

    val result =
      if(! regions.hasNext)
        defaultBytes
      else
        regions
          .map{
            case (MemoryRegion(storedAddr, StoredBytes(value, storedBytes, storedByteOrder)), alignedRead) =>
              ReadBytes[Addr,Val](
                value = value,
                storedBytes = storedBytes,
                storedByteOrder = storedByteOrder,
                readAligned = alignedRead.toToppedBoolean,
                readBytes = Topped.Actual(length)
              )
          }
          .reduce(Join(_,_).get)

    memoryAddr.ifAddrLeSize(readAddr, sizeIntOps.sub(sizeIntOps.mul(mem.numPages, pageSize), sizeIntOps.integerLit(length))) {
      result
    }

  override def write(key: Key, addr: Addr, bytes: Bytes[Addr,Val]): JOptionA[Unit] =
    bytes match
      case bs: StoredBytes[Addr,Val] =>
        val Mem(store, numPages, pageLimit) = memories(key)
        val ctxs = memLocAllocator(key,addr,bytes)
        val newRegion = MemoryRegion(addr, bs)
        val storedSize = bs.storedBytes match
          case Topped.Actual(len) => sizeIntOps.integerLit(len)
          case Topped.Top => Join(sizeIntOps.integerLit(1), sizeIntOps.integerLit(Int.MaxValue)).get

        memoryAddr.ifAddrLeSize(newRegion.startAddr, sizeIntOps.sub(sizeIntOps.mul(numPages, pageSize), storedSize)) {
          var newStore = store
          for(ctx <- ctxs) {
            newStore += PhysicalAddress(ctx, Recency.Recent) -> newRegion
            for (oldRegion <- Join(store.get(PhysicalAddress(ctx, Recency.Recent)), store.get(PhysicalAddress(ctx, Recency.Old))).get) {
              newStore += PhysicalAddress(ctx, Recency.Old) -> oldRegion
            }
          }
          memories += key -> Mem(newStore, numPages, pageLimit)

          ()
        }
      case bs: ReadBytes[Addr,Val] =>
        throw IllegalArgumentException(s"Can only store StoredBytes, but got $bytes")

  override def copy(key: Key, srcAddr: Addr, dstAddr: Addr, byteAmount: Size): JOptionA[Unit] = ???
  override def fill(key: Key, addr: Addr, byteAmount: Size, value: Bytes[Addr, Val]): JOptionA[Unit] = ???
  override def init(key: Key, tableAddr: Addr, dataAddr: Addr, byteAmount: Size, dataBytes: Bytes[Addr, Val]): JOptionA[Unit] = ???

  override def size(key: Key): Size =
    memories(key).numPages

  override def grow(key: Key, deltaPages: Size): JOptionA[Size] =
    val Mem(addressRanges, numPages, pageLimit) = memories(key)
    val newNumPages = sizeIntOps.add(numPages, deltaPages)

    val (resultPages, returnValue) = memorySize.ifSizeLeLimit(numPages, sizeIntOps.min(maxPages, pageLimit)) {
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

case class MemoryRegion[Addr, Val](startAddr: Addr, bytes: StoredBytes[Addr,Val]):
  def endAddr(using addrIntOps: IntegerOps[Int,Addr], joinAddr: Join[Addr]): Addr =
    bytes.storedBytes match
      case Topped.Actual(bs) => addrIntOps.add(startAddr, addrIntOps.integerLit(bs))
      case Topped.Top => addrIntOps.add(startAddr, joinAddr(addrIntOps.integerLit(0), addrIntOps.integerLit(Int.MaxValue)).get)

  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    valueIterator(startAddr) //++ valueIterator(bytes._value)

enum Bytes[Addr, Val]:
  case StoredBytes(value: Val, storedBytes: Topped[Int], storedByteOrder: Topped[ByteOrder])
  case ReadBytes(value: Val, storedBytes: Topped[Int], storedByteOrder: Topped[ByteOrder], readAligned: Topped[Boolean], readBytes: Topped[Int])

  def _value: Val =
    this match
      case bytes: StoredBytes[Addr, Val] => bytes.value
      case bytes: ReadBytes[Addr, Val] => bytes.value

  override def toString: String =
    this match
      case bytes: StoredBytes[Addr, Val] => s"${bytes.value}[${bytes.storedBytes}]"
      case bytes: ReadBytes[Addr, Val] =>
        if (bytes.storedBytes == bytes.readBytes)
          s"${bytes.value}"
        else
          s"ReadBytes(${bytes.value}, ${bytes.storedBytes}, ${bytes.storedByteOrder}, ${bytes.readAligned}, ${bytes.readBytes})"

given CombineMem[Context: Finite, Addr, Size, Val, W <: Widening](using combineAddr: Combine[Addr,W], combineSize: Combine[Size,W], combineVal: Combine[Val,W]): Combine[Mem[Context, Addr, Size, Val], W] with
  override def apply(v1: Mem[Context, Addr, Size, Val], v2: Mem[Context, Addr, Size, Val]): MaybeChanged[Mem[Context, Addr, Size, Val]] =
    for {
      store <- CombineFiniteKeySortedMap[PhysicalAddress[Context], MemoryRegion[Addr, Val], W](v1.store, v2.store)
      numPages <- combineSize(v1.numPages, v2.numPages)
      pageLimit <- combineSize(v1.pageLimit, v2.pageLimit)
    } yield (Mem(store, numPages, pageLimit))

given CombineRegion[Addr, Val, W <: Widening](using combineAddr: Combine[Addr, W], combineBytes: Combine[Bytes[Addr, Val], W]): Combine[MemoryRegion[Addr, Val], W] with
  override def apply(v1: MemoryRegion[Addr, Val], v2: MemoryRegion[Addr, Val]): MaybeChanged[MemoryRegion[Addr, Val]] =
    if (v1 eq v2)
      Unchanged(v1)
    else
      for {
        startAddr <- combineAddr(v1.startAddr, v2.startAddr)
        bytes <- combineBytes(v1.bytes, v2.bytes)
      } yield (MemoryRegion(startAddr, bytes.asInstanceOf[StoredBytes[Addr, Val]]))

given CombineBytes[Addr, Val, W <: Widening](using combineVal: Combine[Val, W]): Combine[Bytes[Addr, Val], W] with
  override def apply(v1: Bytes[Addr, Val], v2: Bytes[Addr, Val]): MaybeChanged[Bytes[Addr, Val]] =
    (v1, v2) match
      case (_,_) if(v1 eq v2) => Unchanged(v1)
      case (StoredBytes(val1, storedBytes1, byteOrder1), StoredBytes(val2, storedBytes2, byteOrder2)) =>
        for {
          value <- Combine(val1, val2)
          storedBytes <- Join(storedBytes1, storedBytes2)
          byteOrder <- Join(byteOrder1, byteOrder2)
        } yield (StoredBytes(value, storedBytes, byteOrder))
      case (ReadBytes(val1, storedBytes1, byteOrder1, readOffset1, readBytes1), ReadBytes(val2, storedBytes2, byteOrder2, readOffset2, readBytes2)) =>
        for {
          value <- Combine(val1, val2)
          storedBytes <- Join(storedBytes1, storedBytes2)
          byteOrder <- Join(byteOrder1, byteOrder2)
          readOffset <- Join(readOffset1, readOffset2)
          readBytes <- Join(readBytes1, readBytes2)
        } yield (ReadBytes(value, storedBytes, byteOrder, readOffset, readBytes))
      case (_,_) => throw new IllegalArgumentException(s"Cannot join read bytes and stored bytes: $v1 ⊔ $v2")
