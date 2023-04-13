package sturdy.incremental

import org.eclipse.collections.api.RichIterable
import org.eclipse.collections.api.factory.{Lists, Maps, Sets}
import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.api.list.primitive.ImmutableIntList
import org.eclipse.collections.api.map.{ImmutableMap, MutableMap}
import org.eclipse.collections.api.set.MutableSet
import org.eclipse.collections.api.tuple.Pair
import org.eclipse.collections.impl.factory.primitive.IntLists
import org.eclipse.collections.impl.tuple.Tuples
import sturdy.data.given
import sturdy.effect.{EffectStack, RecurrentCall, TrySturdy, given}
import sturdy.fix.*
import sturdy.fix.callgraph.CallGraphLogger
import sturdy.fix.summary.SummaryLogger
import sturdy.incremental.{Change, Changes, Delta, ListDelta}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}

import scala.annotation.tailrec
import scala.collection.{IterableOps, mutable}
import scala.jdk.CollectionConverters.*
import scala.util.Try

def clearSummaries[Dom, Codom, Callee, Ctx, CallSite]
                  (changes: ListDelta[Callee],
                   callGraph: CallGraphLogger[Dom, Callee, CallSite, Ctx],
                   summaryLogger: SummaryLogger[Dom, Codom, Callee]): Unit =
  val replaced: Iterable[Callee] = changes.delta.values.flatMap {
    case c@Change.Replace(from, _to) => Iterable(from)
    case _ => Iterable()
  }
  for(r <- replaced) {
    summaryLogger.clearSummary(r)
    for(caller <- callGraph.calledFromTransitively(r))
      summaryLogger.clearSummary(caller)
  }

