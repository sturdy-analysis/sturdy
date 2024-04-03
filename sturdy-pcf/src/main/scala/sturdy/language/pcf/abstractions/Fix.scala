package sturdy.language.pcf.abstractions

import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.EffectStack
import sturdy.language.pcf.{*}
import sturdy.fix.{*, given}
import sturdy.effect.ObservableJoin
import sturdy.effect.callframe.DecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.store.Store
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.fix.context.CallSiteLogger
import sturdy.fix.context.FiniteCallString
import sturdy.fix.context.Parameters
import sturdy.values.{Widen, Finite, Join}

trait Fix extends Interpreter:
  type FixIn = Exp
  type FixOut = Value

  final def parameters(callFrame: DecidableCallFrame[Unit, String, Value]): context.Sensitivity[FixIn, Parameters[String, Value]] =
    context.parameters[FixIn, String, Value] {
      case FixIn.EnterFunction(f) => Some(f.params.map(x => x -> callFrame.getLocalByName(x).get).toMap)
      case _ => None
    }
  def loopUnwinding[Ctx, In, Out, All](loopUnwindingSteps: Int, phi: Contextual[Ctx, FixIn, FixOut] ?=> Combinator[FixIn, FixOut[Value]])
                                      (using Widen[Value], Widen[In], Widen[Out], Finite[Ctx], EffectStack)
    : Contextual[Ctx, FixIn, FixOut] ?=> Combinator[FixIn, FixOut]
    = conditional({
        //case FixIn => true
        case _ => false
      }, sturdy.fix.unwind(loopUnwindingSteps, phi), phi)

  def contextInsensitive(phi: (Contextual[Unit, FixIn, FixOut]) ?=> Combinator[FixIn, FixOut])(using J[Value]): Combinator[FixIn, FixOut] =
    notContextSensitive(phi)

  def parameterSensitive(analysis: Instance, phi: (Contextual[Parameters[String, Value], FixIn, FixOut]) ?=> Combinator[FixIn, FixOut])(using J[Value]): Combinator[FixIn, FixOut] =
    contextSensitive(parameters(analysis.callFrame), phi)




