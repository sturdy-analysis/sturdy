package sturdy.control

import sturdy.control.BasicControlEvent.Begin

import scala.collection.mutable.ListBuffer

trait ControlObserver[Atom, Section, Exc]:
  // TODO make final
  final def handle(ev: ControlEvent): Unit = ev match
    case e: BasicControlEvent[Atom, Section] => handle(e)
    case e: ExceptionControlEvent[Exc] => handle(e)
    case e: BranchingControlEvent => handle(e)
    case e: FixpointControlEvent => handle(e)

  def handle(ev: BasicControlEvent[Atom, Section]): Unit
  def handle(ev: ExceptionControlEvent[Exc]): Unit
  def handle(ev: BranchingControlEvent): Unit
  def handle(ev: FixpointControlEvent): Unit

class RecordingControlObserver[Atom, Section, Exc](check: Boolean = false) extends ControlObserver[Atom, Section, Exc]:
  private val buf: ListBuffer[ControlEvent] = ListBuffer.empty
  private val checker: Option[ControlEventChecker[Atom, Section, Exc]] = Option.when(check)(new ControlEventChecker)

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit = _handle(ev)
  override def handle(ev: ExceptionControlEvent[Exc]): Unit = _handle(ev)
  override def handle(ev: BranchingControlEvent): Unit = _handle(ev)
  override def handle(ev: FixpointControlEvent): Unit = _handle(ev)

  private def _handle(ev: ControlEvent): Unit =
    buf += ev
    checker.foreach(_.handle(ev))
  
  private def events: List[ControlEvent] = buf.toList

  override def toString: String =
    val esStr = PrintingControlObserver.toString(events, "  ", "\n")
    s"ControlEvents($esStr)"

class PrintingControlObserver[Atom, Section, Exc](_indent: String = "  ", sep: String = "\n")(perEvent: String => Unit) extends ControlObserver[Atom, Section, Exc]:

  
  val buf = new StringBuffer()
  var indent = _indent

  private def occurrence(e: ControlEvent): Unit =
    val s = s"$indent$e"
    buf.append(s).append(sep)
    perEvent(s)

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    ev match
      case Begin(_) =>
        occurrence(ev)
        indent += "  "
      case End(_) =>
        indent = indent.drop(2)
        occurrence(ev)
      case _ => occurrence(ev)

  override def handle(ev: ExceptionControlEvent[Exc]): Unit = occurrence(ev)
    
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
    
  override def handle(ev: FixpointControlEvent): Unit = occurrence(ev)
  
  def getString: String = buf.toString

object PrintingControlObserver:
  def toString[Atom,Section,Exc](es: List[ControlEvent], _indent: String, sep: String): String =
    val obs = new PrintingControlObserver[Atom,Section,Exc](_indent, sep)(_ => ())
    es.foreach(obs.handle)
    obs.getString