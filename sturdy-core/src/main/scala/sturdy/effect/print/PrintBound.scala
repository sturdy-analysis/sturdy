package sturdy.effect.print

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, Effect, Monotone, RetainBoth, TrySturdy}
import sturdy.values.{*, given}

import scala.util.boundary
import scala.util.boundary.break

class PrintBound[A: Join: Widen] extends Print[A], Effect:
  protected var symbol: Option[A] = None

  override def apply(a: A): Unit =
    symbol match
      case None => symbol = Some(a)
      case Some(old) => symbol = Some(Join(old, a).get)

  override type State = Option[A]
  override def getState: State = symbol
  override def setState(st: State): Unit =
    symbol = st
  private def combineSymbols(v1: State, v2: State, comb: (A, A) => MaybeChanged[A]): MaybeChanged[State] =
    (v1, v2) match
      case (_, None) => Unchanged(v1)
      case (None, Some(a)) => Changed(v2)
      case (Some(a1), Some(a2)) => comb(a1, a2).map(Some.apply)
  override def join: Join[State] = combineSymbols(_, _, Join.apply)
  override def widen: Widen[State] = combineSymbols(_, _, Widen.apply)

//  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = super.makeComputationJoiner.map(RetainBoth[A](_))

  def isSound[C](cp: CPrint[C])(using s: Soundness[C, A]): IsSound = boundary:
    cp.getPrinted.foreach { c =>
      val isSound = s.isSound(c, symbol.getOrElse(break(IsSound.NotSound(s"Abstract semantic predicted no prints, but got ${cp.getPrinted}"))))
      if (isSound.isNotSound)
        break(IsSound.NotSound(s"Concretely printed symbol $c not approximated by ${symbol.get}: ${isSound.toString}"))
    }
    IsSound.Sound

