package sturdy.incremental

import sturdy.util.Eq
import sturdy.util.Eq.===

import scala.collection.{IterableOps, mutable}
import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

enum Change[A]:
  case Replace(from: A, to: A) extends Change[A]
  case Add(_new: A) extends Change[A]
  case Remove(old: A) extends Change[A]
  case Nil(old: A, _new: A) extends Change[A]

  def map[B](f: A => B): Change[B] =
    this match
      case Change.Nil(old, _new) => Change.Nil(f(old), f(_new))
      case Change.Remove(old) => Change.Remove(f(old))
      case Change.Add(_new) => Change.Add(f(_new))
      case Change.Replace(from, to) => Change.Replace(f(from), f(to))

  def get: A =
    this match
      case Change.Nil(_, _new) => _new
      case Change.Remove(x) => x
      case Change.Add(y) => y
      case Change.Replace(from, _) => from

  def isNil: Boolean =
    this match
      case Change.Nil(_,_) => true
      case _ => false

type Changes[A] = Iterator[Change[A]]

/**
 * Trait used to compute changes between two elements of A.
 */
trait Identifiable[A] extends Eq[A]:
  type Id
  extension(x: A)
    def id: Id

object ListDelta:
  def nil[A: Identifiable](x: List[A]): ListDelta[A] =
    ListDelta()(Map.empty).nil(x)
  def sub[A: Identifiable](xs: List[A], ys: List[A]): ListDelta[A] =
    ListDelta()(Map.empty).sub(xs, ys)

class ListDelta[A](using val idA: Identifiable[A])(val delta: Map[idA.Id, Change[A]]) extends Delta[List[A]]:
  override def nil(x: List[A]): ListDelta[A] = ListDelta()(x.map(x => (x.id, Change.Nil(x,x))).toMap)

  /**
   * Computes a list of differences between two lists xs and ys, which contains
   * - Nil(x) if x.id == y.id and x === y
   * - Replace(x,y) if x.id == y.id and !(x === y)
   * - Remove(x) if ys does not contain an y with x.id == y.id
   * - Add(y) if xs does not contain an x with x.id == y.id
   */
  override def sub(xs: List[A], ys: List[A]): ListDelta[A] =
    def _sub(xs: List[A], ys: List[A]): List[Change[A]] =
      (xs,ys) match
        case (x :: xRest, y :: yRest) =>
          if(x.id.equals(y.id))
            if(x === y) Change.Nil(x,y) :: _sub(xRest,yRest)
            else Change.Replace(from=x, to=y) :: _sub(xRest,yRest)
          else
            val (eqX, neqX) = yRest.partition(_.id.equals(x.id))
            val (eqY, neqY) = xRest.partition(_.id.equals(y.id))
            _sub(List(x), eqX) ++ _sub(eqY, List(y)) ++ _sub(neqX, neqY)
        case (_ :: _, Nil) => xs.map(Change.Remove(_))
        case (Nil, _ :: _) => ys.map(Change.Add(_))
        case (Nil, Nil) => List.empty

    ListDelta()(_sub(xs, ys).map(x => (x.get.id, x)).toMap)

  override def add(xs: List[A]): List[A] =
    xs.flatMap(x =>
      delta.get(x.id) match
        case Some(Change.Nil(_,y)) => Iterator(y)
        case Some(Change.Add(y)) => Iterator(y)
        case Some(Change.Remove(_)) => Iterator()
        case Some(Change.Replace(_, y)) => Iterator(y)
        case None => Iterator()
    )

  def replace(x: A): A =
    delta.get(x.id) match
      case Some(Change.Nil(_,y)) => y
      case Some(Change.Add(y)) => y
      case Some(Change.Remove(_)) => x
      case Some(Change.Replace(_, y)) => y
      case None => throw new IllegalArgumentException(s"$x does not appear in the list of changes")

  override def isNilChange: Boolean =
    delta.forall{
      case (_,Change.Nil(_,_)) => true
      case _ => false
    }


  def keepOld: Map[idA.Id, A] =
    delta.flatMap {
      case (id, Change.Nil(x,_)) => Iterator((id,x))
      case (id, Change.Add(y)) => Iterator((id,y))
      case (id, Change.Replace(_, y)) => Iterator((id, y))
      case (_, Change.Remove(_)) => Iterator()
    }

object IterableDelta:
  def nil[A: Identifiable: ClassTag](x: Iterable[A]): IterableDelta[A] =
    IterableDelta()(delta = ArraySeq.empty, added = ArraySeq.empty, remapping = Map.empty).nil(x)
  def sub[A: Identifiable: ClassTag](xs: Iterable[A], ys: Iterable[A]): IterableDelta[A] =
    IterableDelta()(delta = ArraySeq.empty, added = ArraySeq.empty, remapping = Map.empty).sub(xs, ys)
final class IterableDelta[A: ClassTag](using val idA: Identifiable[A])
                                      (val delta: ArraySeq[Change[A]],
                                       val added: ArraySeq[A],
                                       val remapping: Map[Int,Int]) extends Delta[Iterable[A]]:
  override def nil(x: Iterable[A]): IterableDelta[A] =
    new IterableDelta()(delta = ArraySeq.from(x.map(a => Change.Nil(a,a))),
      added = ArraySeq.empty,
      remapping = Map.empty)

  override def sub(x: Iterable[A], y: Iterable[A]): IterableDelta[A] =
    val xs: mutable.Map[idA.Id, (Int,A)] = mutable.HashMap.from(x.view.zipWithIndex.map((a,idx) => (a.id, (idx,a))))
    val ys: mutable.Map[idA.Id, (Int,A)] = mutable.HashMap.from(y.view.zipWithIndex.map((a,idx) => (a.id, (idx,a))))
    val remapping: mutable.Map[Int,Int] = mutable.Map.empty
    val delta: mutable.ArraySeq[Change[A]] = mutable.ArraySeq.fill(x.size)(null)
    val added: mutable.ArrayBuffer[A] = mutable.ArrayBuffer.empty

    val ids = xs.keySet.union(ys.keySet)
    for(id <- ids) {
      (xs.get(id), ys.get(id)) match
        case (Some((idx1,a1)), Some((idx2,a2))) =>
          if(idA.eqv(a1,a2)) {
            delta(idx1) = Change.Nil(a1, a2)
          } else {
            delta(idx1) = Change.Replace(a1, a2)
          }
          remapping += idx2 -> idx1
        case (Some((idx1,a1)), None) =>
          delta(idx1) = Change.Remove(a1)
        case (None, Some((idx2,a2))) =>
          added += a2
          remapping += idx2 -> (x.size + added.size - 1)

        case (None,None) => throw IllegalStateException("Cannot happen: The keys we are iterating over are the union of the keys of both maps.")
    }

    new IterableDelta[A]()(
      delta = ArraySeq.unsafeWrapArray(delta.array.asInstanceOf[Array[Change[A]]]),
      added = ArraySeq.unsafeWrapArray(added.toArray),
      remapping = remapping.toMap
    )

  override def add(x: Iterable[A]): Iterable[A] = ???

  override def isNilChange: Boolean = delta.forall(_.isNil) && added.isEmpty

  override def equals(obj: Any): Boolean =
    obj match
      case other: IterableDelta[A] =>
        this.delta.equals(other.delta) &&
        this.added.equals(other.added) &&
        this.remapping.equals(other.remapping)
      case _ => false

  override def hashCode(): Int =
    (delta,added,remapping).hashCode()

  override def toString: String =
    s"$delta + ${added.map(Change.Add(_))}\nremapping: $remapping"