package sturdy.language.wasm.analyses

import sturdy.fix
import sturdy.language.wasm.generic.FixIn
import sturdy.values.unit
import swam.syntax.Call

object Fix:
  final def isFunOrWhile(dom: FixIn): Boolean = dom match
    case _: FixIn.EnterWasmFunction => true
    case FixIn.Eval(_: Call) => true
    case _ => false

  final def casesFunOrWhile(dom: FixIn): Int = dom match
    case _: FixIn.EnterWasmFunction => 0
    case FixIn.Eval(c: Call) => 1
    case _ => -1

  final def callSitesLogger() = fix.context.callSites[FixIn, Call] {
    case FixIn.Eval(c: Call) => Some(c)
    case _ => None
  }

//  final def parameters[V, A](using effects: GenericEffects[V, A])(using effects.StoreJoin[V]): fix.context.Sensitivity[FixIn, Map[A, V]] =
//    fix.context.parametersFromStore {
//      case FixIn.EnterFunction(f) => Some(f.params.map(p => effects.getLocal(p).get))
//      case _ => None
//    }
