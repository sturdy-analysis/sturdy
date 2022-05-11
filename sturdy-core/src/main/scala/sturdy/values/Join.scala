package sturdy.values

sealed trait Widening
object Widening:
  final class Yes extends Widening
  final class No extends Widening

enum MaybeChanged[A]:
  case Changed(a: A)
  case Unchanged(a: A)

  inline def hasChanged: Boolean = this match
    case Changed(_) => true
    case Unchanged(_) => false

  inline def get: A = this match
    case Changed(a) => a
    case Unchanged(a) => a

  inline def map[B](f: A => B): MaybeChanged[B] = this match
    case Changed(a) => MaybeChanged.Changed(f(a))
    case Unchanged(a) => MaybeChanged.Unchanged(f(a))

  inline def ifChanged(f: A => Unit): Unit = this match
    case Changed(a) => f(a)
    case Unchanged(_) => // nothing


object MaybeChanged:
  inline def apply[A](a: A, hasChanged: Boolean): MaybeChanged[A] =
    if (hasChanged) MaybeChanged.Changed(a) else MaybeChanged.Unchanged(a)
  inline def apply[A](a: A, previous: A): MaybeChanged[A] =
    if (a == previous) MaybeChanged.Unchanged(a) else MaybeChanged.Changed(a)
  def unapply[A](mc: MaybeChanged[A]): (A, Boolean) = mc match
    case Changed(a) => (a, true)
    case Unchanged(a) => (a, false)

def Changed[A](a: A) = MaybeChanged.Changed(a)
def Unchanged[A](a: A) = MaybeChanged.Unchanged(a)

trait Combine[V, W <: Widening]:
  def apply(v1: V, v2: V): MaybeChanged[V]

type Join[V] = Combine[V, Widening.No]
type Widen[V] = Combine[V, Widening.Yes]

object Combine:
  inline def apply[V, W <: Widening](v1: V, v2: V)(using j: Combine[V, W]): MaybeChanged[V] = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => Unchanged(r1)
    case _ => j(v1, v2)
  }
object Join:
  inline def apply[V](v1: V, v2: V)(using j: Join[V]): MaybeChanged[V] = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => Unchanged(r1)
    case _ => j(v1, v2)
  }
object Widen:
  inline def apply[V](v1: V, v2: V)(using j: Widen[V]): MaybeChanged[V] = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => Unchanged(r1)
    case _ => j(v1, v2)
  }

given finitely[V](using Join[V], Finite[V]): Widen[V] with
  def apply(v1: V, v2: V) = Join(v1, v2)

//trait Combinable[V]:
//  type CombineCtx[W]
//  def combine[W <: Widening](that: V): CombineCtx[W] ?=> V
//given CombineCombinable[V <: Combinable[V], W <: Widening]: Combine[V, W] with
//  inline def apply(v1: V, v2: V): v1.CombineCtx[W] ?=> V = v1.combine[W](v2)
//
//trait Joinable[V]:
//  type JoinCtx
//  def join(that: V): JoinCtx ?=> V
//given JoinJoinable[V <: Joinable[V]]: Join[V] with
//  inline def apply(v1: V, v2: V): v1.JoinCtx ?=> V = v1.join(v2)
//
//trait Widenable[V]:
//  type WidenCtx
//  def widen(that: V): WidenCtx ?=> V
//given WidenWidenable[V <: Widenable[V]]: Join[V] with
//  inline def apply(v1: V, v2: V): v1.WidenCtx ?=> V = v1.widen(v2)
