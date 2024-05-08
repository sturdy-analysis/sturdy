package sturdy.control

import sturdy.control.FixpointControlEvent.BeginFixpoint

class ControlEventParser[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:
  import ControlEventParser.*

  val CT = ControlTree
  type CT = ControlTree[Atom, Section, Exc, Fx]

  private var stack: List[Entry] = List(Entry.BaseTree(CT.Empty()))

  def getFinalTree: CT = stack match
    case Entry.BaseTree(t) :: Nil => t
    case _ => error(s"Ill-formatted stack, expected singular base tree: $stack")

  def isCatching: Boolean = stack match
    case Entry.Catching(_, _) :: _ => true
    case _ => false

  private def addTree(t: CT): Unit =
    stack = addTree(t, stack)

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    assertNoCatching()
    ev match
      case BasicControlEvent.Atomic(a) => addTree(CT.Atomic(a))
      case BasicControlEvent.Failed() => addTree(CT.Failed())
      case BasicControlEvent.BeginSection(sec: Section) =>
        stack = Entry.Sec(sec, CT.Empty()) :: stack
      case BasicControlEvent.EndSection() => stack match
        case Entry.Sec(sec, t) :: stack_ =>
          stack = addTree(CT.Section(sec, t), stack_)
        case _ => error(s"Entry mismatch, expected end of $ev: $stack")

  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() =>
        assertNoCatching()
        stack = Entry.Try(CT.Empty()) :: stack
      case Throw(exc: Exc) =>
        assertNoCatching()
        addTree(CT.Throw(exc))
      case Catching() => stack match
        case Entry.Try(t) :: stack_ =>
          stack = Entry.Catching(t, List()) :: stack_
        case _ => error(s"Entry mismatch, expected Try for $ev: $stack")
      case BeginHandle(exc: Exc) => stack match
        case Entry.Catching(body, hs) :: stack_ =>
          stack = Entry.Handler(exc, CT.Empty(), body, hs) :: stack_
        case _ => error(s"Entry mismatch, expected Catching for $ev: $stack")
      case EndHandle() => stack match
        case Entry.Handler(hx, t, body, hs) :: stack_ =>
          stack = Entry.Catching(body, hs :+ (hx, t)) :: stack_
        case _ => error(s"Entry mismatch, expected Handler for $ev: $stack")
      case EndTry() => stack match
        case Entry.Try(t) :: stack_ =>
          stack = addTree(CT.Try(t, Nil), stack_)
        case Entry.Catching(body, hs) :: stack_ =>
          stack = addTree(CT.Try(body, hs), stack_)
        case _ => error(s"Entry mismatch, expected Try or Catching for $ev: $stack")

  def assertNoCatching(): Unit =
    if (isCatching) {
      error(s"Control event while catching but outside handler")
    }

  override def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BranchingControlEvent.*
    if (isCatching) {
      // skip forks while catching
      return
    }
    ev match
      case Fork() =>
        stack = Entry.ForkFirst(CT.Empty()) :: stack
      case Switch() => stack match
        case Entry.ForkFirst(t) :: stack_ =>
          stack = Entry.ForkSecond(t, CT.Empty()) :: stack_
        case _ => error(s"Entry mismatch, expected ForkFirst for $ev: $stack")
      case BranchingControlEvent.Join() => stack match
        case Entry.ForkSecond(first, t) :: stack_ =>
          stack = addTree(CT.Fork(first, t), stack_)
        case _ => error(s"Entry mismatch, expected ForkSecond for $ev: $stack")

  private def addTree(t: CT, st: List[Entry]): List[Entry] = st match
    case Entry.BaseTree(t0) :: st_ => Entry.BaseTree(CT.Seq(t0, t)) :: st_
    case Entry.Sec(s, t0) :: st_ => Entry.Sec(s, CT.Seq(t0, t)) :: st_
    case Entry.Try(t0) :: st_ => Entry.Try(CT.Seq(t0, t)) :: st_
    case Entry.Handler(exc, t0, body, handlers) :: st_ => Entry.Handler(exc, CT.Seq(t0, t), body, handlers) :: st_
    case Entry.ForkFirst(t0) :: st_ => Entry.ForkFirst(CT.Seq(t0, t)) :: st_
    case Entry.ForkSecond(first, t0) :: st_ => Entry.ForkSecond(first, CT.Seq(t0, t)) :: st_
    case Entry.Fixpoint(fx, t0) :: st_ => Entry.Fixpoint(fx, CT.Seq(t0, t)) :: st_
    case Entry.Catching(_, _) :: st_ => error(s"Ill-formatted stack, top entry must not be Catching : $stack")
    case Nil => error(s"Stack underflow: cannot add tree $t")
    case _ => assert(false)

  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit =
    import FixpointControlEvent.*
    assertNoCatching()
    ev match
      case BeginFixpoint(fx) =>
        stack = Entry.Fixpoint(fx, CT.Empty()) :: stack
      case Recurrent(fx) =>
        addTree(CT.Recurrent(fx))
      case EndFixpoint() => stack match
        case Entry.Fixpoint(fx, t) :: stack_ =>
          stack = addTree(CT.Fix(fx, t), stack_)
        case _ =>
          error(s"Entry mismatch, expected Fixpoint for $ev: $stack")
      case Restart() =>
        addTree(CT.Restart())

  enum Entry:
    case BaseTree(t: CT)
    case Sec(s: Section, t: CT)
    case Try(t: CT)
    case Catching(body: CT, handlers: List[(Exc, CT)])
    case Handler(exc: Exc, t: CT, body: CT, handlers: List[(Exc, CT)])
    case ForkFirst(t: CT)
    case ForkSecond(t1: CT, t2: CT)
    case Fixpoint(fx: Fx, t: CT)

    override def toString: String = this match
      case BaseTree(t) => s"Tree(${t.toString.take(10)}...)"
      case Sec(s, t) => s"Sec($s, ${t.toString.take(10)}...)"
      case Try(t) => s"Try(${t.toString.take(10)}...)"
      case Catching(body, handlers) => s"Catching(${body.toString.take(10)}..., ${handlers.map((x, t) => s"$x -> ${t.toString.take(10)}...")})"
      case Handler(exc, t, body, handlers) => s"Handler($exc, ${t.toString.take(10)}...)"
      case ForkFirst(t) => s"ForkFirst(${t.toString.take(10)}...)"
      case ForkSecond(first, t) => s"ForkSecond(${first.toString.take(10)}..., ${t.toString.take(10)}...)"
      case Fixpoint(fx, t) => s"Fixpoint($fx, ${t.toString.take(10)}...)"


object ControlEventParser:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)

  def parse[Atom,Section,Exc,Fx](es: Iterable[ControlEvent[Atom,Section,Exc,Fx]]): ControlTree[Atom,Section,Exc,Fx] =
    val parser = new ControlEventParser[Atom,Section,Exc,Fx]
    es.foreach(parser.handle)
    parser.getFinalTree

