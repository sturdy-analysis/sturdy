package sturdy.effect.store

import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, JOptionA, MayJoin, MustMap}
import sturdy.effect.store.*
import sturdy.effect.store.must.AbstractMustStore
import sturdy.values.{*, given}
import sturdy.{IsSound, Soundness}

class PowersetAddrBackwardsStore[Addr <: ManageableAddr, V](_init: Map[Addr, V], assert: (V, V) => V)(using Join[V], Widen[V], Top[V], Top[Powerset[Addr]], Finite[Addr])
  extends BackwardStore[Powerset[Addr], V, WithJoin], AbstractMustStore[Addr, V]:

  this.store = MustMap(_init)

  override def write(xs: Powerset[Addr] => Powerset[Addr], v: V => V): Unit =
    val addrs = xs(Top.top[Powerset[Addr]]).set
    if (addrs.size == 1)
      weakUpdate(addrs.head, v(Top.top[V]), true)
    else for x <- addrs do
      weakUpdate(x, v(Top.top[V]), false)

  override def read(xs: Powerset[Addr] => Powerset[Addr]): V => JOption[MayJoin.WithJoin, V] =
    val addrs = xs(Top.top[Powerset[Addr]]).set
    if (addrs.isEmpty) {
      v => JOptionA.none
    } else {
      import scala.util.boundary
      boundary:
        val vs = for (a <- addrs) yield
          store.get(a) match
            case None =>
              val res: V => JOption[MayJoin.WithJoin, V] = v => JOptionA.NoneSome(Top.top[V])
              boundary.break(res)
            case Some(v) => v
              //refine tempV with expected
              //write refined to addr a
        (expected: V) => {
          val v = vs.reduce(Join.apply(_, _).get)
          val refined = this.assert(v, expected)
          write(xs, _ => v)
          JOptionA.Some(refined)
        }
    }

//  def storeIsSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, Powerset[Addr]], vSoundness: Soundness[cV, V]): IsSound = {
//    import sturdy.values
//    import scala.util.boundary
//  
//    val abstractedKeys = c.entries.keySet.flatMap(k => varAbstractly.apply(k).set)
//    boundary:
//      if (!abstractedKeys.subsetOf(store.m.keySet)) {
//        val missing = c.entries.flatMap { kv =>
//          val k = kv._1
//          val ak = varAbstractly.apply(k)
//          if (ak.set.subsetOf(store.m.keySet))
//            None
//          else
//            Some(s"abs($k->${kv._2})=$ak")
//        }
//        IsSound.NotSound(s"${classOf[PowersetAddrBackwardsStore[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $store")
//      } else {
//        c.entries.foreachEntry { case (x, v) =>
//          val avs = this.read(varAbstractly.apply(x)) match
//            case JOptionA.None() => Powerset()
//            case JOptionA.NoneSome(a) => Powerset(a)
//            case JOptionA.Some(a) => Powerset(a)
//          val subSound = Soundness.isSound(v, avs)
//          if (subSound.isNotSound)
//            boundary.break(subSound)
//        }
//        IsSound.Sound
//      }
//  }