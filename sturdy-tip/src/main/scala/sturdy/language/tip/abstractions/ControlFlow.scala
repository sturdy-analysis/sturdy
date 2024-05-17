package sturdy.language.tip.abstractions

import sturdy.effect.{EffectStack, ObservableJoin}
import sturdy.effect.except.ObservableExcept
import sturdy.fix
import sturdy.fix.cfg.{ControlFlowGraph, ControlLogger}
import sturdy.language.tip.{Exp, FixIn, FixOut, Interpreter, Program, Stm}
import sturdy.util.Label

import collection.mutable

enum CfgNode extends ControlFlowGraph.Node:
  case Start
  case Statement(stm: Stm)
  case Call(call: Exp.Call)
  case CallReturn(call: Call)
  case Enter(fun: String)
  case Exit(fun: String)

  override def isStartNode: Boolean = this == Start

  override def isImportantControlNode: Boolean = this match
    case _: (Enter | Exit) => true
    case _ => false

  override def getBeginNode: Option[CfgNode] = this match
    case CallReturn(call) => Some(call)
    case _ => None

  override def toString: String = this match
    case Start => "Start"
    case Statement(stm) => stm.toString
    case Call(call) => call.toString
    case CallReturn(call) => s"Return $call"
    case Enter(fun) => s"enter $fun"
    case Exit(fun) => s"exit $fun"

case class CfgConfig(contextSensitive: Boolean, granularity: CfgGranularity, endNodes: Boolean):
  import CfgGranularity.*
  def getInNode(in: FixIn): Option[CfgNode] = in match
    case FixIn.Eval(c: Exp.Call) => Some(CfgNode.Call(c))
    case FixIn.Eval(_) => None
    case FixIn.Run(s) => Option.when(granularity == CfgGranularity.AllNodes)(CfgNode.Statement(s))
    case FixIn.EnterFunction(f) => Some(CfgNode.Enter(f.name))

  def getOutNode[V](in: FixIn, out: FixOut[V]): Option[CfgNode] = (in, out) match
    case (FixIn.Eval(c: Exp.Call), FixOut.Eval(_)) if endNodes => Some(CfgNode.CallReturn(CfgNode.Call(c)))
    case (FixIn.EnterFunction(fun), FixOut.ExitFunction(_)) => Some(CfgNode.Exit(fun.name))
    case _ => None

object CfgConfig:
  val CallGraph: CfgConfig = CfgConfig(contextSensitive = true, CfgGranularity.OnlyCalls, endNodes = true)
  def AllNodes(sensitive: Boolean): CfgConfig = CfgConfig(sensitive, CfgGranularity.AllNodes, endNodes = true)

enum CfgGranularity:
  case AllNodes
  case OnlyCalls


trait ControlFlow extends Interpreter:
  def controlFlow[Ctx](config: CfgConfig)(using effects: EffectStack): ControlLogger[Ctx, FixIn, FixOut[Value], Nothing, CfgNode] =
    val cfg = fix.control[Ctx, FixIn, FixOut[Value], Nothing, CfgNode](config.contextSensitive, CfgNode.Start)
                (config.getInNode)(config.getOutNode)(using effects, ObservableExcept.None)
    cfg


object ControlFlow:
  import ControlFlowGraph.CNode

  /** A node in `modules` is _dead_ if its unreachable according to the `cfg`.
   * Only returns nodes that represent statements */
  def deadCode[Ctx](cfg: ControlFlowGraph[CfgNode, Ctx], prog: Program): Set[CfgNode] =
    val allNodes = allCfgNodes(prog)
    cfg.filterDeadNodes(allNodes).filter(_.isInstanceOf[CfgNode.Statement])

  def allCfgNodes(prog: Program): Set[CfgNode] =
    val nodes: mutable.Set[CfgNode] = mutable.Set.empty
    for (fun <- prog.funs) {
      nodes += CfgNode.Enter(fun.name)
      nodes += CfgNode.Exit(fun.name)
      fun.body.fold[Unit](using s =>
        nodes += CfgNode.Statement(s), {
        case c: Exp.Call =>
          nodes += CfgNode.Call(c)
          nodes += CfgNode.CallReturn(CfgNode.Call(c))
        case _ => // nothing
      })
    }
    nodes.toSet

