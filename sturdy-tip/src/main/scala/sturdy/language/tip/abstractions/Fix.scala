package sturdy.language.tip.abstractions

import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.language.tip.{Program, Stm}
import sturdy.language.tip.GenericInterpreter.{FixIn, FixOut}
import sturdy.fix.{*, given}
import sturdy.language.tip.Exp
import sturdy.effect.ObservableJoin
import sturdy.effect.callframe.DecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.store.Store
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.fix.context.CallSiteLogger
import sturdy.fix.context.FiniteCallString
import sturdy.fix.context.Parameters
import sturdy.language.tip.Function
import sturdy.language.tip.Interpreter
import sturdy.values.{Widen, Finite, Join}

def isFunOrWhile(dom: FixIn): Int = dom match
  case FixIn.EnterFunction(_) => 0
  case FixIn.Run(Stm.While(_, _)) => 1
  case _ => -1

trait Fix extends Interpreter:

  final def callSitesLogger(): CallSiteLogger[FixIn, Exp.Call] = context.callSites[FixIn, Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }
  type CallString = context.CallString[Exp.Call]
  given Finite[CallString] = context.FiniteCallString

  final def parameters(callFrame: DecidableCallFrame[Unit, String, Value]): context.Sensitivity[FixIn, Parameters[String, Value]] =
    context.parameters[FixIn, String, Value] {
      case FixIn.EnterFunction(f) => Some(f.params.map(x => x -> callFrame.getLocalByName(x).get).toMap)
      case _ => None
    }



  def loopUnwinding[Ctx, In, Out, All](loopUnwindingSteps: Int, phi: Contextual[Ctx, FixIn, FixOut[Value]] ?=> Combinator[FixIn, FixOut[Value]])
                                      (using AnalysisState[FixIn, In, Out, All])
                                      (using Widen[Value], Widen[In], Widen[Out], Finite[Ctx], EffectStack)
    : Contextual[Ctx, FixIn, FixOut[Value]] ?=> Combinator[FixIn, FixOut[Value]]
    = conditional({
        case FixIn.Run(Stm.While(_, _)) => true
        case _ => false
      }, sturdy.fix.unwind(loopUnwindingSteps, phi), phi)

  def contextInsensitive(phi: (Contextual[Unit, FixIn, FixOut[Value]]) ?=> Combinator[FixIn, FixOut[Value]])(using J[Value]): Combinator[FixIn, FixOut[Value]] =
    notContextSensitive(phi)

  def parameterSensitive(analysis: Instance, phi: (Contextual[Parameters[String, Value], FixIn, FixOut[Value]]) ?=> Combinator[FixIn, FixOut[Value]])(using J[Value]): Combinator[FixIn, FixOut[Value]] =
    contextSensitive(parameters(analysis.callFrame), phi)

  def callSiteSensitive(k: Int, phi: (Contextual[CallString, FixIn, FixOut[Value]], Finite[CallString]) ?=> Combinator[FixIn, FixOut[Value]]): Combinator[FixIn, FixOut[Value]] =
    val callSites = callSitesLogger()
    log(callSites, contextSensitive(callSites.callString(k), phi))

//  def callSiteSensitiveFixpoint[In, Out, All](loopUnwindingSteps: Int): AnalysisState[FixIn, In, Out, All] ?=> ContextualFixpoint[FixIn, FixOut[Value]] = new ContextualFixpoint {
//    override type Ctx = CallString
//    val callSites = callSitesLogger()
//    protected override def context = callSites.callString(2)
//    protected override def contextFree = log(callSites, _)
//    override def contextSensitive = dispatch(isFunOrWhile, Seq(
//      // call
//      iter.topmost,
//      // while
//      unwind(loopUnwindingSteps,
//        iter.innermost
//      )
//    ))
//  }


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

//  def controlFlow(sensitive: Boolean, onlyCalls: Boolean, analysis: Instance) =
//    val cfg = control[analysis.fixpoint.Ctx, FixIn, FixOut[Value], Nothing, CfgNode](sensitive, CfgNode.Start) {
//      case FixIn.Run(Stm.Block(_)) => None
//      case FixIn.Run(s) => if (onlyCalls) None else Some(CfgNode.Statement(s))
//      case FixIn.EnterFunction(f) => Some(CfgNode.Enter(f))
//      case FixIn.Eval(c: Exp.Call) => Some(CfgNode.Call(c))
//      case _ => None
//    } {
//      case (FixIn.EnterFunction(f), FixOut.ExitFunction(_)) => Some(CfgNode.Exit(f))
//      case (FixIn.Eval(c: Exp.Call), _) => Some(CfgNode.CallReturn(CfgNode.Call(c)))
//      case _ => None
//    } (using analysis.effectStack, ObservableExcept.None)
//    analysis.fixpoint.addContextSensitiveLogger(cfg.logger)
//    cfg

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
