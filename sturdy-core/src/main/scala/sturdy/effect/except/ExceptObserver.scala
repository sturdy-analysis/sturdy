package sturdy.effect.except

import sturdy.effect.SturdyException

trait ExceptObserver[Exc]:
  def handled(exc: Exc): Unit

trait ObservableExcept[Exc]:
  private var observers: List[ExceptObserver[Exc]] = Nil
  def addExceptObserver(obs: ExceptObserver[Exc]): Unit =
    observers +:= obs

  def foreachException(ex: SturdyException)(f: Exc => Unit): Unit
  def handled(exc: Exc): Unit =
    observers.foreach(_.handled(exc))

object ObservableExcept:
  def None: ObservableExcept[Unit] = new ObservableExcept[Unit] {
    override def foreachException(ex: SturdyException)(f: Unit => Unit): Unit = {}
  }