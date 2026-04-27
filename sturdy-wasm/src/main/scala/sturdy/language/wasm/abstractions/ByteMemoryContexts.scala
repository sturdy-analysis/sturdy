package sturdy.language.wasm.abstractions

import sturdy.language.wasm.generic.{FixIn, FuncId, InstLoc, given}
import sturdy.values.{*, given}

trait ByteMemoryContexts {
  enum ByteMemoryCtx:
    case Fill(site: FixIn)
    case Global(name: String, offset: Long)
    case Stack(function: FuncId, offset: Int)
    case Heap(allocSite: InstLoc, offset: Int)
    case Dynamic(storeInstruction: FixIn)

    override def toString: String =
      this match
        case Fill(site) => s"Fill@$site"
        case Global(name,offset) => s"G$name@$offset"
        case Stack(fun,offset) => s"S$fun+$offset"
        case Heap(allocSite, offset) => s"H${allocSite}+${offset}"
        case Dynamic(storeInstruction) => s"Dynamic@$storeInstruction"

  given Ordering[ByteMemoryCtx] = {
    case (ByteMemoryCtx.Fill(site1), ByteMemoryCtx.Fill(site2)) => Ordering[FixIn].compare(site1, site2)
    case (ByteMemoryCtx.Global(_name1, offset1), ByteMemoryCtx.Global(_name2, offset2)) => Ordering[Long].compare(offset1, offset2)
    case (ByteMemoryCtx.Stack(fun1,offset1), ByteMemoryCtx.Stack(fun2,offset2)) => Ordering[(FuncId,Int)].compare((fun1,offset1), (fun2,offset2))
    case (ByteMemoryCtx.Heap(site1, offset1), ByteMemoryCtx.Heap(site2, offset2)) => Ordering[(InstLoc,Int)].compare((site1, offset1), (site2, offset2))
    case (ByteMemoryCtx.Dynamic(storeInst1), ByteMemoryCtx.Dynamic(storeInst2)) => Ordering[FixIn].compare(storeInst1, storeInst2)
    case (ctx1, ctx2) => Ordering.by[ByteMemoryCtx, Int] {
      case _: ByteMemoryCtx.Fill => 1
      case _: ByteMemoryCtx.Global => 2
      case _: ByteMemoryCtx.Stack => 3
      case _: ByteMemoryCtx.Heap => 4
      case _: ByteMemoryCtx.Dynamic => 5
    }.compare(ctx1, ctx2)
  }
  given Finite[ByteMemoryCtx] with {}
}
