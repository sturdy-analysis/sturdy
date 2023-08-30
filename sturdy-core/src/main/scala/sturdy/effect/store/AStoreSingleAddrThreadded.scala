package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.*
import sturdy.values.Join
import sturdy.effect.{ComputationJoiner, Effect}
import sturdy.values.Abstractly
import sturdy.values.Finite
import sturdy.values.Widen

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
class AStoreSingleAddrThreadded[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using Join[V], Widen[V], Finite[Addr])
  extends AStore[Addr, V]:

  protected val store: AStoreGenericThreadded[Addr, V] = AStoreGenericThreadded(_init)
  
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

  override def strongUpdate(addr: Addr, value: V): Unit =
    store.strongUpdate(addr, value)

  override def weakUpdate(addr: Addr, value: V): Unit =
    store.weakUpdate(addr, value)

  override def free(x: Addr): Unit =
    () // nothing

  override def delete(x: Addr): Unit =
    store.delete(x)

  override final def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = store.makeComputationJoiner
  override final type State = store.State
  override final def getState: State = store.getState
  override final def setState(st: State): Unit = store.setState(st)
  override final def join: Join[State] = store.join
  override final def widen: Widen[State] = store.widen

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Addr], vSoundness: Soundness[cV, V]): IsSound = {
    val abstractedKeys = c.entries.keySet.map(varAbstractly.apply)
    if (!abstractedKeys.subsetOf(store.addrs)) {
      val missing = c.entries.keySet.flatMap{ k =>
        val ak = varAbstractly.apply(k)
        if (store.addrs.contains(ak))
          None
        else
          Some((k, ak))
      }
      IsSound.NotSound(s"${classOf[AStoreSingleAddrThreadded[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
    } else {
      c.entries.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, store(varAbstractly.apply(x)))
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }