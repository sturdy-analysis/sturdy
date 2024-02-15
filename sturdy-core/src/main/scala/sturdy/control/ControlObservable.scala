package sturdy.control

import sturdy.effect.JoinObserver
import sturdy.effect.except.ExceptObserver

import scala.collection.mutable.ListBuffer

trait ControlObservable[Atom, Section, Exc] extends JoinObserver, ExceptObserver[Exc]:
  private val observers: ListBuffer[ControlObserver[Atom, Section, Exc]] = ListBuffer.empty
  def addControlObserver(obs: ControlObserver[Atom, Section, Exc]): obs.type =
    observers += obs
    obs
  def removeControlObserver(obs: ControlObserver[Atom, Section, Exc]): Unit =
    observers -= obs

  def triggerControlEvent(ev: ControlEvent): Unit =
    observers.foreach(_.handle(ev))

  override def joinStart(): Unit = triggerControlEvent(BranchingControlEvent.Fork())
  override def joinSwitch(leftFailed: Boolean): Unit =
    triggerControlEvent(BranchingControlEvent.Switch())
  override def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit =
    triggerControlEvent(BranchingControlEvent.Join())

  override def repeating(): Unit =
    triggerControlEvent(FixpointControlEvent.RepeatFixpoint())
  
  override def throwing(exc: Exc): Unit = triggerControlEvent(ExceptionControlEvent.Throw(exc))
  override def handling(exc: Exc): Unit = triggerControlEvent(ExceptionControlEvent.Handle(exc))
  override def tryStart(): Unit = triggerControlEvent(ExceptionControlEvent.BeginTry())
  override def tryEnd(): Unit = triggerControlEvent(ExceptionControlEvent.EndTry())

  override def catchStart(): Unit = ()
  override def catchEnd(): Unit = ()

  


