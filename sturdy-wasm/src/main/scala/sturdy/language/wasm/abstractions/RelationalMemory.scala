package sturdy.language.wasm.abstractions

import apron.Interval
import sturdy.apron.{ApronExpr, *, given}
import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.bytememory.{*, given}
import sturdy.effect.bytememory.Bytes.*
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.DecidableSymbolTable
import sturdy.fix.DomLogger
import sturdy.language.wasm.analyses.RelationalAnalysis.{Bool, I32, VirtAddr}
import sturdy.language.wasm.generic.WasmFailure.MemoryAccessOutOfBounds
import sturdy.language.wasm.generic.{ExternalValue, FixIn, FrameData, GlobalAddr, InstLoc, MemoryAddr, ModuleInstance}
import sturdy.util.Lazy
import sturdy.values.addresses.{AddressLimits, AddressOffset}
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.UnsignedOrderingOps
import sturdy.values.{*, given}
import sturdy.values.references.{AbstractReference, PhysicalAddress, PowVirtualAddress, Recency, ReferenceOps, VirtualAddress}
import swam.syntax.StoreNInst

import scala.collection.immutable.ArraySeq

trait RelationalMemory extends RelationalValues:
  import RelI32.*
  import Type.*
  import ApronCons.*
  import ApronExpr.*
  import ApronBool.*

  final type Addr = NumExpr | AllocationSites
  final type Size = ApronExpr[VirtAddr, Type]
  final type Bytes = sturdy.effect.bytememory.Bytes[Value]

  case class StaticMemoryLayout(
    tableRange: Interval,
    dataRange: Interval,
    globalRanges: Vector[(String, Interval)],
    stackRange: Interval,
    stackPointer: GlobalAddr,
    heapRange: Interval
  )

  var optionStaticMemoryLayout: Option[StaticMemoryLayout] = None

  def calculateStaticMemoryLayout(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, GlobalAddr, Value]): Option[StaticMemoryLayout] = {
    for{
      tableBase <- intervalOfExport("__table_base")
      globalBase <- intervalOfExport("__global_base")
      dataEnd <- intervalOfExport("__data_end")
      globalRanges = calculateGlobalRanges(globalBase, dataEnd)
      stackLow <- intervalOfExport("__stack_low")
      stackHigh <- intervalOfExport("__stack_high")
      heapBase <- intervalOfExport("__heap_base")
      heapEnd <- intervalOfExport("__heap_end")
    } yield(StaticMemoryLayout(
      tableRange = Interval(tableBase.inf(), globalBase.sup()),
      dataRange = Interval(globalBase.inf(), dataEnd.sup()),
      globalRanges = globalRanges,
      stackRange = Interval(stackLow.inf(), stackHigh.sup()),
      stackPointer = GlobalAddr(0),
      heapRange = Interval(heapBase.inf(), heapEnd.sup())
    ))
  }

  private inline def intervalOfExport(name: String)(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, GlobalAddr, Value]): Option[Interval] =
    for(
      case ExternalValue.Global(n) <- moduleInstance.findExport(name);
      value <- globals.get((), GlobalAddr(n)).toOption
    ) yield(apronState.getInterval(value.asInt32.asNumExpr))

  private def calculateGlobalRanges(dataStart: Interval, dataEnd: Interval)(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, GlobalAddr, Value]): Vector[(String,Interval)] = {
    val specialGlobals = Set("__memory_base", "__table_base", "__dso_handle", "__data_end", "__stack_low",
                             "__stack_high", "__global_base", "__heap_base", "__heap_end")
    var globalStarts = for {
      case (name, ExternalValue.Global(n)) <- moduleInstance.exports;
      if (!specialGlobals.contains(name))
      value <- globals.get((), GlobalAddr(n)).toOption
    } yield (name, apronState.getInterval(value.asInt32.asNumExpr))

    globalStarts +:= ".rodata" -> dataStart
    globalStarts +:= "__data_end" -> dataEnd

    globalStarts = globalStarts.sortBy((_name, iv) => iv.inf())

    globalStarts.zip(globalStarts.tail).map {
      case ((name, iv),(_, ivNext)) =>
        val end = apronState.getInterval(ApronExpr.intSub(ApronExpr.constant(ivNext, I32Type), ApronExpr.lit(1, I32Type), I32Type))
        (name, Interval(iv.inf(), end.inf()))
    }
  }

  given RelationalAddressOffset(using f: Failure, lazyEffectStack: Lazy[EffectStack], apronState: ApronState[VirtAddr, Type], unsignedOrderingOps: UnsignedOrderingOps[I32, Bool]): AddressOffset[Addr] with
    override def addOffsetToAddr(newOffset: Int, addr: Addr): Addr = {
      given effectStack: EffectStack = lazyEffectStack.value
      addr match
        case AllocationSites(reference, size) =>
          AllocationSites(
            reference.mapAddr(sites =>
              PowVirtualAddress(sites.iterator.map {
                case VirtualAddress(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset)), n, addrTrans) =>
                  apronState.alloc(AddrCtx.Heap(HeapCtx.Alloc(site, initOffset + newOffset)))
                case virt => throw new IllegalArgumentException(s"Expected HeapCtx.Alloc, but got ${virt.ctx}")
              })
            ), size)
        case NumExpr(baseAddr) =>
          val resAddr = baseAddr match {
            case ApronExpr.Binary(BinOp.Add, left, otherScalarOffset@ApronExpr.Constant(_: apron.Scalar, _, _), _, _, _, _) =>
              val resultOffset = apronState.getInterval(ApronExpr.intAdd(otherScalarOffset, ApronExpr.lit(newOffset, I32Type), I32Type))
              if(resultOffset.isScalar) {
                ApronExpr.intAdd(left, ApronExpr.constant(resultOffset.inf(), I32Type), I32Type)
              } else {
                ApronExpr.intAdd(baseAddr, ApronExpr.lit(newOffset, I32Type), I32Type)
              }
            case _ =>
              ApronExpr.intAdd(baseAddr, ApronExpr.lit(newOffset, I32Type), I32Type)
          }
          given Join[ApronExpr[VirtAddr, Type]] = apronState.join
          NumExpr(apronState.ifThenElse(effectStack)(unsignedOrderingOps.ltUnsigned(NumExpr(resAddr), NumExpr(ApronExpr.lit[VirtAddr, Type](newOffset, baseAddr._type)))) {
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

    override def addSizeToAddr(size: Size, addr: Addr): Addr =
      addr match
        case NumExpr(addrExpr) => NumExpr(ApronExpr.intAdd(addrExpr, size, Type.I32Type))
        case AllocationSites(_,_) => throw IllegalArgumentException("Adding a size to allocation-sites is not supported by the analysis.")

    override def ifAddrLeSize[A: WithJoin](addr: Addr, size: Size)(f: => A): JOptionA[A] =
      given Join[A] = implicitly[WithJoin[A]].j
      addr match
        case NumExpr(addrExpr) =>
          apronState.ifThenElse(le(addrExpr, size)) {
            JOptionA.Some(f)
          } {
            JOptionA.None[A]()
          }
        case _: AllocationSites =>
          JOptionA.Some(f)

    override def ifSizeLeLimit[A: WithJoin](size: ApronExpr[VirtAddr, Type], limit: ApronExpr[VirtAddr, Type])(ifTrue: => A)(ifFalse: => A): A =
      given Join[A] = implicitly[WithJoin[A]].j
      apronState.ifThenElse(And(Constraint(le(lit(0, Type.I32Type), size)), Constraint(le(size, limit))))(ifTrue)(ifFalse)

  given RelationalSizeOps(using apronState: ApronState[VirtAddr, Type], integerOps: IntegerOps[Int,Size]): SizeOps[Size] with {
    override def fromInt(size: Int): Size = ApronExpr.lit(size, Type.I32Type)
    override def add(size1: Size, size2: Size): Size = ApronExpr.intAdd(size1, size2, Type.I32Type)
    override def sub(size1: Size, size2: Size): Size = ApronExpr.intSub(size1, size2, Type.I32Type)
    override def mul(size1: Size, size2: Size): Size = ApronExpr.intMul(size1, size2, Type.I32Type)
    override def min(size1: Size, size2: Size): Size = integerOps.min(size1, size2)
    override def toTopped(size: Size): Topped[Int] = {
      val (l,h) = apronState.getIntInterval(size)
      if(l == h)
        Topped.Actual(l)
      else
        Topped.Top
    }
  }


  given RelationalLanguageSpecificMemOps(using apronState: ApronState[VirtAddr, Type], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]], sizeOps: SizeOps[Size], globals: DecidableSymbolTable[Unit, GlobalAddr, Value], heapAlloc: HeapAlloc): LanguageSpecificMemOps[HeapCtx, Addr, Size, Value] with
    override def matchRegion[Timestamp: PartialOrder](addr: Addr, size: Size, mem: Mem[HeapCtx, Addr, Timestamp, Value, Size]): Iterator[(PhysicalAddress[HeapCtx], MemoryRegion[Addr, Size, Timestamp, Value], AlignedRead)] =
      addr match
        case AllocationSites(reference, size) =>
          // We assume that each malloc addresses is allocated in their own isolated part of the heap.
          // Hence, a malloc address does not overlap with a static address
          val sites = refOps.deref(reference)
          val allocSites = for {
            case PhysicalAddress(AddrCtx.Heap(ctx@HeapCtx.Alloc(_,_)), recency) <- sites.physicalAddresses;
            phys: PhysicalAddress[HeapCtx] = PhysicalAddress(ctx,recency)
            region <- mem.store.get(phys)
          } yield(phys, region, AlignedRead.Aligned)
          allocSites.iterator
        case NumExpr(readStart) =>

          // Matching HeapCtx.Static
          val readStartIv = apronState.getIntInterval(readStart)
          val readStartLow = math.max(0, readStartIv._1)
          val readStartHigh = math.max(readStartLow, readStartIv._2)
          val readEnd = ApronExpr.intAdd(readStart, size, readStart._type)
          val readEndIv = apronState.getIntInterval(readEnd)
          val readEndLow = math.max(0, readEndIv._1)
          val readEndHigh = math.max(readEndLow, readEndIv._2)

          var overwritingRegions: List[MemoryRegion[Addr,Size,Timestamp,Value]] = List()

          val defaultGlobals = for {
            case staticMemoryLayout <- optionStaticMemoryLayout.iterator
            // Overlaps with global region
            if(apron.DoubleScalar(readStartLow).cmp(staticMemoryLayout.dataRange.sup()) <= 0 && staticMemoryLayout.dataRange.inf().cmp(apron.DoubleScalar(readEndHigh)) <= 0)
            case (phys@PhysicalAddress(ctx: HeapCtx.Global, _),region) <- mem.store.iterator
          } yield(phys,region,alignedRead(ctx, readStart, region))

          val defaultStackRegions = for {
            case staticMemoryLayout <- optionStaticMemoryLayout.iterator
            // Overlaps with global region
            if (apron.DoubleScalar(readStartLow).cmp(staticMemoryLayout.stackRange.sup()) <= 0 && staticMemoryLayout.stackRange.inf().cmp(apron.DoubleScalar(readEndHigh)) <= 0)
            case (phys@PhysicalAddress(ctx: HeapCtx.Stack, _), region) <- mem.store.iterator
          } yield (phys, region, alignedRead(ctx, readStart, region))

          val matchingStaticallyKnownRegions = (readStart, optionStaticMemoryLayout) match {
            case (ApronExpr.Binary(BinOp.Add, baseAddr, ApronExpr.Constant(offset: apron.Scalar, _, _), _, _, _, _), Some(staticMemoryLayout)) =>
              heapAlloc.getHeapCtx(staticMemoryLayout, readStart, baseAddr, offset) match {
                case Some(ctx) =>
                  val recentRegion = mem.store.get(PhysicalAddress(ctx, Recency.Recent))
                  if(recentRegion.exists(region => apronState.assert(ApronCons.eq(region.startAddr.asInstanceOf[NumExpr].expr, readStart)) == Topped.Actual(true) && !isSummaryRegion(ctx,region))) {
                    overwritingRegions ++= recentRegion
                    Iterator((PhysicalAddress(ctx, Recency.Recent), recentRegion.get, alignedRead(ctx, readStart, recentRegion.get)))
                  } else {
                    val oldRegion = mem.store.get(PhysicalAddress(ctx, Recency.Old))
                    recentRegion.map(region => (PhysicalAddress(ctx, Recency.Recent), region, alignedRead(ctx, readStart, region))).iterator ++ oldRegion.map(region => (PhysicalAddress(ctx, Recency.Old), region, alignedRead(ctx, readStart, region))).iterator
                  }
                case None => defaultGlobals ++ defaultStackRegions
              }
            case _ => defaultGlobals ++ defaultStackRegions
          }

          def regionOverlaps(i: Int, region: MemoryRegion[Addr, Size, Timestamp, Value]): Boolean = {
            val (sizeLow,sizeHigh) = apronState.getIntInterval(region.byteSize)
            val regionEndLow = i.toLong + sizeLow.toLong
            val regionEndHigh = i.toLong + sizeHigh.toLong
            readStartLow < regionEndLow && regionEndHigh <= readEndHigh
          }

          val matchingStaticRegions =
            if (readEndHigh - readStartLow <= 20) {
              for {
                i <- math.max(0, readStartLow - 16).until(readEndHigh).iterator;
                phys = PhysicalAddress(HeapCtx.Static(i), Recency.Recent);
                region <- mem.store.get(phys).iterator
                if (regionOverlaps(i, region))
              } yield (phys, region, alignedRead(phys.ctx, readStart, region))
            } else {
              for {
                (phys, region) <- mem.store.iterator
                if (
                  phys.ctx match
                    case HeapCtx.Static(i) => regionOverlaps(i, region)
                    case _ => false
                  )
              } yield (phys, region, alignedRead(phys.ctx, readStart, region))
            }

          overwritingRegions ++= mem.store.get(PhysicalAddress(HeapCtx.Static(readStartLow),Recency.Recent))

          // Matching HeapCtx.Dynamic
          val matchingDynamicRegions = for {
            (physAddr, region) <- mem.store.iterator
            if (physAddr.ctx match
              case _: HeapCtx.Fill | _: HeapCtx.Dynamic => true
              case _ => false
              )
            if (
              // Filter out dynamic regions that are certainly overwritten by the
              // static region at the start of the read.
              if(readStartLow == readStartHigh) {
                overwritingRegions.forall(overwritingRegion =>
                  concurrentOrNewerThan(region.timestamp, overwritingRegion.timestamp)
                )
              } else {
                true
              }
            )
            if {
              val regionStart = region.startAddr.asInstanceOf[NumExpr].expr

              // Check if the **end** of the region overlaps with the read address range.
              val regionEnd = ApronExpr.intAdd(regionStart, region.byteSize, regionStart._type)

              apronState.assert(ApronBool.And(
                ApronBool.Constraint(ApronCons.lt(readStart, regionEnd)),
                ApronBool.Constraint(ApronCons.le(regionEnd, readEnd))
              )) != Topped.Actual(false)
            }
          } yield ((physAddr, region, alignedRead(physAddr.ctx, readStart, region)))

          matchingStaticallyKnownRegions ++ matchingStaticRegions ++ matchingDynamicRegions

    override def knownStartAddrAndSize(ctx: HeapCtx, startAddr: Addr, byteSize: Int): (Addr, Size) =
      val default = (startAddr, ApronExpr.lit(byteSize, I32Type): Size)
      optionStaticMemoryLayout match
        case Some(staticMemoryLayout) =>
          ctx match
            case HeapCtx.Global(name) =>
              staticMemoryLayout.globalRanges
                .find((glob,_) => name == glob)
                .map((_,iv) =>
                  (NumExpr(ApronExpr.constant(iv.inf(), I32Type)): Addr,
                   ApronExpr.constant(apronState.getInterval(
                     ApronExpr.intAdd(
                       ApronExpr.lit(1, I32Type),
                       ApronExpr.intSub(
                         ApronExpr.constant(iv.sup(), I32Type),
                         ApronExpr.constant(iv.inf(), I32Type),
                         I32Type),
                       I32Type)),
                     I32Type))
                )
                .getOrElse(default)
            case _ => default
        case None => default


    override def isSummaryRegion[Timestamp: PartialOrder](ctx: HeapCtx, region: MemoryRegion[Addr, Size, Timestamp, Value]): Boolean =
      ctx match {
        case _: HeapCtx.Fill | _: HeapCtx.Dynamic | HeapCtx.Stack(_, Topped.Top) => true
        case _: HeapCtx.Static | _: HeapCtx.Stack | _: HeapCtx.Global =>
          byteSizeOfValue(region.value) match
            case Topped.Top => true
            case Topped.Actual(byteSize) =>
              apronState.assert(ApronCons.eq(ApronExpr.lit(byteSize, I32Type), region.byteSize)) != Topped.Actual(true)
        case _: HeapCtx.Alloc => false // TODO: We do not support malloc-allocated arrays for now.
      }


    private def byteSizeOfValue(value: Value): Topped[Int] =
      value match
        case Value.TopValue => Topped.Top
        case Value.Num(_: NumValue.Int32) | Value.Num(_: NumValue.Float32) => Topped.Actual(4)
        case Value.Num(_: NumValue.Int64) | Value.Num(_: NumValue.Float64) => Topped.Actual(8)
        case _: Value.Vec => Topped.Actual(16)
        case _: Value.Ref => Topped.Top

    private def alignedRead[Timestamp,Val](ctx: HeapCtx, readStart: ApronExpr[VirtAddr, Type], memoryRegion: MemoryRegion[Addr, Size, Timestamp, Val]): AlignedRead =
      if(memoryRegion.alignment.size > 1) {
        AlignedRead.MaybeAligned
      } else {
        memoryRegion.startAddr match
          case NumExpr(regionStart) =>
            val alignment = ApronExpr.lit[VirtAddr,Type](scala.math.pow(2,memoryRegion.alignment.set.head).toInt, I32Type)
            // aligned if readStart ≡ regionStart (mod 2^memoryRegion.alignment)
            val res = apronState.assert(ApronCons.eq(ApronExpr.intMod(readStart, alignment, I32Type), ApronExpr.intMod(regionStart, alignment, I32Type))) match
              case Topped.Actual(true) => AlignedRead.Aligned
              case _ => AlignedRead.MaybeAligned
            res
          case _: AllocationSites => AlignedRead.Aligned
      }

    private inline def concurrentOrNewerThan[Timestamp: PartialOrder](timestamp1: Timestamp, timestamp2: Timestamp): Boolean =
      !PartialOrder[Timestamp].lteq(timestamp1, timestamp2)

  given CombineAddr[W <: Widening](using combineI32: Combine[I32, W]): Combine[Addr, W] with
    def apply(v1: Addr, v2: Addr): MaybeChanged[Addr] = combineI32(v1, v2).map(_.asInstanceOf[Addr])

  class HeapAlloc(rootFrameData: FrameData)(using apronState: ApronState[VirtAddr, Type], domLogger: DomLogger[FixIn], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]], globals: DecidableSymbolTable[Unit, GlobalAddr, Value])
    extends Allocator[IterableOnce[HeapCtx], (ByteMemoryAllocationContext,MemoryAddr,Addr)] with Stateless:
    var contextLog: Map[FixIn, Set[HeapCtx]] = Map()

    override def alloc(args: (ByteMemoryAllocationContext, MemoryAddr, Addr)): IterableOnce[HeapCtx] =
      val loc = domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))
      val ctxs = alloc0(args._1, args._2, args._3, loc)
      contextLog += loc -> (contextLog.getOrElse(loc, Set()) ++ ctxs.toSet)
      ctxs

    def alloc0(byteMemoryAllocationContext: ByteMemoryAllocationContext, memoryAddr: MemoryAddr, addr: Addr, loc: FixIn): Iterable[HeapCtx] = {
      addr match
        case NumExpr(effectiveAddr) =>
          val (l, u) = apronState.getIntInterval(effectiveAddr)
          val defaultAddr =
            if (l == u)
              HeapCtx.Static(u)
            else
              HeapCtx.Dynamic(loc)

          (byteMemoryAllocationContext, effectiveAddr, optionStaticMemoryLayout) match {
            case (ByteMemoryAllocationContext.Fill, _, _) =>
              Iterable(HeapCtx.Fill(loc))
            case (ByteMemoryAllocationContext.Write, ApronExpr.Binary(BinOp.Add, baseAddr, ApronExpr.Constant(offset: apron.Scalar, _, _), _, _, _, _), Some(staticMemoryLayout)) =>
              Iterable(getHeapCtx(staticMemoryLayout, effectiveAddr, baseAddr, offset).getOrElse(defaultAddr))
            case (ByteMemoryAllocationContext.Copy, ApronExpr.Binary(BinOp.Add, _, destAddr, _, _, _, _), Some(staticMemoryLayout)) =>
              val offset = apron.DoubleScalar(0)
              Iterable(getHeapCtx(staticMemoryLayout, ApronExpr.intAdd(destAddr, ApronExpr.constant(offset, I32Type), I32Type), destAddr, offset).getOrElse(defaultAddr))
            case _ =>
              Iterable(defaultAddr)
          }


        case AllocationSites(reference, _) =>
          val sites = refOps.deref(reference)
          sites.iterator.map {
            case VirtualAddress(AddrCtx.Heap(heapCtx), _, _) => heapCtx
            case virt => throw IllegalArgumentException(s"Expected HeapCtx, but got ${virt.ctx}")
          }.toSet
    }

    def getHeapCtx(staticMemoryLayout: StaticMemoryLayout, effectiveAddr: ApronExpr[VirtAddr,Type], baseAddr: ApronExpr[VirtAddr, Type], offset: apron.Scalar): Option[HeapCtx] = {

      val effectiveAddrIv = apronState.getInterval(effectiveAddr)
      val offsetMatchesGlobal = for {
        (globalName, globalRange) <- staticMemoryLayout.globalRanges
        if (Interval(offset,offset).isLeq(globalRange) || effectiveAddrIv.isLeq(globalRange))
      } yield HeapCtx.Global(globalName)

      if(offsetMatchesGlobal.size == 1) {
        offsetMatchesGlobal.headOption

      } else {

        val constantMatchesGlobal = for {
          const <- baseAddr.constants
          (globalName, globalRange) <- staticMemoryLayout.globalRanges
          if (const.cmp(globalRange) == 0 || const.cmp(globalRange) == 1) // if const is equal or included in globalRange
        } yield HeapCtx.Global(globalName)

        if(constantMatchesGlobal.size == 1) {
          constantMatchesGlobal.headOption

        } else {

          val addrMatchesGlobal = for {
            addr <- baseAddr.addrs
            iv = apronState.getInterval(ApronExpr.addr(addr, I32Type))
            (globalName, globalRange) <- staticMemoryLayout.globalRanges
            if (iv.isLeq(globalRange))
          } yield HeapCtx.Global(globalName)
          if(addrMatchesGlobal.size == 1) {
            addrMatchesGlobal.headOption
          } else {

            val addrMatchesStack = for {
              case Value.Num(NumValue.Int32(NumExpr(stackPointer))) <- globals.get((), staticMemoryLayout.stackPointer).toOption
              if(baseAddr.addrs.exists(v => apronState.getInterval(ApronExpr.addr(v, I32Type)).isLeq(staticMemoryLayout.stackRange)))
              case FixIn.Eval(_,InstLoc.InFunction(func,_)) <- domLogger.currentDom
            } yield(HeapCtx.Stack(func, getStackOffset(baseAddr,offset)))
            if(addrMatchesStack.isDefined) {
              addrMatchesStack
            } else {

              val priorCtxs = contextLog.getOrElse(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), Set())
              if(priorCtxs.size == 1) {
                priorCtxs.headOption
              } else {
                None
              }
            }
          }
        }
      }
    }

  private inline def getStackOffset(baseAddr: ApronExpr[VirtAddr,Type], offset: apron.Scalar)(using apronState: ApronState[VirtAddr,Type]): Topped[Int] =
    baseAddr match
      case _: ApronExpr.Addr[VirtAddr,Type] => toppedFromOption(apronState.getInt(ApronExpr.constant(offset, I32Type)))
      case ApronExpr.Binary(BinOp.Add, _: ApronExpr.Addr[VirtAddr,Type], const@ApronExpr.Constant(_, _, _), _, _, _, _) =>
        toppedFromOption(apronState.getInt(ApronExpr.intAdd(const, ApronExpr.constant(offset, I32Type), I32Type)))
      case ApronExpr.Binary(BinOp.Sub, _: ApronExpr.Addr[VirtAddr,Type], const@ApronExpr.Constant(_, _, _), _, _, _, _) =>
        toppedFromOption(apronState.getInt(ApronExpr.intSub(const, ApronExpr.constant(offset, I32Type), I32Type)))
      case ApronExpr.Binary(BinOp.Add, ApronExpr.Binary(BinOp.Mul, _, _, _, _, _, _), _: ApronExpr.Addr[VirtAddr,Type], _, _, _, _) => // Array access
        toppedFromOption(apronState.getInt(ApronExpr.constant(offset, I32Type)))
      case _ => Topped.Top

  private inline def toppedFromOption[A](opt: Option[A]): Topped[A] =
    opt match {
      case Some(x) => Topped.Actual(x)
      case None => Topped.Top
    }

