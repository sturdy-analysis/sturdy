package sturdy.fix.summary

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import sturdy.data.MutableMap.updateWith
import sturdy.effect.{TrySturdy, given}
import sturdy.fix.{Logger, State}
import sturdy.values.Widen

final class SummaryLogger[Dom, Codom: Widen](using val state: State)
                                            (emptySummary: Dom => Summary[state.In, (TrySturdy[Codom], state.Out)]) extends Logger[Dom, Codom]:

  /** Cache that tracks the summary of each function. */
  val cache: MutableMap[Dom, Summary[state.In, (TrySturdy[Codom], state.Out)]] = Maps.mutable.empty()

  override def enter(dom: Dom): Unit = {}
  override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    val in = state.getInState(dom)
    val out = state.getOutState(dom)

    given widenOut: Widen[state.Out] = state.widenOut(dom)

    cache.updateWith(dom, emptySummary(dom), summary =>
      summary.addMapping(in, (codom, out))
      summary
    )