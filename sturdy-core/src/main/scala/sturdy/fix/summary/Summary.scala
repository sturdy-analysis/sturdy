package sturdy.fix.summary

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import sturdy.data.MutableMap.updateWith
import sturdy.fix.{Contextual, HasContext}
import sturdy.values.{PartialOrder, Widen, given}

/** Summary of how a monotone function maps inputs to outputs. */
trait Summary[In, Out]:

  /** Adds a mapping from a new input `x` to a new output `y` to the summary.
   *  Adding multiple outputs `y`, `y'` for the same input `x` widens the output to `y ∇ y'`. */
  def addMapping(x: In, y: Out): Unit

  /** Query the summary of function `f` for an input `x`. There are three cases to consider:
   *   - The summary contains input `x` and returns the exact output `f(x)`.
   *   - The summary does not contain an input `x`, but contains a greater input x' ⊒ x.
   *     In this case the summary may return the output `f(x')` which overapproximates `f(x)`, because `f` is monotone.
 *     - The summary does not contain an input greater or equal to `x`, in which case it returns `None`.` */
  def apply(x: In): Result

  enum Result:
    case Exact(in: In, out: Out)
    case Overapproximate(in: In, out: Out)
    case None
    def getIn: Option[In] =
      this match
        case Exact(in,_) => Some(in)
        case Overapproximate(in,_) => Some(in)
        case None => Option.empty

object SingletonSummary:
  def apply[In: PartialOrder,Out](widenIn: Widen[In], widenOut: Widen[Out]): SingletonSummary[In,Out] =
    new SingletonSummary(using summon[PartialOrder[In]], widenIn, widenOut)
final class SingletonSummary[In: PartialOrder: Widen, Out: Widen] extends Summary[In,Out]:
  var in: Option[In] = None
  var out: Option[Out] = None
  var overapproximate: Boolean = false

  override def addMapping(newIn: In, newOut: Out): Unit =
    in match
      case Some(oldIn) =>
        val widenedIn = Widen(oldIn, newIn)
        in = Some(widenedIn.get)
        overapproximate = widenedIn.hasChanged
      case None => in = Some(newIn)

    out match
      case Some(oldOut) => out = Some(Widen(oldOut, newOut).get)
      case None => out = Some(newOut)

  override def apply(queryIn: In): Result =
    (in,out) match
      case (Some(i), Some(o)) if PartialOrder[Option[In]].lteq(Some(queryIn),in)  =>
        if (overapproximate || i != queryIn)
          Result.Overapproximate(i, o)
        else
          Result.Exact(i, o)
      case (_,_) => Result.None


  override def toString: String =
    (in,out) match
      case (Some(i), Some(o)) => s"$i => $o"
      case (_, _) => "None"

final class CacheSummary[In, Out: Widen] extends Summary[In, Out]:
  val cache: MutableMap[In, Out] = Maps.mutable.empty()

  override def addMapping(in: In, out: Out): Unit =
    cache.merge(in, out, (oldOut, newOut) => Widen(oldOut, newOut).get)

  override def apply(in: In): Result =
    cache.get(in) match
      case null => Result.None
      case out => Result.Exact(in, out)

  override def toString: String =
    cache.toString

final class ContextSensitiveSummary[Ctx, In: Widen, Out: Widen](using context: HasContext[Ctx]) extends Summary[In, Out]:
  val cache: MutableMap[Ctx, Result] = Maps.mutable.empty()

  override def addMapping(in: In, out: Out): Unit =
    val ctx = context.getCurrentContext
    cache.merge(ctx, Result.Exact(in,out), {
      case (Result.Exact(oldIn,oldOut),Result.Exact(newIn,newOut)) =>
        val widenedIn = Widen(oldIn, newIn)
        val widenedOut = Widen(oldOut, newOut)
        if(widenedIn.hasChanged)
          Result.Overapproximate(widenedIn.get,widenedOut.get)
        else
          Result.Exact(widenedIn.get,widenedOut.get)
      case (Result.Overapproximate(oldIn,oldOut),Result.Exact(newIn,newOut)) => Result.Overapproximate(Widen(oldIn, newIn).get, Widen(oldOut, newOut).get)
      case _ => throw new IllegalStateException()
    })

  override def apply(in: In): Result =
    val ctx = context.getCurrentContext
    cache.get(ctx) match
      case null => Result.None
      case res if(res.getIn.contains(in)) => res
      case _ => Result.None

  override def toString: String =
    cache.toString
