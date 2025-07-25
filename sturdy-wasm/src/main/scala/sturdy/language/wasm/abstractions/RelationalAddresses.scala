package sturdy.language.wasm.abstractions

import sturdy.apron.{ApronExpr, ApronState, ApronVar, BinOp}
import sturdy.language.wasm.generic.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.CallFrame
import sturdy.fix.DomLogger
import sturdy.values.Finite
import sturdy.values.references.{*, given}

trait RelationalAddresses extends RelationalTypes:
  enum AddrCtx:
    case CallFrame(callFramePosition: Int, programPos: Option[FixIn], function: FrameData)
    case Global(addr: Int)
    case Stack(stackPosition: Int, programPosition: FixIn, function: FrameData)
    case Heap(ctx: HeapCtx)
    case Temp(programPosition: FixIn, tpe: Type)

    override def toString: String =
      this match
        case CallFrame(callFramePosition, Some(programPos), function) => s"L$callFramePosition@$function:$programPos"
        case CallFrame(callFramePosition, None, function) => s"L$callFramePosition@$function"
        case Global(addr) => s"G$addr"
        case Stack(stackPosition, programPosition, function) => s"S$stackPosition@$function:$programPosition"
        case Heap(ctx) => ctx.toString
        case Temp(programPosition, tpe) => s"T$programPosition:$tpe"

  enum HeapCtx:
    case Dynamic(storeInstruction: FixIn)
    case Static(offset: Int)
    case Alloc(allocSite: FixIn, offset: Int)

    override def toString: String =
      this match
        case Dynamic(storeInstruction) => s"$storeInstruction"
        case Static(offset) => s"$offset"
        case Alloc(FixIn.Eval(_,allocSite), offset) => s"Alloc@${allocSite}+${offset}"


  final type VirtAddr = VirtualAddress[AddrCtx]
  final type PhysAddr = PhysicalAddress[AddrCtx]
  final type PowVirtAddr = PowVirtualAddress[AddrCtx]
  final type PowPhysAddr = PowersetAddr[PhysAddr, PhysAddr]

  def tempRelationalAlloc(rootFrameData: FrameData)(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[Type, AddrCtx] = AAllocatorFromContext(
    tpe =>
      AddrCtx.Temp(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), tpe)
  )
  def localAlloc(ssa: Boolean, rootFrameData: FrameData)(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[(Int, FrameData, Option[InstLoc]), AddrCtx] = AAllocatorFromContext(
    (i, data, _) =>
      if ssa then
        AddrCtx.CallFrame(i, Some(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))), data)
      else
        AddrCtx.CallFrame(i, None, data)
  )

  def stackAlloc[Var,Val,Site, J[_]<: MayJoin[_]](rootFrameData: FrameData, callFrame: CallFrame[FrameData, Var, Val, Site, J])(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[(Int,Type), AddrCtx] = new AAllocatorFromContext(
    (idx: Int, tpe: Type) =>
      val fixIn = domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))
      AddrCtx.Stack(idx, fixIn, callFrame.data)
  )

  def heapAlloc[Bytes](rootFrameData: FrameData)(using apronState: ApronState[VirtAddr, Type], domLogger: DomLogger[FixIn]): AAllocatorFromContext[(MemoryAddr,ApronExpr[VirtAddr, Type],Bytes), AddrCtx] = AAllocatorFromContext(
    (key, addr, _) =>
      // Static string initialized at module level
      val (l,u) = apronState.getIntInterval(addr)
      if(l == u)
        AddrCtx.Heap(HeapCtx.Static(u))
      else
        addr match
          case ApronExpr.Addr(ApronVar(VirtualAddress(alloc@AddrCtx.Heap(_: HeapCtx.Alloc), _, _)), _, _) => alloc
          case _ => AddrCtx.Heap(HeapCtx.Dynamic(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))))
  )

  given Ordering[AddrCtx] = {
    case (AddrCtx.CallFrame(callFramePos1, progPos1, data1), AddrCtx.CallFrame(callFramePos2, progPos2, data2)) => Ordering[(FrameData, Option[FixIn], Int)].compare((data1, progPos1, callFramePos1), (data2, progPos2, callFramePos2))
    case (AddrCtx.Global(idx1), AddrCtx.Global(idx2)) => Ordering[Int].compare(idx1, idx2)
    case (AddrCtx.Stack(stackPos1, programPos1, data1), AddrCtx.Stack(stackPos2, programPos2, data2)) => Ordering[(FrameData, FixIn, Int)].compare((data1, programPos1, stackPos1),(data2, programPos2, stackPos2))
    case (AddrCtx.Temp(programPos1, tpe1), AddrCtx.Temp(programPos2, tpe2)) => Ordering[(FixIn,Type)].compare((programPos1, tpe1), (programPos2, tpe2))
    case (ctx1, ctx2) => Ordering.by[AddrCtx, Int]{
      case _: AddrCtx.CallFrame => 1
      case _: AddrCtx.Global => 2
      case _: AddrCtx.Stack => 3
      case _: AddrCtx.Heap => 4
      case _: AddrCtx.Temp => 5
    }.compare(ctx1, ctx2)
  }
  given Finite[AddrCtx] with {}

  given Ordering[HeapCtx] = {
    case (HeapCtx.Dynamic(storeInst1), HeapCtx.Dynamic(storeInst2)) => Ordering[FixIn].compare(storeInst1, storeInst2)
    case (HeapCtx.Static(offset1), HeapCtx.Static(offset2)) => Ordering[Int].compare(offset1, offset2)
    case (HeapCtx.Alloc(site1, offset1), HeapCtx.Alloc(site2, offset2)) => Ordering[(FixIn, Int)].compare((site1, offset1), (site2, offset2))
    case (ctx1, ctx2) => Ordering.by[HeapCtx, Int] {
      case _: HeapCtx.Static => 1
      case _: HeapCtx.Alloc => 2
      case _: HeapCtx.Dynamic => 3
    }.compare(ctx1, ctx2)
  }
  given Finite[HeapCtx] with {}

  given Ordering[VirtAddr] = VirtualAddressOrdering