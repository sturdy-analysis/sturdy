package sturdy.language.wasm.abstractions

import sturdy.language.wasm.generic.{*, given}
import sturdy.data.{*, given}
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.fix.DomLogger
import sturdy.values.Finite
import sturdy.values.references.{*, given}

trait RelationalAddresses extends RelationalTypes:
  enum AddrCtx:
    case CallFrame(callFrameAddr: Int, frame: FrameData)
    case Stack(stackAddr: Int, fixIn: FixIn, frame: FrameData)
    case Temp(fixin: FixIn, tpe: Type)

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

  given Ordering[AddrCtx] = Ordering.by[AddrCtx, Either[(Int,FrameData), Either[(Int,FixIn,FrameData),(FixIn,Type)]]] {
    case AddrCtx.CallFrame(callFrameAddr, data) => Left(callFrameAddr, data)
    case AddrCtx.Stack(stackAddr, fixin, data) => Right(Left(stackAddr, fixin, data))
    case AddrCtx.Temp(fixin, tpe) => Right(Right(fixin, tpe))
  }
  given Finite[AddrCtx] with {}
  
  given Ordering[VirtAddr] = VirtualAddressOrdering


