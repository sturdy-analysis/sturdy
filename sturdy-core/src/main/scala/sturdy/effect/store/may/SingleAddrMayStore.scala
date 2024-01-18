package sturdy.effect.store.may

import sturdy.{IsSound, Soundness}
import sturdy.data.*
import sturdy.effect.Effect
import sturdy.effect.store.*
import sturdy.values.{Abstractly, Finite, Join, Widen}

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
class SingleAddrMayStore[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using Join[V], Widen[V], Finite[Addr])
  extends MayStore[Addr, V, WithJoin], AbstractMayStore[Addr, V]:

  this.store = MayMap(_init)

  override def read(x: Addr): JOptionA[V] =
    store.get(x) match
      case scala.None => JOptionA.none
      case scala.Some(v) =>
        if (x.isManaged)
          JOptionA.some(v)
        else
          JOptionA.noneSome(v)

  override def write(x: Addr, v: V): Unit =
    weakUpdate(x, v)

  override def free(x: Addr): Unit =
    () // nothing

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Addr], vSoundness: Soundness[cV, V]): IsSound = {
    val abstractedKeys = c.entries.keySet.map(varAbstractly.apply)
    if (!abstractedKeys.subsetOf(store.m.keySet)) {
      val missing = c.entries.keySet.flatMap{ k =>
        val ak = varAbstractly.apply(k)
        if (store.m.keySet.contains(ak))
          None
        else
          Some((k, ak))
      }
      IsSound.NotSound(s"${classOf[SingleAddrMayStore[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
    } else {
      c.entries.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, store(varAbstractly.apply(x)))
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }