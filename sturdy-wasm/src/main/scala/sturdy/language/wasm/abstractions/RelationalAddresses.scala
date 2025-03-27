package sturdy.language.wasm.abstractions

import sturdy.language.wasm.generic.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.fix.DomLogger
import sturdy.values.Finite
import sturdy.values.references.{*, given}

trait RelationalAddresses extends RelationalTypes:
  enum AddrCtx:
    case CallFrame(callFramePosition: Int, frame: FrameData)
    case Stack(stackPosition: Int, programPosition: FixIn, frame: FrameData)
    case Global(addr: Int)
    case Temp(programPosition: FixIn, tpe: Type)

  final type VirtAddr = VirtualAddress[AddrCtx]
  final type PhysAddr = PhysicalAddress[AddrCtx]
  final type PowVirtAddr = PowVirtualAddress[AddrCtx]
  final type PowPhysAddr = PowersetAddr[PhysAddr, PhysAddr]

  def tempRelationalAlloc(rootFrameData: FrameData)(using domLogger: DomLogger[FixIn]): AAllocatorFromContext[Type, AddrCtx] = AAllocatorFromContext(
    tpe =>
      AddrCtx.Temp(domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module)), tpe)
  )
  given localRelationaAlloc: AAllocatorFromContext[(Int, FrameData, Option[InstLoc]), AddrCtx] = AAllocatorFromContext(
    (i, data, _) => AddrCtx.CallFrame(i, data)
  )

  given Ordering[AddrCtx] = {
    case (AddrCtx.CallFrame(callFramePos1, data1), AddrCtx.CallFrame(callFramePos2, data2)) => Ordering[(FrameData, Int)].compare((data1, callFramePos1), (data2, callFramePos2))
    case (AddrCtx.Stack(stackPos1, programPos1, data1), AddrCtx.Stack(stackPos2, programPos2, data2)) => Ordering[(FrameData, FixIn, Int)].compare((data1, programPos1, stackPos1),(data2, programPos2, stackPos2))
    case (AddrCtx.Global(idx1), AddrCtx.Global(idx2)) => Ordering[Int].compare(idx1, idx2)
    case (AddrCtx.Temp(programPos1, tpe1), AddrCtx.Temp(programPos2, tpe2)) => Ordering[(FixIn,Type)].compare((programPos1, tpe1), (programPos2, tpe2))
    case (ctx1, ctx2) => Ordering.by[AddrCtx, Int]{
      case _: AddrCtx.CallFrame => 1
      case _: AddrCtx.Stack => 2
      case _: AddrCtx.Global => 3
      case _: AddrCtx.Temp => 4
    }.compare(ctx1, ctx2)

  }
//  Ordering.by[AddrCtx, Either[(Int,FrameData), Either[(Int,FixIn,FrameData),Either[Int, (FixIn,Type)]]]] {
//    case AddrCtx.CallFrame(callFramePos, data) => Left(callFramePos, data)
//    case AddrCtx.Stack(stackPos, programPos, data) => Right(Left(stackPos, programPos, data))
//    case AddrCtx.Global(idx) => Right(Right(Left(idx)))
//    case AddrCtx.Temp(fixin, tpe) => Right(Right(Right(fixin, tpe)))
//  }
  given Finite[AddrCtx] with {}
  
  given Ordering[VirtAddr] = VirtualAddressOrdering


