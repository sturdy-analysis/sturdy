package sturdy.incremental

import sturdy.util.Eq
import sturdy.util.Eq.===

enum Change[A]:
  case Replace[A](from: A, to: A) extends Change[A]
  case Add(_new: A) extends Change[A]
  case Remove(old: A) extends Change[A]
  case Nil(old: A) extends Change[A]

  def map[B](f: A => B): Change[B] =
    this match
      case Change.Nil(x) => Change.Nil(f(x))
      case Change.Remove(old) => Change.Remove(f(old))
      case Change.Add(_new) => Change.Add(f(_new))
      case Change.Replace(from, to) => Change.Replace(f(from), f(to))

type Changes[A] = Iterator[Change[A]]

/**
 * Trait used to compute changes between two elements of A.
 */
trait Identifiable[A] extends Eq[A]:
  type Id
  extension(x: A)
    def id: Id

object ListDelta:
  def nil[A: Identifiable](x: List[A]) =
    ListDelta(List.empty).nil(x)
  def sub[A: Identifiable](xs: List[A], ys: List[A]) =
    ListDelta(List.empty).sub(xs, ys)

class ListDelta[A: Identifiable](val delta: List[Change[A]]) extends Delta[List[A]]:
  override def nil(x: List[A]): Delta[List[A]] = ListDelta(x.map(Change.Nil(_)))

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
            if(x === y) Change.Nil(x) :: _sub(xRest,yRest)
            else Change.Replace(from=x, to=y) :: _sub(xRest,yRest)
          else
            val (eqX, neqX) = yRest.partition(_.id.equals(x.id))
            val (eqY, neqY) = xRest.partition(_.id.equals(y.id))
            _sub(List(x), eqX) ++ _sub(eqY, List(y)) ++ _sub(neqX, neqY)
        case (_ :: _, Nil) => xs.map(Change.Remove(_))
        case (Nil, _ :: _) => ys.map(Change.Add(_))
        case (Nil, Nil) => List.empty

    ListDelta(_sub(xs, ys))

  override def add(_x: List[A]) = delta.flatMap{
    case Change.Nil(x) => Iterator(x)
    case Change.Add(y) => Iterator(y)
    case Change.Remove(_) => Iterator()
    case Change.Replace(_,y) => Iterator(y)
  }

  override def isNilChange: Boolean =
    delta.forall{
      case Change.Nil(_) => true
      case _ => false
    }
