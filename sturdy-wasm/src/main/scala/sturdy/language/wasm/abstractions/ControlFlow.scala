package sturdy.language.wasm.abstractions

import sturdy.control.{BasicControlEvent, ControlEvent, ControlObservable}
import sturdy.effect.{EffectStack, ObservableJoin, TrySturdy}
import sturdy.effect.except.{LiftedExceptObserver, ObservableExcept}
import swam.syntax.{Block, Call, CallIndirect, If, Inst, Loop}
import sturdy.language.wasm.generic.InstLoc
import sturdy.language.wasm.generic.FuncId
import sturdy.fix
import sturdy.fix.Logger
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut
import sturdy.language.wasm.generic.*
import sturdy.values.booleans.ObservedBooleanBranching
import swam.OpCode

import collection.mutable

object Control:
  type Atom = (Inst, InstLoc)
  type Section = FuncId | (Block | Loop | If | Call | CallIndirect, InstLoc)
  type Exc = JumpTarget

trait Control extends Interpreter:
  import Control.*

  def controlEventLogger(observable: ControlObservable[Atom, Section, Exc],
                         obsJoin: ObservableJoin,
                         obsExc: ObservableExcept[WasmException[Value]]
                        )(using effects: EffectStack): Logger[FixIn, FixOut[Value]] =
    obsJoin.addJoinObserver(observable)
    obsExc.addExceptObserver(new LiftedExceptObserver(_.target, observable))
    new Logger:
      override def enter(dom: FixIn): Unit = dom match
        case FixIn.EnterWasmFunction(id, _, _) => observable.triggerControlEvent(BasicControlEvent.Begin(id))
        case FixIn.Eval(c: (Block | Loop | If | Call | CallIndirect), loc) => observable.triggerControlEvent(BasicControlEvent.Begin((c,loc)))
        case FixIn.Eval(inst, loc) => observable.triggerControlEvent(BasicControlEvent.Atomic((inst, loc)))
        case _ => // nothing

      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit = dom match
        case FixIn.EnterWasmFunction(id, _, _) => observable.triggerControlEvent(BasicControlEvent.End(id))
        case FixIn.Eval(c: (Block | Loop | If | Call | CallIndirect), loc) => observable.triggerControlEvent(BasicControlEvent.End((c, loc)))
        case _ => // nothing


enum CfgNode extends ControlFlowGraph.Node:
  case Start
  case Instruction(inst: Inst, loc: InstLoc)
  case Labled(inst: Block | Loop | If, loc: InstLoc)
  case LabledEnd(startNode: Labled)
  case Call(inst: swam.syntax.Call | CallIndirect, loc: InstLoc)
  case CallReturn(startNode: CfgNode.Call)
  case Enter(funId: FuncId)
  case Exit(funId: FuncId)

  override def isStartNode: Boolean = this == Start

  override def isImportantControlNode: Boolean = this match
    case _: (Enter | Exit) => true
    case _ => false

  override def getBeginNode: Option[CfgNode] = this match
    case LabledEnd(begin) => Some(begin)
    case CallReturn(call) => Some(call)
    case _ => None

  def isInstruction: Boolean = this match
    case _: (Instruction | Call | Labled) => true
    case _ => false

  override def toString: String = this match
    case Start => "Start"
    case Instruction(inst, loc) => s"$inst @$loc"
    case Labled(inst, loc) => inst match
      case Block(_, _) => s"Block @$loc"
      case Loop(_, _) => s"Loop @$loc"
      case If(_, _, _) => s"If @$loc"
    case LabledEnd(labeled) => s"End $labeled"
    case Call(inst, loc) => s"$inst @$loc"
    case CallReturn(call) => s"End $call"
    case Enter(funId) => s"enter $funId"
    case Exit(funId) => s"exit $funId"

case class CfgConfig(contextSensitive: Boolean, granularity: CfgGranularity, endNodes: Boolean):
  import CfgGranularity.*
  import swam.syntax.Call
  def getInNode(in: FixIn): Option[CfgNode] = in match
    case FixIn.Eval(c: (Call | CallIndirect), loc) => Some(CfgNode.Call(c, loc))
    case FixIn.Eval(c: (Block | Loop | If), loc) => granularity match
      case AllNodes | OnlyControl => Some(CfgNode.Labled(c, loc))
      case _ => None
    case FixIn.Eval(inst, loc) => granularity match
      case AllNodes => Some(CfgNode.Instruction(inst, loc))
      case OnlyControl if inst.opcode >= OpCode.Unreachable && inst.opcode <= OpCode.CallIndirect => Some(CfgNode.Instruction(inst, loc))
      case _ => None
    case FixIn.EnterWasmFunction(id, _, _) =>  Some(CfgNode.Enter(id))
    case _ => None

  def getOutNode[V](in: FixIn, out: FixOut[V]): Option[CfgNode] = (in, out) match
    case (FixIn.EnterWasmFunction(id, _, _), FixOut.ExitWasmFunction(_)) => Some(CfgNode.Exit(id))
    case (FixIn.Eval(c: (Call | CallIndirect), loc), _) if endNodes => Some(CfgNode.CallReturn(CfgNode.Call(c, loc)))
    case (FixIn.Eval(c: (Block | Loop | If), loc), _) if endNodes && granularity != OnlyCalls => Some(CfgNode.LabledEnd(CfgNode.Labled(c, loc)))
    case _ => None

