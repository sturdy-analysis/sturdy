package sturdy.language.tip.abstractions

import sturdy.language.tip.{Program, Stm}
import sturdy.language.tip.GenericInterpreter.{FixIn, FixOut, GenericEffects}
import sturdy.fix
import sturdy.language.tip.Exp
import sturdy.data.unit
import sturdy.effect.ObservableJoin
import sturdy.effect.except.ObservableExcept
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.fix.context.FiniteCallString
import sturdy.language.tip.Function
import sturdy.language.tip.Interpreter
import sturdy.values.Finite

trait Fix extends Interpreter:
  final def isFunOrWhile(dom: FixIn): Int = dom match
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(Stm.While(_, _)) => 1
    case _ => -1

  final def callSitesLogger() = fix.context.callSites[FixIn, Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }
  type CallString = fix.context.CallString[Exp.Call]
  given Finite[CallString] = fix.context.FiniteCallString

  final def parameters[MayJoin[_]](using effects: GenericEffects[Value, Addr, MayJoin])(using MayJoin[Value]): fix.context.Sensitivity[FixIn, Parameters] =
    fix.context.parametersFromStore {
      case FixIn.EnterFunction(f) => Some(f.params.map(p => effects.getLocalByName(p).get))
      case _ => None
    }
  type Parameters = Map[Addr, Value]

  enum CfgNode extends ControlFlowGraph.Node:
    case Start
    case Statement(s: Stm)
    case Call(call: Exp.Call)
    case CallReturn(startNode: Call)
    case Enter(fun: Function)
    case Exit(fun: Function)

    override def isStartNode: Boolean = this == Start
    override def isImportantControlNode: Boolean = this match
      case _: (Enter | Exit) => true
      case _ => false
    override def getBeginNode: Option[ControlFlowGraph.Node] = this match
      case CallReturn(call) => Some(call)
      case _ => None

  def controlFlow(sensitive: Boolean, onlyCalls: Boolean, analysis: Instance) =
    val cfg = fix.control[Ctx, FixIn, FixOut[Value], Nothing, CfgNode](sensitive, CfgNode.Start) {
      case FixIn.Run(Stm.Block(_)) => None
      case FixIn.Run(s) => if (onlyCalls) None else Some(CfgNode.Statement(s))
      case FixIn.EnterFunction(f) => Some(CfgNode.Enter(f))
      case FixIn.Eval(c: Exp.Call) => Some(CfgNode.Call(c))
      case _ => None
    } {
      case (FixIn.EnterFunction(f), FixOut.ExitFunction(_)) => Some(CfgNode.Exit(f))
      case (FixIn.Eval(c: Exp.Call), _) => Some(CfgNode.CallReturn(CfgNode.Call(c)))
      case _ => None
    } (using analysis.effects, ObservableExcept.None)
    analysis.addContextSensitiveLogger(cfg.logger)
    cfg

  def allCfgNodes(prog: Program, onlyCalls: Boolean): Set[CfgNode] =
    prog.fold(using {
      case fun => Set(CfgNode.Enter(fun), CfgNode.Exit(fun))
    }, {
      case Stm.Block(_) => Set()
      case s => if (onlyCalls) Set() else Set(CfgNode.Statement(s))
    }, {
      case c: Exp.Call => Set(CfgNode.Call(c), CfgNode.CallReturn(CfgNode.Call(c)))
      case _ => Set()
    })
