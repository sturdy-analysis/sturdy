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

  override def toString: String =
    val esStr = PrintingControlObserver.toString(events, "  ", "\n")
    s"ControlEvents($esStr)"

class PrintingControlObserver[Atom, Section, Exc](_indent: String = "  ", sep: String = "\n")(perEvent: String => Unit) extends ControlObserver[Atom, Section, Exc]:
  import ControlEvent.*
  val buf = new StringBuffer()
  var indent = _indent

  private def occurrence(e: ControlEvent[Atom, Section, Exc]): Unit =
    val s = s"$indent$e"
    buf.append(s).append(sep)
    perEvent(s)

  override def handle(e: ControlEvent[Atom, Section, Exc]): Unit = e match
    case Begin(_) =>
      occurrence(e)
      indent += "  "
    case End(_) =>
      indent = indent.drop(2)
      occurrence(e)
    case Fork() =>
      occurrence(e)
      indent += "  "
    case Switch() =>
      indent = indent.drop(2)
      occurrence(e)
      indent += "  "
    case Join() =>
      indent = indent.drop(2)
      occurrence(e)
    case _ =>
      occurrence(e)

  def getString: String = buf.toString

object PrintingControlObserver:
  def toString[Atom,Section,Exc](es: List[ControlEvent[Atom,Section,Exc]], _indent: String, sep: String): String =
    val obs = new PrintingControlObserver[Atom,Section,Exc](_indent, sep)(_ => ())
    es.foreach(obs.handle)
    obs.getString