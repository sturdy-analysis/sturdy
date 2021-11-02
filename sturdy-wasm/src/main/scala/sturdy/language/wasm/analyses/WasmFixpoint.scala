package sturdy.language.wasm.analyses

import sturdy.data.finiteUnit
import sturdy.effect.AnalysisState
import sturdy.effect.Effectful
import sturdy.fix
import sturdy.fix.Contextual
import sturdy.fix.Combinator
import sturdy.fix.Combinator
import sturdy.fix.Contextual
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{FixIn, finiteFixIn, FixOut}
import sturdy.values.Finite
import sturdy.values.{Widen, Finite}

trait WasmFixpoint[V, In, Out, All](val config: WasmConfig)
                     (using state: AnalysisState[In, Out, All])
                     (using widenCodom: Widen[FixOut[V]], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
  extends fix.Fixpoint[FixIn, FixOut[V]]:
  override type Ctx = config.ctx.Ctx

  val (contextPreparation, sensitivity) = config.ctx.make[V]
  import config.ctx.finiteCtx
  override protected def contextFree = contextPreparation
  override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
  override protected def contextSensitive = config.fix.get

case class WasmConfig(fix: FixpointConfig = FixpointConfig(fix.iter.Config.Innermost, 0), ctx: ContextConfig = Insensitive)
object WasmConfig:
  def default = WasmConfig()

case class FixpointConfig(iter: fix.iter.Config, loopUnwinding: Int):
  def get[V, In, Out, All, Ctx]
    (using state: AnalysisState[In, Out, All])
    (using widenCodom: Widen[FixOut[V]], widenIn: Widen[In], widenOut: Widen[Out], j: Effectful)
    (using Finite[Ctx])
    : Contextual[Ctx, FixIn, FixOut[V]] ?=> Combinator[FixIn, FixOut[V]] =

    if (loopUnwinding <= 0)
      fix.filter(isFunOrWhile, iter.get)
    else
      fix.dispatch(casesFunOrWhile, Seq(
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

case class PreviousCallSites(k: Int) extends ContextConfig:
  type Ctx = CallString
  override val finiteCtx = implicitly
  override def make[V] = {
    val callSites = previousCallSitesLogger(k)
    (fix.log(callSites, _), callSites.callString)
  }

case class SurroundingCallSites(k: Int) extends ContextConfig:
  type Ctx = CallString
  override val finiteCtx = implicitly
  override def make[V] = {
    val callSites = surroundingCallSitesLogger()
    (fix.log(callSites, _), callSites.callString(k))
  }
