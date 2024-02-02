package sturdy.control

class ControlEventChecker[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import ControlEventChecker.*

  enum Entry:
    case Sec(s: Section)
    case Try()
    case Catching(failed: Boolean)
    case ForkFirst(excBeforeFork: Set[Exc])
    case ForkSecond(firstFailing: Boolean, excAfterFirst: Set[Exc])
    case Fixpoint(failing: Boolean, stack: List[Entry], tries: List[Set[Exc]])

  private var started = false
  private var failing_ = false
  private var stack: List[Entry] = List()
  private var tries: List[Set[Exc]] = List(Set())
  private var fixpoint: Option[Entry.Fixpoint] = None

  private def pushEntry(e: Entry): Unit =
    stack = e :: stack
  
  private def updateEntry(f: PartialFunction[Entry, Option[Entry]])(ev: ControlEvent): Unit = stack match
    case Nil => error(s"No entry to close, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => stack = rest
      case Some(Some(replace)) => stack = replace :: rest

  private def updateThroughForks(entries: List[Entry])(f: PartialFunction[Entry, Option[Entry]])(ev: ControlEvent): List[Entry] = entries match
    case Nil => error(s"No try entry to catch, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => e match
        case Entry.ForkFirst(_) | Entry.ForkSecond(_,_) => e :: updateThroughForks(rest)(f)(ev)
        case _ => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => rest
      case Some(Some(replace)) => replace :: rest

  
  def failing : Boolean = failing_
  
  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    if(!started) ev match
      case Start() => started = true
      case _ => error(s"Sequence must begin with ControlEvent.Start(): $ev")
    else if (failing_) ev match
      case End(sec) =>
        updateEntry { case Entry.Sec(sec) => None}(ev)
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case BasicControlEvent.Start() => error(s"Repeated Start() event: $ev")
      case BasicControlEvent.Atomic(a) => // fine
      case BasicControlEvent.Failed() => failing_ = true
      case BasicControlEvent.Begin(sec: Section) => pushEntry(Entry.Sec(sec))
      case BasicControlEvent.End(sec) => updateEntry { case Entry.Sec(sec) => None }(ev)
    fixpoint = None

  override def handle(ev: ExceptionControlEvent[Exc]): Unit =
    import ExceptionControlEvent.*
    if(!started) error(s"Sequence must begin with ControlEvent.Start(): $ev")
    if(failing_) ev match
      case ExceptionControlEvent.Catching() =>
        stack = updateThroughForks(stack) { case Entry.Try() => Some(Entry.Catching(failing_)) }(ev)
        failing_ = false
      case ExceptionControlEvent.Handle(exc: Exc) =>
        stack = updateThroughForks(stack) { case e@Entry.Catching(_) => Some(e) }(ev)
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ExceptionControlEvent.EndTry() =>
        updateEntry {
          case Entry.Try() => None
          case Entry.Catching(failed) =>
            failing_ = failed
            None
        }(ev)
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case ExceptionControlEvent.BeginTry() =>
        pushEntry(Entry.Try())
        tries = Set() +: tries
      case ExceptionControlEvent.Throw(exc: Exc) =>
        tries = (tries.head + exc) :: tries.tail
        failing_ = true
      case ExceptionControlEvent.Catching() =>
        stack = updateThroughForks(stack) { case Entry.Try() => Some(Entry.Catching(failing_)) }(ev)
      case ExceptionControlEvent.Handle(exc: Exc) =>
        stack = updateThroughForks(stack) { case e@Entry.Catching(_) => Some(e) }(ev)
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ExceptionControlEvent.EndTry() =>
        updateEntry { case Entry.Try() | Entry.Catching(_) => None }(ev)
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
    fixpoint = None


  override def handle(ev: BranchingControlEvent): Unit =
    import BranchingControlEvent.*
    if (!started) error(s"Sequence must begin with ControlEvent.Start(): $ev")
    if(failing_) ev match
      case Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing, excAfterFirst) =>
          failing_ = firstFailing
          tries = (tries.head ++ excAfterFirst) :: tries.tail
          None }(ev)
      case Switch() =>
        updateEntry { case Entry.ForkFirst(excBeforeFork) =>
          val excAfterFirst = tries.head
          tries = excBeforeFork :: tries.tail
          Some(Entry.ForkSecond(failing_, excAfterFirst)) }(ev)
        failing_ = false
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case Fork() =>
        pushEntry(Entry.ForkFirst(tries.head))
      case Switch() =>
        updateEntry { case Entry.ForkFirst(excBeforeFork) =>
          val excAfterFirst = tries.head
          tries = excBeforeFork :: tries.tail
          Some(Entry.ForkSecond(failing_, excAfterFirst)) }(ev)
      case BranchingControlEvent.Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing, excAfterFirst) =>
          tries = (tries.head ++ excAfterFirst) :: tries.tail
          None }(ev)
    fixpoint = None

  override def handle(ev: FixpointControlEvent): Unit =
    import FixpointControlEvent.*
    if(failing_) ev match
      case RepeatFixpoint() =>
        fixpoint match
          case Some(e) =>
            failing_ = e.failing
            stack = e.stack
            tries = e.tries
          case None =>
            error(s"Cannot repeat here, expected end of fixpoint before")
        failing_ = false
      case RecurrentCall(recFailing) => failing_ = recFailing
      case EndFixpoint() =>
        updateEntry { case e: Entry.Fixpoint =>
          fixpoint = Some(e)
          None
        }(ev)
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case BeginFixpoint() => pushEntry(Entry.Fixpoint(failing_, stack, tries))
      case RecurrentCall(recFailing) => failing_ = recFailing
      case RepeatFixpoint() => fixpoint match
        case Some(e) =>
          failing_ = e.failing
          stack = e.stack
          tries = e.tries
        case None =>
          error(s"Cannot repeat here, expected end of fixpoint before")
      case EndFixpoint() =>
        updateEntry { case e: Entry.Fixpoint =>
          fixpoint = Some(e)
          None
        }(ev)
    ev match
      case FixpointControlEvent.EndFixpoint() => // nothing
      case _ => fixpoint = None


object ControlEventChecker:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)
  private case class InvalidControlEventSequence(msg: String) extends Exception(msg)
  
  private enum ForkState:
    case First
    case Second(firstFailing: Boolean)

