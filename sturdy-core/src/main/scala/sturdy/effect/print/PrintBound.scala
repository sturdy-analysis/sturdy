package sturdy.effect.print

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, Effect, Monotone, RetainBoth, TrySturdy}
import sturdy.values.{*, given}

import scala.util.boundary
import scala.util.boundary.break


trait Serializer[A, Serialized]:
  def serialize(a: A): Serialized

final class IdSerializer[A] extends Serializer[A,A]:
  override def serialize(a: A): A = a

class PrintBound[A: Join: Widen] extends PrintBoundSerializable[A,A](using serializer = IdSerializer[A], joinSerialized = implicitly, widenSerialized = implicitly)

class PrintBoundSerializable[A,S](using val serializer: Serializer[A,S], joinSerialized: Join[S], widenSerialized: Widen[S]) extends Print[A], Effect:
  protected var symbol: Option[S] = None

  override def apply(a: A): Unit =
    symbol match
      case None => symbol = Some(serializer.serialize(a))
      case Some(old) => symbol = Some(joinSerialized(old, serializer.serialize(a)).get)

  override type State = Option[S]
  override def getState: State = symbol
  override def setState(st: State): Unit =
    symbol = st
  private def combineSymbols(v1: State, v2: State, comb: (S, S) => MaybeChanged[S]): MaybeChanged[State] =
    (v1, v2) match
      case (_, None) => Unchanged(v1)
      case (None, Some(a)) => Changed(v2)
      case (Some(a1), Some(a2)) => comb(a1, a2).map(Some.apply)
  override def join: Join[State] = combineSymbols(_, _, joinSerialized.apply)
  override def widen: Widen[State] = combineSymbols(_, _, widenSerialized.apply)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = super.makeComputationJoiner.map(RetainBoth[A](_))

  def isSound[C](cp: CPrint[C])(using s: Soundness[C, S]): IsSound = boundary:
    cp.getPrinted.foreach { c =>
      val isSound = s.isSound(c, symbol.getOrElse(break(IsSound.NotSound(s"Abstract semantic predicted no prints, but got ${cp.getPrinted}"))))
      if (isSound.isNotSound)
        break(IsSound.NotSound(s"Concretely printed symbol $c not approximated by ${symbol.get}: ${isSound.toString}"))
    }
    IsSound.Sound

