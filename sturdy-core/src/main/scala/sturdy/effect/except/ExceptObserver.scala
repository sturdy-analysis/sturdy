package sturdy.effect.except

import sturdy.effect.SturdyThrowable

trait ExceptObserver[Exc]:
  def throwing(exc: Exc): Unit
  def handling(exc: Exc): Unit
  def tryStart(): Unit
  def tryEnd(): Unit
  def catchStart(): Unit
  def catchEnd(): Unit

class LiftedExceptObserver[Exc, UExc](lift: Exc => UExc, obs: ExceptObserver[UExc]) extends ExceptObserver[Exc]:
  override def throwing(exc: Exc): Unit = obs.throwing(lift(exc))
  override def handling(exc: Exc): Unit = obs.handling(lift(exc))
  override def tryStart(): Unit = obs.tryStart()
  override def tryEnd(): Unit = obs.tryEnd()
  override def catchStart(): Unit = obs.catchStart()
  override def catchEnd(): Unit = obs.catchEnd()

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