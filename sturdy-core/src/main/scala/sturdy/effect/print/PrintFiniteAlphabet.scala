package sturdy.effect.print

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.Monotone
import sturdy.values.{*, given}

class PrintFiniteAlphabet[A](using Finite[A]) extends Print[A], Monotone:
  protected var symbols: Set[A] = Set()

  override def apply(a: A): Unit =
    symbols += a

  override type State = Powerset[A]
  override def getState: State = Powerset(symbols)
  override def setState(st: State): Unit = symbols = st.set
  override def mapState(st: State, f: [X] => X => X): State =
    st.map(f[A])
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly

  def isSound[C](c: CPrint[C])(using s: Soundness[C, A]): IsSound =
    c.getPrinted.foreach { c =>
      val aproxOk = symbols.exists(a => s.isSound(c, a).isSound)
      if (!aproxOk)
        return IsSound.NotSound(s"concrete printed $c symbol is not approximated by alphabet $symbols")
    }
    IsSound.Sound