package sturdy.language.wasm.abstractions

import sturdy.apron.{ApronBool, ApronCons, ApronExpr, ApronState, ApronVar}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.bytememory.{*, given}
import sturdy.effect.bytememory.Bytes.*
import sturdy.effect.failure.Failure
import sturdy.fix.DomLogger
import sturdy.language.wasm.analyses.RelationalAnalysis.{Bool, I32, VirtAddr}
import sturdy.language.wasm.generic.WasmFailure.MemoryAccessOutOfBounds
import sturdy.language.wasm.generic.{FixIn, FrameData, MemoryAddr}
import sturdy.util.Lazy
import sturdy.values.addresses.{AddressLimits, AddressOffset}
import sturdy.values.ordering.UnsignedOrderingOps
import sturdy.values.{*, given}
import sturdy.values.references.{PhysicalAddress, PowVirtualAddress, Recency, VirtualAddress}

trait RelationalMemory extends RelationalValues:
  import RelI32.*
  import ApronCons.*
  import ApronExpr.*
  import ApronBool.*

  final type Addr = NumExpr | AllocationSites
  final type Size = ApronExpr[VirtAddr, Type]
  final type Bytes = sturdy.effect.bytememory.Bytes[Value]

  given RelationalAddressOffset(using f: Failure, lazyEffectStack: Lazy[EffectStack], apronState: ApronState[VirtAddr, Type], unsignedOrderingOps: UnsignedOrderingOps[I32, Bool]): AddressOffset[Addr] with
    override def addOffsetToAddr(newOffset: Int, addr: Addr): Addr = {
      given effectStack: EffectStack = lazyEffectStack.value
      addr match
        case _ if newOffset == 0 =>
          addr
        case AllocationSites(sites, size) =>
          AllocationSites(PowVirtualAddress(sites.iterator.map {
            case VirtualAddress(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset)), n, addrTrans) =>
              apronState.alloc(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset + newOffset)))
            case v@VirtualAddress(AddrCtx.Heap(HeapCtx.Static(0)), n, addrTrans) =>
              v
            case virt => throw new IllegalArgumentException(s"Expected HeapCtx.Alloc, but got ${virt.ctx}")
          }), size)
        case _ =>
          val expr = addr.asNumExpr
          val resAddr = ApronExpr.intAdd[VirtAddr, Type](expr, ApronExpr.lit[VirtAddr, Type](newOffset, expr._type), expr._type)
          given Join[ApronExpr[VirtAddr, Type]] = apronState.join
          NumExpr(apronState.ifThenElse(effectStack)(unsignedOrderingOps.ltUnsigned(NumExpr(resAddr), NumExpr(ApronExpr.lit[VirtAddr, Type](newOffset, expr._type)))) {
            f.fail(MemoryAccessOutOfBounds, s"$addr + $newOffset")
          } {
            addr match
              case NumExpr(ApronExpr.Constant(_, floatSpecials, tpe)) => ApronExpr.Constant(apronState.getInterval(resAddr), floatSpecials, tpe)
              case _ => resAddr
          })
    }

  given RelationalAddressLimits(using apronState: ApronState[VirtAddr, Type]): AddressLimits[Addr, Size, WithJoin] with
    override def ifAddrLeSize[A: WithJoin](addr: Addr, size: Size)(f: => A): JOptionA[A] =
      given Join[A] = implicitly[WithJoin[A]].j
      addr match
        case NumExpr(addrExpr) =>
          apronState.ifThenElse(And(Constraint(le(ApronExpr.lit(0, Type.I32Type), addrExpr)), Constraint(le(addrExpr, size)))) {
            JOptionA.Some(f)
          } {
            JOptionA.None[A]()
          }
        case _: AllocationSites =>
          JOptionA.Some(f)

    override def ifSizeLeLimit[A: WithJoin](size: ApronExpr[VirtAddr, Type], limit: ApronExpr[VirtAddr, Type])(ifTrue: => A)(ifFalse: => A): A =
      given Join[A] = implicitly[WithJoin[A]].j
      apronState.ifThenElse(And(Constraint(le(lit(0, Type.I32Type), size)), Constraint(le(size, limit))))(ifTrue)(ifFalse)

  given RelationalMatchRegions(using apronState: ApronState[VirtAddr, Type]): MatchRegions[HeapCtx, Addr, Size] with
    override def apply[Val, Timestamp: PartialOrder](addr: Addr, mem: Mem[HeapCtx, Addr, Timestamp, Val, Size]): Topped[IterableOnce[(MemoryRegion[Addr, Timestamp, Val], AlignedRead)]] =
      addr match
        case AllocationSites(sites, size) =>
          // We assume that each malloc addresses is allocated in their own isolated part of the heap.
          // Hence, a malloc address does not overlap with a static address
          Topped.Actual(
            for {
              phys <- sites.physicalAddresses;
              heapCtx <- allocOrNull(phys.ctx)
              region <- mem.store.get(PhysicalAddress(heapCtx, phys.recency))
            } yield((region, AlignedRead.Aligned))
          )
        case NumExpr(addrExpr) =>
          val iv = apronState.getIntInterval(addrExpr)
          if(iv._1 == iv._2) {
            mem.store.get(PhysicalAddress(HeapCtx.Static(iv._1), Recency.Recent)) match
              case Some(staticRegion) =>
                Topped.Actual(
                  Iterator((staticRegion, AlignedRead.Aligned)) ++ mem.store.iterator.filter {
                    case (PhysicalAddress(_:HeapCtx.Dynamic, _),otherRegion) =>
                      concurrentOrNewerThan(otherRegion.timestamp, staticRegion.timestamp) && addressIncludedInRegion(iv._1, otherRegion)
                    case _ => false
                  }.map((_,region) => (region,AlignedRead.MaybeAligned))
                )
              case None =>
                Topped.Actual(
                  mem.store.iterator.filter {
                    case (PhysicalAddress(_: HeapCtx.Dynamic, _), otherRegion) =>
                      addressIncludedInRegion(iv._1, otherRegion)
                    case _ => false
                  }.map((_, region) => (region, AlignedRead.MaybeAligned))
                )
          } else {
            Topped.Top
          }

    private inline def concurrentOrNewerThan[Timestamp: PartialOrder](timestamp1: Timestamp, timestamp2: Timestamp): Boolean =
      !PartialOrder[Timestamp].lteq(timestamp1, timestamp2)

    private inline def addressIncludedInRegion[Timestamp,Val](n: Int, region: MemoryRegion[Addr,Timestamp,Val]): Boolean =
      val startAddrIv = apronState.getInterval(region.startAddr.asInstanceOf[NumExpr].expr)
      startAddrIv.inf.cmp(n) <= 0 && (
        region.byteSize match
          case Topped.Actual(bs) => startAddrIv.sup.cmp(n-bs) >= 0
          case _ => true
      )

