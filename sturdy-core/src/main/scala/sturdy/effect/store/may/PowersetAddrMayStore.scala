package sturdy.effect.store.may

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.*
import sturdy.values.{*, given}
import sturdy.{IsSound, Soundness}

import scala.collection.mutable.ListBuffer
import scala.reflect.Selectable.reflectiveSelectable


class PowersetAddrMayStore[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using Join[V], Widen[V], Finite[Addr])
  extends MayStore[Powerset[Addr], V, WithJoin], AbstractMayStore[Addr, V]:

  this.store = MayMap(_init)

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

  override def write(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    for x <- addrs do
      weakUpdate(x, v)

  override def free(xs: Powerset[Addr]): Unit =
    () // nothing

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Powerset[Addr]], vSoundness: Soundness[cV, V]): IsSound = {
    import sturdy.values

    val abstractedKeys = c.entries.keySet.flatMap(k => varAbstractly.apply(k).set)
    if (!abstractedKeys.subsetOf(store.m.keySet)) {
      val missing = c.entries.flatMap{ kv =>
        val k = kv._1
        val ak = varAbstractly.apply(k)
        if (ak.set.subsetOf(store.m.keySet))
          None
        else
          Some(s"abs($k->${kv._2})=$ak")
      }
      IsSound.NotSound(s"${classOf[PowersetAddrMayStore[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
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