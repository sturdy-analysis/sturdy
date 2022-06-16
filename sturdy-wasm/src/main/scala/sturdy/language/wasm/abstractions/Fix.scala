package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.ObservableJoin
import sturdy.effect.callframe.CallFrame
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{FuncId, FixIn, FixOut, FrameData, InstLoc}
import sturdy.values.Finite
import sturdy.values.{Combine, MaybeChanged, Widening, Unchanged}
import swam.FuncIdx
import swam.syntax.{Loop, CallIndirect, If, Inst, Block, Call}

object Fix:
  final def isFunOrLoop(dom: FixIn): Boolean = isFunOrLoopToIndex(dom) > -1

  final def isFunOrLoopToIndex(dom: FixIn): Int = dom match
    case _: FixIn.EnterWasmFunction => 0
    case FixIn.Eval(_: Loop, _) | FixIn.MostGeneralClientLoop(_) => 1
    case _ => -1

  final def callSitesLogger() = fix.context.callSites[FixIn, (Call | CallIndirect, InstLoc)] {
    case FixIn.Eval(c: Call, loc) => Some((c, loc))
    case FixIn.Eval(c: CallIndirect, loc) => Some((c, loc))
    case _ => None
  }
  final def previousCallSitesLogger(k: Int) = fix.context.previousCallSites[FixIn, (Call | CallIndirect, InstLoc)](k) {
    case FixIn.Eval(c: Call, loc) => Some((c, loc))
    case FixIn.Eval(c: CallIndirect, loc) => Some((c, loc))
    case _ => None
  }
  type CallString = fix.context.CallString[(Call | CallIndirect, InstLoc)]
  given Finite[CallString] = fix.context.FiniteCallString

  given CombineFixOut[V, W <: Widening] (using w: Combine[V, W]): Combine[FixOut[V], W] with
    override def apply(out1: FixOut[V], out2: FixOut[V]): MaybeChanged[FixOut[V]] = (out1, out2) match
      case (FixOut.Eval(), FixOut.Eval()) => Unchanged(FixOut.Eval())
      case (FixOut.ExitWasmFunction(vs1), FixOut.ExitWasmFunction(vs2)) => Combine[List[V], W](vs1, vs2).map(FixOut.ExitWasmFunction.apply)
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")
