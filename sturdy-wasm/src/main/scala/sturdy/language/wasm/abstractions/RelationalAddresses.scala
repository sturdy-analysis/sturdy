package sturdy.language.wasm.abstractions

import sturdy.apron.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.CallFrame
import sturdy.fix.DomLogger
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

trait RelationalAddresses extends RelationalTypes:
  enum AddrCtx:
    case CallFrame(callFramePosition: Int, programPos: Option[FixIn], function: FrameData)
    case Global(addr: Int | String)
    case Stack(stackPosition: Int, programPosition: FixIn, function: FrameData)
    case ByteMemory(ctx: ByteMemoryCtx)
    case Temp(programPosition: FixIn, tpe: Type)

    override def toString: String =
      this match
        case CallFrame(callFramePosition, Some(fixin), function) => s"L$callFramePosition@${fixin.instLoc}"
        case CallFrame(callFramePosition, None, function) => s"L$callFramePosition@$function"
        case Global(addr) => s"G$addr"
        case Stack(stackPosition, programPosition, function) => s"S$stackPosition@$function:$programPosition"
        case ByteMemory(ctx) => ctx.toString
        case Temp(programPosition, tpe) => s"T$programPosition:$tpe"

  enum ByteMemoryCtx:
    case Fill(site: FixIn)
    case Global(name: String)
    case Stack(function: FuncId, offset: Int)
    case Heap(allocSite: InstLoc, offset: Int)
    case Static(offset: Int)
    case Dynamic(storeInstruction: FixIn)

    override def toString: String =
      this match
        case Fill(site) => s"Fill@$site"
        case Global(name) => s"G$name"
        case Stack(fun,offset) => s"S$fun+$offset"
        case Heap(allocSite, offset) => s"H${allocSite}+${offset}"
        case Static(offset) => s"$offset"
        case Dynamic(storeInstruction) => s"Dynamic@$storeInstruction"


  final type VirtAddr = VirtualAddress[AddrCtx]
  final type PhysAddr = PhysicalAddress[AddrCtx]
  final type PowVirtAddr = PowVirtualAddress[AddrCtx]
  final type PowPhysAddr = PowersetAddr[PhysAddr, PhysAddr]

  def tempRelationalAlloc(rootFrameData: FrameData)(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[Type, AddrCtx] = AAllocatorFromContext {
    tpe => 
      val res = AddrCtx.Temp(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), tpe)
      res
  }

  def combineExprAlloc(rootFrameData: FrameData)(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[(ApronExpr[VirtAddr,Type], ApronExpr[VirtAddr,Type]), AddrCtx] = AAllocatorFromContext {
    case (ApronExpr.Addr(ApronVar(VirtualAddress(ctx: AddrCtx.Temp, _, _)), _, _),_) => ctx
    case (_, ApronExpr.Addr(ApronVar(VirtualAddress(ctx: AddrCtx.Temp, _, _)), _, _)) => ctx
    case (e1, e2) =>
      val tpe = Join(e1._type, e2._type).get
      AddrCtx.Temp(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), tpe)
  }

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

  given Ordering[AddrCtx] = {
    case (AddrCtx.CallFrame(callFramePos1, progPos1, data1), AddrCtx.CallFrame(callFramePos2, progPos2, data2)) => Ordering[(FrameData, Option[FixIn], Int)].compare((data1, progPos1, callFramePos1), (data2, progPos2, callFramePos2))
    case (AddrCtx.Global(idx1:Int), AddrCtx.Global(idx2:Int)) => Ordering[Int].compare(idx1, idx2)
    case (AddrCtx.Global(idx1:String), AddrCtx.Global(idx2:String)) => Ordering[String].compare(idx1, idx2)
    case (AddrCtx.Global(idx1), AddrCtx.Global(idx2)) => Ordering.by[String | Int, Int]{
        case _: Int => 1
        case _: String => 2
      }.compare(idx1, idx2)
    case (AddrCtx.Stack(stackPos1, programPos1, data1), AddrCtx.Stack(stackPos2, programPos2, data2)) => Ordering[(FrameData, FixIn, Int)].compare((data1, programPos1, stackPos1),(data2, programPos2, stackPos2))
    case (AddrCtx.Temp(programPos1, tpe1), AddrCtx.Temp(programPos2, tpe2)) => Ordering[(FixIn,Type)].compare((programPos1, tpe1), (programPos2, tpe2))
    case (ctx1, ctx2) => Ordering.by[AddrCtx, Int]{
      case _: AddrCtx.CallFrame => 1
      case _: AddrCtx.Global => 2
      case _: AddrCtx.Stack => 3
      case _: AddrCtx.ByteMemory => 4
      case _: AddrCtx.Temp => 5
    }.compare(ctx1, ctx2)
  }
  given Finite[AddrCtx] with {}

  given Ordering[ByteMemoryCtx] = {
    case (ByteMemoryCtx.Fill(site1), ByteMemoryCtx.Fill(site2)) => Ordering[FixIn].compare(site1, site2)
    case (ByteMemoryCtx.Static(offset1), ByteMemoryCtx.Static(offset2)) => Ordering[Int].compare(offset1, offset2)
    case (ByteMemoryCtx.Global(name1), ByteMemoryCtx.Global(name2)) => Ordering[String].compare(name1, name2)
    case (ByteMemoryCtx.Stack(fun1,offset1), ByteMemoryCtx.Stack(fun2,offset2)) => Ordering[(FuncId,Int)].compare((fun1,offset1), (fun2,offset2))
    case (ByteMemoryCtx.Heap(site1, offset1), ByteMemoryCtx.Heap(site2, offset2)) => Ordering[(InstLoc,Int)].compare((site1, offset1), (site2, offset2))
    case (ByteMemoryCtx.Dynamic(storeInst1), ByteMemoryCtx.Dynamic(storeInst2)) => Ordering[FixIn].compare(storeInst1, storeInst2)
    case (ctx1, ctx2) => Ordering.by[ByteMemoryCtx, Int] {
      case _: ByteMemoryCtx.Fill => 1
      case _: ByteMemoryCtx.Global => 2
      case _: ByteMemoryCtx.Stack => 3
      case _: ByteMemoryCtx.Heap => 4
      case _: ByteMemoryCtx.Static => 5
      case _: ByteMemoryCtx.Dynamic => 6
    }.compare(ctx1, ctx2)
  }
  given Finite[ByteMemoryCtx] with {}

  given Ordering[VirtAddr] = VirtualAddressOrdering