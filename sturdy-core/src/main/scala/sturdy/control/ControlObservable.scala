package sturdy.control

import sturdy.effect.JoinObserver

import scala.collection.mutable.ListBuffer

trait ControlObservable[Atom, Section, Exc] extends JoinObserver:
  private val observers: ListBuffer[ControlObserver[Atom, Section, Exc]] = ListBuffer.empty
  def addControlObserver(obs: ControlObserver[Atom, Section, Exc]): Unit =
    observers += obs
  def removeControlObserver(obs: ControlObserver[Atom, Section, Exc]): Unit =
    observers -= obs

  def triggerControlEvent(ev: ControlEvent[Atom, Section, Exc]): Unit =
    observers.foreach(_.handle(ev))

  override def joinStart(): Unit = triggerControlEvent(ControlEvent.Fork())
  override def joinSwitch(leftFailed: Boolean): Unit =
    if (leftFailed)
      println("#### Switch leftFailed")
    triggerControlEvent(ControlEvent.Switch())
  override def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit =
    if (leftFailed || rightFailed)
      println(s"#### Join leftFailed=$leftFailed, rightfailed=$rightFailed")
    triggerControlEvent(ControlEvent.Join())

  override def repeating(): Unit =
    triggerControlEvent(ControlEvent.FixpointRepeat())



