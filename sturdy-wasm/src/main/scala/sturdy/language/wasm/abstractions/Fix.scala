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

trait Fix extends Interpreter:
  final def isFunOrWhile(dom: FixIn[Value]): Boolean = dom match
    case _: FixIn.EnterWasmFunction[Value] => true
    case FixIn.Eval(_: Loop, _) => true
    case _ => false

  final def casesFunOrWhile(dom: FixIn[Value]): Int = dom match
    case _: FixIn.EnterWasmFunction[Value] => 0
    case FixIn.Eval(_: Loop, _) => 1
    case _ => -1


  final def callSitesLogger() = fix.context.callSites[FixIn[Value], Call | CallIndirect] {
    case FixIn.Eval(c: Call, _) => Some(c)
    case FixIn.Eval(c: CallIndirect, _) => Some(c)
    case _ => None
  }
  type CallString = fix.context.CallString[Call | CallIndirect]
  given Finite[CallString] = fix.context.FiniteCallString

  given CombineFixOut[W <: Widening] (using w: Combine[Value, W]): Combine[FixOut[Value], W] with
    override def apply(out1: FixOut[Value], out2: FixOut[Value]): MaybeChanged[FixOut[Value]] = (out1, out2) match
      case (FixOut.Eval(), FixOut.Eval()) => Unchanged(FixOut.Eval())
      case (FixOut.ExitWasmFunction(vs1), FixOut.ExitWasmFunction(vs2)) => Combine[List[Value], W](vs1, vs2).map(FixOut.ExitWasmFunction.apply)
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")
