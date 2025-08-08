
package sturdy.effect.bytememory

import apron.Interval
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import ApronBool.*
import ApronCons.*
import ApronExpr.*
import Bytes.*
import sturdy.util.Profiler

import java.nio.ByteOrder
import scala.collection.immutable.SortedMap
import scala.reflect.ClassTag

class RelationalMemory
  [
    Key: Finite,
    Context: Ordering: Finite,
    Type : ApronType : Join: Widen,
    Val: Join: Widen
  ]
  (val
     defaultBytes: Bytes[Val],
     memLocAllocator: Allocator[Context,(Key,ApronExpr[VirtualAddress[Context], Type],Bytes[Val])]
  )
  (using
     apronState: ApronState[VirtualAddress[Context], Type],
     typeIntOps: IntegerOps[Int, Type],
     joinApronExpr: Join[ApronExpr[VirtualAddress[Context], Type]]
  )
  extends Memory[Key, ApronExpr[VirtualAddress[Context], Type], Bytes[Val], ApronExpr[VirtualAddress[Context], Type], WithJoin]:

  type MemoryCtx = Context
  type Addr = ApronExpr[VirtualAddress[Context], Type]
  private val intType: Type = typeIntOps.integerLit(0)
  private val pageSize: ApronExpr[VirtualAddress[Context], Type] = ApronExpr.lit(ConcreteMemory.pageSize, intType)
  private val maxPages: ApronExpr[VirtualAddress[Context], Type] = ApronExpr.lit(ConcreteMemory.maxPageNum, intType)

  case class Mem(store: SortedMap[PhysicalAddress[MemoryCtx], MemoryRegion[VirtualAddress[Context], Type, Val]],
                 numPages: ApronExpr[VirtualAddress[Context], Type],
                 pageLimit: ApronExpr[VirtualAddress[Context], Type]):
    def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
      store.values.flatMap(_.addressIterator(valueIterator)).iterator ++
        valueIterator(numPages) ++
        valueIterator(pageLimit)

    override def equals(obj: Any): Boolean =
      obj match
        case other: Mem => MapEquals(this.store, other.store) && this.numPages.equals(other.numPages) && this.pageLimit == other.pageLimit
        case _ => false

  var memories: Map[Key, Mem] = Map()
  
  override def read(key: Key, addr: Addr, length: Int): JOptionA[Bytes[Val]] =
    val Mem(store, numPages, _) = memories(key)
    val matchingRegions = store
      .filter((_,region) =>
        // TODO: Address overlaps with range (use length)
        region.contains(addr) match
          case Topped.Actual(true) | Topped.Top => true
          case Topped.Actual(false) => false
      )

    val result =
      if(matchingRegions.isEmpty)
        defaultBytes
      else
        matchingRegions
          .map{
            case (ctx,MemoryRegion(startAddr, StoredBytes(value, storedBytes, storedByteOrder))) =>
                ReadBytes(
                  value = value,
                  storedBytes = storedBytes,
                  storedByteOrder = storedByteOrder,
                  readOffset = apronState.getInterval(ApronExpr.intSub(startAddr, addr)),
                  readBytes = Topped.Actual(length)
                )
          }
          .reduce(Join(_,_).get)

    val endAddr = intAdd(addr,interval(length, length, addr._type))
    apronState.ifThenElse(ApronCons.le(endAddr, intMul(numPages, pageSize))) {
      JOptionA.Some(result)
    } {
      JOptionA.None()
    }

  override def write(key: Key, addr: Addr, bytes: Bytes[Val]): JOptionA[Unit] =
    bytes match
      case bs: StoredBytes[Val] =>
        val Mem(store, numPages, pageLimit) = memories(key)
        val ctx = memLocAllocator(key,addr,bytes)
        val newRegion = MemoryRegion(addr, bs)

        apronState.ifThenElse(ApronCons.le(newRegion.endAddr, ApronExpr.intMul(numPages, pageSize))) {
          var newStore = store
          newStore += PhysicalAddress(ctx, Recency.Recent) -> newRegion
          for (oldRegion <- Join(store.get(PhysicalAddress(ctx, Recency.Recent)),
            store.get(PhysicalAddress(ctx, Recency.Old))).get) {
            newStore += PhysicalAddress(ctx, Recency.Old) -> oldRegion
          }
          memories += key -> Mem(newStore, numPages, pageLimit)

          JOptionA.Some(())
        } {
          JOptionA.None()
        }
      case bs: ReadBytes[Val] =>
        throw IllegalArgumentException(s"Can only store StoredBytes, but got $bytes")

  override def size(key: Key): ApronExpr[VirtualAddress[Context], Type] =
    memories(key).numPages

  override def grow(key: Key, deltaPages: ApronExpr[VirtualAddress[Context], Type]): JOptionA[ApronExpr[VirtualAddress[Context], Type]] =
    val Mem(addressRanges, numPages, pageLimit) = memories(key)
    val newNumPages = ApronExpr.intAdd(numPages, deltaPages)

    val (resultPages, returnValue) = apronState.ifThenElse(And(Constraint(le(newNumPages, maxPages)), Constraint(le(newNumPages, pageLimit)))) {
      (newNumPages, JOptionA.Some(numPages))
    } {
      (numPages, JOptionA.None[ApronExpr[VirtualAddress[Context], Type]]())
    }
    memories += key -> Mem(addressRanges, resultPages, pageLimit)

    returnValue

  override def putNew(key: Key, initSize: ApronExpr[VirtualAddress[Context], Type], sizeLimit: Option[ApronExpr[VirtualAddress[Context], Type]]): Unit =
    memories += key -> Mem(SortedMap.empty, initSize, sizeLimit.getOrElse(ApronExpr.interval(0d, Double.PositiveInfinity, intType)))

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    memories.values.flatMap(_.addressIterator(valueIterator)).iterator

  case class RelationalMemoryState(state: Map[Key, Mem]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: RelationalMemoryState => MapEquals(this.state, other.state)
        case _ => false


  override type State = RelationalMemoryState

  override def getState: State = RelationalMemoryState(memories)

  override def setState(st: State): Unit = memories = st.state

  override def join: Join[State] = (s1: State,s2:State) => Profiler.addTime("RelationalMemoryState.combine") { Join(s1.state,s2.state).map(RelationalMemoryState(_)) }
  override def widen: Widen[State] = (s1: State,s2:State) => Profiler.addTime("RelationalMemoryState.combine") { Widen(s1.state,s2.state).map(RelationalMemoryState(_)) }

  given JoinMem: Join[Mem] with
    override def apply(v1: Mem, v2: Mem): MaybeChanged[Mem] =
      for {
        store <- Join(v1.store, v2.store)
        numPages <- apronState.join(v1.numPages, v2.numPages)
        pageLimit <- apronState.join(v1.pageLimit, v2.pageLimit)
      } yield (Mem(store, numPages, pageLimit))

  given WidenMem: Widen[Mem] with
    override def apply(v1: Mem, v2: Mem): MaybeChanged[Mem] =
      for {
        store <- Widen(v1.store, v2.store)
        numPages <- apronState.widen(v1.numPages, v2.numPages)
        pageLimit <- apronState.widen(v1.pageLimit, v2.pageLimit)
      } yield (Mem(store, numPages, pageLimit))


enum Bytes[Val]:
  case StoredBytes(value: Val, storedBytes: Topped[Int], storedByteOrder: Topped[ByteOrder])
  case ReadBytes(value: Val, storedBytes: Topped[Int], storedByteOrder: Topped[ByteOrder], readOffset: Interval, readBytes: Topped[Int])

  def _value: Val =
    this match
      case bytes: StoredBytes[Val] => bytes.value
      case bytes: ReadBytes[Val] => bytes.value

  override def toString: String =
    this match
      case bytes: StoredBytes[Val] => s"${bytes.value}[${bytes.storedBytes}]"
      case bytes: ReadBytes[Val] =>
        if(bytes.storedBytes == bytes.readBytes)
          s"${bytes.value}[${bytes.readOffset}]"
        else
          s"ReadBytes(${bytes.value}, ${bytes.storedBytes}, ${bytes.storedByteOrder}, ${bytes.readOffset}, ${bytes.readBytes})"

case class MemoryRegion[Addr, Type: ApronType, Val](startAddr: ApronExpr[Addr, Type], bytes: StoredBytes[Val]):
  def endAddr: ApronExpr[Addr, Type] =
    val addrType = startAddr._type
    bytes.storedBytes match
      case Topped.Actual(bs) => intAdd(startAddr, lit(bs, addrType), addrType)
      case Topped.Top => intAdd(startAddr, interval(0d, Double.PositiveInfinity, addrType), addrType)

  def contains(addr: ApronExpr[Addr, Type])(using apronState: ApronState[Addr,Type]): Topped[Boolean] =
    apronState.ifThenElse[Topped[Boolean]](
      And(Constraint(le(startAddr, addr)),
        Constraint(le(addr, endAddr)))
    ) {
      Topped.Actual(true)
    } {
      Topped.Actual(false)
    }

  def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    valueIterator(startAddr) //++ valueIterator(bytes._value)

given CombineBytes[Val, W <: Widening](using combineVal: Combine[Val, W]): Combine[Bytes[Val], W] with
  override def apply(v1: Bytes[Val], v2: Bytes[Val]): MaybeChanged[Bytes[Val]] =
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


given JoinRegion[Addr,Type: ApronType,Val](using joinVal: Join[Val], apronState: ApronState[Addr,Type]): Join[MemoryRegion[Addr,Type,Val]] with
  override def apply(v1: MemoryRegion[Addr,Type,Val], v2: MemoryRegion[Addr,Type,Val]): MaybeChanged[MemoryRegion[Addr,Type,Val]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
      for {
        startAddr <- apronState.join(v1.startAddr, v2.startAddr)
        bytes <- CombineBytes(v1.bytes, v2.bytes)
      } yield (MemoryRegion(startAddr, bytes.asInstanceOf[StoredBytes[Val]]))

given WidenRegion[Addr,Type: ApronType, Val](using widenVal: Widen[Val], apronState: ApronState[Addr,Type]): Widen[MemoryRegion[Addr,Type,Val]] with
  override def apply(v1: MemoryRegion[Addr,Type,Val], v2: MemoryRegion[Addr,Type,Val]): MaybeChanged[MemoryRegion[Addr,Type,Val]] =
    if(v1 eq v2)
      Unchanged(v1)
    else
      for {
        startAddr <- apronState.widen(v1.startAddr, v2.startAddr)
        bytes <- CombineBytes(v1.bytes, v2.bytes)
      } yield (MemoryRegion(startAddr, bytes.asInstanceOf[StoredBytes[Val]]))
