package sturdy.control

import sturdy.control.FixpointControlEvent.BeginFixpoint

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

class ControlEventGraphBuilder[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:
  import ControlEventGraphBuilder.*

  private case class CNode(n: Node[Atom, Section], exc: Boolean)
  private implicit def cnode[A <: Atom,S <: Section](n: Node[A, S]): CNode = CNode(n, false)
  private type CEdge = Edge[Atom, Section]

  private type ActiveExc = Set[(Exc, List[CNode])]

  private enum Entry:
    case Sec(s: Section)
    case Try(outside: ActiveExc)
    case Catching(bodyTails: List[CNode], bodyExc: ActiveExc, outside: ActiveExc, handlers: List[Result])
    case Handler(exc: Exc)
    case ForkFirst(originTails: List[CNode], originExc: ActiveExc)
    case ForkSecond(firstTails: List[CNode], firstOriginExc: ActiveExc)
    case Fixpoint(fx: Fx)

  private case class Result(tails: List[CNode], xs: ActiveExc):
    def ||(that: Result): Result =
      Result(this.tails ++ that.tails, this.xs ++ that.xs)
  private object Result:
    val empty: Result = Result(List(), Set())

  private var stack: List[Entry] = List()
  private var predecessors: List[CNode] = List(Node.Start())
  private var activeExc: ActiveExc = Set()
  private val edges: mutable.Set[CEdge] = mutable.Set.empty
  private val fixpoints: mutable.Map[Fx, Result] = mutable.Map.empty

  def get: ControlGraph[Atom, Section] =
    if (stack.nonEmpty) throw new Exception(s"Stack non empty $stack")
    ControlGraph(edges.toSet)

  def isCatching: Boolean = stack match
    case (_: Entry.Catching) :: _ => true
    case _ => false

  private def addNode(node: CNode): List[CNode] =
    val previous = predecessors
    predecessors.foreach { n =>
      val edge = Edge(n.n, node.n, if (n.exc) EdgeType.Exceptional else EdgeType.CF)
      edges += edge
    }
    predecessors = List(node)
    previous

  private def addBlockPairEdges(sec: Section): Unit =
    if (!edges.contains(Edge(Node.BlockStart(sec), Node.BlockEnd(sec), EdgeType.CF)))
      edges += Edge(Node.BlockStart(sec), Node.BlockEnd(sec), EdgeType.BlockPair)

  def assertNoCatching(): Unit =
    if (isCatching) {
      error(s"Control event while catching but outside handler")
    }

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    assertNoCatching()
    ev match
      case BasicControlEvent.Atomic(a) => addNode(Node.Atomic(a))
      case BasicControlEvent.Failed() =>
        addNode(Node.Failure())
        predecessors = List.empty
      case BasicControlEvent.BeginSection(sec: Section) =>
        addNode(Node.BlockStart(sec))
        stack = Entry.Sec(sec) :: stack
      case BasicControlEvent.EndSection() => stack match
        case Entry.Sec(sec) :: stack_ =>
          stack = stack_
          if (predecessors.isEmpty) {
            // nothing
          } else {
            addNode(Node.BlockEnd(sec))
//            addBlockPairEdges(sec)
          }
        case _ => error(s"Entry mismatch, expected end of $ev: $stack")

  override def handle(ev: BranchingControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BranchingControlEvent.*
    if (isCatching) {
      // skip forks while catching
      return
    }
    ev match
      case Fork() =>
        stack = Entry.ForkFirst(predecessors, activeExc) :: stack
        activeExc = Set.empty
      case Switch() => stack match
        case Entry.ForkFirst(originTails, originExc) :: stack_ =>
          stack = Entry.ForkSecond(predecessors, originExc ++ activeExc) :: stack_
          predecessors = originTails
          activeExc = Set.empty
        case _ => error(s"Entry mismatch, expected ForkFirst for $ev: $stack")
      case BranchingControlEvent.Join() => stack match
        case Entry.ForkSecond(firstTails, firstOriginExc) :: stack_ =>
          stack = stack_
          predecessors = firstTails ++ predecessors
          activeExc = firstOriginExc ++ activeExc
        case _ => error(s"Entry mismatch, expected ForkSecond for $ev: $stack")


  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() =>
        assertNoCatching()
        stack = Entry.Try(outside = activeExc) :: stack
        activeExc = Set.empty
      case Throw(exc: Exc) =>
        assertNoCatching()
        activeExc = activeExc + (exc -> predecessors)
        predecessors = List.empty
      case Catching() => stack match
        case Entry.Try(outside) :: stack_ =>
          stack = Entry.Catching(
            bodyTails = predecessors,
            bodyExc = activeExc,
            outside,
            handlers = List.empty
          ) :: stack_
        case _ => error(s"Entry mismatch, expected Try for $ev: $stack")
      case BeginHandle(exc: Exc) => stack match
        case (c: Entry.Catching) :: stack_ =>
          stack = Entry.Handler(exc) :: c :: stack_
          predecessors = c.bodyExc.filter(_._1 == exc).flatMap(_._2).toList.map(_.copy(exc = true))
          activeExc = Set.empty
        case _ => error(s"Entry mismatch, expected Catching for $ev: $stack")
      case EndHandle() => stack match
        case Entry.Handler(hx) :: Entry.Catching(bodyTails, bodyExc, outside, hres) :: stack_ =>
          stack = Entry.Catching(bodyTails, bodyExc, outside, hres :+ Result(predecessors, activeExc)) :: stack_
        case _ => error(s"Entry mismatch, expected Handler for $ev: $stack")
      case EndTry() => stack match
        case Entry.Try(outside) :: stack_ =>
          stack = stack_
          activeExc = outside
        case Entry.Catching(bodyTails, bodyExc, outside, hs) :: stack_ =>
          stack = stack_
          val result = hs.foldRight(Result(bodyTails, Set()))(_ || _)
          predecessors = result.tails
          activeExc = outside ++ result.xs
        case _ => error(s"Entry mismatch, expected Try or Catching for $ev: $stack")

  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit =
    import FixpointControlEvent.*
    assertNoCatching()
    ev match
      case BeginFixpoint(fx) =>
        stack = Entry.Fixpoint(fx) :: stack
      case Recurrent(fx) =>
        val Result(tails, xs) = fixpoints.getOrElse(fx, Result.empty)
        predecessors = tails
        activeExc = xs
      case EndFixpoint() => stack match
        case Entry.Fixpoint(fx) :: stack_ =>
          stack = stack_
          fixpoints += fx -> (fixpoints.getOrElse(fx, Result.empty) || Result(predecessors, activeExc))
        case _ =>
          error(s"Entry mismatch, expected Fixpoint for $ev: $stack")
      case Restart() =>
        predecessors = List.empty
        activeExc = Set.empty


object ControlEventGraphBuilder:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
