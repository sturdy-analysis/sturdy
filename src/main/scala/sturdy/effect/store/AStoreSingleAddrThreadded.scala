package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.JoinComputation
import sturdy.values.Abstractly
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreSingleAddrThreadded[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using JoinValue[V])
  extends Store[Addr, V], AStoreGenericThreadded[Addr, V]:

  this.store = _init
  
  override type StoreJoin[A] = JoinValue[A]

  override def read[A](x: Addr, found: V => A, notFound: => A): StoreJoined[A] =
    store.get(x) match
      case None => notFound
      case Some(v) =>
        if (x.isManaged)
          found(v)
        else
          joinComputations(found(v))(notFound)

  override def write(x: Addr, v: V): Unit =
    weakUpdate(x, v)
  
  override def free(x: Addr): Unit =
    () // nothing

  override def scopedAddresses[A](xs: Iterable[Addr])(f: => A): A =
    val before = ListBuffer[(Addr, Option[V])]()
    for (x <- xs)
      before += x -> store.get(x)
    try f finally {
      for ((x, mv) <- before)
        dirtyAddrs -= x
        mv match
          case None => store -= x
          case Some(old) => store += x -> old
    }

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Addr], vSoundness: Soundness[cV, V]): IsSound = {
    val abstractedKeys = c.getStore.keySet.map(varAbstractly.abstractly)
    if (!abstractedKeys.subsetOf(store.keySet)) {
      val missing = c.getStore.keySet.flatMap{ k =>
        val ak = varAbstractly.abstractly(k)
        if (store.keySet.contains(ak))
          None
        else
          Some((k, ak))
      }
      IsSound.NotSound(s"${classOf[AStoreSingleAddrThreadded[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
    } else {
      c.getStore.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, store(varAbstractly.abstractly(x)))
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }