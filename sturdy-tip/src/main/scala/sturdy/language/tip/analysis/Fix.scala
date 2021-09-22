package sturdy.language.tip.analysis

import sturdy.language.tip.Stm
import sturdy.language.tip.GenericInterpreter.FixIn
import sturdy.fix
import sturdy.language.tip.GenericInterpreter.GenericEffects
import sturdy.language.tip.Exp
import sturdy.values.unit

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
