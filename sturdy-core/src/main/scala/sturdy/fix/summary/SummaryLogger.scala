package sturdy.fix.summary

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.impl.tuple.Tuples
import sturdy.data.MutableMap.updateWith
import sturdy.effect.{TrySturdy, given}
import sturdy.fix.summary.Summary.Result
import sturdy.fix.{Combinator, Fixpoint, Logger, State, log}
import sturdy.values.Widen
import scala.jdk.CollectionConverters.*

final class SummaryLogger[Dom, Codom: Widen, Callee](using val state: State)
                                                    (val getCallee: Dom => Option[Callee],
                                                     newSummary: Callee => Summary[state.In, (TrySturdy[Codom], state.Out)])
  extends Logger[Dom, Codom]:

  /** Cache that tracks the summary of each function. */
  var cache: MutableMap[Callee, Summary[state.In, (TrySturdy[Codom], state.Out)]] = Maps.mutable.empty()
  var inStack: List[state.In] = List.empty

  override def enter(dom: Dom): Unit =
    inStack = state.getInState(dom) :: inStack

  override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    val in = inStack.head
    inStack = inStack.tail

    getCallee(dom) match
      case Some(callee) =>
        val out = state.getOutState(dom)

        given widenOut: Widen[state.Out] = state.widenOut(dom)

        cache.updateWith(callee, newSummary(callee), summary =>
          summary.addMapping(in, (codom, out))
          summary
        )
      case None => {}

  def summaryOf(callee: Callee): Option[Summary[state.In, (TrySturdy[Codom], state.Out)]] =
    Option(cache.get(callee))

  def clearSummary(callee: Callee): Unit =
    cache.removeKey(callee)

  def loadSummaries(other: SummaryLogger[Dom, Codom, Callee]): Unit =
    cache.withMap(other.cache.asInstanceOf)

  override def equals(obj: Any): Boolean =
    obj match
      case other: SummaryLogger[Dom, Codom, Callee] =>
        this.cache.equals(other.cache)
      case _ => false

  override def hashCode(): Int =
    this.cache.hashCode()

  override def toString: String =
    val builder = new StringBuilder()
    for(entry <- this.cache.entrySet().asScala) {
      builder.append(entry.getKey())
             .append(":")
             .append(entry.getValue())
             .append("\n")
    }
    builder.toString()

  override def clone(): SummaryLogger[Dom, Codom, Callee] =
    val res = new SummaryLogger[Dom, Codom, Callee](using state)(getCallee, newSummary)
    res.cache = this.cache.clone().asInstanceOf
    res.inStack = this.inStack.asInstanceOf
    res

def reuseSummaries[Dom, Codom, Callee](overapproximate: Boolean = false,
                                       summaryLogger: SummaryLogger[Dom, Codom, Callee],
                                       phi: Combinator[Dom, Codom])
                                      (using state: State): Combinator[Dom, Codom] =
  ReuseSummaries(overapproximate, summaryLogger, phi)
final class ReuseSummaries[Dom, Codom, Callee](overapproximate: Boolean,
                                               summaryLogger: SummaryLogger[Dom, Codom, Callee],
                                               phi: Combinator[Dom, Codom])
                                              (using state: State)
                                               extends Combinator[Dom, Codom]:

  override def apply(f: Dom => Codom): Dom => Codom = (dom: Dom) =>
    val result =
      for(callee <- summaryLogger.getCallee(dom);
          summary <- summaryLogger.summaryOf(callee))
        yield(summary(state.getInState(dom).asInstanceOf[summaryLogger.state.In]))

    result match
      case Some(Result.Exact(in,(codom, out))) =>
        if(Fixpoint.DEBUG)
          println(s"REUSE SUMMARY ($dom, $in, $codom, $out)")
        state.setOutState(dom, out.asInstanceOf[state.Out])
        codom.getOrThrow
      case Some(Result.Overapproximate(in,(codom, out))) if overapproximate =>
        if (Fixpoint.DEBUG)
          println(s"REUSE SUMMARY ($dom, $in, $codom, $out)")
        state.setOutState(dom, out.asInstanceOf[state.Out])
        codom.getOrThrow
      case _ => phi(f)(dom)