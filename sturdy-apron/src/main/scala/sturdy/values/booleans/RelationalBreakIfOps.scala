package sturdy.values.booleans

import sturdy.data.given
import sturdy.apron.{*,given}
import sturdy.effect.EffectStack
import sturdy.values.{*,given}
import sturdy.values.references.VirtualAddress


given RelationalBreakIf[Ctx, Type, Val](using apronState: ApronRecencyState[Ctx,Type,Val], effectStack: EffectStack): BreakIf[ApronCons[VirtualAddress[Ctx],Type]] with
  override type State = apronState.recencyStore.State

  override def break(br: State => Unit): Unit =
    br(apronState.recencyStore.getState)

  override def breakIf(cond: ApronCons[VirtualAddress[Ctx], Type])(break: State => Unit): Unit =
    apronState.ifThenElse(effectStack)(cond) {
      break(apronState.recencyStore.getState)
    } {
    }
    apronState.addConstraints(cond.negated)(using ResolveState.Internal)

  override def assertCondition(cond: ApronCons[VirtualAddress[Ctx], Type], state: State): Unit =
    apronState.recencyStore.setState(state)
    apronState.addConstraints(cond)(using ResolveState.Internal)

  override def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = apronState.recencyStore.joinClosingOver
  override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = apronState.recencyStore.widenClosingOver

given RelationalBreakIfBool[Ctx, Type, Val](using apronState: ApronRecencyState[Ctx,Type,Val], effectStack: EffectStack): BreakIf[ApronBool[VirtualAddress[Ctx],Type]] with
  override type State = apronState.relationalStore.State

  override def break(br: State => Unit): Unit =
    br(apronState.relationalStore.getState)

  override def breakIf(cond: ApronBool[VirtualAddress[Ctx], Type])(break: State => Unit): Unit =
    //    breakIfDebug(cond)(break)
    val res = apronState.assert(cond)(using ResolveState.Internal)
    apronState.ifThenElse(effectStack)(cond) {
      break(apronState.relationalStore.getState)
    } {

    }
    apronState.addCondition(cond.negated)(using ResolveState.Internal)

  override def assertCondition(cond: ApronBool[VirtualAddress[Ctx], Type], state: State): Unit =
    apronState.relationalStore.setState(state, widening = false)
    apronState.addCondition(cond)(using ResolveState.Internal)

  override def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = apronState.relationalStore.joinClosingOver

  override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = apronState.relationalStore.widenClosingOver

def breakIfDebug[Ctx, Type, Val](using apronState: ApronRecencyState[Ctx,Type,Val], effectStack: EffectStack)(cond: ApronBool[VirtualAddress[Ctx], Type])(break: apronState.relationalStore.State => Unit): Unit =
  val res = apronState.assert(cond)(using ResolveState.Internal)
  apronState.ifThenElse(effectStack)(cond) {
    break(apronState.relationalStore.getState)
  } {

  }
  apronState.addCondition(cond.negated)(using ResolveState.Internal)