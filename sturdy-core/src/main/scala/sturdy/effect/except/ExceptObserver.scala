package sturdy.effect.except

import sturdy.effect.SturdyException

trait ExceptObserver[Exc]:
  def throwing(exc: Exc): Unit
  def handling(exc: Exc): Unit
  def tryStart(): Unit
  def tryEnd(): Unit
  def catchStart(): Unit
  def catchEnd(): Unit

trait ObservableExcept[Exc]:
  private var observers: List[ExceptObserver[Exc]] = Nil
  def addExceptObserver(obs: ExceptObserver[Exc]): Unit =
    observers +:= obs

  protected def throwing(exc: Exc): Unit =
    observers.foreach(_.throwing(exc))
  protected def handling(exc: Exc): Unit =
    observers.foreach(_.handling(exc))
  protected def tryStart(): Unit =
    observers.foreach(_.tryStart())
  protected def tryEnd(): Unit =
    observers.foreach(_.tryEnd())
  protected def catchStart(): Unit =
    observers.foreach(_.catchStart())
  protected def catchEnd(): Unit =
    observers.foreach(_.catchEnd())
  
  inline def handling[A](exc: Exc, handle: Exc => A): A =
    handling(exc)
    handle(exc)
   

object ObservableExcept:
  def None: ObservableExcept[Nothing] = new ObservableExcept {}