package sturdy.control

class ControlEventChecker[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import ControlEvent.*
  import ControlEventChecker.*
  
  private var started = false
  private var failing = false
  private var sections: List[Section] = List()
  private var forks: List[ForkState] = List() // switched == true

  override def handle(ev: ControlEvent[Atom, Section, Exc]): Unit =
    if (!started) ev match
      case Start() => started = true
      case _ => error(s"Sequence must begin with ControlEvent.Start(): $ev")
    else if (failing) ev match
      case ControlEvent.End(sec) => sections match
        case Nil => error(s"No section to close: $ev")
        case `sec` :: restSections => sections = restSections
        case s :: _ => error(s"Section mismatch, expected End($s): $ev")
      case ControlEvent.Switch() => forks match
        case Nil => error(s"No fork to switch: $ev")
        case ForkState.First :: restForks =>
          forks = ForkState.Second(failing) +: restForks
          failing = false
        case _ => error(s"Fork mismatch, expected Join: $ev")
      case ControlEvent.Join() => forks match
        case Nil => error(s"No fork to join: $ev")
        case ForkState.Second(firstFailing) :: restForks =>
          forks = restForks
          failing = firstFailing
        case _ => error(s"Fork mismatch, expected Switch: $ev")
      case _ => error(s"Invalid event after failure: $ev")
    else ev match
      case ControlEvent.Start() => error(s"Repeated Start() event: $ev")
      case ControlEvent.Atomic(a) => // fine
      case ControlEvent.Failed() => failing = true
      case ControlEvent.Begin(sec) => sections = sec +: sections
      case ControlEvent.End(sec) => sections match
        case Nil => error(s"No section to close: $ev")
        case `sec` :: restSections => sections = restSections
        case s :: _ => error(s"Section mismatch, expected End($s): $ev")
      case ControlEvent.BeginTry() => ???
      case ControlEvent.Throw() => ???
      case ControlEvent.Catch() => ???
      case ControlEvent.EndTry() => ???
      case ControlEvent.Fork() => forks = ForkState.First +: forks
      case ControlEvent.Switch() => forks match
        case Nil => error(s"No fork to switch: $ev")
        case ForkState.First :: restForks => forks = ForkState.Second(false) +: restForks
        case _ => error(s"Fork mismatch, expected Join: $ev")
      case ControlEvent.Join() => forks match
        case Nil => error(s"No fork to join: $ev")
        case ForkState.Second(firstFailing) :: restForks => forks = restForks
        case _ => error(s"Fork mismatch, expected Switch: $ev")


object ControlEventChecker:
  private def error(msg: String): Nothing = throw InvalidControlEventSequence(msg)
  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
  
  private enum ForkState:
    case First
    case Second(firstFailing: Boolean)
