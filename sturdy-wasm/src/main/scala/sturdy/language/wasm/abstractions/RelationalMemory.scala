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
import sturdy.values.references.{AbstractReference, PhysicalAddress, PowVirtualAddress, Recency, ReferenceOps, VirtualAddress}

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
        case AllocationSites(reference, size) =>
          AllocationSites(
            reference.mapAddr(sites =>
              PowVirtualAddress(sites.iterator.map {
                case VirtualAddress(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset)), n, addrTrans) =>
                  apronState.alloc(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset + newOffset)))
                case virt => throw new IllegalArgumentException(s"Expected HeapCtx.Alloc, but got ${virt.ctx}")
              })
            ), size)
        case _ =>
          val expr = addr.asNumExpr
          val resAddr = ApronExpr.intAdd[VirtAddr, Type](expr, ApronExpr.lit[VirtAddr, Type](newOffset, expr._type), expr._type).simplify
          given Join[ApronExpr[VirtAddr, Type]] = apronState.join
          NumExpr(apronState.ifThenElse(effectStack)(unsignedOrderingOps.ltUnsigned(NumExpr(resAddr), NumExpr(ApronExpr.lit[VirtAddr, Type](newOffset, expr._type)))) {
            f.fail(MemoryAccessOutOfBounds, s"$addr + $newOffset")
          } {
            resAddr
          })
    }

    override def moveAddress(addr: Addr, srcOffset: Addr, dstOffset: Addr): Addr =
      (addr,srcOffset,dstOffset) match {
        case (NumExpr(addrExpr), NumExpr(srcOffsetExpr), NumExpr(dstOffsetExpr)) =>
          val tpe = addrExpr._type
          NumExpr(ApronExpr.intAdd(ApronExpr.intSub(addrExpr, srcOffsetExpr, tpe), dstOffsetExpr, tpe))
        case _ => throw IllegalArgumentException(s"moveAddress only supports numeric expressions, but got ($addr,$srcOffset,$dstOffset)")
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

  given RelationalLanguageSpecificMemOps(using apronState: ApronState[VirtAddr, Type], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]]): LanguageSpecificMemOps[HeapCtx, Addr, Size] with
    override def matchRegion[Val, Timestamp: PartialOrder](addr: Addr, size: Size, mem: Mem[HeapCtx, Addr, Timestamp, Val, Size]): Iterator[(PhysicalAddress[HeapCtx], MemoryRegion[Addr, Timestamp, Val], AlignedRead)] =
      addr match
        case AllocationSites(reference, size) =>
          // We assume that each malloc addresses is allocated in their own isolated part of the heap.
          // Hence, a malloc address does not overlap with a static address
          val sites = refOps.deref(reference)
          val allocSites = for {
            phys <- sites.physicalAddresses;
            heapCtx <- allocOrNull(phys.ctx)
            region <- mem.store.get(PhysicalAddress(heapCtx, phys.recency))
          } yield((PhysicalAddress(heapCtx, phys.recency), region, AlignedRead.Aligned))
          allocSites.iterator
        case NumExpr(readStart) =>
          val readStartIv = apronState.getIntInterval(readStart)
          val readStartLow = math.max(0, readStartIv._1)
          val readStartHigh = math.max(readStartLow, readStartIv._2)
          val readEnd = ApronExpr.intAdd(readStart, size, readStart._type)
          val readEndIv = apronState.getIntInterval(readEnd)
          val readEndLow = math.max(0, readEndIv._1)
          val readEndHigh = math.max(readEndLow, readEndIv._2)


          def regionOverlaps(i: Int, region: MemoryRegion[Addr,Timestamp,Val]) =
            region.byteSize match
              case Topped.Top => i < readEndHigh
              case Topped.Actual(byteSize) =>
                val regionEnd = i + byteSize
                readStartLow < regionEnd && regionEnd <= readEndHigh

          val matchingStaticRegions =
            if(readEndHigh - readStartLow <= 20) {
              for {
                i <- math.max(0, readStartLow - 16).until(readEndHigh).iterator;
                physAddr = PhysicalAddress(HeapCtx.Static(i), Recency.Recent);
                region <- mem.store.get(physAddr).iterator
                if (regionOverlaps(i,region))
              } yield (physAddr, region, alignedRead(readStart, region))
            } else {
              for {
                (physAddr,region) <- mem.store.iterator
                if (
                  physAddr.ctx match
                    case HeapCtx.Static(i) => regionOverlaps(i, region)
                    case _ => false
                  )
              } yield (physAddr, region, alignedRead(readStart, region))

            }

          val staticRegionAtReadStart = mem.store.get(PhysicalAddress(HeapCtx.Static(readStartLow), Recency.Recent))
          val matchingDynamicRegions = for {
            (physAddr,region) <- mem.store.iterator
            if (physAddr match
              case PhysicalAddress(_: HeapCtx.Dynamic, _) => true
              case _ => false
            )
            if(
              // Filter out dynamic regions that are certainly overwritten by the
              // static region at the start of the read.
              readStartLow == readStartHigh &&
              staticRegionAtReadStart.exists(staticRegion =>
                concurrentOrNewerThan(region.timestamp, staticRegion.timestamp)
              )
            )
            if {
              val regionStart = region.startAddr.asInstanceOf[NumExpr].expr

              region.byteSize match
                case Topped.Top =>
                  apronState.satisfies(ApronCons.le(regionStart, readEnd)) != Topped.Actual(false)
                case Topped.Actual(byteSize) =>
                  // Check if the **end** of the region overlaps with the read address range.
                  val regionEnd = ApronExpr.intAdd(regionStart, ApronExpr.lit(byteSize, regionStart._type), regionStart._type)
                  apronState.satisfies(ApronCons.lt(readStart, regionEnd)) != Topped.Actual(false) && apronState.satisfies(ApronCons.le(regionEnd, readEnd)) != Topped.Actual(false)
            }
          } yield((physAddr, region, alignedRead(readStart, region)))

          matchingStaticRegions ++ matchingDynamicRegions

    private inline def alignedRead[Timestamp,Val](readStart: ApronExpr[VirtAddr, Type], memoryRegion: MemoryRegion[Addr, Timestamp, Val]): AlignedRead =
      memoryRegion.startAddr match
        case NumExpr(regionStart) =>
          apronState.getBoolean(ApronCons.eq(readStart, regionStart)) match
            case Topped.Actual(true) => AlignedRead.Aligned
            case _ => AlignedRead.MaybeAligned
        case _: AllocationSites => AlignedRead.MaybeAligned

    private inline def concurrentOrNewerThan[Timestamp: PartialOrder](timestamp1: Timestamp, timestamp2: Timestamp): Boolean =
      !PartialOrder[Timestamp].lteq(timestamp1, timestamp2)

    private def allocOrNull(ctx: AddrCtx): Iterable[HeapCtx] =
      ctx match
        case (AddrCtx.Heap(heapCtx: (HeapCtx.Alloc | HeapCtx.Static))) => Some(heapCtx)
        case _ => None

  given CombineAddr[W <: Widening](using combineI32: Combine[I32, W]): Combine[Addr, W] with
    def apply(v1: Addr, v2: Addr): MaybeChanged[Addr] = combineI32(v1, v2).map(_.asInstanceOf[Addr])

  def heapAlloc(rootFrameData: FrameData)(using apronState: ApronState[VirtAddr, Type], domLogger: DomLogger[FixIn], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]]):
    AAllocatorFromContext[(MemoryAddr,Addr), IterableOnce[HeapCtx]] =
      AAllocatorFromContext((key, addr) =>
        addr match
          case NumExpr(addr) =>
            val (l,u) = apronState.getIntInterval(addr)
            if(l == u)
              Iterator(HeapCtx.Static(u))
            else
              Iterator(HeapCtx.Dynamic(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))))
          case AllocationSites(reference, _) =>
            val sites = refOps.deref(reference)
            sites.iterator.map {
              case VirtualAddress(AddrCtx.Heap(heapCtx), _, _) => heapCtx
              case virt => throw IllegalArgumentException(s"Expected HeapCtx, but got ${virt.ctx}")
            }.toSet
      )

  def moveMemLoc(rootFrameData: FrameData)(using apronState: ApronState[VirtAddr,Type], domLogger: DomLogger[FixIn]): AAllocatorFromContext[(MemoryAddr,HeapCtx,Addr), HeapCtx] =
    AAllocatorFromContext((memoryAddr, ctx, destAddr) =>
      destAddr match
        case NumExpr(addr) =>
          val (l, u) = apronState.getIntInterval(addr)
          if (l == u)
            HeapCtx.Static(u)
          else
            ctx match
              case _: HeapCtx.Dynamic => ctx
              case _: HeapCtx.Static  => HeapCtx.Dynamic(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)))
              case _: HeapCtx.Alloc   => ctx
        case AllocationSites(reference, _) =>
          throw IllegalArgumentException("Moving malloc-allocated addresses is not supported by the analysis.")
    )