package sturdy.control

class ControlEventChecker[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import ControlEventChecker.*

  enum Entry:
    case Sec(s: Section)
    case Try()
    case Catching(failed: Boolean)
    case ForkFirst(excBeforeFork: Set[Exc])
    case ForkSecond(firstFailing: Boolean, excAfterFirst: Set[Exc])
    case Fixpoint(failing: Boolean, tries: List[Set[Exc]])

  private var failing_ = false
  private var stack: List[Entry] = List()
  private var tries: List[Set[Exc]] = List(Set())
  private var fixpoint: Option[Entry.Fixpoint] = None

  private def pushEntry(e: Entry): Unit =
    stack = e :: stack
  
  private def updateEntry(ev: ControlEvent)(f: PartialFunction[Entry, Option[Entry]]): Unit = stack match
    case Nil => error(s"No entry to close, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => stack = rest
      case Some(Some(replace)) => stack = replace :: rest

  private def updateThroughForks(entries: List[Entry], ev: ControlEvent)(f: PartialFunction[Entry, Option[Entry]]): List[Entry] = entries match
    case Nil => error(s"No try entry to catch, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => e match
        case Entry.ForkFirst(_) | Entry.ForkSecond(_,_) => e :: updateThroughForks(rest, ev)(f)
        case _ => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => rest
      case Some(Some(replace)) => replace :: rest

  
  def failing : Boolean = failing_
  
  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    if (failing_) {
      ev match
        case BasicControlEvent.End(sec) => updateEntry(ev) { case Entry.Sec(sec) => None }
        case _ => error(s"Invalid event after failure: $ev")
    } else {
      ev match
        case BasicControlEvent.Atomic(a) => // fine
        case BasicControlEvent.Failed() => failing_ = true
        case BasicControlEvent.Begin(sec: Section) => pushEntry(Entry.Sec(sec))
        case BasicControlEvent.End(sec) => updateEntry(ev) { case Entry.Sec(sec) => None }
    }
    fixpoint = None

  override def handle(ev: ExceptionControlEvent[Exc]): Unit =
    import ExceptionControlEvent.*
    if(failing_) ev match
      case Catching() =>
        stack = updateThroughForks(stack, ev) { case Entry.Try() => Some(Entry.Catching(failing_)) }
        failing_ = false
      case Handle(exc: Exc) =>
        stack = updateThroughForks(stack, ev) { case e@Entry.Catching(_) => Some(e) }
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case EndTry() =>
        updateEntry(ev) {
          case Entry.Try() => None
          case Entry.Catching(failed) =>
            failing_ = failed
            None
        }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case BeginTry() =>
        pushEntry(Entry.Try())
        tries = Set() +: tries
      case Throw(exc: Exc) =>
        tries = (tries.head + exc) :: tries.tail
        failing_ = true
      case Catching() =>
        stack = updateThroughForks(stack, ev) { case Entry.Try() => Some(Entry.Catching(failing_)) }
      case Handle(exc: Exc) =>
        stack = updateThroughForks(stack, ev) { case e@Entry.Catching(_) => Some(e) }
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case EndTry() =>
        updateEntry(ev) { case Entry.Try() | Entry.Catching(_) => None }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
    fixpoint = None


  override def handle(ev: BranchingControlEvent): Unit =
    import BranchingControlEvent.*
    if(failing_) ev match
      case Join() =>
        updateEntry(ev) { case Entry.ForkSecond(firstFailing, excAfterFirst) =>
          failing_ = firstFailing
          tries = (tries.head ++ excAfterFirst) :: tries.tail
          None }
      case Switch() =>
        updateEntry(ev) { case Entry.ForkFirst(excBeforeFork) =>
          val excAfterFirst = tries.head
          tries = excBeforeFork :: tries.tail
          Some(Entry.ForkSecond(failing_, excAfterFirst)) }
        failing_ = false
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case Fork() =>
        pushEntry(Entry.ForkFirst(tries.head))
      case Switch() =>
        updateEntry(ev) { case Entry.ForkFirst(excBeforeFork) =>
          val excAfterFirst = tries.head
          tries = excBeforeFork :: tries.tail
          Some(Entry.ForkSecond(failing_, excAfterFirst)) }
      case BranchingControlEvent.Join() =>
        updateEntry(ev) { case Entry.ForkSecond(firstFailing, excAfterFirst) =>
          tries = (tries.head ++ excAfterFirst) :: tries.tail
          None }
    fixpoint = None

  override def handle(ev: FixpointControlEvent): Unit =
    import FixpointControlEvent.*
    if(failing_) ev match
      case RepeatFixpoint() =>
        fixpoint match
          case Some(e) =>
            failing_ = e.failing
            tries = e.tries
          case None =>
            error(s"Cannot repeat here, expected end of fixpoint before")
        failing_ = false
      case RecurrentCall(recFailing) => failing_ = recFailing
      case EndFixpoint() =>
        updateEntry(ev) { case e: Entry.Fixpoint =>
          fixpoint = Some(e)
          None
        }
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case BeginFixpoint() => pushEntry(Entry.Fixpoint(failing_, tries))
      case RecurrentCall(recFailing) => failing_ = recFailing
      case RepeatFixpoint() => fixpoint match
        case Some(e) =>
          failing_ = e.failing
          tries = e.tries
        case None =>
          error(s"Cannot repeat here, expected end of fixpoint before")
      case EndFixpoint() =>
        updateEntry(ev) { case e: Entry.Fixpoint =>
          fixpoint = Some(e)
          None
        }
    ev match
      case FixpointControlEvent.EndFixpoint() => // nothing
      case _ => fixpoint = None


object ControlEventChecker:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
  
  private enum ForkState:
    case First
    case Second(firstFailing: Boolean)

