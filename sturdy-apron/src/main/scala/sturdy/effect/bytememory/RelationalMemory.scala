
package sturdy.effect.bytememory

import apron.{Abstract1, Interval, Manager}
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{RecencyStore, RelationalStore}
import sturdy.values.integer.IntegerOps
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import ApronBool.*
import ApronCons.*

trait HasSize[Context,Type]:
  def size: ApronExpr[VirtualAddress[Context], Type]

class RelationalMemory
  [
    Key: Finite,
    Context: Ordering: Finite,
    Type : ApronType : Join: Widen,
    Bytes <: HasSize[Context,Type] : Join : Widen
  ]
  (val
     apronState: ApronState[VirtualAddress[Context], Type],
     byteStore: RecencyStore[Context, VirtualAddress[Context], Bytes],
     defaultBytes: Bytes,
     memLocAllocator: Allocator[Context,(Key,ApronExpr[VirtualAddress[Context], Type],Bytes)]
  )
  (using
     typeIntOps: IntegerOps[Int, Type],
     joinApronExpr: Join[ApronExpr[VirtualAddress[Context], Type]]
  )
  extends Memory[Key, ApronExpr[VirtualAddress[Context], Type], Bytes, ApronExpr[VirtualAddress[Context], Type], WithJoin]:

  type MemoryLoc = Context
  type Addr = ApronExpr[VirtualAddress[Context], Type]
  private val intType: Type = typeIntOps.integerLit(0)
  private val pageSize: ApronExpr[VirtualAddress[Context], Type] = ApronExpr.intLit(ConcreteMemory.pageSize, intType)
  private val maxPages: ApronExpr[VirtualAddress[Context], Type] = ApronExpr.intLit(ConcreteMemory.maxPageNum, intType)

  case class Mem(addressRanges: Map[MemoryLoc, Range], numPages: ApronExpr[VirtualAddress[Context], Type], pageLimit: ApronExpr[VirtualAddress[Context], Type])
  var memories: Map[Key, Mem] = Map()

  case class Range(startAddr: ApronExpr[VirtualAddress[Context], Type], numBytes: ApronExpr[VirtualAddress[Context], Type]):
    def contains(addr: Addr): Topped[Boolean] =
      apronState.ifThenElse[Topped[Boolean]](ApronBool.And(ApronBool.Constraint(ApronCons.le(startAddr, addr)), ApronBool.Constraint(ApronCons.le(addr, numBytes)))) {
        Topped.Actual(true)
      } {
        Topped.Actual(false)
      }
  
  override def read(key: Key, addr: Addr, length: Int): JOptionA[Bytes] =
    val Mem(addressRanges, numPages, _) = memories(key)
    val result = addressRanges
      .filter((_,range) =>
        range.contains(addr) match
          case Topped.Actual(true) | Topped.Top => true
          case Topped.Actual(false) => false
      )
      .keys
      .map(ctx => byteStore.store.read(PowersetAddr(PhysicalAddress(ctx, Recency.Recent))).asInstanceOf[JOptionA[Bytes]])
      .reduce(Join(_,_).get)
      .toOption
      .getOrElse(defaultBytes)

    apronState.ifThenElse(ApronCons.le(addr, ApronExpr.intMul(numPages, pageSize))) {
      JOptionA.Some(result)
    } {
      JOptionA.None()
    }

  override def write(key: Key, addr: Addr, bytes: Bytes): JOptionA[Unit] =
    val Mem(addressRanges, numPages, pageLimit) = memories(key)
    val ctx = memLocAllocator(key,addr,bytes)
    val virt = byteStore.alloc(ctx)
    byteStore.write(virt, bytes)
    val newRange = Range(startAddr = addr, numBytes = bytes.size)
    addressRanges.get(ctx) match
      case Some(range) =>
        memories += (key -> Mem(addressRanges + (ctx -> Join(range, newRange).get), numPages, pageLimit))
      case None =>
        memories += (key -> Mem(addressRanges + (ctx -> newRange), numPages, pageLimit))

    apronState.ifThenElse(ApronCons.le(addr, ApronExpr.intMul(numPages, pageSize))) {
      JOptionA.Some(())
    } {
      JOptionA.None()
    }

  override def size(key: Key): ApronExpr[VirtualAddress[Context], Type] =
    memories(key).numPages

  override def grow(key: Key, deltaPages: ApronExpr[VirtualAddress[Context], Type]): JOptionA[ApronExpr[VirtualAddress[Context], Type]] =
    val Mem(addressRanges, numPages, pageLimit) = memories(key)
    val newNumPages = ApronExpr.intAdd(numPages, deltaPages)
    memories += key -> Mem(addressRanges, newNumPages, pageLimit)

    apronState.ifThenElse(And(Constraint(le(newNumPages, maxPages)), Constraint(le(newNumPages, pageLimit)))) {
      JOptionA.Some(newNumPages)
    } {
      JOptionA.None[ApronExpr[VirtualAddress[Context], Type]]()
    }

  override def putNew(key: Key, initSize: ApronExpr[VirtualAddress[Context], Type], sizeLimit: Option[ApronExpr[VirtualAddress[Context], Type]]): Unit =
    memories += key -> Mem(Map.empty, initSize, sizeLimit.getOrElse(ApronExpr.top(intType)))

  override type State = Map[Key, Mem]

  override def getState: State = memories

  override def setState(st: State): Unit = memories = st

  override def join: Join[State] = Join(_,_)

  override def widen: Widen[State] = Widen(_,_)

  given JoinRange: Join[Range] with
    override def apply(v1: Range, v2: Range): MaybeChanged[Range] =
      for {
        startAddr <- apronState.join(v1.startAddr, v2.startAddr)
        numBytes <- apronState.join(v1.numBytes, v2.numBytes)
      } yield (Range(startAddr, numBytes))

  given WidenRange: Widen[Range] with
    override def apply(v1: Range, v2: Range): MaybeChanged[Range] =
      for {
        startAddr <- apronState.widen(v1.startAddr, v2.startAddr)
        numBytes <- apronState.widen(v1.numBytes, v2.numBytes)
      } yield (Range(startAddr, numBytes))

  given JoinMem: Join[Mem] with
    override def apply(v1: Mem, v2: Mem): MaybeChanged[Mem] =
      for {
        addrRanges <- Join(v1.addressRanges, v2.addressRanges)
        numPages <- apronState.join(v1.numPages, v2.numPages)
        pageLimit <- apronState.join(v1.pageLimit, v2.pageLimit)
      } yield (Mem(addrRanges, numPages, pageLimit))

  given WidenMem: Widen[Mem] with
    override def apply(v1: Mem, v2: Mem): MaybeChanged[Mem] =
      for {
        addrRanges <- Widen(v1.addressRanges, v2.addressRanges)
        numPages <- apronState.widen(v1.numPages, v2.numPages)
        pageLimit <- apronState.widen(v1.pageLimit, v2.pageLimit)
      } yield (Mem(addrRanges, numPages, pageLimit))