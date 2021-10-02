package sturdy.language.tip.analysis

import sturdy.language.tip.Stm
import sturdy.language.tip.GenericInterpreter.{FixIn, FixOut, GenericEffects}
import sturdy.fix
import sturdy.language.tip.Exp
import sturdy.data.unit
import sturdy.effect.ObservableJoin
import sturdy.language.tip.Function

object Fix:
  final def isFunOrWhile(dom: FixIn): Int = dom match
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(Stm.While(_, _)) => 1
    case _ => -1

  final def callSitesLogger() = fix.context.callSites[FixIn, Exp.Call] {
    case FixIn.Eval(c: Exp.Call) => Some(c)
    case _ => None
  }

  final def parameters[V, A](using effects: GenericEffects[V, A])(using effects.StoreJoin[V]): fix.context.Sensitivity[FixIn, Map[A, V]] =
    fix.context.parametersFromStore {
      case FixIn.EnterFunction(f) => Some(f.params.map(p => effects.getLocal(p).get))
      case _ => None
    }

  enum CfgNode:
    case Statement(s: Stm)
    case Call(call: Exp.Call)
    case CallReturn(call: Exp.Call) extends CfgNode, fix.CallReturnNode(Call(call))
    case Enter(fun: Function) extends CfgNode, fix.ImportantControlNode
    case Exit(fun: Function) extends CfgNode, fix.ImportantControlNode

  def control[Ctx, V](sensitive: Boolean, allStatements: Boolean)(using effect: ObservableJoin) = fix.control[Ctx, FixIn, FixOut[V], CfgNode](sensitive) {
    case FixIn.Run(Stm.Block(_)) => None
    case FixIn.Run(s) => if (allStatements) Some(CfgNode.Statement(s)) else None
    case FixIn.EnterFunction(f) => Some(CfgNode.Enter(f))
    case FixIn.Eval(c: Exp.Call) => Some(CfgNode.Call(c))
    case _ => None
  } {
    case (FixIn.EnterFunction(f), FixOut.ExitFunction(_)) => Some(CfgNode.Exit(f))
    case (FixIn.Eval(c: Exp.Call), _) => Some(CfgNode.CallReturn(c))
    case _ => None
  }