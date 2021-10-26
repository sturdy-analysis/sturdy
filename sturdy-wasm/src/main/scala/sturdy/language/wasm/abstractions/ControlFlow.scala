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
import sturdy.language.wasm.generic.*
import swam.syntax.If
import swam.syntax.CallIndirect
import collection.mutable

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


object ControlFlow:
  def allCfgNodes(modules: List[ModuleInstance]): Set[CfgNode] =
    val nodes: mutable.Set[CfgNode] = mutable.Set.empty
    for (mod <- modules; fun <- mod.functions) fun match {
      case f@FunctionInstance.Wasm(modInst, funcIx, func, ft) =>
        println(s"in function $f.")
        nodes += CfgNode.Enter(FuncId(modInst, funcIx))
        nodes += CfgNode.Exit(FuncId(modInst, funcIx))
        val (_,body) = withLocations(func.body, InstLoc.InFunction(FuncId(modInst, funcIx), 0))
        println(body)
        nodes ++= body.flatMap(instToCfgNode)
      case FunctionInstance.Host(hostF) => ???
    }
    for (mod <- modules; exp <- mod.exports) exp._2 match {
      case ExternalValue.Function(funcIx) =>
        nodes ++= instToCfgNode(swam.syntax.Call(funcIx) -> InstLoc.InvokeExported(mod, exp._1))
      case _ => // nothing
    }

    Set.from(nodes)

  def withLocations(instr: Vector[Inst], startLoc: InstLoc): (InstLoc, Vector[(Inst, InstLoc)]) =
    instr.foldLeft[(InstLoc, Vector[(Inst, InstLoc)])]((startLoc, Vector.empty)) { case ((loc, res), next) =>
      println(s"visiting $next with loc $loc")
      next match
        case Block(_, body) =>
          val (nestedLoc, nestedRes) = withLocations(body, loc+1)
          (nestedLoc, res ++: (next, loc) +: nestedRes)
        case Loop(_, body) =>
          val (nestedLoc, nestedRes) = withLocations(body, loc+1)
          (nestedLoc, res ++: (next, loc) +: nestedRes)
        case If(_, thenInstr, elseInstr) =>
          val (nestedLocThen, nestedResThen) = withLocations(thenInstr, loc+1)
          val (nestedLocElse, nestedResElse) = withLocations(elseInstr, nestedLocThen)
          (nestedLocElse, res ++: (next, loc) +: nestedResThen ++: nestedResElse)
        case _ => (loc+1, res :+ (next, loc))
    }

  def instToCfgNode(inst: (Inst, InstLoc)): Set[CfgNode] = inst match
    case (inst: swam.syntax.Call, loc) => Set(CfgNode.Call(inst, loc), CfgNode.CallReturn(CfgNode.Call(inst, loc)))
    case (inst: swam.syntax.CallIndirect, loc) => Set(CfgNode.Call(inst, loc), CfgNode.CallReturn(CfgNode.Call(inst, loc)))
    case (inst, loc) => Set(CfgNode.Instruction(inst, loc))