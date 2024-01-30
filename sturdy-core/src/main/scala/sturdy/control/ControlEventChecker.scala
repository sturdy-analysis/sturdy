package sturdy.control

class ControlEventChecker[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import ControlEvent.*
  import ControlEventChecker.*

  enum Entry:
    case Sec(s: Section)
    case Try()
    case ForkFirst()
    case ForkSecond(firstFailing: Boolean)

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
      case ControlEvent.Catch(exc) =>
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
        failing = false
      case ControlEvent.EndTry() =>
        updateEntry { case Entry.Try() => None }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case ControlEvent.FixpointRepeat() =>
        failing = false
      case ControlEvent.FixpointAbort() => // still failing
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
      case ControlEvent.Catch(exc) =>
        if (!tries.head.contains(exc))
          error(s"Exception $exc not currently active")
        val remaining = tries.head - exc
        tries = remaining :: tries.tail
      case ControlEvent.EndTry() =>
        updateEntry { case Entry.Try() => None }
        val stillActive = tries.head
        val rest = tries.tail
        tries = (rest.head ++ stillActive) :: rest.tail
      case ControlEvent.Fork() =>
        pushEntry(Entry.ForkFirst())
      case ControlEvent.Switch() =>
        updateEntry { case Entry.ForkFirst() => Some(Entry.ForkSecond(failing)) }
      case ControlEvent.Join() =>
        updateEntry { case Entry.ForkSecond(firstFailing) => None }
      case ControlEvent.FixpointAbort() => failing = true
      case ControlEvent.FixpointRepeat() => // nothing


object ControlEventChecker:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)
  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
  
  private enum ForkState:
    case First
    case Second(firstFailing: Boolean)

