package sturdy.control

import sturdy.effect.JoinObserver
import sturdy.effect.except.ExceptObserver

import scala.collection.mutable.ListBuffer

trait ControlObservable[Atom, Section, Exc] extends JoinObserver, ExceptObserver[Exc]:
  private val observers: ListBuffer[ControlObserver[Atom, Section, Exc]] = ListBuffer.empty
  def addControlObserver(obs: ControlObserver[Atom, Section, Exc]): Unit =
    observers += obs
  def removeControlObserver(obs: ControlObserver[Atom, Section, Exc]): Unit =
    observers -= obs

  def triggerControlEvent(ev: ControlEvent[Atom, Section, Exc]): Unit =
    observers.foreach(_.handle(ev))

  override def joinStart(): Unit = triggerControlEvent(ControlEvent.Fork())
  override def joinSwitch(leftFailed: Boolean): Unit =
    triggerControlEvent(ControlEvent.Switch())
  override def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit =
    triggerControlEvent(ControlEvent.Join())

  override def repeating(): Unit =
    triggerControlEvent(ControlEvent.FixpointRepeat())
  
  override def throwing(exc: Exc): Unit = triggerControlEvent(ControlEvent.Throw(exc))
  override def handling(exc: Exc): Unit = triggerControlEvent(ControlEvent.Handle(exc))
  override def tryStart(): Unit = triggerControlEvent(ControlEvent.BeginTry())
  override def tryEnd(): Unit = triggerControlEvent(ControlEvent.EndTry())
  override def catchStart(): Unit = triggerControlEvent(ControlEvent.Catching())
  override def catchEnd(): Unit = ()

  


