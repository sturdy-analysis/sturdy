package sturdy.language.wasm.analyses

import sturdy.control.FixpointControlEvent
import sturdy.data.MayJoin
import sturdy.data.finiteUnit
import sturdy.effect.EffectStack
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.{Combinator, Contextual, Stack, StackConfig}
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.language.wasm.generic.{FixIn, FixOut, finiteFixIn}
import sturdy.report.Properties
import sturdy.values.Finite
import sturdy.values.{Finite, Join, Widen}

//trait WasmFixpoint[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, J[_] <: MayJoin[_]]
//  (val config: WasmConfig)(using Widen[FixOut[V]])
//  extends GenericInterpreter[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, J], fix.Fixpoint[FixIn, FixOut[V]]:
//
//  override type Ctx = config.ctx.Ctx
//
//  implicit def widenState: Widen[State]
//  implicit def widenInState: Widen[InState]
//  implicit def widenOutState: Widen[OutState]
//
//  val (contextPreparation, sensitivity) = config.ctx.make[V]
//  import config.ctx.finiteCtx
//  override protected def contextFree = contextPreparation
//  override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
//  override protected def contextSensitive = config.fix.get(using analysisState, effectStack)

case class WasmConfig(fix: FixpointConfig = FixpointConfig(), ctx: ContextConfig = Insensitive, localSSA: Boolean = false):
  override def toString: String = s"$fix $ctx"

  def withObservers[Fx](newObservers: Iterable[FixpointControlEvent[Nothing,Nothing,Nothing,Fx] => Unit]): WasmConfig =
    WasmConfig(fix.withObservers(newObservers), ctx, localSSA = localSSA)

object WasmConfig:
  def default = WasmConfig()

case class FixpointConfig(stack: StackConfig = StackedStates(), iter: fix.iter.Config = fix.iter.Config.Innermost, loopUnwinding: Int = 0):
  def withObservers[Fx](newObservers: Iterable[FixpointControlEvent[Nothing,Nothing,Nothing,Fx] => Unit]): FixpointConfig =
    FixpointConfig(stack.withObservers(newObservers), iter, loopUnwinding)
  
  override def toString: String =
    if (loopUnwinding <= 0)
      iter.toString
    else
      s"$iter-unwindLoop($loopUnwinding)"

  def get[V, In, Out, All, Ctx]
    (using EffectStack)
    (using Join[V], Widen[V])
    (using Finite[Ctx])
    : Contextual[Ctx, FixIn, FixOut[V]] ?=> Combinator[FixIn, FixOut[V]] =

    if (loopUnwinding <= 0)
      fix.filter(isFunOrLoop, iter.get(stack))
    else
      fix.dispatch(isFunOrLoopToIndex, Seq(
        // enter Wasm function
        iter.get(stack),
        // loop
        fix.unwind(loopUnwinding, iter.get(stack))
      ))

sealed trait ContextConfig:
  type Ctx
  implicit val finiteCtx: Finite[Ctx]
  def make[V]: (Combinator[FixIn, FixOut[V]] => Combinator[FixIn, FixOut[V]], Sensitivity[FixIn, Ctx])

case object Insensitive extends ContextConfig:
  type Ctx = Unit
  override val finiteCtx = implicitly
  override def make[V] = (identity, fix.context.none)
  override def toString: String = "nocontext"


case class PreviousCallSites(k: Int) extends ContextConfig:
  type Ctx = CallString
  override val finiteCtx = implicitly
  override def make[V] = {
    val callSites = previousCallSitesLogger(k)
    (fix.log(callSites, _), callSites.callString)
  }
  override def toString: String = s"previous-calls($k)"

case class CallSites(k: Int) extends ContextConfig:
  type Ctx = CallString
  override val finiteCtx = implicitly
  override def make[V] = {
    val callSites = callSitesLogger()
    (fix.log(callSites, _), callSites.callString(k))
  }
  override def toString: String = s"calls($k)"
