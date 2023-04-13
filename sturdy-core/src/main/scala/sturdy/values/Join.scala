package sturdy.values

sealed trait Widening
object Widening:
  final class Yes extends Widening
  final class No extends Widening

trait Combine[V, W <: Widening] extends PartialOrder[V]:
  /**
   * Computes an upper bound `u` of its arguments `v1` and `v2`, i.e, `v1 ⊑ u ⊒ v2`.
   * If `Widening.No`, then `u` is the least upper bound `v1 ⊔ v2` of `v1` and `v2`.
   * If `Widening.Yes`, then the fixpoint iteration `x_{n+1} = apply(x_n, f(x_n))` is guaranteed to have a limit, i.e., there exits a `k` such that `x_{k+1} = x_k`.
   * `apply(v1,v2)` returns `Changed(u)` if `v1 ⊏ u` and `Unchanged(u)` if `u ⊑ v2`.
   */
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
  override def lteq(v1: V, v2: V): Boolean = summon[Join[V]].lteq(v1,v2)

enum MaybeChanged[+A]:
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

  def toOption: Option[A] = this match
    case Changed(a) => Some(a)
    case Unchanged(_) => None

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
