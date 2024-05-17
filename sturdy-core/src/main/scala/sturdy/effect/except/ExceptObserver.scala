package sturdy.effect.except

import sturdy.effect.SturdyThrowable

trait ExceptObserver[Exc]:
  def throwing(exc: Exc): Unit
  def handlingStart(exc: Exc): Unit
  def handlingEnd(): Unit
  def tryStart(): Unit
  def tryEnd(): Unit
  def catchStart(): Unit
  def catchEnd(): Unit

class LiftedExceptObserver[Exc, UExc](lift: Exc => UExc, obs: ExceptObserver[UExc]) extends ExceptObserver[Exc]:
  override def throwing(exc: Exc): Unit = obs.throwing(lift(exc))
  override def handlingStart(exc: Exc): Unit = obs.handlingStart(lift(exc))
  override def handlingEnd(): Unit = obs.handlingEnd()
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
  protected def handlingStart(exc: Exc): Unit =
    observers.foreach(_.handlingStart(exc))
  protected def handlingEnd(): Unit =
    observers.foreach(_.handlingEnd())
  protected def tryStart(): Unit =
    observers.foreach(_.tryStart())
  protected def tryEnd(): Unit =
    observers.foreach(_.tryEnd())
  protected def catchStart(): Unit =
    observers.foreach(_.catchStart())
  protected def catchEnd(): Unit =
    observers.foreach(_.catchEnd())
  

object ObservableExcept:
  def None: ObservableExcept[Nothing] = new ObservableExcept {}