package sturdy.effect.store.must

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.*
import sturdy.values.{*, given}
import sturdy.{IsSound, Soundness}

import scala.collection.mutable.ListBuffer
import scala.reflect.Selectable.reflectiveSelectable


class PowersetAddrMustStore[Addr <: ManageableAddr, V](_init: Map[Addr, V])(using Join[V], Widen[V], Top[V], Finite[Addr])
  extends MustStore[Powerset[Addr], V, WithJoin], AbstractMustStore[Addr, V]:

  this.store = MustMap(_init)

  override def read(xs: Powerset[Addr]): JOptionA[V] =
    val addrs = xs.set
    if (addrs.isEmpty) {
      JOptionA.none
    } else {
      import scala.util.boundary
      boundary:
        val vs = for (a <- addrs) yield
          store.get(a) match
            case None => boundary.break(JOptionA.NoneSome(Top.top[V]))
            case Some(v) => v
        JOptionA.Some(vs.reduce(Join.apply(_, _).get))
    }

  override def write(xs: Powerset[Addr], v: V): Unit =
    val addrs = xs.set
    if (addrs.size == 1)
      weakUpdate(addrs.head, v, true)
    else for x <- addrs do
      weakUpdate(x, v, false)

  override def free(xs: Powerset[Addr]): Unit =
    xs.foreach(store -= _)

  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Powerset[Addr]], vSoundness: Soundness[cV, V]): IsSound = {
    import sturdy.values
    import scala.util.boundary

    val abstractedKeys = c.entries.keySet.flatMap(k => varAbstractly.apply(k).set)
    boundary:
      if (!abstractedKeys.subsetOf(store.m.keySet)) {
        val missing = c.entries.flatMap{ kv =>
          val k = kv._1
          val ak = varAbstractly.apply(k)
          if (ak.set.subsetOf(store.m.keySet))
            None
          else
            Some(s"abs($k->${kv._2})=$ak")
        }
        IsSound.NotSound(s"${classOf[PowersetAddrMustStore[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
      } else {
        c.entries.foreachEntry { case (x, v) =>
          val avs = this.read(varAbstractly.apply(x)) match
            case JOptionA.None() => Powerset()
            case JOptionA.NoneSome(a) => Powerset(a)
            case JOptionA.Some(a) => Powerset(a)
          val subSound = Soundness.isSound(v, avs)
          if (subSound.isNotSound)
            boundary.break(subSound)
        }
        IsSound.Sound
      }
  }