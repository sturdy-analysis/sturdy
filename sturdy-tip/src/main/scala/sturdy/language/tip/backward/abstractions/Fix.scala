package sturdy.language.tip.backward.abstractions

import sturdy.data.MayJoin.WithJoin
import sturdy.data.{MayJoin, noJoin}
import sturdy.effect.{EffectStack, ObservableJoin}
import sturdy.effect.callframe.DecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.store.Store
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.fix.context.{CallSiteLogger, FiniteCallString, Parameters}
import sturdy.fix.{*, given}
import sturdy.language.tip.backward.{BackFixIn, BackFixOut, BackwardsInterpreter}
import sturdy.language.tip.{Exp, Function, Interpreter, Program, Stm}
import sturdy.values.{Finite, Join, Widen}

trait Fix extends BackwardsInterpreter:
  def isFunOrWhile(dom: BackFixIn[Value]): Int = dom match
    case BackFixIn.EnterFunction(_) => 0
    case BackFixIn.Iterate(_) => 1
    case _ => -1

  final def callSitesLogger(): CallSiteLogger[BackFixIn[Value], Exp.Call] = context.callSites[BackFixIn[Value], Exp.Call] {
    case BackFixIn.Eval(c: Exp.Call, v) => Some(c)
    case _ => None
  }
  type CallString = context.CallString[Exp.Call]
  given Finite[CallString] = context.FiniteCallString

  final def parameters(callFrame: DecidableCallFrame[Unit, String, Value]): context.Sensitivity[BackFixIn[Value], Parameters[String, Value]] =
    context.parameters[BackFixIn[Value], String, Value] {
      case BackFixIn.EnterFunction(f) => Some(f.params.map(x => x -> callFrame.getLocalByName(x).get).toMap)
      case _ => None
    }



  def loopUnwinding[Ctx, In, Out, All](loopUnwindingSteps: Int, phi: Contextual[Ctx, BackFixIn[Value], BackFixOut[Value]] ?=> Combinator[BackFixIn[Value], BackFixOut[Value]])
                                      (using Widen[Value], Widen[In], Widen[Out], Finite[Ctx], EffectStack)
    : Contextual[Ctx, BackFixIn[Value], BackFixOut[Value]] ?=> Combinator[BackFixIn[Value], BackFixOut[Value]]
    = conditional({
        case BackFixIn.Run(Stm.While(_, _)) => true
        case _ => false
      }, sturdy.fix.unwind(loopUnwindingSteps, phi), phi)

  def contextInsensitive(phi: (Contextual[Unit, BackFixIn[Value], BackFixOut[Value]]) ?=> Combinator[BackFixIn[Value], BackFixOut[Value]])(using WithJoin[Value]): Combinator[BackFixIn[Value], BackFixOut[Value]] =
    notContextSensitive(phi)

  def parameterSensitive(analysis: Instance, phi: (Contextual[Parameters[String, Value], BackFixIn[Value], BackFixOut[Value]]) ?=> Combinator[BackFixIn[Value], BackFixOut[Value]])(using WithJoin[Value]): Combinator[BackFixIn[Value], BackFixOut[Value]] =
    contextSensitive(parameters(analysis.callFrame), phi)

  def callSiteSensitive(k: Int, phi: (Contextual[CallString, BackFixIn[Value], BackFixOut[Value]], Finite[CallString]) ?=> Combinator[BackFixIn[Value], BackFixOut[Value]]): Combinator[BackFixIn[Value], BackFixOut[Value]] =
    val callSites = callSitesLogger()
    log(callSites, contextSensitive(callSites.callString(k), phi))

//  def callSiteSensitiveFixpoint[In, Out, All](loopUnwindingSteps: Int): AnalysisState[BackFixIn, In, Out, All] ?=> ContextualFixpoint[BackFixIn, BackFixOut[Value]] = new ContextualFixpoint {
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
//    val cfg = control[analysis.fixpoint.Ctx, BackFixIn, BackFixOut[Value], Nothing, CfgNode](sensitive, CfgNode.Start) {
//      case BackFixIn.Run(Stm.Block(_)) => None
//      case BackFixIn.Run(s) => if (onlyCalls) None else Some(CfgNode.Statement(s))
//      case BackFixIn.EnterFunction(f) => Some(CfgNode.Enter(f))
//      case BackFixIn.Eval(c: Exp.Call) => Some(CfgNode.Call(c))
//      case _ => None
//    } {
//      case (BackFixIn.EnterFunction(f), BackFixOut.ExitFunction(_)) => Some(CfgNode.Exit(f))
//      case (BackFixIn.Eval(c: Exp.Call), _) => Some(CfgNode.CallReturn(CfgNode.Call(c)))
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
