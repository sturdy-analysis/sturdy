package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.ObservableJoin
import sturdy.effect.callframe.CallFrame
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData}
import sturdy.values.{Combine, Widening}
import swam.FuncIdx
import swam.syntax.{Loop, Call, Inst, CallIndirect}

trait Fix extends Interpreter:
  final def isFunOrWhile(dom: FixIn[Value]): Boolean = dom match
    case _: FixIn.EnterWasmFunction[Value] => true
    case FixIn.Eval(_: Loop) => true
    case _ => false

  final def frameSensitive(using frame: CallFrame[FrameData[Value], _, _]): Sensitivity[FixIn[Value], FrameData[Value]] = new Sensitivity {
    override def emptyContext: FrameData[Value] = FrameData.empty

    override def switchCall(dom: FixIn[Value]): Boolean = dom match
      case _: FixIn.EnterWasmFunction[Value] => true // called by invoke and invokeExported
      case _ => false

    override def apply(dom: FixIn[Value]): FrameData[Value] = frame.getFrameData
  }

  final def casesFunOrWhile(dom: FixIn[Value]): Int = dom match
    case _: FixIn.EnterWasmFunction[Value] => 0
    case FixIn.Eval(_: Loop) => 1
    case _ => -1

  final def callSitesLogger() = fix.context.callSites[FixIn[Value], Inst] {
    case FixIn.Eval(c: Call) => Some(c)
    case FixIn.Eval(c: CallIndirect) => Some(c)
    case _ => None
  }

  given CombineFixOut[W <: Widening] (using w: Combine[Value, W]): Combine[FixOut[Value], W] with
    override def apply(out1: FixOut[Value], out2: FixOut[Value]): FixOut[Value] = (out1, out2) match
      case (FixOut.Eval(), FixOut.Eval()) => FixOut.Eval()
      case (FixOut.ExitWasmFunction(vs1), FixOut.ExitWasmFunction(vs2)) => FixOut.ExitWasmFunction(Combine[List[Value], W](vs1, vs2))
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")

  enum CfgNode:
    case Instruction(inst: Inst)
    case Call(inst: swam.syntax.Call | CallIndirect)
    case CallReturn(callNode: Call) extends CfgNode, fix.CallReturnNode[Call]
    case Enter(funId: Either[FuncIdx, Value]) extends CfgNode, fix.ImportantControlNode
    case Exit(funId: Either[FuncIdx, Value]) extends CfgNode, fix.ImportantControlNode

    override def toString: String = this match
      case Instruction(inst) => inst.toString
      case Call(inst) => inst.toString
      case CallReturn(inst) => s"CallReturn($inst)"
      case Enter(funId) => funId match
        case Left(ix) => s"enter direct $ix"
        case Right(v) => s"enter indirect $v"
      case Exit(funId) => funId match
        case Left(ix) => s"exit direct $ix"
        case Right(v) => s"exit indirect $v"

  def control[Ctx](sensitive: Boolean, onlyCalls: Boolean)(using effect: ObservableJoin) = fix.control[Ctx, FixIn[Value], FixOut[Value], CfgNode](sensitive) {
    case FixIn.Eval(c: Call) => Some(CfgNode.Call(c))
    case FixIn.Eval(c: CallIndirect) => Some(CfgNode.Call(c))
    case FixIn.Eval(inst) => if (onlyCalls) None else Some(CfgNode.Instruction(inst))
    case FixIn.EnterWasmFunction(id, _, _) => Some(CfgNode.Enter(id))
  } {
    case (FixIn.EnterWasmFunction(id, _, _), FixOut.ExitWasmFunction(_)) => Some(CfgNode.Exit(id))
    case (FixIn.Eval(c: (Call | CallIndirect)), _) => Some(CfgNode.CallReturn(CfgNode.Call(c)))
    case _ => None
  }

//  def allCfgNodes(prog: Program, allStatements: Boolean): Set[CfgNode] =
//    prog.fold(using {
//      case fun => Set(CfgNode.Enter(fun), CfgNode.Exit(fun))
//    }, {
//      case Stm.Block(_) => Set()
//      case s => if (allStatements) Set(CfgNode.Statement(s)) else Set()
//    }, {
//      case c: Exp.Call => Set(CfgNode.Call(c), CfgNode.CallReturn(c))
//      case _ => Set()
//    })