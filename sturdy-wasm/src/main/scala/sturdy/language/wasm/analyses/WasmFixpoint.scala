package sturdy.language.wasm.analyses

import sturdy.data.MayJoin
import sturdy.data.finiteUnit
import sturdy.effect.AnalysisState
import sturdy.effect.EffectStack
import sturdy.effect.Effectful
import sturdy.fix
import sturdy.fix.{Combinator, Contextual, StackConfig}
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

case class WasmConfig(fix: FixpointConfig = FixpointConfig(), ctx: ContextConfig = Insensitive):
  override def toString: String = s"$fix $ctx"

object WasmConfig:
  def default = WasmConfig()

case class FixpointConfig(iter: fix.iter.Config = fix.iter.Config.Innermost(StackConfig.StackedCfgNodes()), loopUnwinding: Int = 0):
  override def toString: String =
    if (loopUnwinding <= 0)
      iter.toString
    else
      s"$iter-unwindLoop($loopUnwinding)"

  def get[V, In, Out, All, Ctx]
    (using AnalysisState[FixIn, In, Out, All], EffectStack)
    (using Widen[FixOut[V]], Widen[In], Widen[Out], Join[Out])
    (using Finite[Ctx])
    : Contextual[Ctx, FixIn, FixOut[V]] ?=> Combinator[FixIn, FixOut[V]] =

    if (loopUnwinding <= 0)
      fix.filter(isFunOrLoop, iter.get)
    else
      fix.dispatch(isFunOrLoopToIndex, Seq(
        // enter Wasm function
        iter.get,
        // loop
        fix.unwind(loopUnwinding, iter.get)
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