//  def moveMemLoc(rootFrameData: FrameData)(using apronState: ApronState[VirtAddr,Type], domLogger: DomLogger[FixIn]): AAllocatorFromContext[(MemoryAddr,HeapCtx,Addr), HeapCtx] =
//    AAllocatorFromContext((memoryAddr, ctx, destAddr) =>
//      (destAddr, optionStaticMemoryLayout) match
//        case (NumExpr(ApronExpr.Binary(BinOp.Add, ApronExpr.Binary(BinOp.Sub, addr, srcOffset, _, _, _, _), destOffset, _, _, _, _)), Some(staticMemoryLayout)) =>
//          val (l, u) = apronState.getIntInterval(destOffset)
//          val matchingGlobals = for {
//            (global,range) <- staticMemoryLayout.globalRanges
//            if(Interval(l,u).isLeq(range))
//          } yield(HeapCtx.Global(global))
//
//          if(matchingGlobals.size == 1)
//            matchingGlobals.head
//          else
//            if (l == u)
//              HeapCtx.Static(u)
//            else
//              ctx match
//                case _: HeapCtx.Dynamic => ctx
//                case _ => HeapCtx.Dynamic(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)))
//        case (AllocationSites(reference, _), _) =>
//          throw IllegalArgumentException("Moving malloc-allocated addresses is not supported by the analysis.")
//    )