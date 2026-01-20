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
import sturdy.language.wasm.generic.{ExternalValue, FixIn, FrameData, FuncId, InstLoc, MemoryAddr, ModuleInstance}
import sturdy.language.wasm.generic
import sturdy.util.Lazy
import sturdy.values.addresses.{AddressLimits, AddressOffset}
import sturdy.values.integer.IntegerOps
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}
import swam.binary.custom.dwarf.{CType, DW_OP_addr, GlobalVariable, LocationExpressionParser, unknownType}
import swam.binary.custom.dwarf.llvm.DWARFContext

trait RelationalMemory extends RelationalValues:
  import RelI32.*
  import Type.*
  import ApronCons.*
  import ApronExpr.*
  import ApronBool.*

  final type Addr = NumExpr | GlobalAddr | StackAddr | HeapAddr
  final type Size = ApronExpr[VirtAddr, Type]
  final type Bytes = sturdy.effect.bytememory.Bytes[Value]

  //TODO: refactor StaticMemoryLayout into its own file and change the Interval Class used
  //case class StaticMemoryLayout(
  //  tableRange: Interval,
  //  dataRange: Interval, //replace with vector strings
  //  globalRanges: Vector[(String, Interval)],
  //  stackRange: Interval, //replace with vector of functions
  //  stackPointer: generic.GlobalAddr,
  //  heapRange: Interval //TODO: use sturdy NumericInterval
  //                             //do globals first
  //                             //also do strings (address should be lower than globalbase and higher than DSO_handle
  //                             //TODO add functions to staticmemorylayout
  //):
  //  def getGlobalRange(name: String): Option[Interval] = globalRanges.find((global, _) => name == global).map(_._2)

  var optionStaticMemoryLayout: Option[StaticMemoryLayout] = None

  def parseStaticMemoryLayout(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, generic.GlobalAddr, Value]): Option[StaticMemoryLayout] = {
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
      stackPointer = generic.GlobalAddr(0),
      heapRange = Interval(heapBase.inf(), heapEnd.sup())
    )
  }

  private inline def intervalOfExport(name: String)(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, generic.GlobalAddr, Value]): Option[Interval] =
    for(
      case ExternalValue.Global(n) <- moduleInstance.findExport(name);
      value <- globals.get((), generic.GlobalAddr(n)).toOption
    ) yield apronState.getInterval(value.asInt32.asNumExpr)

  /**
   * tries to determine the global addresses of global variables.
   * If a dwarfSyntaxTree is available it uses the extra information to determine the actual locations of global variables.
   */
  private def parseGlobalRanges(dataStart: Interval, dataEnd: Interval)(using moduleInstance: ModuleInstance, failure: Failure, apronState: ApronState[VirtAddr, Type], globals: DecidableSymbolTable[Unit, generic.GlobalAddr, Value]): Vector[(String,Interval,CType)] = {
    val specialGlobals = Set("__memory_base", "__table_base", "__dso_handle", "__data_end", "__stack_low",
                             "__stack_high", "__global_base", "__heap_base", "__heap_end")
    moduleInstance.dwarfSyntaxTree match {
      case Some(dwarfSyntaxTree) =>
        var globals: Vector[(String, Interval, CType)] = (
          for {
            case GlobalVariable(name, cType, location) <- dwarfSyntaxTree.globals // take dwarfdebug information as "ground truth" and only consider globals that exist in the dwarf debug information
            currGlobalStartAddr: Int = dwarfSyntaxTree.parseLocationExpr(location) match {
              case DW_OP_addr(addr) => addr
              case _ => sys.error("globals are expected to have a known location")
            }
            currGlobalSize: Int = dwarfSyntaxTree.getTypeSize(cType)
            interval = new apron.Interval(currGlobalStartAddr, currGlobalStartAddr + currGlobalSize - 1)
          } yield (name, interval, cType)
          ).toVector
        //globals = globals.prepended((".rodata", dataStart, unknownType()))
        //globals = globals.prepended(("__data_end", dataEnd, unknownType()))

        globals = globals.sortBy((_name, interval, cType) => interval.inf())

        //if (globals.headOption.exists((name, iv, cType) => name == ".rodata" && iv.isBottom))
        //  globals = globals.tail

        if (globals.headOption.exists(_._1 == "__data_end")) {
          println("<empty>")
          Vector()
        } else {
          println(globals)
          globals
        }
      case None =>
        var globalStarts = for {
          case (name, ExternalValue.Global(n)) <- moduleInstance.exports 
          if !specialGlobals.contains(name)
          value <- globals.get((), generic.GlobalAddr(n)).toOption
        } yield (name, apronState.getInterval(value.asInt32.asNumExpr))

        globalStarts +:= ".rodata" -> dataStart
        globalStarts +:= "__data_end" -> dataEnd

        globalStarts = globalStarts.sortBy((_name, iv) => iv.inf())

        //set end address of a global to the start address of the next global (potentially loses precision)
        var globalRanges = globalStarts.zip(globalStarts.tail).map {
          case ((name, iv),(_, ivNext)) =>
            val end = apronState.getInterval(ApronExpr.intSub(ApronExpr.constant(ivNext, I32Type), ApronExpr.lit(1, I32Type), I32Type))
            (name, Interval(iv.inf(), end.inf()), unknownType())
        }

        if(globalRanges.headOption.exists((name, iv, cType) => name == ".rodata" && iv.isBottom))
          globalRanges = globalRanges.tail

        if(globalRanges.headOption.exists(_._1 == "__data_end")) {
          println("<empty>")
          Vector()
        } else {
          println(globalRanges)
          globalRanges
        }
      //moduleInstance
    }
  }

  given RelationalAddressOffset(using f: Failure, lazyEffectStack: Lazy[EffectStack], apronState: ApronState[VirtAddr, Type]): AddressOffset[Addr] with
    override def addOffsetToAddr(newOffset: Int, addr: Addr): Addr = {
      given effectStack: EffectStack = lazyEffectStack.value
      addr match
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
        case glob:GlobalAddr =>
          glob.addOffset(ApronExpr.lit(newOffset, I32Type))
        case stackAddr: StackAddr =>
          stackAddr.addOffset(ApronExpr.lit(newOffset, I32Type))
        case heapAddr: HeapAddr =>
          heapAddr.addOffset(ApronExpr.lit(newOffset, I32Type))
    }

    override def moveAddress(addr: Addr, srcOffset: Addr, dstOffset: Addr): Addr =
      (addr,srcOffset,dstOffset) match {
        case (NumExpr(addrExpr), NumExpr(srcOffsetExpr), NumExpr(dstOffsetExpr)) =>
          val tpe = addrExpr._type
          NumExpr(ApronExpr.intAdd(ApronExpr.intSub(addrExpr, srcOffsetExpr, tpe), dstOffsetExpr, tpe))
        case (_, _, glob: GlobalAddr) => glob
        case (_, _, dst: StackAddr) => dst
        case (_, _, ha: HeapAddr) => ha
      }

  given RelationalAddressLimits(using apronState: ApronState[VirtAddr, Type]): AddressLimits[Addr, Size, WithJoin] with

    override def addSizeToAddr(size: Size, addr: Addr): Addr = addr.addOffset(size)

    override def ifAddrLeSize[A: WithJoin](addr: Addr, size: Size)(f: => A): JOptionA[A] =
      given Join[A] = implicitly[WithJoin[A]].j
      addr match
        case NumExpr(addrExpr) =>
          apronState.ifThenElse(le(addrExpr, size)) {
            JOptionA.Some(f)
          } {
            JOptionA.None[A]()
          }
        case _: GlobalAddr | _: StackAddr | _: HeapAddr =>
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


  given RelationalLanguageSpecificMemOps(using apronState: ApronState[VirtAddr, Type], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]], sizeOps: SizeOps[Size], globals: DecidableSymbolTable[Unit, generic.GlobalAddr, Value], heapAlloc: HeapAlloc): LanguageSpecificMemOps[ByteMemoryCtx, Addr, Size, Value] with
    override def matchRegion[Timestamp: PartialOrder](addr: Addr, size: Size, alignment: Int, mem: Mem[ByteMemoryCtx, Addr, Timestamp, Value, Size]): Iterable[(PhysicalAddress[ByteMemoryCtx], MemoryRegion[Addr, Size, Timestamp, Value], AlignedRead)] =
      addr match
        case NumExpr(readStart) =>
          // Can read from all HeapCtx
          val readStartIv = apronState.getInterval(readStart)
          val readEnd = ApronExpr.intAdd(readStart, size, readStart._type)
          val readEndIv = apronState.getInterval(readEnd)

          val matchingRegions = for {
            (physAddr,region) <- mem.store
            if region.startAddr.isInstanceOf[NumExpr] && readRangeOverlapsRegion(readStart, readStartIv, readEnd, readEndIv, physAddr.ctx, region) != Topped.Actual(false)
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

        case GlobalAddr(globs, offset) =>
          // Can read from HeapCtx.Fill, HeapCtx.Global, and HeapCtx.Dynamic
          val readStartsEnds = globs.set.iterator.map((name, start) =>
            val readStart = ApronExpr.intAdd(ApronExpr.lit(start, I32Type), offset, I32Type)
            val readEnd = ApronExpr.intAdd(readStart, size, I32Type)
            (name, (readStart, readEnd))
          ).toMap

          val matchingRegions = for {
            case (physAddr@PhysicalAddress(ctx:(ByteMemoryCtx.Fill | ByteMemoryCtx.Global | ByteMemoryCtx.Dynamic), _),region) <- mem.store
            if readRangeOverlapsGlobalRegion(readStartsEnds, ctx, region) != Topped.Actual(false)
          } yield (physAddr, region)

          val overwritingRegions = matchingRegions.filter((phys, region) =>
            !isSummaryRegion(phys.ctx, region)
          )

          val newestMatchingRegions = matchingRegions.filter((phys, region) =>
            overwritingRegions.forall((_,overwritingRegion) =>
              concurrentOrNewerThan(region.timestamp, overwritingRegion.timestamp)
            )
          )

          val result = newestMatchingRegions.map((phys, region) =>
            val aligned =
              if(readStartsEnds.forall { case (_, (readStart, _)) => alignedRead(phys.ctx, readStart, alignment, region) == AlignedRead.Aligned })
                AlignedRead.Aligned
              else
                AlignedRead.MaybeAligned
            (phys, region, aligned)

          )

          result

        case StackAddr(function, frameSize, stackPointer, initialOffset, _) =>
          // Can read from HeapCtx.Fill, HeapCtx.Stack, and HeapCtx.Dynamic

          val readStart = addr.getEffectiveAddress
          val readEnd = ApronExpr.intAdd(readStart, size, I32Type)
          val matchingRegions = for {
            case (physAddr@PhysicalAddress(ctx:(ByteMemoryCtx.Fill | ByteMemoryCtx.Stack | ByteMemoryCtx.Dynamic), _),region) <- mem.store
            if readRangeOverlapsStackRegion(function, initialOffset, readStart, readEnd, ctx, region) != Topped.Actual(false)
          } yield (physAddr, region)

          val overwritingRegions = matchingRegions.filter((phys, region) =>
            !isSummaryRegion(phys.ctx, region)
          )

          val newestMatchingRegions = matchingRegions.filter((phys, region) =>
            overwritingRegions.forall((_,overwritingRegion) =>
              concurrentOrNewerThan(region.timestamp, overwritingRegion.timestamp)
            )
          )

          val result = newestMatchingRegions.map((phys, region) => (phys, region, alignedRead(phys.ctx, readStart, alignment, region)))

          result

        case HeapAddr(reference, size, initialOffset, offset) =>
          // Can possibly read from HeapCtx.Fill, HeapCtx.Heap, and HeapCtx.Dynamic

          val heapRegion = optionStaticMemoryLayout.map(_.heapRange).getOrElse(ApronExpr.topInterval)
          val readStart = ApronExpr.constant(heapRegion, I32Type): ApronExpr[VirtAddr, Type]

          val virtSites = refOps.deref(reference)
          val physSites = for {
            case PhysicalAddress(AddrCtx.ByteMemory(ctx@ByteMemoryCtx.Heap(_, _)), recency) <- virtSites.physicalAddresses
          } yield(PhysicalAddress(ctx, recency))

          val matchingRegions = for {
            case (physAddr@PhysicalAddress(ctx: (ByteMemoryCtx.Fill | ByteMemoryCtx.Heap | ByteMemoryCtx.Dynamic), recency), region) <- mem.store
            if readRangeOverlapsHeapRegion(physSites, initialOffset, PhysicalAddress(ctx, recency), region) != Topped.Actual(false)
          } yield (physAddr, region)

          val overwritingRegions = matchingRegions.filter((phys, region) =>
            !isSummaryRegion(phys.ctx, region)
          )

          val newestMatchingRegions = matchingRegions.filter((phys, region) =>
            overwritingRegions.forall((_, overwritingRegion) =>
              concurrentOrNewerThan(region.timestamp, overwritingRegion.timestamp)
            )
          )

          val result = newestMatchingRegions.map((phys, region) =>
            phys.ctx match
              case _: ByteMemoryCtx.Heap => (phys, region, AlignedRead.Aligned)
              case _ /* :(HeapCtx.Fill | HeapCtx.Dynamic) */ => (phys, region, alignedRead(phys.ctx, readStart, alignment, region))
          )

          result

    private def readRangeOverlapsRegion[Timestamp](
                                                    readStart: ApronExpr[VirtAddr, Type],
                                                    readStartIv: Interval,
                                                    readEnd: ApronExpr[VirtAddr, Type],
                                                    readEndIv: Interval,
                                                    ctx: ByteMemoryCtx,
                                                    region: MemoryRegion[Addr, Size, Timestamp, Value]
                                                  ): Topped[Boolean] =
      val regionStart = region.startAddr.getEffectiveAddress
      val regionEnd = ApronExpr.intAdd(regionStart, region.regionByteSize, regionStart._type)

      (ctx, optionStaticMemoryLayout) match {
        case (ByteMemoryCtx.Static(i), _) =>
          overlaps(readStart,readEnd, regionStart, regionEnd)
        case (ByteMemoryCtx.Global(name), Some(staticMemoryLayout)) =>
          val globalIv = staticMemoryLayout.getGlobalRange(name).get
          if (readEndIv.sup().cmp(globalIv.inf()) <= 0 || globalIv.sup().cmp(readStartIv.inf()) < 0) {
            Topped.Actual(false)
          } else {
            overlaps(readStart,readEnd, regionStart, regionEnd)
          }
        case (_: ByteMemoryCtx.Stack, Some(staticMemoryLayout)) =>
          if (readEndIv.sup().cmp(staticMemoryLayout.stackRange.inf()) <= 0 || staticMemoryLayout.stackRange.sup().cmp(readStartIv.inf()) < 0) {
            Topped.Actual(false)
          } else {
            overlaps(readStart,readEnd, regionStart, regionEnd)
          }
        case (_: (ByteMemoryCtx.Fill | ByteMemoryCtx.Dynamic), _) =>
          overlaps(readStart,readEnd, regionStart, regionEnd)
      }


    private def readRangeOverlapsGlobalRegion[Timestamp](
                                                          readStartsEnds: Map[String, (ApronExpr[VirtAddr, Type],ApronExpr[VirtAddr, Type])],
                                                          ctx: (ByteMemoryCtx.Fill | ByteMemoryCtx.Global | ByteMemoryCtx.Dynamic),
                                                          region: MemoryRegion[Addr, Size, Timestamp, Value]
    ): Topped[Boolean] =
      ctx match {
        case ByteMemoryCtx.Global(name) =>
          Topped.Actual(readStartsEnds.contains(name))
        case _: ByteMemoryCtx.Fill | _: ByteMemoryCtx.Dynamic =>
          val regionStart = region.startAddr.asInstanceOf[NumExpr].expr
          if(readStartsEnds.forall {
            case (_,(readStart,readEnd)) =>
              overlaps(
                readStart = readStart,
                readEnd = readEnd,
                regionStart = regionStart,
                regionEnd = ApronExpr.intAdd(regionStart, region.regionByteSize, regionStart._type)
              ) == Topped.Actual(false)
          }) {
            Topped.Actual(false)
          } else {
            Topped.Top
          }
      }

    private def readRangeOverlapsStackRegion[Timestamp](
                                                         frames: Powerset[FuncId],
                                                         initialOffset: Powerset[Int],
                                                         readStart: ApronExpr[VirtAddr, Type],
                                                         readEnd: ApronExpr[VirtAddr, Type],
                                                         ctx: (ByteMemoryCtx.Fill | ByteMemoryCtx.Stack | ByteMemoryCtx.Dynamic),
                                                         region: MemoryRegion[Addr, Size, Timestamp, Value]
    ): Topped[Boolean] =
      ctx match {
        case ByteMemoryCtx.Stack(func, _) =>
          if (!frames.set.contains(func))
            Topped.Actual(false)
          else
            region.startAddr match
              case stackAddress: StackAddr =>
                if(initialOffset.intersect(stackAddress.initialOffset).isEmpty)
                  Topped.Actual(false)
                else
                  Topped.Top
              case _ => Topped.Top
        case _: ByteMemoryCtx.Fill | _: ByteMemoryCtx.Dynamic =>
          val regionStart = region.startAddr.asInstanceOf[NumExpr].expr
          overlaps(
            readStart = readStart,
            readEnd = readEnd,
            regionStart = regionStart,
            regionEnd = ApronExpr.intAdd(regionStart, region.regionByteSize, regionStart._type)
          )
      }

    private def readRangeOverlapsHeapRegion[Timestamp](
                                                        sites: Set[PhysicalAddress[ByteMemoryCtx.Heap]],
                                                        initialOffset: Powerset[Int],
                                                        physAddr: PhysicalAddress[ByteMemoryCtx.Fill | ByteMemoryCtx.Heap | ByteMemoryCtx.Dynamic],
                                                        region: MemoryRegion[Addr, Size, Timestamp, Value]
    ): Topped[Boolean] =
      physAddr.ctx match {
        case _: ByteMemoryCtx.Heap =>
          if(sites.contains(physAddr.asInstanceOf)) {
            region.startAddr match {
              case heapAddr:HeapAddr =>
                if(initialOffset.intersect(heapAddr.initialOffset).isEmpty)
                  Topped.Actual(false)
                else
                  Topped.Top
              case _ => Topped.Top
            }
          } else
            Topped.Actual(false)
        case _: ByteMemoryCtx.Fill | _: ByteMemoryCtx.Dynamic => Topped.Top
      }

    private inline def overlaps(readStart: ApronExpr[VirtAddr,Type], readEnd: ApronExpr[VirtAddr,Type], regionStart: ApronExpr[VirtAddr,Type], regionEnd: ApronExpr[VirtAddr,Type])(using apronState: ApronState[VirtAddr,Type]): Topped[Boolean] =
      apronState.assert(ApronBool.And(
        ApronBool.Constraint(ApronCons.lt(readStart, regionEnd)),
        ApronBool.Constraint(ApronCons.lt(regionStart, readEnd))
      ))

    override def computeStartAddrAndSize(ctx: ByteMemoryCtx, startAddr: Addr, byteSize: Int): (Addr, Size) =
      val default = (startAddr, ApronExpr.lit(byteSize, I32Type): Size)
      optionStaticMemoryLayout match
        case Some(staticMemoryLayout) =>
          (ctx,startAddr) match
            case (ByteMemoryCtx.Global(name),_) =>
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
            case (_, stackAddr: StackAddr) =>
              (
                stackAddr.copy(otherOffset = ApronExpr.lit(0, I32Type)),
                apronState.toNonRelational(ApronExpr.intAdd(stackAddr.otherOffset, ApronExpr.lit(byteSize, I32Type), I32Type))
              )
            case (_, heapAddr: HeapAddr) =>
              (
                heapAddr.copy(otherOffset = ApronExpr.lit(0, I32Type)),
                apronState.toNonRelational(ApronExpr.intAdd(heapAddr.otherOffset, ApronExpr.lit(byteSize, I32Type), I32Type))
              )
            case _ => default
        case None => default


    override def isSummaryRegion[Timestamp: PartialOrder](ctx: ByteMemoryCtx, region: MemoryRegion[Addr, Size, Timestamp, Value]): Boolean =
      ctx match {
        case _: ByteMemoryCtx.Fill | _: ByteMemoryCtx.Dynamic => true
        case _: ByteMemoryCtx.Static | _: ByteMemoryCtx.Stack | _: ByteMemoryCtx.Global =>
          region.elementByteSize match
            case Topped.Top => true
            case Topped.Actual(byteSize) =>
              apronState.assert(ApronCons.eq(ApronExpr.lit(byteSize, I32Type), region.regionByteSize)) != Topped.Actual(true)
        case _: ByteMemoryCtx.Heap => false // TODO: We do not support malloc-allocated arrays for now.
      }

    private def alignedRead[Timestamp,Val](ctx: ByteMemoryCtx, readStart: ApronExpr[VirtAddr, Type], alignment: Int, memoryRegion: MemoryRegion[Addr, Size, Timestamp, Val]): AlignedRead =
//      if(memoryRegion.alignment.set.forall(_ == alignment))
//        AlignedRead.Aligned
//      else if(memoryRegion.alignment.set.head == 0)
//        AlignedRead.Aligned
//      else
        val alignment = ApronExpr.lit[VirtAddr,Type](scala.math.pow(2,memoryRegion.alignment.set.head).toInt, I32Type)
        // aligned if readStart ≡ regionStart (mod 2^memoryRegion.alignment)
        val res = apronState.assert(
          ApronBool.And(
            ApronBool.Constraint(ApronCons.eq(ApronExpr.intMod(readStart, alignment, I32Type), ApronExpr.lit(0, I32Type))),
            ApronBool.Constraint(ApronCons.eq(ApronExpr.intMod(memoryRegion.startAddr.asNumExpr, alignment, I32Type), ApronExpr.lit(0, I32Type)))
          )) match
          case Topped.Actual(true) => AlignedRead.Aligned
          case _ => AlignedRead.MaybeAligned
        res

    private inline def concurrentOrNewerThan[Timestamp: PartialOrder](timestamp1: Timestamp, timestamp2: Timestamp): Boolean =
      PartialOrder[Timestamp].lteq(timestamp2, timestamp1) ||
      !PartialOrder[Timestamp].lteq(timestamp1, timestamp2)

  given CombineAddr[W <: Widening](using combineI32: Combine[I32, W]): Combine[Addr, W] with
    def apply(v1: Addr, v2: Addr): MaybeChanged[Addr] = combineI32(v1, v2).map(_.asInstanceOf[Addr])

  class HeapAlloc(rootFrameData: FrameData)(using apronState: ApronState[VirtAddr, Type], domLogger: DomLogger[FixIn], refOps: ReferenceOps[PowVirtAddr, AbstractReference[PowVirtAddr]], globals: DecidableSymbolTable[Unit, generic.GlobalAddr, Value])
    extends Allocator[IterableOnce[ByteMemoryCtx], (ByteMemoryAllocationContext,MemoryAddr,Addr)] with Stateless:
    private var contextLog: Map[FixIn, Set[ByteMemoryCtx]] = Map()

    override def alloc(args: (ByteMemoryAllocationContext, MemoryAddr, Addr)): IterableOnce[ByteMemoryCtx] =
      val loc = domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))
      val ctxs = alloc0(args._1, args._2, args._3, loc)
      contextLog += loc -> (contextLog.getOrElse(loc, Set()) ++ ctxs.toSet)
      ctxs


    private def alloc0(byteMemoryAllocationContext: ByteMemoryAllocationContext, memoryAddr: MemoryAddr, addr: Addr, loc: FixIn): Iterable[ByteMemoryCtx] = {
      addr match
        case NumExpr(effectiveAddr) =>
          val (l, u) = apronState.getIntInterval(effectiveAddr)
          val defaultAddr =
            if (l == u)
              ByteMemoryCtx.Static(u)
            else
              ByteMemoryCtx.Dynamic(loc)

          (byteMemoryAllocationContext, effectiveAddr, optionStaticMemoryLayout) match {
            case (ByteMemoryAllocationContext.Fill, _, _) =>
              Iterable(ByteMemoryCtx.Fill(loc))
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

        case GlobalAddr(glob, offset) =>
          for {
            (name,_) <- glob.set
          } yield(ByteMemoryCtx.Global(name))

        case StackAddr(functions, frameSize, stackPointer, initialOffset, _) =>
          for {
            fun <- functions.set
            offset <- if(initialOffset.isEmpty) Iterator(0) else initialOffset.set.iterator
          } yield ByteMemoryCtx.Stack(fun, offset)

        case HeapAddr(reference, _, initialOffset, _) =>
          val sites = refOps.deref(reference)
          sites.addrs.keysIterator.flatMap {
            case AddrCtx.ByteMemory(heapCtx: ByteMemoryCtx.Heap) =>
              for {
                offset <- if(initialOffset.isEmpty) Iterator(0) else initialOffset.set.iterator
              } yield heapCtx.copy(offset = offset)
            case ctx => throw IllegalArgumentException(s"Expected HeapCtx, but got ${ctx}")
          }.toSet
    }

    private def getHeapCtx(staticMemoryLayout: StaticMemoryLayout, dom: FixIn, effectiveAddr: ApronExpr[VirtAddr,Type], baseAddr: ApronExpr[VirtAddr, Type], offset: Interval): Option[ByteMemoryCtx] = {

      val effectiveAddrIv = apronState.getInterval(effectiveAddr)
      val offsetMatchesGlobal = for {
        (globalName: String, globalRange: Interval, cType: CType) <- staticMemoryLayout.globalRanges
        if offset.isLeq(globalRange) || effectiveAddrIv.isLeq(globalRange)
      } yield ByteMemoryCtx.Global(globalName)

      if(offsetMatchesGlobal.size == 1) {
        offsetMatchesGlobal.headOption

      } else {

        val constantMatchesGlobal = for {
          const <- baseAddr.constants
          (globalName: String, globalRange: Interval, cType: CType) <- staticMemoryLayout.globalRanges
          if const.cmp(globalRange) == 0 || const.cmp(globalRange) == 1 // if const is equal or included in globalRange
        } yield ByteMemoryCtx.Global(globalName)

        if(constantMatchesGlobal.size == 1) {
          constantMatchesGlobal.headOption

        } else {

          val addrMatchesGlobal = for {
            addr <- baseAddr.addrs
            iv = apronState.getInterval(ApronExpr.addr(addr, I32Type))
            (globalName, globalRange, cType) <- staticMemoryLayout.globalRanges
            if iv.isLeq(globalRange)
          } yield ByteMemoryCtx.Global(globalName)
          if(addrMatchesGlobal.size == 1) {
            addrMatchesGlobal.headOption
          } else {

            if(effectiveAddrIv.isLeq(staticMemoryLayout.stackRange)) {
              for{
                case FixIn.Eval(_, InstLoc.InFunction(func, _)) <- Some(dom)
              } yield ByteMemoryCtx.Stack(func, addScalarAdditionOperands(effectiveAddr))
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

  private def getHeapOffset(offset: ApronExpr[VirtAddr,Type])(using apronState: ApronState[VirtAddr,Type]): Topped[Int] =
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
