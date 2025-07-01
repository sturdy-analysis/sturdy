package sturdy.effect.store

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.values.references.AbstractAddr
import sturdy.values.{*, given}

import scala.util.boundary, boundary.break
import scala.reflect.ClassTag

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
        case Some(old) => store += x -> Join(old, v).get
      }
    }

  override def free(xs: AA): Unit =
    if (xs.isStrong)
      xs.reduce(x => store -= x)

  override type State = Map[A, V]
  override def getState: State = store
  override def setState(s: State): Unit = this.store = s
  override def setBottom: Unit = this.store = Map()
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    store.keysIterator.flatMap(valueIterator) ++ store.valuesIterator.flatMap(valueIterator)

  def isSound[cAddr, cV](c: CStore[cAddr, cV])(using varAbstractly: Abstractly[cAddr, AA], vSoundness: Soundness[cV, V]): IsSound = boundary:
    c.entries.foreachEntry { case (a, v) =>
      val aa = varAbstractly(a)
      read(aa) match
        case JOptionA.None() => break(IsSound.NotSound(s"Concrete address $a abstracts to $aa, which is not bound in store"))
        case JOptionA.Some(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) break(s)
        case JOptionA.NoneSome(av) =>
          val s = vSoundness.isSound(v, av)
          if (s.isNotSound) break(s)
    }
    IsSound.Sound
