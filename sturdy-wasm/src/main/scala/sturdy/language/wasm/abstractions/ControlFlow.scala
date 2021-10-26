package sturdy.language.wasm.abstractions

import sturdy.effect.ObservableJoin
import swam.syntax.Loop
import sturdy.language.wasm.generic.InstLoc
import swam.syntax.Block
import sturdy.language.wasm.generic.FuncId
import swam.syntax.Inst
import sturdy.fix
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut
import swam.syntax.If
import swam.syntax.CallIndirect

enum CfgNode:
  case Instruction(inst: Inst, loc: InstLoc)
  case Call(inst: swam.syntax.Call | CallIndirect, loc: InstLoc)
  case CallReturn(callNode: Call) extends CfgNode, fix.CallReturnNode[Call]
  case Enter(funId: FuncId) extends CfgNode, fix.ImportantControlNode
  case Exit(funId: FuncId) extends CfgNode, fix.ImportantControlNode

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

trait ControlFlow extends Interpreter:
  import swam.syntax.Call

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