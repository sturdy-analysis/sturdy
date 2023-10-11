package sturdy.effect.store

import sturdy.effect.Effect
import sturdy.values.integer.NumericInterval
import sturdy.values.{references, *, given}
import sturdy.values.references.{*, given}
import sturdy.data.{*,given}

trait ClosedEquality[Cls, A]:
  def closedEquals(closure1: Cls, a1: A, closure2: Cls, a2: A): Boolean
  def closedHashCode(closure: Cls, a: A): Int

object ClosedEquality:
  def apply[Cls, A](closure1: Cls, a1: A, closure2: Cls, a2: A)(using eq: ClosedEquality[Cls,A]): Boolean =
    eq.closedEquals(closure1, a1, closure2, a2)

object ClosedHashCode:
  def apply[Cls, A](closure: Cls, a: A)(using eq: ClosedEquality[Cls,A]): Int =
    eq.closedHashCode(closure, a)

class AddressClosure[Cls, A](val closure: Cls, val a: A)(using ClosedEquality[Cls, A]):
  override def equals(obj: Any): Boolean =
    try{
      obj match
        case other: AddressClosure[?, ?] =>
          ClosedEquality(
            this.closure,
            this.a,
            other.closure.asInstanceOf,
            other.a.asInstanceOf)
        case _ => false
    } catch {
      case _: ClassCastException => false
    }

  override def hashCode(): Int =
    ClosedHashCode(this.closure, this.a)

given addressClosureCombine[Cls, A, W <: Widening](using Combine[Cls, W], Combine[A, W], ClosedEquality[Cls, A]): Combine[AddressClosure[Cls, A], W] with
  override def apply(v1: AddressClosure[Cls, A], v2: AddressClosure[Cls, A]): MaybeChanged[AddressClosure[Cls, A]] =
    Combine((v1.closure, v1.a), (v2.closure, v2.a)).map(AddressClosure(_, _))

/**
 * Closed equality for hashmaps. Closes equality on values, not on keys.
 */
given mapClosedEquality[Cls, K, V] (using ClosedEquality[Cls, V]): ClosedEquality[Cls, Map[K,V]] with
  override def closedEquals(cls1: Cls, m1: Map[K, V], closure2: Cls, m2: Map[K, V]): Boolean =
    m1.view.mapValues(AddressClosure(cls1, _)) == m2.view.mapValues((AddressClosure(closure2, _)))
  override def closedHashCode(cls: Cls, m: Map[K, V]): Int =
    m.view.mapValues(AddressClosure(cls, _)).hashCode()

given numericIntervalEquality[Cls, I]: ClosedEquality[Cls, NumericInterval[I]] with
  override def closedEquals(cls1: Cls, n1: NumericInterval[I], closure2: Cls, n2: NumericInterval[I]): Boolean =
    n1 == n2
  override def closedHashCode(cls: Cls, n: NumericInterval[I]): Int =
    n.hashCode()

class RecencyClosure[Context](val addressTranslation: AddressTranslation[Context], val effect: Effect)(using ClosedEquality[addressTranslation.State, effect.State]) extends Effect:

  override type State = AddressClosure[addressTranslation.State, effect.State]

  override def getState: State =
    new AddressClosure(addressTranslation.getState, effect.getState)

  override def setState(st: State): Unit =
    addressTranslation.setState(st.closure)
    effect.setState(st.a)

  override def join: Join[State] =
    given addrTransJoin: Join[addressTranslation.State] = addressTranslation.join
    given effectJoin: Join[effect.State] = effect.join
    implicitly[Join[State]]

  override def widen: Widen[State] =
    given addrTransWiden: Widen[addressTranslation.State] = addressTranslation.widen
    given effectWiden: Widen[effect.State] = effect.widen
    implicitly[Widen[State]]
