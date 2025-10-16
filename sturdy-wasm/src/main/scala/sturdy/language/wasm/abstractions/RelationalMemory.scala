package sturdy.language.wasm.abstractions

import apron.*
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
import sturdy.language.wasm.generic.{ExternalValue, FixIn, FrameData, FuncId, GlobalAddr, InstLoc, MemoryAddr, ModuleInstance}
import sturdy.util.Lazy
import sturdy.values.addresses.{AddressLimits, AddressOffset}
import sturdy.values.integer.IntegerOps
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

trait RelationalMemory extends RelationalValues:
  import RelI32.*
  import Type.*
  import ApronCons.*
  import ApronExpr.*
  import ApronBool.*

  final type Addr = NumExpr | HeapAddr | StackAddr
  final type Size = ApronExpr[VirtAddr, Type]
  final type Bytes = sturdy.effect.bytememory.Bytes[Value]

  case class StaticMemoryLayout(
    tableRange: Interval,
    dataRange: Interval,
    globalRanges: Vector[(String, Interval)],
    stackRange: Interval,
    stackPointer: GlobalAddr,
    heapRange: Interval
  ):
    def getGlobalRange(name: String): Option[Interval] = globalRanges.find((global, _) => name == global).map(_._2)

  var optionStaticMemoryLayout: Option[StaticMemoryLayout] = None

  def parseStaticMemoryLayout(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, GlobalAddr, Value]): Option[StaticMemoryLayout] = {
    for{
      tableBase <- intervalOfExport("__table_base")
      globalBase <- intervalOfExport("__global_base")
      dataEnd <- intervalOfExport("__data_end")
      globalRanges = parseGlobalRanges(globalBase, dataEnd)
      stackLow <- intervalOfExport("__stack_low")
      stackHigh <- intervalOfExport("__stack_high")
      heapBase <- intervalOfExport("__heap_base")
      heapEnd <- intervalOfExport("__heap_end")
    } yield StaticMemoryLayout(
      tableRange = Interval(tableBase.inf(), globalBase.sup()),
      dataRange = Interval(globalBase.inf(), dataEnd.sup()),
      globalRanges = globalRanges,
      stackRange = Interval(stackLow.inf(), stackHigh.sup()),
      stackPointer = GlobalAddr(0),
      heapRange = Interval(heapBase.inf(), heapEnd.sup())
    )
  }

  private inline def intervalOfExport(name: String)(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, GlobalAddr, Value]): Option[Interval] =
    for(
      case ExternalValue.Global(n) <- moduleInstance.findExport(name);
      value <- globals.get((), GlobalAddr(n)).toOption
    ) yield apronState.getInterval(value.asInt32.asNumExpr)

  private def parseGlobalRanges(dataStart: Interval, dataEnd: Interval)(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, GlobalAddr, Value]): Vector[(String,Interval)] = {
    val specialGlobals = Set("__memory_base", "__table_base", "__dso_handle", "__data_end", "__stack_low",
                             "__stack_high", "__global_base", "__heap_base", "__heap_end")
    var globalStarts = for {
      case (name, ExternalValue.Global(n)) <- moduleInstance.exports;
      if !specialGlobals.contains(name)
      value <- globals.get((), GlobalAddr(n)).toOption
    } yield (name, apronState.getInterval(value.asInt32.asNumExpr))

    globalStarts +:= ".rodata" -> dataStart
    globalStarts +:= "__data_end" -> dataEnd

    globalStarts = globalStarts.sortBy((_name, iv) => iv.inf())

    val globalRanges = globalStarts.zip(globalStarts.tail).map {
      case ((name, iv),(_, ivNext)) =>
        val end = apronState.getInterval(ApronExpr.intSub(ApronExpr.constant(ivNext, I32Type), ApronExpr.lit(1, I32Type), I32Type))
        (name, Interval(iv.inf(), end.inf()))
    }

    if(globalRanges.headOption.exists(_._1 == "__data_end"))
      Vector()
    else
      globalRanges
  }

  given RelationalAddressOffset(using f: Failure, lazyEffectStack: Lazy[EffectStack], apronState: ApronState[VirtAddr, Type]): AddressOffset[Addr] with
    override def addOffsetToAddr(newOffset: Int, addr: Addr): Addr = {
      given effectStack: EffectStack = lazyEffectStack.value
      addr match
        case HeapAddr(reference, size) =>
          HeapAddr(
            reference.mapAddr(sites =>
              PowVirtualAddress(sites.iterator.map {
                case VirtualAddress(AddrCtx.Heap(HeapCtx.Heap(site, initOffset)), n, addrTrans) =>
                  apronState.alloc(AddrCtx.Heap(HeapCtx.Heap(site, initOffset + newOffset)))
                case virt => throw new IllegalArgumentException(s"Expected HeapCtx.Alloc, but got ${virt.ctx}")
              })
            ), size)
        case st@StackAddr(func, stackPointer, offset) =>
          st.copy(offset = apronState.simplify(ApronExpr.intAdd(offset, ApronExpr.lit(newOffset, I32Type), I32Type)))
        case NumExpr(baseAddr) =>
          val effectiveAddr = apronState.simplify(ApronExpr.intAdd(baseAddr, ApronExpr.lit(newOffset, I32Type), I32Type))
          given Join[ApronExpr[VirtAddr, Type]] = apronState.join
          NumExpr(apronState.ifThenElse(effectStack)(
            ApronBool.And(
              ApronBool.Constraint(ApronCons.le(ApronExpr.lit[VirtAddr, Type](Integer.MIN_VALUE, baseAddr._type), effectiveAddr)),
              ApronBool.Constraint(ApronCons.le(effectiveAddr, ApronExpr.lit[VirtAddr, Type](Integer.MAX_VALUE, baseAddr._type)))
            )
          ) {
            effectiveAddr
          } {
            f.fail(MemoryAccessOutOfBounds, s"$addr + $newOffset")
          })
    }

    override def moveAddress(addr: Addr, srcOffset: Addr, dstOffset: Addr): Addr =
      (addr,srcOffset,dstOffset) match {
        case (NumExpr(addrExpr), NumExpr(srcOffsetExpr), NumExpr(dstOffsetExpr)) =>
          val tpe = addrExpr._type
          NumExpr(ApronExpr.intAdd(ApronExpr.intSub(addrExpr, srcOffsetExpr, tpe), dstOffsetExpr, tpe))
        case (_, _, dst: StackAddr) => dst
        case _ => throw IllegalArgumentException(s"moveAddress only supports numeric expressions, but got ($addr,$srcOffset,$dstOffset)")
      }

  given RelationalAddressLimits(using apronState: ApronState[VirtAddr, Type]): AddressLimits[Addr, Size, WithJoin] with

    override def addSizeToAddr(size: Size, addr: Addr): Addr =
      addr match
        case NumExpr(addrExpr) => NumExpr(ApronExpr.intAdd(addrExpr, size, Type.I32Type))
        case _: StackAddr => throw IllegalArgumentException("Adding a size to stack address is not supported by the analysis.")
        case _: HeapAddr => throw IllegalArgumentException("Adding a size to allocation-sites is not supported by the analysis.")

    override def ifAddrLeSize[A: WithJoin](addr: Addr, size: Size)(f: => A): JOptionA[A] =
      given Join[A] = implicitly[WithJoin[A]].j
      addr match
        case NumExpr(addrExpr) =>
          apronState.ifThenElse(le(addrExpr, size)) {
            JOptionA.Some(f)
          } {
            JOptionA.None[A]()
          }
        case _: StackAddr | _: HeapAddr =>
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
    override def matchRegion[Timestamp: PartialOrder](addr: Addr, size: Size, alignment: Int, mem: Mem[HeapCtx, Addr, Timestamp, Value, Size]): Iterable[(PhysicalAddress[HeapCtx], MemoryRegion[Addr, Size, Timestamp, Value], AlignedRead)] =
      addr match
        case HeapAddr(reference, size) =>
          // Can possibly read from HeapCtx.Fill, HeapCtx.Heap, and HeapCtx.Dynamic

          val sites = refOps.deref(reference)
          for {
            case PhysicalAddress(AddrCtx.Heap(ctx@HeapCtx.Heap(_,_)), recency) <- sites.physicalAddresses;
            phys: PhysicalAddress[HeapCtx] = PhysicalAddress(ctx,recency)
            region <- mem.store.get(phys)
          } yield(phys, region, AlignedRead.Aligned)
        case StackAddr(frames, stackPointer, offset) =>
          // Can possibly read from HeapCtx.Fill, HeapCtx.Stack, and HeapCtx.Dynamic

          val readStart = ApronExpr.intAdd(ApronExpr.addr(stackPointer, I32Type), offset, I32Type)
          val readEnd = ApronExpr.intAdd(readStart, size, I32Type)
          val matchingRegions = for {
            case (physAddr@PhysicalAddress(ctx:(HeapCtx.Fill | HeapCtx.Stack | HeapCtx.Dynamic), _),region) <- mem.store
            if readRangeOverlapsStackRegion(frames, stackPointer, offset, readStart, readEnd, ctx, region) != Topped.Actual(false)
          } yield (physAddr, region)

          val overwritingRegions = matchingRegions.filter((phys, region) =>
            !isSummaryRegion(phys.ctx, region) && apronState.assert(ApronCons.eq(offset, region.startAddr.asInstanceOf[StackAddr].offset)) == Topped.Actual(true)
          )

          val newestMatchingRegions = matchingRegions.filter((phys, region) =>
            overwritingRegions.forall((_,overwritingRegion) =>
              concurrentOrNewerThan(region.timestamp, overwritingRegion.timestamp)
            )
          )

          val result = newestMatchingRegions.map((phys, region) => (phys, region, alignedRead(phys.ctx, readStart, alignment, region)))

          result

        case NumExpr(readStart) =>
          // Can read from all HeapCtx
          val readStartIv = apronState.getInterval(readStart)
          val readEnd = ApronExpr.intAdd(readStart, size, readStart._type)
          val readEndIv = apronState.getInterval(readEnd)
          val offset = readStart match {
            case ApronExpr.Binary(BinOp.Add, ApronExpr.Constant(baseAddr, _, _), ApronExpr.Constant(staticOffset, _, _), _, _, _, _) if staticOffset.isZero => baseAddr
            case ApronExpr.Binary(BinOp.Add, ApronExpr.Binary(BinOp.Add, baseAddr, dynamicOffset: ApronExpr.Addr[?,?], _, _, _, _), ApronExpr.Constant(staticOffset, _, _), _, _, _, _) if staticOffset.isZero => apronState.getInterval(dynamicOffset)
            case ApronExpr.Binary(BinOp.Add, _, offset, _, _, _, _) => apronState.getInterval(offset)
            case _ => throw Error(s"Address $readStart is not in format baseAddress + offset.")
          }
          val matchingGlobal = for {
            staticMemoryLayout <- optionStaticMemoryLayout
            (name,range) <- staticMemoryLayout.globalRanges.find((name, range) =>
              readStartIv.isLeq(range) ||
              (range.inf().cmp(offset.inf()) <= 0 && offset.sup().cmp(range.sup()) <= 0)
            )
          } yield (name,range)

          val matchingRegions = for {
            (physAddr,region) <- mem.store
            if region.startAddr.isInstanceOf[NumExpr] && readRangeOverlapsRegion(readStart, readStartIv, readEnd, readEndIv, matchingGlobal, physAddr.ctx, region) != Topped.Actual(false)
          } yield (physAddr, region)

          val overwritingRegions = matchingRegions.filter((phys, region) =>
            !isSummaryRegion(phys.ctx, region) && apronState.assert(ApronCons.eq(readStart, region.startAddr.asInstanceOf[NumExpr].expr)) == Topped.Actual(true)
          )

          val newestMatchingRegions = matchingRegions.filter((phys, region) =>
            overwritingRegions.forall((_,overwritingRegion) =>
              concurrentOrNewerThan(region.timestamp, overwritingRegion.timestamp)
            )
          )

          val result = newestMatchingRegions.map((phys, region) => (phys, region, alignedRead(phys.ctx, readStart, alignment, region)))

          result

    private def readRangeOverlapsRegion[Timestamp](
      readStart: ApronExpr[VirtAddr,Type],
      readStartIv: Interval,
      readEnd: ApronExpr[VirtAddr,Type],
      readEndIv: Interval,
      matchingGlobal: Option[(String,Interval)],
      ctx: HeapCtx,
      region: MemoryRegion[Addr,Size,Timestamp,Value]
    ): Topped[Boolean] =
      val regionStart = region.startAddr match {
        case NumExpr(expr) => expr
        case StackAddr(_, stackPointer, offset) => ApronExpr.intAdd(ApronExpr.addr(stackPointer, I32Type), offset, I32Type)
        case _ => throw IllegalArgumentException()
      }
      val regionEnd = ApronExpr.intAdd(regionStart, region.regionByteSize, regionStart._type)

      inline def overlap = apronState.assert(ApronBool.And(
        ApronBool.Constraint(ApronCons.lt(readStart, regionEnd)),
        ApronBool.Constraint(ApronCons.lt(regionStart, readEnd))
      ))

      (ctx, optionStaticMemoryLayout) match {
        case (_: HeapCtx.Fill, _) => Topped.Top
        case (HeapCtx.Static(i), _) =>
          if(matchingGlobal.isDefined || readEndIv.sup().cmp(i) <= 0 || DoubleScalar(i).cmp(readStartIv.inf()) < 0) {
            Topped.Actual(false)
          } else {
            overlap
          }
        case (HeapCtx.Global(name), Some(staticMemoryLayout)) =>
          matchingGlobal match {
            case Some((matchingName, _)) =>
              if(name == matchingName)
                Topped.Actual(true)
              else
                Topped.Actual(false)
            case None =>
              val globalIv = staticMemoryLayout.getGlobalRange(name).get
              if(readEndIv.sup().cmp(globalIv.inf()) <= 0 || globalIv.sup().cmp(readStartIv.inf()) < 0) {
                Topped.Actual(false)
              } else {
                overlap
              }
          }
        case (_: HeapCtx.Stack, Some(staticMemoryLayout)) =>
          if(matchingGlobal.isDefined || readEndIv.sup().cmp(staticMemoryLayout.stackRange.inf()) <= 0 || staticMemoryLayout.stackRange.sup().cmp(readStartIv.inf()) < 0) {
            Topped.Actual(false)
          } else {
            overlap
          }
        case _ => overlap
      }

    private def readRangeOverlapsStackRegion[Timestamp](
      frames: Powerset[Frame],
      stackPointer: VirtAddr,
      offset: ApronExpr[VirtAddr, Type],
      readStart: ApronExpr[VirtAddr, Type],
      readEnd: ApronExpr[VirtAddr, Type],
      ctx: (HeapCtx.Fill | HeapCtx.Stack | HeapCtx.Dynamic),
      region: MemoryRegion[Addr, Size, Timestamp, Value]
    ): Topped[Boolean] =
      ctx match {
        case HeapCtx.Stack(func, _) =>
          if(!frames.set.map(_.func).contains(func))
            Topped.Actual(false)
          else
            // For stack regions, we only compare the offset within the stack frame.
            val readStart = offset
            val readEnd = ApronExpr.intAdd(offset, region.regionByteSize, I32Type)
            val regionStart = region.startAddr.asInstanceOf[StackAddr].offset
            val regionEnd = ApronExpr.intAdd(regionStart, region.regionByteSize, regionStart._type)
            apronState.assert(ApronBool.And(
              ApronBool.Constraint(ApronCons.lt(offset, regionEnd)),
              ApronBool.Constraint(ApronCons.lt(regionStart, readEnd))
            ))
        case _: HeapCtx.Fill => Topped.Top
        case _: HeapCtx.Dynamic =>
          val regionStart = region.startAddr.asInstanceOf[NumExpr].expr
          val regionEnd = ApronExpr.intAdd(regionStart, region.regionByteSize, regionStart._type)
          apronState.assert(ApronBool.And(
            ApronBool.Constraint(ApronCons.lt(readStart, regionEnd)),
            ApronBool.Constraint(ApronCons.lt(regionStart, readEnd))
          ))
      }


    override def knownStartAddrAndSize(ctx: HeapCtx, startAddr: Addr, byteSize: Int): (Addr, Size) =
      val default = (startAddr, ApronExpr.lit(byteSize, I32Type): Size)
      optionStaticMemoryLayout match
        case Some(staticMemoryLayout) =>
          ctx match
            case HeapCtx.Global(name) =>
              staticMemoryLayout.getGlobalRange(name).map(iv =>
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
          region.elementByteSize match
            case Topped.Top => true
            case Topped.Actual(byteSize) =>
              apronState.assert(ApronCons.eq(ApronExpr.lit(byteSize, I32Type), region.regionByteSize)) != Topped.Actual(true)
        case _: HeapCtx.Heap => false // TODO: We do not support malloc-allocated arrays for now.
      }

    private def alignedRead[Timestamp,Val](ctx: HeapCtx, readStart: ApronExpr[VirtAddr, Type], alignment: Int, memoryRegion: MemoryRegion[Addr, Size, Timestamp, Val]): AlignedRead =
      if(memoryRegion.alignment.set.forall(_ == alignment))
        AlignedRead.Aligned
      else if(memoryRegion.alignment.set.head == 0)
        AlignedRead.Aligned
      else
        val alignment = ApronExpr.lit[VirtAddr,Type](scala.math.pow(2,memoryRegion.alignment.set.head).toInt, I32Type)
        // aligned if readStart ≡ regionStart (mod 2^memoryRegion.alignment)
        val res = apronState.assert(ApronCons.eq(ApronExpr.intMod(readStart, alignment, I32Type), ApronExpr.lit(0, I32Type))) match
          case Topped.Actual(true) => AlignedRead.Aligned
          case _ => AlignedRead.MaybeAligned
        res

    private inline def concurrentOrNewerThan[Timestamp: PartialOrder](timestamp1: Timestamp, timestamp2: Timestamp): Boolean =
      PartialOrder[Timestamp].lteq(timestamp2, timestamp1) ||
      !PartialOrder[Timestamp].lteq(timestamp1, timestamp2)

  given CombineAddr[W <: Widening](using combineI32: Combine[I32, W]): Combine[Addr, W] with
    def apply(v1: Addr, v2: Addr): MaybeChanged[Addr] = combineI32(v1, v2).map(_.asInstanceOf[Addr])

  class HeapAlloc(rootFrameData: FrameData)(using apronState: ApronState[VirtAddr, Type], domLogger: DomLogger[FixIn], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]], globals: DecidableSymbolTable[Unit, GlobalAddr, Value])
    extends Allocator[IterableOnce[HeapCtx], (ByteMemoryAllocationContext,MemoryAddr,Addr)] with Stateless:
    private var contextLog: Map[FixIn, Set[HeapCtx]] = Map()

    override def alloc(args: (ByteMemoryAllocationContext, MemoryAddr, Addr)): IterableOnce[HeapCtx] =
      val loc = domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))
      val ctxs = alloc0(args._1, args._2, args._3, loc)
      contextLog += loc -> (contextLog.getOrElse(loc, Set()) ++ ctxs.toSet)
      ctxs


    private def alloc0(byteMemoryAllocationContext: ByteMemoryAllocationContext, memoryAddr: MemoryAddr, addr: Addr, loc: FixIn): Iterable[HeapCtx] = {
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
            case (ByteMemoryAllocationContext.Write, ApronExpr.Binary(BinOp.Add, baseAddr, offset,_, _, _, _), Some(staticMemoryLayout)) =>
              val ctx = getHeapCtx(staticMemoryLayout, loc, effectiveAddr, baseAddr, apronState.getInterval(offset)).getOrElse(defaultAddr)
              Iterable(ctx)
            case (ByteMemoryAllocationContext.Copy, ApronExpr.Binary(BinOp.Add, _, destAddr, _, _, _, _), Some(staticMemoryLayout)) =>
              val offset = Interval(DoubleScalar(0), DoubleScalar(0))
              val ctx = getHeapCtx(staticMemoryLayout, loc, ApronExpr.intAdd(destAddr, ApronExpr.constant(offset, I32Type), I32Type), destAddr, offset).getOrElse(defaultAddr)
              Iterable(ctx)
            case _ =>
              Iterable(defaultAddr)
          }

        case StackAddr(frames, stackPointer, offset) =>
          for {
            frame <- frames.set
          } yield(HeapCtx.Stack(frame.func, getStackOffset(offset)))

        case HeapAddr(reference, _) =>
          val sites = refOps.deref(reference)
          sites.iterator.map {
            case VirtualAddress(AddrCtx.Heap(heapCtx), _, _) => heapCtx
            case virt => throw IllegalArgumentException(s"Expected HeapCtx, but got ${virt.ctx}")
          }.toSet
    }

    private def getHeapCtx(staticMemoryLayout: StaticMemoryLayout, dom: FixIn, effectiveAddr: ApronExpr[VirtAddr,Type], baseAddr: ApronExpr[VirtAddr, Type], offset: Interval): Option[HeapCtx] = {

      val effectiveAddrIv = apronState.getInterval(effectiveAddr)
      val offsetMatchesGlobal = for {
        (globalName, globalRange) <- staticMemoryLayout.globalRanges
        if offset.isLeq(globalRange) || effectiveAddrIv.isLeq(globalRange)
      } yield HeapCtx.Global(globalName)

      if(offsetMatchesGlobal.size == 1) {
        offsetMatchesGlobal.headOption

      } else {

        val constantMatchesGlobal = for {
          const <- baseAddr.constants
          (globalName, globalRange) <- staticMemoryLayout.globalRanges
          if const.cmp(globalRange) == 0 || const.cmp(globalRange) == 1 // if const is equal or included in globalRange
        } yield HeapCtx.Global(globalName)

        if(constantMatchesGlobal.size == 1) {
          constantMatchesGlobal.headOption

        } else {

          val addrMatchesGlobal = for {
            addr <- baseAddr.addrs
            iv = apronState.getInterval(ApronExpr.addr(addr, I32Type))
            (globalName, globalRange) <- staticMemoryLayout.globalRanges
            if iv.isLeq(globalRange)
          } yield HeapCtx.Global(globalName)
          if(addrMatchesGlobal.size == 1) {
            addrMatchesGlobal.headOption
          } else {

            if(effectiveAddrIv.isLeq(staticMemoryLayout.stackRange)) {
              for{
                case FixIn.Eval(_, InstLoc.InFunction(func, _)) <- Some(dom)
              } yield HeapCtx.Stack(func, getStackOffset(effectiveAddr))
            } else {

              val priorCtxs = contextLog.getOrElse(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), Set())
              if (priorCtxs.size == 1) {
                priorCtxs.headOption
              } else {
                None
              }
            }
          }
        }
      }
    }

  private def getStackOffset(offset: ApronExpr[VirtAddr,Type])(using apronState: ApronState[VirtAddr,Type]): Topped[Int] =
    Topped.Actual(addScalarAdditionOperands(offset))

  private def addScalarAdditionOperands(expr: ApronExpr[VirtAddr,Type])(using apronState: ApronState[VirtAddr,Type]): Int =
    expr match
      case ApronExpr.Constant(_: Scalar, _, _) =>
        apronState.getInt(expr).getOrElse(0)
      case ApronExpr.Binary(BinOp.Add, l, r, _, _, _, _) =>
        addScalarAdditionOperands(l) + addScalarAdditionOperands(r)
      case _ => 0

  private inline def toppedFromOption[A](opt: Option[A]): Topped[A] =
    opt match {
      case Some(x) => Topped.Actual(x)
      case None => Topped.Top
    }
