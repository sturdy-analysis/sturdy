package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.values.{*, given}

import scala.collection.mutable.ListBuffer
import reflect.Selectable.reflectiveSelectable

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
class AStoreMultiAddrThreadded[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using Join[V])
  extends Store[Powerset[Addr], V, WithJoin], AStoreGenericThreadded[Addr, V]:

  this.store = _init

  override def read(xs: Powerset[Addr]): JOptionA[V] = {
    var needsNotFound = false
    val vs = ListBuffer[V]()
    for (x <- xs.set)
      if (!x.isManaged)
        needsNotFound = true
      store.get(x) match
        case None =>
          needsNotFound = true
        case Some(v) => vs += v
    if (vs.isEmpty)
      JOptionA.None()
    else if (needsNotFound)
      JOptionA.NoneSome(vs.reduce(Join(_, _).get))
    else
      JOptionA.Some(vs.reduce(Join(_, _).get))
  }

  override def write(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    for x <- addrs do
      weakUpdate(x, v)

  override def free(xs: Powerset[Addr]): Unit =
    () // nothing

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Powerset[Addr]], vSoundness: Soundness[cV, V]): IsSound = {
    import sturdy.values

    val abstractedKeys = c.getState.keySet.flatMap(k => varAbstractly.abstractly(k).set)
    if (!abstractedKeys.subsetOf(store.keySet)) {
      val missing = c.getState.flatMap{ kv =>
        val k = kv._1
        val ak = varAbstractly.abstractly(k)
        if (ak.set.subsetOf(store.keySet))
          None
        else
          Some(s"abs($k->${kv._2})=$ak")
      }
      IsSound.NotSound(s"${classOf[AStoreMultiAddrThreadded[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
    } else {
      given EffectStack = EffectStack(List(this))
      c.getState.foreachEntry { case (x, v) =>
        val avs = this.read(varAbstractly.abstractly(x)).option(Powerset[V]())(Powerset(_))
        val subSound = Soundness.isSound(v, avs)
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }