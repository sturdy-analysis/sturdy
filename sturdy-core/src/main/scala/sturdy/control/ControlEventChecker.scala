package sturdy.control

import sturdy.control.FixpointControlEvent.BeginFixpoint

class ControlEventChecker[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:
  import ControlEventChecker.*

  enum Entry:
    case Sec
    case Try
    case Catching
    case Handle
    case ForkFirst
    case ForkSecond
    case Fixpoint(fx: Fx)

  private var stack: List[Entry] = List()

  private def pushEntry(e: Entry): Unit =
    stack = e :: stack

  private def updateEntry(ev: ControlEvent[Atom,Section,Exc,Fx])(f: PartialFunction[Entry, Option[Entry]]): Unit = stack match
    case Nil => error(s"No entry to close, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => error(s"Entry mismatch, expected end of $e: $ev")
      case Some(None) => stack = rest
      case Some(Some(replace)) => stack = replace :: rest

  private def updateThroughForks(ev: ControlEvent[Atom,Section,Exc,Fx])(f: PartialFunction[Entry, Option[Entry]]): Unit =
    stack = updateThroughForks_(stack, ev)(f)

  private def updateThroughForks_(entries: List[Entry], ev: ControlEvent[Atom,Section,Exc,Fx])(f: PartialFunction[Entry, Option[Entry]]): List[Entry] = entries match
    case Nil => error(s"No try entry to catch, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => e match
        case Entry.ForkFirst | Entry.ForkSecond => e :: updateThroughForks_(rest, ev)(f)
        case _ => error(s"Entry mismatch, expected end of $e: $ev")
      case Some(None) => rest
      case Some(Some(replace)) => replace :: rest

  
  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    ev match
      case BasicControlEvent.Atomic(a) => // fine
      case BasicControlEvent.Failed() => // fine
      case BasicControlEvent.BeginSection(sec: Section) => pushEntry(Entry.Sec)
      case BasicControlEvent.EndSection() => updateEntry(ev) { case Entry.Sec => None }

  override def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BranchingControlEvent.*
    ev match
      case Fork() => pushEntry(Entry.ForkFirst)
      case Switch() => updateEntry(ev) { case Entry.ForkFirst => Some(Entry.ForkSecond) }
      case BranchingControlEvent.Join() => updateEntry(ev) { case Entry.ForkSecond => None }

  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() => pushEntry(Entry.Try)
      case Throw(exc: Exc) => // fine
      case Catching() => updateEntry(ev) { case Entry.Try => Some(Entry.Catching) }
      case BeginHandle(exc: Exc) => 
        updateThroughForks(ev) { case Entry.Catching => Some(Entry.Catching) }
        pushEntry(Entry.Handle)
      case EndHandle() => updateEntry(ev) { case Entry.Handle => None }
      case EndTry() => updateEntry(ev) { case Entry.Try | Entry.Catching => None }

  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit =
    import FixpointControlEvent.*
    ev match
      case BeginFixpoint(fx) => pushEntry(Entry.Fixpoint(fx))
      case Recurrent(fx) =>
      case EndFixpoint() =>
        updateEntry(ev) { case Entry.Fixpoint(fx) => None }
      case Restart() => // fine


object ControlEventChecker:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)

