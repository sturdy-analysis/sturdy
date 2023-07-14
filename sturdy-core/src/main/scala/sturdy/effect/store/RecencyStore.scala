package sturdy.effect.store

import sturdy.data.{JOption, JOptionA, WithJoin}
import sturdy.{IsSound, Soundness}
import sturdy.effect.allocation.{PhysicalAddress, RecencyAllocator, VirtualAddress}
import sturdy.values.{Abstractly, Finite, Join, Widen}
import sturdy.effect.allocation.Recency.*

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
class RecencyStore[Context, V](val alloc: RecencyAllocator[Context], _init: Map[PhysicalAddress[Context], V])
                              (using Join[V], Widen[V], Finite[PhysicalAddress[Context]])
                              extends AStoreGenericThreadded[PhysicalAddress[Context], V],
                                      Store[VirtualAddress[Context], V, WithJoin]:

  this.store = _init

  alloc.addAllocationObserver {
    (ctx, _) =>
      this.read(PhysicalAddress(ctx,Recent)).map {
        oldVal =>
          this.weakUpdate(PhysicalAddress(ctx, Old), oldVal)
      }
  }

  override def read(x: VirtualAddress[Context]): JOption[WithJoin, V] =
    read(x.lookupPhysicalAddress)

  def read(x: PhysicalAddress[Context]): JOption[WithJoin, V] =
    store.get(x) match
      case scala.None    => JOptionA.none
      case scala.Some(v) => JOptionA.noneSome(v)

  def write(x: VirtualAddress[Context], v: V): Unit =
    write(x.lookupPhysicalAddress, v)

  def write(x: PhysicalAddress[Context], v: V): Unit =
    x.recency match
      case Recent => strongUpdate(x, v)
      case Old => weakUpdate(x, v)

  override def free(x: VirtualAddress[Context]): Unit =
    free(x.lookupPhysicalAddress)

  def free(x: PhysicalAddress[Context]): Unit =
    x.recency match
      case Recent => store -= x
      case Old => // do nothing