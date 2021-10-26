package sturdy.language.wasm.abstractions

import sturdy.effect.ObservableJoin
import swam.syntax.Loop
import sturdy.language.wasm.generic.InstLoc
import swam.syntax.Block
import sturdy.language.wasm.generic.FuncId
import swam.syntax.Inst
import sturdy.fix
import sturdy.fix.ControlFlowGraph
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut
import sturdy.language.wasm.generic.*
import swam.OpCode
import swam.syntax.If
import swam.syntax.CallIndirect

import collection.mutable

enum CfgNode:
  case Instruction(inst: Inst, loc: InstLoc)
  case Labeled(inst: Block | Loop | If, loc: InstLoc)
  case LabeledEnd(startNode: Labeled) extends CfgNode, fix.EndNode[Labeled]
  case Call(inst: swam.syntax.Call | CallIndirect, loc: InstLoc)
  case CallReturn(startNode: Call) extends CfgNode, fix.EndNode[Call]
  case Enter(funId: FuncId) extends CfgNode, fix.ImportantControlNode
  case Exit(funId: FuncId) extends CfgNode, fix.ImportantControlNode

  override def toString: String = this match
    case Instruction(inst, loc) => s"$inst @$loc"
    case Labeled(inst, loc) => inst match
      case Block(_, _) => s"Block @$loc"
      case Loop(_, _) => s"Loop @$loc"
      case If(_, _, _) => s"If @$loc"
    case LabeledEnd(labeled) => s"End $labeled"
    case Call(inst, loc) => s"$inst @$loc"
    case CallReturn(call) => s"End $call"
    case Enter(funId) => s"enter $funId"
    case Exit(funId) => s"exit $funId"

case class CfgConfig(contextSensitive: Boolean, granularity: CfgGranularity)
object CfgConfig:
  val CallGraph: CfgConfig = CfgConfig(contextSensitive = true, CfgGranularity.OnlyCalls)
  def ControlGraph(sensitive: Boolean): CfgConfig = CfgConfig(sensitive, CfgGranularity.OnlyControl)
  def AllNodes(sensitive: Boolean): CfgConfig = CfgConfig(sensitive, CfgGranularity.AllNodes)
enum CfgGranularity:
  case AllNodes
  case OnlyControl
  case OnlyCalls

trait ControlFlow extends Interpreter:
  import swam.syntax.Call

  def control[Ctx](config: CfgConfig)(using effect: ObservableJoin) = fix.control[Ctx, FixIn[Value], FixOut[Value], CfgNode](config.contextSensitive) {
    case FixIn.Eval(c: Call, loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(c: CallIndirect, loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(c: (Block | Loop | If), loc) => Some(CfgNode.Labeled(c, loc))
    case FixIn.Eval(inst, loc) =>
      val includeNode = config.granularity match
        case CfgGranularity.AllNodes => true
        case CfgGranularity.OnlyCalls => false
        case CfgGranularity.OnlyControl => inst.opcode >= OpCode.Unreachable && inst.opcode <= OpCode.CallIndirect
      if (includeNode)
        Some(CfgNode.Instruction(inst, loc))
      else
        None
    case FixIn.EnterWasmFunction(id, _, _) => Some(CfgNode.Enter(id))
  } {
    case (FixIn.EnterWasmFunction(id, _, _), FixOut.ExitWasmFunction(_)) => Some(CfgNode.Exit(id))
    case (FixIn.Eval(c: (Call | CallIndirect), loc), _) => Some(CfgNode.CallReturn(CfgNode.Call(c, loc)))
    case (FixIn.Eval(c: (Block | Loop | If), loc), _) => Some(CfgNode.LabeledEnd(CfgNode.Labeled(c, loc)))
    case _ => None
  }


object ControlFlow:
  import ControlFlowGraph.CNode


  def allCfgNodes(modules: List[ModuleInstance]): Set[CfgNode] =
    val nodes: mutable.Set[CfgNode] = mutable.Set.empty
    for (mod <- modules; fun <- mod.functions) fun match {
      case f@FunctionInstance.Wasm(modInst, funcIx, func, ft) =>
        nodes += CfgNode.Enter(FuncId(modInst, funcIx))
        nodes += CfgNode.Exit(FuncId(modInst, funcIx))
        val (_,body) = withLocations(func.body, InstLoc.InFunction(FuncId(modInst, funcIx), 0))
        nodes ++= body.flatMap(instToCfgNode)
      case FunctionInstance.Host(hostF) =>
    }
    for (mod <- modules; exp <- mod.exports) exp._2 match {
      case ExternalValue.Function(funcIx) =>
        nodes ++= instToCfgNode(swam.syntax.Call(funcIx) -> InstLoc.InvokeExported(mod, exp._1))
      case _ => // nothing
    }

    Set.from(nodes)

  def withLocations(instr: Vector[Inst], startLoc: InstLoc): (InstLoc, Vector[(Inst, InstLoc)]) =
    instr.foldLeft[(InstLoc, Vector[(Inst, InstLoc)])]((startLoc, Vector.empty)) { case ((loc, res), next) =>
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