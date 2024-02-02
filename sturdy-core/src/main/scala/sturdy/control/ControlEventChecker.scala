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
  var failing = false
  private var stack: List[Entry] = List()
  private var tries: List[Set[Exc]] = List(Set())
  private var fixpoint: Option[Entry.Fixpoint] = None

  private def pushEntry(e: Entry): Unit =
    stack = e :: stack

  private def updateEntry(f: PartialFunction[Entry, Option[Entry]])(using ev: ControlEvent): Unit = stack match
    case Nil => error(s"No entry to close, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => stack = rest
      case Some(Some(replace)) => stack = replace :: rest

  private def updateThroughForks(entries: List[Entry])(f: PartialFunction[Entry, Option[Entry]])(using ev: ControlEvent): List[Entry] = entries match
    case Nil => error(s"No try entry to catch, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => e match
        case Entry.ForkFirst(_) | Entry.ForkSecond(_,_) => e :: updateThroughForks(rest)(f)
        case _ => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => rest
      case Some(Some(replace)) => replace :: rest

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit = ???
  override def handle(ev: ExceptionControlEvent[Exc]): Unit = ???
  override def handle(ev: BranchingControlEvent): Unit = ???
  override def handle(ev: FixpointControlEvent): Unit = ???

  override def handle(ev: ControlEvent): Unit =
    given ControlEvent = ev

    if (!started) ev match
      case BasicControlEvent.Start() => started = true
      case _ => error(s"Sequence must begin with ControlEvent.Start(): $ev")
      
    else if (failing) ev match
      case BasicControlEvent.End(sec) =>
        updateEntry { case Entry.Sec(sec) => None}
      case BranchingControlEvent.Switch() =>
        updateEntry { case Entry.ForkFirst(excBeforeFork) =>
          val excAfterFirst = tries.head
          tries = excBeforeFork :: tries.tail
          Some(Entry.ForkSecond(failing, excAfterFirst))
        }
        failing = false
      case BranchingControlEvent.Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing, excAfterFirst) =>
          failing = firstFailing
          tries = (tries.head ++ excAfterFirst) :: tries.tail
          None
        }
      case ExceptionControlEvent.Catching() =>
        stack = updateThroughForks(stack) { case Entry.Try() => Some(Entry.Catching(failing)) }
        failing = false
      case ExceptionControlEvent.Handle(exc: Exc) =>
        stack = updateThroughForks(stack) { case e@Entry.Catching(_) => Some(e) }
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ExceptionControlEvent.EndTry() =>
        updateEntry {
          case Entry.Try() => None
          case Entry.Catching(failed) =>
            failing = failed
            None
        }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case FixpointControlEvent.RepeatFixpoint() =>
        fixpoint match
          case Some(e) =>
            failing = e.failing
            stack = e.stack
            tries = e.tries
          case None =>
            error(s"Cannot repeat here, expected end of fixpoint before")
        failing = false
      case FixpointControlEvent.RecurrentCall(recFailing) =>
        failing = recFailing
      case FixpointControlEvent.EndFixpoint() =>
        updateEntry { case e: Entry.Fixpoint =>
          fixpoint = Some(e)
          None
        }
      case _ => error(s"Invalid event after failure: $ev")
      
    else ev match
      case BasicControlEvent.Start() => error(s"Repeated Start() event: $ev")
      case BasicControlEvent.Atomic(a) => // fine
      case BasicControlEvent.Failed() => failing = true
      case BasicControlEvent.Begin(sec: Section) =>
        pushEntry(Entry.Sec(sec))
      case BasicControlEvent.End(sec) =>
        updateEntry { case Entry.Sec(sec) => None}
      case ExceptionControlEvent.BeginTry() =>
        pushEntry(Entry.Try())
        tries = Set() +: tries
      case ExceptionControlEvent.Throw(exc: Exc) =>
        tries = (tries.head + exc) :: tries.tail
        failing = true
      case ExceptionControlEvent.Catching() =>
        stack = updateThroughForks(stack) { case Entry.Try() => Some(Entry.Catching(failing)) }
      case ExceptionControlEvent.Handle(exc: Exc) =>
        stack = updateThroughForks(stack) { case e@Entry.Catching(_) => Some(e) }
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ExceptionControlEvent.EndTry() =>
        updateEntry { case Entry.Try() | Entry.Catching(_) => None }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case BranchingControlEvent.Fork() =>
        pushEntry(Entry.ForkFirst(tries.head))
      case BranchingControlEvent.Switch() =>
        updateEntry { case Entry.ForkFirst(excBeforeFork) =>
          val excAfterFirst = tries.head
          tries = excBeforeFork :: tries.tail
          Some(Entry.ForkSecond(failing, excAfterFirst))
        }
      case BranchingControlEvent.Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing, excAfterFirst) =>
          tries = (tries.head ++ excAfterFirst) :: tries.tail
          None
        }
      case FixpointControlEvent.BeginFixpoint() =>
        pushEntry(Entry.Fixpoint(failing, stack, tries))
      case FixpointControlEvent.RecurrentCall(recFailing) => failing = recFailing
      case FixpointControlEvent.RepeatFixpoint() =>
        fixpoint match
          case Some(e) =>
            failing = e.failing
            stack = e.stack
            tries = e.tries
          case None =>
            error(s"Cannot repeat here, expected end of fixpoint before")
      case FixpointControlEvent.EndFixpoint() =>
        updateEntry { case e: Entry.Fixpoint =>
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

