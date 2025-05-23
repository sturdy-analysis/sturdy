
package sturdy.effect.bytememory

import apron.{Abstract1, Interval, Manager}
import sturdy.apron.{*, given}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.RelationalStore
import sturdy.values.integer.IntegerOps
import sturdy.values.references.{*, given}
import sturdy.values.{Finite, Join, Topped, Widen}

class RelationalMemory
  [
    Key,
    Context: Ordering: Finite,
    Type : ApronType : Join: Widen,
    PowMemoryLoc <: AbstractAddr[PhysicalAddress[Context]],
    Addr,
    Bytes : Join: Widen,
    Size
  ]
  (val
     apronState: ApronRecencyState[Context, Type, Bytes],
     memLocAllocator: Allocator[Context,(Key,Addr,Bytes)],
     sizeType: Type
  )
  (using
     relationalAddress: RelationalExpr[Addr, VirtualAddress[Context], Type],
     relationalBytes: RelationalExpr[Bytes, VirtualAddress[Context], Type],
     relationalSize: RelationalExpr[Size, VirtualAddress[Context], Type]
  )
  extends Memory[Key, Addr, Bytes, Size, WithJoin]:

  type MemoryLoc = Context

  case class Mem(addressRanges: Map[MemoryLoc, ApronExpr[VirtualAddress[Context],Type]], memorySize: ApronExpr[VirtualAddress[Context], Type])
  val emptyMemory: Mem = Mem(Map(), ApronExpr.intLit(0, sizeType))
  var memories: Map[Key, Mem] = Map()

  override def read(key: Key, addr: Addr, length: Int): JOptionA[Bytes] =
    memories.get(key) match
      case Some(Mem(addressRanges, memorySize)) =>
        val inRangeOfAddress = addressRanges.filter((_,range) =>
          ???
        )
        inRangeOfAddress.size match
          case 0 => JOptionA.None()
          case 1 =>
            val virt = apronState.recencyStore.addressTranslation.allocNoRetire(inRangeOfAddress.head._1)
            val tpe = apronState.relationalStore.getType(PowersetAddr(PhysicalAddress(virt.ctx, Recency.Recent))).toOption.get
            JOptionA.Some(relationalBytes.makeRelationalExpr(ApronExpr.addr(virt, tpe)))
          case _ /* address in range of multiple memory objects */ =>
            // Return non-relational join of all values in address range.
            val (iv,tpe) = inRangeOfAddress.keys.map((ctx:Context) =>
                val phys = PhysicalAddress(ctx, Recency.Recent)
                val tpe = apronState.relationalStore.getType(PowersetAddr(phys)).toOption.get
                val iv = apronState.relationalStore.getBound(ApronExpr.addr(phys, tpe))
                (iv,tpe)
              ).reduce(Join(_,_).get)
            JOptionA.NoneSome(relationalBytes.makeRelationalExpr(ApronExpr.constant(iv, tpe)))

      case None => JOptionA.None()


  override def write(key: Key, addr: Addr, bytes: Bytes): JOptionA[Unit] =
    memories.get(key) match
      case Some(Mem(addressRanges, memorySize)) =>
        (relationalAddress.getRelationalExpr(addr), relationalBytes.getRelationalExpr(bytes)) match
          case (Some(addrExpr), Some(bytesExpr)) =>
            val ctx = memLocAllocator(key,addr,bytes)
            val virt = apronState.recencyStore.alloc(ctx)
            apronState.assign(virt, bytesExpr)
            addressRanges.get(ctx) match
              case Some(range) =>
                memories += (key -> Mem(addressRanges + (ctx -> apronState.join(range, addrExpr).get), memorySize))
              case None =>
                memories += (key -> Mem(addressRanges + (ctx -> addrExpr), memorySize))
            apronState.getBoolean(ApronCons.le(addrExpr, memorySize)) match
              case Topped.Actual(true) => JOptionA.Some(())
              case Topped.Actual(false) => JOptionA.None()
              case Topped.Top => JOptionA.NoneSome(())
          case (None, _) =>
            throw IllegalArgumentException(s"Memory address $addr does not contain an apron expression")
          case (_, None) =>
            throw IllegalArgumentException(s"Bytes $bytes do not contain an apron expression")
      case None => JOptionA.None()

  override def size(key: Key): Size =
    memories.get(key).map(mem =>
      relationalSize.makeRelationalExpr(mem.memorySize)
    ).getOrElse(relationalSize.makeRelationalExpr(ApronExpr.constant(ApronExpr.topInterval, sizeType)))

  override def grow(key: Key, delta: Size): JOptionA[Size] = ???

  override def putNew(key: Key, initSize: Size, sizeLimit: Option[Size]): Unit = ???

  override type State = this.type

  override def getState: State = ???

  override def setState(st: State): Unit = ???

  override def join: Join[State] = ???

  override def widen: Widen[State] = ???