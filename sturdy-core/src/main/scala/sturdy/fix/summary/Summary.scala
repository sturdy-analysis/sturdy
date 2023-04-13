package sturdy.fix.summary

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.impl.tuple.Tuples
import sturdy.data.MutableMap.updateWith
import sturdy.fix.summary.Summary.Result
import sturdy.fix.{Contextual, HasContext}
import sturdy.values.{PartialOrder, Widen, given}

import scala.jdk.CollectionConverters.*

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
  def apply(x: In): Result[In,Out]

  def domain: Iterable[In]

object Summary {

  enum Result[+In, +Out]:
    case Exact(in: In, out: Out)
    case Overapproximate(in: In, out: Out)
    case None extends Result[Nothing,Nothing]

    def getIn: Option[In] =
      this match
        case Exact(in, _) => Some(in)
        case Overapproximate(in, _) => Some(in)
        case None => Option.empty

  given ResultOrder[In: PartialOrder, Out: PartialOrder]: PartialOrder[Result[In,Out]] with
    override def lteq(x: Result[In, Out], y: Result[In, Out]): Boolean =
      (x,y) match
        case (Result.Exact(in1,out1), Result.Exact(in2,out2)) =>
          summon[PartialOrder[In]].lteq(in1,in2) && summon[PartialOrder[Out]].lteq(out1,out2)
        case (Result.Exact(in1, out1), Result.Overapproximate(in2, out2)) =>
          summon[PartialOrder[In]].lteq(in1,in2) && summon[PartialOrder[Out]].lteq(out1,out2)
        case (Result.Overapproximate(in1, out1), Result.Overapproximate(in2, out2)) =>
          summon[PartialOrder[In]].lteq(in1,in2) && summon[PartialOrder[Out]].lteq(out1,out2)
        case (Result.None, Result.None) => true
        case (_,_) => false

}

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

  override def apply(queryIn: In): Result[In,Out] =
    (in,out) match
      case (Some(i), Some(o)) if PartialOrder[Option[In]].lteq(Some(queryIn),in)  =>
        if (overapproximate || i != queryIn)
          Result.Overapproximate(i, o)
        else
          Result.Exact(i, o)
      case (_,_) =>
        Result.None

  override def domain: Iterable[In] = in

  override def equals(obj: Any): Boolean =
    obj match
      case other: SingletonSummary[In, Out] =>
        this.in.equals(other.in) && this.out.equals(other.out)
      case _ => false

  override def hashCode(): Int =
    (this.in,this.out).hashCode()

  override def toString: String =
    (in,out) match
      case (Some(i), Some(o)) => s"$i => $o"
      case (_, _) => "None"

final class CacheSummary[In, Out: Widen] extends Summary[In, Out]:
  var cache: MutableMap[In, Out] = Maps.mutable.empty()

  override def addMapping(in: In, out: Out): Unit =
    cache.merge(in, out, (oldOut, newOut) => Widen(oldOut, newOut).get)

  override def apply(in: In): Result[In,Out] =
    cache.get(in) match
      case null => Result.None
      case out => Result.Exact(in, out)

  override def domain: Iterable[In] = cache.keysView().asScala

  override def equals(obj: Any): Boolean =
    obj match
      case other: CacheSummary[In, Out] =>
        this.cache.equals(other.cache)
      case _ => false

  override def hashCode(): Int = this.cache.hashCode()

  override def toString: String =
    val builder = StringBuilder()
    for(entry <- cache.entrySet().asScala) {
      builder.append("in = ")
             .append(entry.getKey)
             .append("\n")
             .append("out = ")
             .append(entry.getValue)
             .append("\n\n")
    }
    builder.toString

final class ContextSensitiveSummary[Ctx, In: Widen, Out: Widen](using context: HasContext[Ctx]) extends Summary[In, Out]:
  var cache: MutableMap[Ctx, Result[In,Out]] = Maps.mutable.empty()

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

  override def apply(in: In): Result[In,Out] =
    val ctx = context.getCurrentContext
    cache.get(ctx) match
      case null => Result.None
      case res if(res.getIn.contains(in)) => res
      case _ => Result.None

  override def domain: Iterable[In] = cache.valuesView().asScala.flatMap(_.getIn)

  override def equals(obj: Any): Boolean =
    obj match
      case other: ContextSensitiveSummary[Ctx, In, Out] =>
        this.cache.equals(other.cache)
      case _ => false

  override def hashCode(): Int = this.cache.hashCode()

  override def toString: String =
    cache.toString
