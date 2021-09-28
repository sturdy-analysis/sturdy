package sturdy.language.wasm.analyses

import sturdy.data.CombineEquiList
import sturdy.effect.callframe.CallFrame
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData}
import sturdy.values.{Combine, Widening}
import swam.syntax.{Loop, Call, Inst, CallIndirect}

object Fix:
  final def isFunOrWhile[V](dom: FixIn[V]): Boolean = dom match
    case _: FixIn.EnterWasmFunction[V] => true
    case FixIn.Eval(_: Loop) => true
    case _ => false

  final def frameSensitive[V](using frame: CallFrame[FrameData[V], _, _]): Sensitivity[FixIn[V], FrameData[V]] = new Sensitivity {
    override def emptyContext: FrameData[V] = FrameData.empty
    override def switchCall(dom: FixIn[V]): Boolean = dom match
      case _: FixIn.EnterWasmFunction[V] => true  // called by invoke and invokeExported
      case _ => false
    override def apply(dom: FixIn[V]): FrameData[V] = frame.getFrameData
  }

  final def casesFunOrWhile[V](dom: FixIn[V]): Int = dom match
    case _: FixIn.EnterWasmFunction[V] => 0
    case FixIn.Eval(_: Loop) => 1
    case _ => -1

  final def callSitesLogger[V]() = fix.context.callSites[FixIn[V], Inst] {
    case FixIn.Eval(c: Call) => Some(c)
    case FixIn.Eval(c: CallIndirect) => Some(c)
    case _ => None
  }

  given CombineFixOut[V, W <: Widening](using w: Combine[V, W]): Combine[FixOut[V], W] with
    override def apply(out1: FixOut[V], out2: FixOut[V]): FixOut[V] = (out1, out2) match
      case (FixOut.Eval(), FixOut.Eval()) => FixOut.Eval()
      case (FixOut.ExitWasmFunction(vs1), FixOut.ExitWasmFunction(vs2)) => FixOut.ExitWasmFunction(Combine[List[V], W](vs1, vs2))
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")


//  final def parameters[V, A](using effects: GenericEffects[V, A])(using effects.StoreJoin[V]): fix.context.Sensitivity[FixIn, Map[A, V]] =
//    fix.context.parametersFromStore {
//      case FixIn.EnterFunction(f) => Some(f.params.map(p => effects.getLocal(p).get))
//      case _ => None
//    }