//
//    private def staticOrDynamic(heapCtx: HeapCtx, addr: Addr): Iterable[(HeapCtx.Static | HeapCtx.Dynamic, ApronExpr[VirtAddr, Type])] =
//      (heapCtx,addr) match
//        case (ctx : (HeapCtx.Static | HeapCtx.Dynamic), NumExpr(addrExpr)) => Some((ctx, addrExpr))
//        case (ctx : HeapCtx.Alloc, _: AllocationSites) => None
//        case _ => throw IllegalStateException(s"Expected allocation sites stored at allocation context, but got context $heapCtx and address $addr")

    private def allocOrNull(ctx: AddrCtx): Iterable[HeapCtx.Alloc | HeapCtx.Static] =
      ctx match
        case (AddrCtx.Heap(heapCtx: (HeapCtx.Alloc | HeapCtx.Static))) => Some(heapCtx)
        case _ => None

  given CombineAddr[W <: Widening](using combineI32: Combine[I32, W]): Combine[Addr, W] with
    def apply(v1: Addr, v2: Addr): MaybeChanged[Addr] = combineI32(v1, v2).map(_.asInstanceOf[Addr])

  def heapAlloc[Bytes](rootFrameData: FrameData)(using apronState: ApronState[VirtAddr, Type], domLogger: DomLogger[FixIn]):
    AAllocatorFromContext[(MemoryAddr,Addr,Bytes), IterableOnce[HeapCtx]] =
      AAllocatorFromContext((key, addr, _) =>
        addr match
          case NumExpr(addr) =>
            val (l,u) = apronState.getIntInterval(addr)
            if(l == u)
              Iterator(HeapCtx.Static(u))
            else
              Iterator(HeapCtx.Dynamic(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))))
          case AllocationSites(sites, _) =>
            sites.iterator.map {
              case VirtualAddress(AddrCtx.Heap(heapCtx), _, _) => heapCtx
              case virt => throw IllegalArgumentException(s"Expected HeapCtx, but got ${virt.ctx}")
            }
      )