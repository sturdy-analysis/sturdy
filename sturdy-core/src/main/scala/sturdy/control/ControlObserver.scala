package sturdy.control

import scala.collection.mutable.ListBuffer

trait ControlObserver[Atom, Section, Exc]:
  def handle(ev: ControlEvent[Atom, Section, Exc]): Unit

class RecordingControlObserver[Atom, Section, Exc](check: Boolean = false) extends ControlObserver[Atom, Section, Exc]:
  private val buf: ListBuffer[ControlEvent[Atom, Section, Exc]] = ListBuffer.empty
  private val checker: Option[ControlEventChecker[Atom, Section, Exc]] = Option.when(check)(new ControlEventChecker)
  
  override def handle(ev: ControlEvent[Atom, Section, Exc]): Unit = 
    buf += ev
    checker.foreach(_.handle(ev))
  
  def events: List[ControlEvent[Atom, Section, Exc]] = buf.toList

  override def toString: String = s"ControlEvents(${events.mkString("\n  ", "\n  ", "\n")})"
