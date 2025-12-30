package sturdy.control

import sturdy.effect.JoinObserver
import sturdy.effect.except.ExceptObserver

import scala.collection.mutable.ListBuffer

/**
 * Observer pattern.
 * Implemented by interpreters to send events to observers.
 */
trait ControlObservable[Atom, Section, Exc, Fx] extends JoinObserver, ExceptObserver[Exc]:
  private val observers: ListBuffer[ControlObserver[Atom, Section, Exc, Fx]] = ListBuffer.empty
  def addControlObserver(obs: ControlObserver[Atom, Section, Exc, Fx]): obs.type =
    observers += obs
    obs
  def removeControlObserver(obs: ControlObserver[Atom, Section, Exc, Fx]): Unit =
    observers -= obs

  def triggerControlEvent(ev: ControlEvent[Atom,Section,Exc,Fx]): Unit =
    observers.foreach(_.handle(ev))

  override def joinStart(): Unit = triggerControlEvent(BranchingControlEvent.Fork())
  override def joinSwitch(leftFailed: Boolean): Unit =
    triggerControlEvent(BranchingControlEvent.Switch())
  override def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit =
    triggerControlEvent(BranchingControlEvent.Join())

  override def repeating(): Unit =
    triggerControlEvent(FixpointControlEvent.Restart())
  
  override def throwing(exc: Exc): Unit = triggerControlEvent(ExceptionControlEvent.Throw(exc))
  override def handlingStart(exc: Exc): Unit = triggerControlEvent(ExceptionControlEvent.BeginHandle(exc))
  override def handlingEnd(): Unit = triggerControlEvent(ExceptionControlEvent.EndHandle())
  override def tryStart(): Unit = triggerControlEvent(ExceptionControlEvent.BeginTry())
  override def tryEnd(): Unit = triggerControlEvent(ExceptionControlEvent.EndTry())

  override def catchStart(): Unit = triggerControlEvent(ExceptionControlEvent.Catching())
  override def catchEnd(): Unit = ()

  


