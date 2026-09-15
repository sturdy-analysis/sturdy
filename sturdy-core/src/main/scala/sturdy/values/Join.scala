package sturdy.values

/**
 * Computes an upper bound of two values according to a partial order.
 *
 * If `W` is [[Widening.No]], then the upper bound is the ''least'' upper bound.
 * If `W` is [[Widening.Yes]], then successive applications of [[Combine.apply]] stabilize at some point.
 */
trait Combine[V, W <: Widening]:
  /**
   * Computes an upper bound `u` of two values `v1` and `v2` according to a partial order (⊑).
   * @return `Changed(u)` if `v1 ⊏ u`
   * @return `Unchanged(u)` if `u ⊑ v1`
   */
  def apply(v1: V, v2: V): MaybeChanged[V]

object Combine:
  inline def apply[V, W <: Widening](v1: V, v2: V)(using j: Combine[V, W]): MaybeChanged[V] = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => Unchanged(r1)
    case _ => j(v1, v2)
  }

/** Marker if a given combinator is a widening operator. */
sealed trait Widening
object Widening:
  final class Yes extends Widening
  final class No extends Widening


type Join[V] = Combine[V, Widening.No]
object Join:
  inline def apply[V](v1: V, v2: V)(using j: Join[V]): MaybeChanged[V] = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => Unchanged(r1)
    case _ => j(v1, v2)
  }

type Widen[V] = Combine[V, Widening.Yes]
object Widen:
  inline def apply[V](v1: V, v2: V)(using j: Widen[V]): MaybeChanged[V] = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => Unchanged(r1)
    case _ => j(v1, v2)
  }

trait StackWidening[V]:
  /**
   * Ensures that every stack has a recurrent recursive call by overapproximating the current call. More formally:
   * Given an infinite sequence of calls C, then the sequence W_n := StackWidening(W_1..W_n-1, C_n) terminates with W_j = W_j+1 for some j and W_i >= C_i for all i.
   */
  def apply(stack: List[V], call: V): MaybeChanged[V]


/** Indicates if the result of an upper bound combinator has grown. */
enum MaybeChanged[+A]:
  case Changed(a: A)
  case Unchanged(a: A)

  def hasChanged: Boolean = this match
    case Changed(_) => true
    case Unchanged(_) => false

  inline def get: A = this match
    case Changed(a) => a
    case Unchanged(a) => a

  inline def map[B](f: A => B): MaybeChanged[B] = this match
    case Changed(a) => MaybeChanged.Changed(f(a))
    case Unchanged(a) => MaybeChanged.Unchanged(f(a))

  inline def flatMap[B](f: A => MaybeChanged[B]): MaybeChanged[B] = this match
    case Changed(a) => MaybeChanged.Changed(f(a).get)
    case Unchanged(a) => f(a)

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

inline def Changed[A](a: A) = MaybeChanged.Changed(a)
inline def Unchanged[A](a: A) = MaybeChanged.Unchanged(a)

final class CannotJoinException(message: String) extends RuntimeException(message)

final class CombineCheckTermination[V, W <: Widening](using combine: Combine[V,W]) extends Combine[V,W]:
  override def apply(v1: V, v2: V): MaybeChanged[V] =
    val result = combine(v1, v2)
    if(result.get.equals(v1) || v1.equals(result.get)) {
        assert(! result.hasChanged, s"In combine($v1,$v2), result ${result.get} equal to $v1, but result indicates change.")
    }
    result

/** If `V` a lattice of finite height, then there is no need to widen. */
given finitely[V](using Join[V], Finite[V]): Widen[V] with
  def apply(v1: V, v2: V) = Join(v1, v2)