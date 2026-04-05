package sturdy.control

import sturdy.control.FixpointControlEvent.BeginFixpoint


/**
  * Observes an interpreter and checks if the control events it sends form a valid sequence.
  * Validate according to the following grammar :
  *
  * S ::= empty | S S | Atomic(a) | Failed
  *        | BeginSection(sec) S EndSection
  *        | Fork S Switch S Join
  *        | Throw(exc)
  *        | BeginTry S (Catching C)? EndTry
  *        | BeginFix(fix) S EndFix
  *        | Recurrent(fix) | Restart
  * C ::= empty | C C | Fork C Switch C Join
  *        | BeginHandle(exc) C EndHandle
  *
 */
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

  private def updateThroughSections(ev: ControlEvent[Atom,Section,Exc,Fx])(f: PartialFunction[Entry, Option[Entry]]): Unit =
    stack = updateThroughSections_(stack, ev)(f)

  private def updateThroughSections_(entries: List[Entry], ev: ControlEvent[Atom,Section,Exc,Fx])(f: PartialFunction[Entry, Option[Entry]]): List[Entry] = entries match
    case Nil => error(s"No entry to update, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => e match
        case Entry.Sec => e :: updateThroughSections_(rest, ev)(f)
        case _ => error(s"Entry mismatch, expected end of $e: $ev")
      case Some(None) => rest
      case Some(Some(replace)) => replace :: rest

  def isCatching: Boolean = isCatching(stack)
  def isCatching(st: List[Entry]): Boolean = st match
    case Entry.Catching :: _ => true
    case (Entry.ForkFirst | Entry.ForkSecond | Entry.Sec) :: st_ => isCatching(st_)
    case _ => false

  private def assertNoCatching(): Unit =
    if (isCatching) {
      error(s"Control event while catching but outside handler: $stack")
    }

  private def assertCatching(): Unit =
    if (!isCatching) {
      error(s"Expected catching mode: $stack")
    }

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    assertNoCatching()
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
      case BeginTry() =>
        assertNoCatching()
        pushEntry(Entry.Try)
      case Throw(exc: Exc) =>
        assertNoCatching()
      case Catching() => updateThroughSections(ev) { case Entry.Try => Some(Entry.Catching) }
      case BeginHandle(exc: Exc) =>
        assertCatching()
        pushEntry(Entry.Handle)
      case EndHandle() => updateEntry(ev) { case Entry.Handle => None }
      case EndTry() => updateThroughSections(ev) { case Entry.Try | Entry.Catching => None }

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
