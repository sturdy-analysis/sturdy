package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, EffectStack}
import sturdy.values.{*, given}

import scala.collection.mutable.ListBuffer
import reflect.Selectable.reflectiveSelectable

/*
 * An abstract threadded store. The store tracks if an address is definitely bound,
 * maybe bound, or unbound and calls the corresponding continuations upon read.
 * Internally, the store tracks dirty addresses that have been (re)writteb to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
class AStoreMultiAddrThreadded[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using Join[V], Widen[V], Finite[Addr])
  extends AStore[Powerset[Addr], V]:

  protected val store = AStoreGenericThreadded(_init)

  override def read(xs: Powerset[Addr]): JOptionA[V] = {
    var needsNotFound = false
    var vs: Option[V] = None
    for (x <- xs.set)
      if (!x.isManaged)
        needsNotFound = true
      store.get(x) match
        case None =>
          needsNotFound = true
        case Some(v) =>
          vs = vs match
            case None => Some(v)
            case Some(v_) => Some(Join(v_, v).get)
    if (vs.isEmpty)
      JOptionA.None()
    else if (needsNotFound)
      JOptionA.NoneSome(vs.get)
    else
      JOptionA.Some(vs.get)
  }

  override def write(xs: Powerset[Addr], v: V): Unit = weakUpdate(xs,v)

  override def strongUpdate(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    for x <- addrs do
      store.strongUpdate(x, v)

  override def weakUpdate(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    for x <- addrs do
      store.weakUpdate(x, v)

  override def free(xs: Powerset[Addr]): Unit =
    () // nothing

  override def delete(xs: Powerset[Addr]): Unit =
    val addrs = xs.set
    for x <- addrs do
      store.delete(x)

  override final def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = store.makeComputationJoiner
  override final type State = store.State
  override final def getState: State = store.getState
  override final def setState(st: State): Unit = store.setState(st)
  override final def join: Join[State] = store.join
  override final def widen: Widen[State] = store.widen


  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Powerset[Addr]], vSoundness: Soundness[cV, V]): IsSound = {
    import sturdy.values

    val abstractedKeys = c.entries.keySet.flatMap(k => varAbstractly.apply(k).set)
    if (!abstractedKeys.subsetOf(store.addrs)) {
      val missing = c.entries.flatMap{ kv =>
        val k = kv._1
        val ak = varAbstractly.apply(k)
        if (ak.set.subsetOf(store.addrs))
          None
        else
          Some(s"abs($k->${kv._2})=$ak")
      }
      IsSound.NotSound(s"${classOf[AStoreMultiAddrThreadded[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
    } else {
      c.entries.foreachEntry { case (x, v) =>
        val avs = this.read(varAbstractly.apply(x)) match
          case JOptionA.None() => Powerset()
          case JOptionA.NoneSome(a) => Powerset(a)
          case JOptionA.Some(a) => Powerset(a)
        val subSound = Soundness.isSound(v, avs)
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }