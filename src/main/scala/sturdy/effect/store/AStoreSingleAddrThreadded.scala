package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.JoinComputation
import sturdy.values.Abstractly
import sturdy.values.JoinValue

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreSingleAddrThreadded[Addr, V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Addr, V], AStoreGenericThreadded[Addr, V]:

  this.store = _init
  
  override type StoreJoin[A] = JoinValue[A]

  override def read[A](x: Addr, found: V => A, notFound: => A): StoreJoined[A] =
    store.get(x) match
      case None => notFound
      case Some((definite, v)) =>
        if definite then
          found(v)
        else
          joinValues(found(v), notFound)

  override def write(x: Addr, v: V): Unit =
    dirtyAddrs += x
    store += x -> ((true, v))

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
      IsSound.NotSound(s"${this.getClass.getName}: Expected all concrete keys to be contained, but $missing are missing in $this")
    } else if (store.exists(e => e._2._1 && !abstractedKeys.contains(e._1))) {
      val missing = store.filter(_._2._1).keySet -- abstractedKeys
      IsSound.NotSound(s"${this.getClass.getName}: Expected all definitely bound keys to be bound in concrete environment, but $missing are missing in $this")
    } else {
      c.getStore.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, store(varAbstractly.abstractly(x))._2)
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }