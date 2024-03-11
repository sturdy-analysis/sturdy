package sturdy.control

import sturdy.control.FixpointControlEvent.BeginFixpoint

class ControlEventParser[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:
  import ControlEventParser.*

  val CT = ControlTree
  type CT = ControlTree[Atom, Section, Exc, Fx]

  enum Entry:
    case Tree(t: CT)
    case Sec(s: Section)
    case Try
    case Catching(body: CT, handlers: List[(Exc, CT)])
    case Handler(exc: Exc)
    case ForkFirst
    case ForkSecond(first: CT)
    case Fixpoint(fx: Fx)

    override def toString: String = this match
      case Tree(t) => s"Tree(${t.toString.take(10)}...)"
      case Sec(s) => s"Sec($s)"
      case Try => "Try"
      case Catching(body, handlers) => s"Catching(${body.toString.take(10)}..., ${handlers.map((x,t) => s"$x -> ${t.toString.take(10)}...")})"
      case Handler(exc) => s"Handler($exc)"
      case ForkFirst => "ForkFirst"
      case ForkSecond(first) => s"ForkSecond(${first.toString.take(10)}...)"
      case Fixpoint(fx) => s"Fixpoint($fx)"

  private var stack: List[Entry] = List(Entry.Tree(CT.Empty()))

  def isCatching: Boolean = stack match
    case Entry.Catching(_, _) :: _ => true
    case _ => false

  private def addTree(t: CT): Unit =
    stack = addTree(t, stack)

  private def addTree(t: CT, st: List[Entry]): List[Entry] = st match
    case Entry.Tree(t0) :: st_ => Entry.Tree(CT.Seq(t0, t)) :: st_
    case Nil => error(s"Stack underflow: cannot add tree $t")
    case _ => error(s"Ill-formatted stack, top entry must be a tree: $stack")

  def getFinalTree: CT = stack match
    case Entry.Tree(t) :: Nil => t
    case _ => error(s"Ill-formatted stack, expected singular tree: $stack")

  def assertNoCatching(): Unit =
    if (isCatching) {
      error(s"Control event while catching but outside handler")
    }

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    assertNoCatching()
    ev match
      case BasicControlEvent.Atomic(a) => addTree(CT.Atomic(a))
      case BasicControlEvent.Failed() => addTree(CT.Failed())
      case BasicControlEvent.BeginSection(sec: Section) =>
        stack = Entry.Tree(CT.Empty()) :: Entry.Sec(sec) :: stack
      case BasicControlEvent.EndSection() => stack match
        case Entry.Tree(t) :: Entry.Sec(sec) :: stack_ =>
          stack = addTree(CT.Section(sec, t), stack_)
        case _ => error(s"Entry mismatch, expected end of $ev: $stack")

  override def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BranchingControlEvent.*
    if (isCatching) {
      // skip forks while catching
      return
    }
    ev match
      case Fork() =>
        stack = Entry.Tree(CT.Empty()) :: Entry.ForkFirst :: stack
      case Switch() => stack match
        case Entry.Tree(t) :: Entry.ForkFirst :: stack_ =>
          stack = Entry.Tree(CT.Empty()) :: Entry.ForkSecond(t) :: stack_
        case _ => error(s"Entry mismatch, expected ForkFirst for $ev: $stack")
      case BranchingControlEvent.Join() => stack match
        case Entry.Tree(t) :: Entry.ForkSecond(first) :: stack_ =>
          stack = addTree(CT.Fork(first, t), stack_)
        case _ => error(s"Entry mismatch, expected ForkSecond for $ev: $stack")


  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() =>
        assertNoCatching()
        stack = Entry.Tree(CT.Empty()) :: Entry.Try :: stack
      case Throw(exc: Exc) =>
        assertNoCatching()
        addTree(CT.Throw(exc))
      case Catching() => stack match
        case Entry.Tree(body) :: Entry.Try :: stack_ =>
          stack = Entry.Catching(body, List()) :: stack_
        case _ => error(s"Entry mismatch, expected Try for $ev: $stack")
      case BeginHandle(exc: Exc) => stack match
        case Entry.Catching(body, hs) :: stack_ =>
          stack = Entry.Tree(CT.Empty()) :: Entry.Handler(exc) :: Entry.Catching(body, hs) :: stack_
        case _ => error(s"Entry mismatch, expected Catching for $ev: $stack")
      case EndHandle() => stack match
        case Entry.Tree(ht) :: Entry.Handler(hx) :: Entry.Catching(body, hs) :: stack_ =>
          stack = Entry.Catching(body, hs :+ (hx, ht)) :: stack_
        case _ => error(s"Entry mismatch, expected Handler for $ev: $stack")
      case EndTry() => stack match
        case Entry.Tree(body) :: Entry.Try :: stack_ =>
          stack = addTree(CT.Try(body, Nil), stack_)
        case Entry.Catching(body, hs) :: stack_ =>
          stack = addTree(CT.Try(body, hs), stack_)
        case _ => error(s"Entry mismatch, expected Try or Catching for $ev: $stack")

  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit =
    import FixpointControlEvent.*
    assertNoCatching()
    ev match
      case BeginFixpoint(fx) =>
        stack = Entry.Tree(CT.Empty()) :: Entry.Fixpoint(fx) :: stack
      case Recurrent(fx) =>
        addTree(CT.Recurrent(fx))
      case EndFixpoint() => stack match
        case Entry.Tree(t) :: Entry.Fixpoint(fx) :: stack_ =>
          stack = addTree(CT.Fix(fx, t), stack_)
        case _ =>
          error(s"Entry mismatch, expected Fixpoint for $ev: $stack")
      case Restart() =>
        addTree(CT.Restart())


object ControlEventParser:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)

  def parse[Atom,Section,Exc,Fx](es: Iterable[ControlEvent[Atom,Section,Exc,Fx]]): ControlTree[Atom,Section,Exc,Fx] =
    val parser = new ControlEventParser[Atom,Section,Exc,Fx]
    es.foreach(parser.handle)
    parser.getFinalTree

