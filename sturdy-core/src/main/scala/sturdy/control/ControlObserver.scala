package sturdy.control

import sturdy.control
import sturdy.control.BasicControlEvent.BeginSection

import scala.collection.mutable.ListBuffer

/**
 * Observer pattern.
 * Used to receive and process control events online.
*/
trait ControlObserver[Atom, Section, Exc,Fx]:
  final def handle(ev: ControlEvent[Atom,Section,Exc,Fx]): Unit = ev match
    case e: BasicControlEvent[Atom,Section,Exc,Fx] => handle(e)
    case e: ExceptionControlEvent[Atom,Section,Exc,Fx] => handle(e)
    case e: BranchingControlEvent[Atom,Section,Exc,Fx] => handle(e)
    case e: FixpointControlEvent[Atom,Section,Exc,Fx] => handle(e)

  def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit
  def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit
  def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit
  def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit

class RecordingControlObserver[Atom, Section, Exc, Fx] extends ControlObserver[Atom, Section, Exc, Fx]:
  private val buf: ListBuffer[ControlEvent[Atom,Section,Exc,Fx]] = ListBuffer.empty

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit = _handle(ev)
  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit = _handle(ev)
  override def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit = _handle(ev)
  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit = _handle(ev)

  private def _handle(ev: ControlEvent[Atom,Section,Exc,Fx]): Unit = buf += ev

  def events: List[ControlEvent[Atom,Section,Exc,Fx]] = buf.toList

  override def toString: String =
    val esStr = PrintingControlObserver.toString(events, "  ", "\n")
    s"ControlEvents($esStr)"

class PrintingControlObserver[Atom, Section, Exc, Fx](_indent: String = "  ", sep: String = "\n")(perEvent: String => Unit) extends ControlObserver[Atom, Section, Exc, Fx]:

  
  val buf = new StringBuffer()
  var indent = _indent

  private def occurrence(e: ControlEvent[Atom,Section,Exc,Fx]): Unit =
    val s = s"$indent$e"
    buf.append(s).append(sep)
    perEvent(s)

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    ev match
      case BeginSection(_) =>
        occurrence(ev)
        indent += "  "
      case EndSection() =>
        indent = indent.drop(2)
        occurrence(ev)
      case _ => occurrence(ev)

  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case control.ExceptionControlEvent.BeginTry() =>
        occurrence(ev)
        indent += "  "
      case control.ExceptionControlEvent.Throw(_) => occurrence(ev)
      case control.ExceptionControlEvent.Catching() =>
        indent = indent.drop(2)
        occurrence(ev)
        indent += "  "
      case control.ExceptionControlEvent.BeginHandle(_) =>
        occurrence(ev)
        indent += "  "
      case control.ExceptionControlEvent.EndHandle() =>
        indent = indent.drop(2)
        occurrence(ev)
      case control.ExceptionControlEvent.EndTry() =>
        indent = indent.drop(2)
        occurrence(ev)

  override def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit =
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

  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit = ev match
    case FixpointControlEvent.BeginFixpoint(fx) =>
      occurrence(ev)
      indent += "  "
    case FixpointControlEvent.Recurrent(failing) =>
      occurrence(ev)
    case FixpointControlEvent.EndFixpoint() =>
      indent = indent.drop(2)
      occurrence(ev)
    case FixpointControlEvent.Restart() =>
      occurrence(ev)


  def getString: String = buf.toString

object PrintingControlObserver:
  def toString[Atom,Section,Exc,Fx](es: List[ControlEvent[Atom,Section,Exc,Fx]], _indent: String, sep: String): String =
    val obs = new PrintingControlObserver[Atom,Section,Exc,Fx](_indent, sep)(_ => ())
    es.foreach(obs.handle)
    obs.getString