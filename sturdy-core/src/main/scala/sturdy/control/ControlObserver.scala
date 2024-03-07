package sturdy.control

import sturdy.control
import sturdy.control.BasicControlEvent.BeginSection

import scala.collection.mutable.ListBuffer

trait ControlObserver[Atom, Section, Exc,Fx]:
  final def handle(ev: ControlEvent): Unit = ev match
    case e: BasicControlEvent[Atom, Section] => handle(e)
    case e: ExceptionControlEvent[Exc] => handle(e)
    case e: BranchingControlEvent => handle(e)
    case e: FixpointControlEvent[Fx] => handle(e)

  def handle(ev: BasicControlEvent[Atom, Section]): Unit
  def handle(ev: ExceptionControlEvent[Exc]): Unit
  def handle(ev: BranchingControlEvent): Unit
  def handle(ev: FixpointControlEvent[Fx]): Unit

class RecordingControlObserver[Atom, Section, Exc, Fx] extends ControlObserver[Atom, Section, Exc, Fx]:
  private val buf: ListBuffer[ControlEvent] = ListBuffer.empty

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit = _handle(ev)
  override def handle(ev: ExceptionControlEvent[Exc]): Unit = _handle(ev)
  override def handle(ev: BranchingControlEvent): Unit = _handle(ev)
  override def handle(ev: FixpointControlEvent[Fx]): Unit = _handle(ev)

  private def _handle(ev: ControlEvent): Unit = buf += ev

  def events: List[ControlEvent] = buf.toList

  override def toString: String =
    val esStr = PrintingControlObserver.toString(events, "  ", "\n")
    s"ControlEvents($esStr)"

class PrintingControlObserver[Atom, Section, Exc, Fx](_indent: String = "  ", sep: String = "\n")(perEvent: String => Unit) extends ControlObserver[Atom, Section, Exc, Fx]:

  
  val buf = new StringBuffer()
  var indent = _indent

  private def occurrence(e: ControlEvent): Unit =
    val s = s"$indent$e"
    buf.append(s).append(sep)
    perEvent(s)

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    ev match
      case BeginSection(_) =>
        occurrence(ev)
        indent += "  "
      case EndSection() =>
        indent = indent.drop(2)
        occurrence(ev)
      case _ => occurrence(ev)

  override def handle(ev: ExceptionControlEvent[Exc]): Unit =
    import ExceptionControlEvent.*
    ev match
      case control.ExceptionControlEvent.BeginTry() =>
        occurrence(ev)
        indent += "  "
      case control.ExceptionControlEvent.Throw(_) => occurrence(ev)
      case control.ExceptionControlEvent.Catching() => throw new Exception("Should not happen")
      case control.ExceptionControlEvent.Handle(_) =>
        indent = indent.drop(2)
        occurrence(ev)
        indent += "  "
      case control.ExceptionControlEvent.EndTry() =>
        indent = indent.drop(2)
        occurrence(ev)

  override def handle(ev: BranchingControlEvent): Unit =
    import BranchingControlEvent.*
    ev match
      case BranchingControlEvent.Fork() =>
        occurrence(ev)
        indent += "  "
      case BranchingControlEvent.Switch() =>
        indent = indent.drop(2)
        occurrence(ev)
        indent += "  "
      case BranchingControlEvent.Join() =>
        indent = indent.drop(2)
        occurrence(ev)

  override def handle(ev: FixpointControlEvent[Fx]): Unit = ev match
    case sturdy.control.FixpointControlEvent.BeginFixpoint(fx) =>
      occurrence(ev)
      indent += "  "
    case sturdy.control.FixpointControlEvent.Recurrent(failing) =>
      occurrence(ev)
    case sturdy.control.FixpointControlEvent.EndFixpoint() =>
      indent = indent.drop(2)
      occurrence(ev)
    case sturdy.control.FixpointControlEvent.RepeatFixpoint() =>
      occurrence(ev)

  def getString: String = buf.toString

object PrintingControlObserver:
  def toString[Atom,Section,Exc,Fx](es: List[ControlEvent], _indent: String, sep: String): String =
    val obs = new PrintingControlObserver[Atom,Section,Exc,Fx](_indent, sep)(_ => ())
    es.foreach(obs.handle)
    obs.getString