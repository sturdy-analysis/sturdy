package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, EffectStack, TrySturdy}
import sturdy.values.references.AbstractAddr
import sturdy.values.{*, given}

import scala.collection.mutable.ListBuffer
import reflect.Selectable.reflectiveSelectable

/** An abstract threaded store. */
class AStoreThreaded[A, AA <: AbstractAddr[A], V](_init: Map[A, V])(using Join[V], Widen[V], Finite[A])
  extends Store[AA, V, WithJoin]:

  private var store: Map[A, V] = _init

  override def read(xs: AA): JOptionA[V] =
    if (xs.isEmpty)
      JOptionA.None()
    else
      xs.reduce(x => JOptionA(store.get(x)))

  override def write(xs: AA, v: V): Unit =
    if (xs.isEmpty) {
      // nothing
    } else if (xs.isStrong) {
      xs.reduce(x => store += x -> v)
    } else {
      xs.reduce { x => store.get(x) match
        case None => store += x -> v
        case Some(old) => Join(old, v).ifChanged(vJ => store += x -> vJ)
      }
    }

  override def free(xs: AA): Unit =
    if (xs.isStrong)
      xs.reduce(x => store -= x)

  override type State = Map[A, V]
  override def getState: Map[A, V] = store
  override def setState(s: Map[A, V]): Unit = this.store = s
  override def join: Join[Map[A, V]] = implicitly
  override def widen: Widen[Map[A, V]] = implicitly

  def isSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, AA], vSoundness: Soundness[cV, V]): IsSound =
    c.entries.foreachEntry { case (a, v) =>
      val aa = varAbstractly(a)
      read(aa) match
        case JOptionA.None() => return IsSound.NotSound(s"Concrete address $a abstracts to $aa, which is not bound in store")
        case JOptionA.Some(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) return s
        case JOptionA.NoneSome(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) return s
    }
    IsSound.Sound
