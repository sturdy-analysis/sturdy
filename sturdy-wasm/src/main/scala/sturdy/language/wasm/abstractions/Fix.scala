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


  final def frameSensitive(using frame: CallFrame[FrameData[Value], _, _, _]): Sensitivity[FixIn[Value], FrameData[Value]] = new Sensitivity {
    override def emptyContext: FrameData[Value] = FrameData.empty

    override def switchCall(dom: FixIn[Value]): Boolean = dom match
      case _: FixIn.EnterWasmFunction[Value] => true // called by invoke and invokeExported
      case _ => false

    override def apply(dom: FixIn[Value]): FrameData[Value] = frame.getFrameData
  }

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

  enum CfgNode:
    case Instruction(inst: Inst, loc: InstLoc[_])
    case Call(inst: swam.syntax.Call | CallIndirect, loc: InstLoc[_])
    case CallReturn(callNode: Call) extends CfgNode, fix.CallReturnNode[Call]
    case Enter(funId: FuncId[Value]) extends CfgNode, fix.ImportantControlNode
    case Exit(funId: FuncId[Value]) extends CfgNode, fix.ImportantControlNode

    override def toString: String = this match
      case Instruction(inst, loc) => inst match
        case Block(_, _) => s"Block @$loc"
        case Loop(_, _) => s"Loop @$loc"
        case If(_, _, _) => s"If @$loc"
        case _ => s"$inst @$loc"
      case Call(inst, loc) => s"$inst @$loc"
      case CallReturn(call) => s"CallReturn(${call.inst}) @${call.loc}"
      case Enter(funId) => s"enter $funId"
      case Exit(funId) => s"exit $funId"

  def control[Ctx](sensitive: Boolean, onlyCalls: Boolean)(using effect: ObservableJoin) = fix.control[Ctx, FixIn[Value], FixOut[Value], CfgNode](sensitive) {
    case FixIn.Eval(c: Call, loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(c: CallIndirect, loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(inst, loc) => if (onlyCalls) None else Some(CfgNode.Instruction(inst, loc))
    case FixIn.EnterWasmFunction(id, _, _) => Some(CfgNode.Enter(id))
  } {
    case (FixIn.EnterWasmFunction(id, _, _), FixOut.ExitWasmFunction(_)) => Some(CfgNode.Exit(id))
    case (FixIn.Eval(c: (Call | CallIndirect), loc), _) => Some(CfgNode.CallReturn(CfgNode.Call(c, loc)))
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