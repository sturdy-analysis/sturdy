package sturdy.fix

import sturdy.effect.TrySturdy
import sturdy.fix.context.Sensitivity

import scala.collection.mutable.ListBuffer

trait Fixpoint[Dom, Codom]:
  final type Fixed = Dom => Codom

  def fixpoint(f: (Dom => Codom) ?=> (Dom => Codom)): Dom => Codom =
    val phi = fixpointAlgorithm()
    computeFixpoint(fixed => phi(f(using fixed)))

  private def computeFixpoint(f: (Dom => Codom) => (Dom => Codom)): Dom => Codom =
    f(dom => computeFixpoint(f)(dom))

  type Ctx
  protected def context: Sensitivity[Dom, Ctx]
  protected def contextFree: Combinator[Dom, Codom] => Combinator[Dom, Codom]
  protected def contextSensitive: Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom]

  private inline def withContext(phi: Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom]): ContextSensitive[Ctx, Dom, Codom] =
    sturdy.fix.contextSensitive(context, phi)

  private val contextFreeLoggers: ListBuffer[Logger[Dom, Codom]] = ListBuffer()
  def addContextFreeLogger(logger: Logger[Dom, Codom]): Unit = contextFreeLoggers += logger
  def removeContextFreeLogger(logger: Logger[Dom, Codom]): Unit = contextFreeLoggers -= logger
  
  private val contextSensitiveLoggers: ListBuffer[Contextual[Ctx, Dom, Codom] ?=> Logger[Dom, Codom]] = ListBuffer()
  def addContextSensitiveLogger(logger: Contextual[Ctx, Dom, Codom] ?=> Logger[Dom, Codom]): Unit = contextSensitiveLoggers += logger
  def removeContextSensitiveLogger(logger: Contextual[Ctx, Dom, Codom] ?=> Logger[Dom, Codom]): Unit = contextSensitiveLoggers -= logger

  private def fixpointAlgorithm(): Combinator[Dom, Codom] =
    val cs: Combinator[Dom, Codom] =
    if (contextSensitiveLoggers.isEmpty)
      withContext(contextSensitive)
    else
      withContext {
        val loggers = contextSensitiveLoggers.toList.map(a => a)
        log(manyLogger(loggers), contextSensitive)
      }

    val phi = contextFree(withContext(cs))
    if (contextFreeLoggers.isEmpty)
      phi
    else
      log(manyLogger(contextFreeLoggers.toList), phi)


trait ContextInsensitive[Dom, Codom] extends Fixpoint[Dom, Codom]:
  final type Ctx = Unit
  final protected override def context: Sensitivity[Dom, Ctx] = sturdy.fix.context.none
  final protected override def contextFree: Combinator[Dom, Codom] => Combinator[Dom, Codom] = f => f
  final protected override def contextSensitive: Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom] = contextInsensitive
  protected def contextInsensitive: Contextual[Ctx, Dom, Codom] ?=> Combinator[Dom, Codom]

trait Concrete[Dom, Codom] extends ContextInsensitive[Dom, Codom]:
  override protected def contextInsensitive = identity

object Fixpoint:
  var DEBUG: Boolean = System.getProperty("STURDY_DEBUG_FIXPOINT", "true").toBoolean
  val DEBUG_CACHE_CHANGES = System.getProperty("STURDY_DEBUG_CACHE_CHANGES", "false").toBoolean