object CfgConfig:
  val CallGraph: CfgConfig = CfgConfig(contextSensitive = true, CfgGranularity.OnlyCalls, endNodes = true)
  def ControlGraph(sensitive: Boolean): CfgConfig = CfgConfig(sensitive, CfgGranularity.OnlyControl, endNodes = true)
  def AllNodes(sensitive: Boolean): CfgConfig = CfgConfig(sensitive, CfgGranularity.AllNodes, endNodes = true)

enum CfgGranularity:
  case AllNodes
  case OnlyControl
  case OnlyCalls


trait ControlFlow extends Interpreter:
  def controlFlow(config: CfgConfig, analysis: Instance) =
    val cfg = fix.control[analysis.fixpoint.Ctx, FixIn, FixOut[Value], WasmException[Value], CfgNode](config.contextSensitive, CfgNode.Start)
                (config.getInNode)(config.getOutNode)(using analysis.effectStack, analysis.except)
    analysis.fixpoint.addContextSensitiveLogger(cfg.logger)
    cfg


object ControlFlow:
  import ControlFlowGraph.CNode

  /** A node in `modules` is _dead_ if its unreachable according to the `cfg`.
   * Only returns nodes that represent actual instructions */
  def deadInstruction[Ctx](cfg: ControlFlowGraph[CfgNode, Ctx], modules: List[ModuleInstance]): Set[CfgNode] =
    val allNodes = allCfgNodes(modules)
    cfg.filterDeadNodes(allNodes).filter(_.isInstruction)

  /** The labels of a Block, Loop, or If instruction are dead if no jump reaches them according to the `cfg`. */
  def deadLabels[Ctx](cfg: ControlFlowGraph[CfgNode, Ctx]): Set[CfgNode.Labled] =
    val revEdges = cfg.getReverseEdges
    cfg.getNodes.flatMap { endCNode => endCNode.node match
      case endNode: CfgNode.LabledEnd => endNode.startNode match
        case lab@CfgNode.Labled(_: (Block | If), _) =>
          val preds = revEdges.getOrElse(endCNode, Set())
          if (!preds.exists(_._2.exceptional))
            Some(lab)
          else
            None
        case lab@CfgNode.Labled(_: Loop, _) =>
          val preds = revEdges.getOrElse(CNode(lab, endCNode.ctx), Set())
          if (!preds.exists(_._2.exceptional))
            Some(lab)
          else
            None
      case _ => None
    }.toSet

  def allCfgNodes(modules: List[ModuleInstance]): Set[CfgNode] =
    modules.toSet.flatMap(allCfgNodes)

  def allCfgNodes(mod: ModuleInstance): Set[CfgNode] =
    val nodes: mutable.Set[CfgNode] = mutable.Set.empty
    for (fun <- mod.functions) fun match {
      case f@FunctionInstance.Wasm(modInst, funcIx, func, ft) =>
        nodes += CfgNode.Enter(FuncId(modInst, funcIx))
        nodes += CfgNode.Exit(FuncId(modInst, funcIx))
        val (_,body) = withLocations(func.body, InstLoc.InFunction(FuncId(modInst, funcIx), 0))
        nodes ++= body.flatMap(instToCfgNode)
      case FunctionInstance.Host(hostF) =>
    }
    for (exp <- mod.exports) exp._2 match {
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
    case (inst: swam.syntax.Block, loc) => Set(CfgNode.Labled(inst, loc), CfgNode.LabledEnd(CfgNode.Labled(inst, loc)))
    case (inst: swam.syntax.Loop, loc) => Set(CfgNode.Labled(inst, loc), CfgNode.LabledEnd(CfgNode.Labled(inst, loc)))
    case (inst: swam.syntax.If, loc) => Set(CfgNode.Labled(inst, loc), CfgNode.LabledEnd(CfgNode.Labled(inst, loc)))
    case (inst, loc) => Set(CfgNode.Instruction(inst, loc))