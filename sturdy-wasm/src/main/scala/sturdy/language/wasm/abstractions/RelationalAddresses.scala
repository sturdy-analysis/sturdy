package sturdy.language.wasm.abstractions

import sturdy.apron.{ApronExpr, ApronState, ApronVar}
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
    case Stack(stackPosition: Int, programPosition: FixIn, function: FrameData)
    case Global(addr: Int)
    case DynamicHeapAddress(memoryAddr: MemoryAddr, storeInstruction: FixIn)
    case StaticHeapAddress(memoryAddr: MemoryAddr, offset: Int)
    case Alloc(allocSite: FixIn)
    case Temp(programPosition: FixIn, tpe: Type)

    override def toString: String =
      this match
        case CallFrame(callFramePosition, Some(programPos), function) => s"L$callFramePosition@$function:$programPos"
        case CallFrame(callFramePosition, None, function) => s"L$callFramePosition@$function"
        case Stack(stackPosition, programPosition, function) => s"S$stackPosition@$function:$programPosition"
        case DynamicHeapAddress(memoryAddr, storeInstruction) => s"D$storeInstruction@$memoryAddr"
        case StaticHeapAddress(memoryAddr, offset) => s"ST$offset@$memoryAddr"
        case Alloc(FixIn.Eval(_,allocSite)) => s"Alloc@${allocSite}"
        case Global(addr) => s"G$addr"
        case Temp(programPosition, tpe) => s"T$programPosition:$tpe"

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
        AddrCtx.StaticHeapAddress(key, u)
      else
        addr match
          case ApronExpr.Addr(ApronVar(VirtualAddress(alloc@AddrCtx.Alloc(site), _, _)), _, _) => alloc

//        domLogger.currentDom match
//          case Some(fixIn) => AddrCtx.Heap(key, fixIn)
//          case None =>
//
//            else
//              throw IllegalArgumentException(s"Expected constant address but got $addr")
  )

  given Ordering[AddrCtx] = {
    case (AddrCtx.CallFrame(callFramePos1, progPos1, data1), AddrCtx.CallFrame(callFramePos2, progPos2, data2)) => Ordering[(FrameData, Option[FixIn], Int)].compare((data1, progPos1, callFramePos1), (data2, progPos2, callFramePos2))
    case (AddrCtx.Stack(stackPos1, programPos1, data1), AddrCtx.Stack(stackPos2, programPos2, data2)) => Ordering[(FrameData, FixIn, Int)].compare((data1, programPos1, stackPos1),(data2, programPos2, stackPos2))
    case (AddrCtx.DynamicHeapAddress(memAddr1, storeInst1), AddrCtx.DynamicHeapAddress(memAddr2, storeInst2)) => Ordering[(MemoryAddr, FixIn)].compare((memAddr1, storeInst1),(memAddr2, storeInst2))
    case (AddrCtx.StaticHeapAddress(memAddr1, offset1), AddrCtx.StaticHeapAddress(memAddr2, offset2)) => Ordering[(MemoryAddr, Int)].compare((memAddr1, offset1),(memAddr2, offset2))
    case (AddrCtx.Alloc(site1), AddrCtx.Alloc(site2)) => Ordering[FixIn].compare(site1, site2)
    case (AddrCtx.Global(idx1), AddrCtx.Global(idx2)) => Ordering[Int].compare(idx1, idx2)
    case (AddrCtx.Temp(programPos1, tpe1), AddrCtx.Temp(programPos2, tpe2)) => Ordering[(FixIn,Type)].compare((programPos1, tpe1), (programPos2, tpe2))
    case (ctx1, ctx2) => Ordering.by[AddrCtx, Int]{
      case _: AddrCtx.CallFrame => 1
      case _: AddrCtx.Stack => 2
      case _: AddrCtx.DynamicHeapAddress => 3
      case _: AddrCtx.StaticHeapAddress => 4
      case _: AddrCtx.Alloc => 5
      case _: AddrCtx.Global => 6
      case _: AddrCtx.Temp => 7
    }.compare(ctx1, ctx2)
  }
  given Finite[AddrCtx] with {}
  
  given Ordering[VirtAddr] = VirtualAddressOrdering


