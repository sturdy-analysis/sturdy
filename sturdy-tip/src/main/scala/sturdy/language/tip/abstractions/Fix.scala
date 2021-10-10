package sturdy.language.tip.abstractions

import sturdy.language.tip.{Stm, Program}
import sturdy.language.tip.GenericInterpreter.{FixIn, FixOut, GenericEffects}
import sturdy.fix
import sturdy.language.tip.Exp
import sturdy.data.unit
import sturdy.effect.ObservableJoin
import sturdy.language.tip.Function

trait Fix:
  final def isFunOrWhile(dom: FixIn): Int = dom match
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(Stm.While(_, _)) => 1
    case _ => -1

  final def callSitesLogger() = fix.context.callSites[FixIn, Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }

  final def parameters[V, A, MayJoin[_]](using effects: GenericEffects[V, A, MayJoin])(using MayJoin[V]): fix.context.Sensitivity[FixIn, Map[A, V]] =
    fix.context.parametersFromStore {
      case FixIn.EnterFunction(f) => Some(f.params.map(p => effects.getLocal(p).get))
      case _ => None
    }

  enum CfgNode:
    case Statement(s: Stm)
    case Call(call: Exp.Call)
    case CallReturn(callNode: Call) extends CfgNode, fix.CallReturnNode[Call]
    case Enter(fun: Function) extends CfgNode, fix.ImportantControlNode
    case Exit(fun: Function) extends CfgNode, fix.ImportantControlNode

  def control[Ctx, V](sensitive: Boolean, onlyCalls: Boolean)(using effect: ObservableJoin) = fix.control[Ctx, FixIn, FixOut[V], CfgNode](sensitive) {
    case FixIn.Run(Stm.Block(_)) => None
    case FixIn.Run(s) => if (onlyCalls) None else Some(CfgNode.Statement(s))
    case FixIn.EnterFunction(f) => Some(CfgNode.Enter(f))
    case FixIn.Eval(c: Exp.Call) => Some(CfgNode.Call(c))
    case _ => None
  } {
    case (FixIn.EnterFunction(f), FixOut.ExitFunction(_)) => Some(CfgNode.Exit(f))
    case (FixIn.Eval(c: Exp.Call), _) => Some(CfgNode.CallReturn(CfgNode.Call(c)))
    case _ => None
  }

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
