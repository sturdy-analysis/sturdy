package sturdy.control

import sturdy.effect.JoinObserver
import sturdy.effect.except.ExceptObserver

import java.nio.file.Path
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex

/**
 * Observer pattern.
 * Implemented by interpreters to sen events to observers.
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

// TODO : Incomplete
class FileReaderControlObservable(path: Path) extends ControlObservable[String, String, String, String] with ExceptObserver[String]:


  val regAtomic = "Atomic(.*)(.*).*".r
  val regFork = "Fork.*".r
  val regSwitch = "Switch.*".r
  val regJoin = "Join.*".r
  val regRestart = "Restart.*".r

  def read(): Unit = {
    val source = Source.fromFile(path.toFile)
    for (line <- source.getLines())
      val event : ControlEvent[String, String, String, String] =
        line match
          case regAtomic(a1, a2) =>
            print(a1, a2)
            BasicControlEvent.Atomic(a1)(a2)
          case regFork() => BranchingControlEvent.Fork()
          case regSwitch() => BranchingControlEvent.Switch()
          case regJoin() => BranchingControlEvent.Join()
          case regRestart() => FixpointControlEvent.Restart()
          case _ => throw new Exception(s"Couldn't parse '$line''")
      triggerControlEvent(event)
    source.close()
  }
  


