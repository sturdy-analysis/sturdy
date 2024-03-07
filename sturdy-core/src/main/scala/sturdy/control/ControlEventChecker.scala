package sturdy.control

import sturdy.control.FixpointControlEvent.BeginFixpoint

class ControlEventChecker[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:
  import ControlEventChecker.*

  enum Entry:
    case Sec
    case Try
    case Catching
    case ForkFirst
    case ForkSecond
    case Fixpoint(fx: Fx)

  private var stack: List[Entry] = List()
  private var fixpoints: Map[Fx, Int] = Map()

  def addFixpoint(fx: Fx): Unit =
    fixpoints += fx -> (fixpoints.getOrElse(fx, 0) + 1)

  def removeFixpoint(fx: Fx): Unit =
    val c = fixpoints.getOrElse(fx, 0)
    if (c > 0)
      fixpoints += fx -> (c - 1)
    else
      throw new IllegalStateException()

  def assertInFixpoint(fx: Fx): Unit =
    if (fixpoints.getOrElse(fx, 0) <= 0)
      error(s"Not in fixpoint $fx, recursion not possible")
  
  private def pushEntry(e: Entry): Unit =
    stack = e :: stack

  private def updateEntry(ev: ControlEvent)(f: PartialFunction[Entry, Option[Entry]]): Unit = stack match
    case Nil => error(s"No entry to close, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => error(s"Entry mismatch, expected end of $e: $ev")
      case Some(None) => stack = rest
      case Some(Some(replace)) => stack = replace :: rest

  private def updateThroughForks(ev: ControlEvent)(f: PartialFunction[Entry, Option[Entry]]): Unit =
    stack = updateThroughForks_(stack, ev)(f)

  private def updateThroughForks_(entries: List[Entry], ev: ControlEvent)(f: PartialFunction[Entry, Option[Entry]]): List[Entry] = entries match
    case Nil => error(s"No try entry to catch, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => e match
        case Entry.ForkFirst | Entry.ForkSecond => e :: updateThroughForks_(rest, ev)(f)
        case _ => error(s"Entry mismatch, expected end of $e: $ev")
      case Some(None) => rest
      case Some(Some(replace)) => replace :: rest

  
  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    ev match
      case BasicControlEvent.Atomic(a) => // fine
      case BasicControlEvent.Failed() => // fine
      case BasicControlEvent.BeginSection(sec: Section) => pushEntry(Entry.Sec)
      case BasicControlEvent.EndSection() => updateEntry(ev) { case Entry.Sec => None }

  override def handle(ev: BranchingControlEvent): Unit =
    import BranchingControlEvent.*
    ev match
      case Fork() => pushEntry(Entry.ForkFirst)
      case Switch() => updateEntry(ev) { case Entry.ForkFirst => Some(Entry.ForkSecond) }
      case BranchingControlEvent.Join() => updateEntry(ev) { case Entry.ForkSecond => None }

  override def handle(ev: ExceptionControlEvent[Exc]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() => pushEntry(Entry.Try)
      case Throw(exc: Exc) => // fine
      case Catching() => updateThroughForks(ev) { case Entry.Try => Some(Entry.Catching) }
      case Handle(exc: Exc) => updateThroughForks(ev) { case Entry.Catching => Some(Entry.Catching) }
      case EndTry() => updateEntry(ev) { case Entry.Try | Entry.Catching => None }

  override def handle(ev: FixpointControlEvent[Fx]): Unit =
    import FixpointControlEvent.*
    ev match
      case BeginFixpoint(fx) =>
        pushEntry(Entry.Fixpoint(fx))
        addFixpoint(fx)
      case Recurrent(fx) => 
        assertInFixpoint(fx)
      case RepeatFixpoint() => // fine
      case EndFixpoint() =>
        updateEntry(ev) { case Entry.Fixpoint(fx) =>
          removeFixpoint(fx)
          None
        }

object ControlEventChecker:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
  
  private enum ForkState:
    case First
    case Second(firstFailing: Boolean)

