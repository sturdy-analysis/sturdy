package sturdy.control

class ControlEventChecker[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import ControlEvent.*
  import ControlEventChecker.*

  enum Entry:
    case Sec(s: Section)
    case Try()
    case Catching(failed: Boolean)
    case ForkFirst()
    case ForkSecond(firstFailing: Boolean)
    case Fixpoint(failing: Boolean, stack: List[Entry], tries: List[Set[Exc]])

  private var started = false
  private var failing = false
  private var stack: List[Entry] = List()
  private var tries: List[Set[Exc]] = List(Set())

  private def pushEntry(e: Entry): Unit =
    stack = e :: stack

  private def updateEntry(f: PartialFunction[Entry, Option[Entry]])(using ev: ControlEvent[Atom, Section, Exc]): Unit = stack match
    case Nil => error(s"No entry to close, stack is empty: $ev")
    case e :: rest => f.lift(e) match
      case None => error(s"Section mismatch, expected end of $e: $ev")
      case Some(None) => stack = rest
      case Some(Some(replace)) => stack = replace :: rest

  private def updateCatching(entries: List[Entry])(using ev: ControlEvent[Atom, Section, Exc]): List[Entry] = entries match
    case Nil => error(s"No try entry to catch, stack is empty: $ev")
    case Entry.Try() :: rest => Entry.Catching(failing) :: rest
    case (e@(Entry.ForkFirst() | Entry.ForkSecond(_))) :: rest => e :: updateCatching(rest)
    case e :: rest => error(s"Section mismatch, expected end of $e: $ev")

  override def handle(ev: ControlEvent[Atom, Section, Exc]): Unit =
    given ControlEvent[Atom, Section, Exc] = ev

    if (!started) ev match
      case Start() => started = true
      case _ => error(s"Sequence must begin with ControlEvent.Start(): $ev")
      
    else if (failing) ev match
      case ControlEvent.End(sec) =>
        updateEntry { case Entry.Sec(sec) => None}
      case ControlEvent.Switch() =>
        updateEntry { case Entry.ForkFirst() => Some(Entry.ForkSecond(failing)) }
        failing = false
      case ControlEvent.Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing) =>
          failing = firstFailing
          None
        }
      case ControlEvent.Catching() =>
        stack = updateCatching(stack)
        failing = false
      case ControlEvent.Handle(exc) =>
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ControlEvent.EndTry() =>
        updateEntry {
          case Entry.Try() => None
          case Entry.Catching(failed) =>
            failing = failed
            None
        }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case ControlEvent.FixpointRepeat() =>
        println("repeat")
        updateEntry { case e: Entry.Fixpoint =>
          failing = e.failing
          stack = e.stack
          tries = e.tries
          Some(e)
        }
        failing = false
      case ControlEvent.FixpointRecurrent() => // still failing
      case ControlEvent.FixpointRelease() =>
        updateEntry { case _: Entry.Fixpoint => None}
      case _ => error(s"Invalid event after failure: $ev")
      
    else ev match
      case ControlEvent.Start() => error(s"Repeated Start() event: $ev")
      case ControlEvent.Atomic(a) => // fine
      case ControlEvent.Failed() => failing = true
      case ControlEvent.Begin(sec) =>
        pushEntry(Entry.Sec(sec))
      case ControlEvent.End(sec) =>
        updateEntry { case Entry.Sec(sec) => None}
      case ControlEvent.BeginTry() =>
        pushEntry(Entry.Try())
        tries = Set() +: tries
      case ControlEvent.Throw(exc) =>
        tries = (tries.head + exc) :: tries.tail
        failing = true
      case ControlEvent.Catching() =>
        stack = updateCatching(stack)
      case ControlEvent.Handle(exc) =>
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ControlEvent.EndTry() =>
        updateEntry { case Entry.Try() | Entry.Catching(_) => None }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case ControlEvent.Fork() =>
        pushEntry(Entry.ForkFirst())
      case ControlEvent.Switch() =>
        updateEntry { case Entry.ForkFirst() => Some(Entry.ForkSecond(failing)) }
      case ControlEvent.Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing) => None }
      case ControlEvent.FixpointPrepare() =>
        pushEntry(Entry.Fixpoint(failing, stack, tries))
      case ControlEvent.FixpointRecurrent() => failing = true
      case ControlEvent.FixpointRepeat() =>
        println("repeat")
        updateEntry { case e: Entry.Fixpoint =>
          failing = e.failing
          stack = e.stack
          tries = e.tries
          Some(e)
        }
      case ControlEvent.FixpointRelease() =>
        updateEntry { case _: Entry.Fixpoint => None}


object ControlEventChecker:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)
  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
  
  private enum ForkState:
    case First
    case Second(firstFailing: Boolean)

