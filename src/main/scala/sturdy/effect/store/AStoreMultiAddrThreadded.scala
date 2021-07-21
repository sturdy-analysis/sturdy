package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.JoinComputation
import sturdy.language.whilelang.ConcreteInterpreter
import sturdy.language.whilelang.analysis.SignAnalysis.Value
import sturdy.values.*

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreMultiAddrThreadded[Addr, V](_init: Map[Addr, (Boolean, V)])(using JoinValue[V])
  extends Store[Powerset[Addr], V], AStoreGenericThreadded[Addr, V]:

  this.store = _init
  
  override type StoreJoin[A] = JoinValue[A]
  
  override def read[A](xs: Powerset[Addr], found: V => A, notFound: => A): StoreJoined[A] = {
    var needsNotFound = false
    var as = ListBuffer[A]()
    for (x <- xs.set)
      store.get(x) match
        case None => needsNotFound = true
        case Some((definite, v)) =>
          as += found(v)
          if !definite then
            needsNotFound = true
    as.reduce(joinValues)
  }

  override def write(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    dirtyAddrs ++= addrs
    if addrs.size == 1 then
      store += addrs.head -> ((true, v))
    else for x <- addrs do
      store.get(x) match
        case None => store += x -> ((false, v))
        case Some((_, old)) => store += x -> ((false, joinValues(old, v)))

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Powerset[Addr]], vSoundness: Soundness[cV, V]): IsSound = {
    import sturdy.values.given

    val abstractedKeys = c.getStore.keySet.flatMap(k => varAbstractly.abstractly(k).set)
    if (!abstractedKeys.subsetOf(store.keySet)) {
      val missing = c.getStore.flatMap{ kv =>
        val k = kv._1
        val ak = varAbstractly.abstractly(k)
        if (ak.set.subsetOf(store.keySet))
          None
        else
          Some(s"abs($k->${kv._2})=$ak")
      }
      IsSound.NotSound(s"${classOf[AStoreMultiAddrThreadded[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
    } else if (store.exists(e => e._2._1 && !abstractedKeys.contains(e._1))) {
      val missing = store.filter(_._2._1).keySet -- abstractedKeys
      IsSound.NotSound(s"${classOf[AStoreMultiAddrThreadded[_, _]].getName}: Expected all definitely bound keys to be bound in concrete store, but $missing are missing in $store")
    } else {
      c.getStore.foreachEntry { case (x, v) =>
        val avs = this.read(varAbstractly.abstractly(x), Powerset(_), Powerset.empty)
        val subSound = Soundness.isSound(v, avs)
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }