package sturdy.effect.print

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.Monotone
import sturdy.values.{*, given}

class PrintBound[A](using Join[A], Widen[A]) extends Print[A], Monotone:
  protected var symbol: Option[A] = None

  override def apply(a: A): Unit =
    symbol match
      case None => symbol = Some(a)
      case Some(old) => symbol = Some(Join(old, a).get)

  override type State = Option[A]
  override def getState: Option[A] = symbol
  override def setState(st: Option[A]): Unit =
    symbol = st

  def combineSymbols(v1: Option[A], v2: Option[A], comb: (A, A) => MaybeChanged[A]): MaybeChanged[Option[A]] = (v1, v2) match
    case (None, None) => Unchanged(None)
    case (Some(a), None) => Unchanged(v1)
    case (None, Some(a)) => Changed(v2)
    case (Some(a1), Some(a2)) => comb(a1, a2).map(Some.apply)
  override def join: Join[Option[A]] = combineSymbols(_, _, summon[Join[A]].apply)
  override def widen: Widen[Option[A]] = combineSymbols(_, _, summon[Widen[A]].apply)

  def isSound[C](cp: CPrint[C])(using s: Soundness[C, A]): IsSound =
    cp.getPrinted.foreach { c =>
      val isSound = s.isSound(c, symbol.getOrElse(return IsSound.NotSound(s"Abstract semantic predicted no prints, but got ${cp.getPrinted}")))
      if (isSound.isNotSound)
        return isSound
    }
    IsSound.Sound

