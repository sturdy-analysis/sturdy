package sturdy.effect.except

import sturdy.effect.SturdyException

trait ExceptObserver[Exc <: LanguageException]:
  def thrown(exc: Exc): Unit
  def handled(exc: Exc): Unit

trait ObservableExcept[Exc <: LanguageException]:
  private var observers: List[ExceptObserver[Exc]] = Nil
  def addExceptObserver(obs: ExceptObserver[Exc]): Unit =
    observers +:= obs

  protected def thrown(exc: Exc): Unit =
    observers.foreach(_.thrown(exc))
  def handled(exc: Exc): Unit =
    observers.foreach(_.handled(exc))

object ObservableExcept:
  def None: ObservableExcept[LanguageException] = new ObservableExcept[LanguageException] {}