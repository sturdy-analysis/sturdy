package sturdy.language.wasm.abstractions

import sturdy.apron.{ApronBool, ApronCons, ApronExpr, ApronState, ApronVar}
import sturdy.data.{*, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.bytememory.{*, given}
import sturdy.effect.bytememory.Bytes.*
import sturdy.fix.DomLogger
import sturdy.language.wasm.analyses.RelationalAnalysis.VirtAddr
import sturdy.language.wasm.generic.{FixIn, FrameData, MemoryAddr}
import sturdy.values.addresses.AddressLimits
import sturdy.values.{*, given}
import sturdy.values.references.{PhysicalAddress, VirtualAddress}

trait RelationalMemory extends RelationalValues:
  import RelI32.*
  import ApronCons.*
  import ApronExpr.*
  import ApronBool.*

  final type Addr = NumExpr | AllocationSites
  final type Size = ApronExpr[VirtAddr, Type]
  final type Bytes = sturdy.effect.bytememory.Bytes[Value]

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
    override def apply[Val](addr: Addr, mem: Mem[HeapCtx, Addr, Size, Val]): IterableOnce[(MemoryRegion[Addr, Val], AlignedRead)] =
      addr match
        case AllocationSites(sites, size) =>
          // We assume that each malloc addresses is allocated in their own isolated part of the heap.
          // Hence, a malloc address does not overlap with a static address
          for {
            phys <- sites.physicalAddresses;
            heapCtx <- allocOrNull(phys.ctx)
            region <- mem.store.get(PhysicalAddress(heapCtx, phys.recency))
          } yield((region, AlignedRead.Aligned))
        case NumExpr(addrExpr) =>
          for {
            case (phys,region) <- mem.store.iterator;
            (ctx, startAddr) <- staticOrDynamic(phys.ctx, region.startAddr);
            matches <- Iterable(apronState.satisfies(ApronCons.eq(addrExpr, startAddr)));
            if(matches == Topped.Actual(true) || matches == Topped.Top)
          } yield((region, if(matches == Topped.Actual(true)) AlignedRead.Aligned else AlignedRead.Unaligned))

    private def staticOrDynamic(heapCtx: HeapCtx, addr: Addr): Iterable[(HeapCtx.Static | HeapCtx.Dynamic, ApronExpr[VirtAddr, Type])] =
      (heapCtx,addr) match
        case (ctx : (HeapCtx.Static | HeapCtx.Dynamic), NumExpr(addrExpr)) => Some((ctx, addrExpr))
        case (ctx : HeapCtx.Alloc, _: AllocationSites) => None
        case _ => throw IllegalStateException(s"Expected allocation sites stored at allocation context, but got context $heapCtx and address $addr")

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