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

  private type CNodes = Set[CNode]
  private type ActiveExc = Map[Exc, CNodes]
  private type Fixpoints = mutable.Map[Fx, (CNodes, ActiveExc)]
  private type Graph = mutable.Set[CEdge]
  private val curg: Graph = mutable.Set.empty

  private var stack: List[Entry] = List()
  private val fixpoints: Fixpoints = mutable.Map.empty
  private var predecessors: CNodes = Set(Node.Start())
  private var activeExc: ActiveExc = Map()

  private def mergeAes(aes1: ActiveExc, aes2: ActiveExc): ActiveExc =
    aes1 ++ aes2.map { case (k, v) => k -> (aes1.getOrElse(k, Set.empty) ++ v) }

  def get: ControlGraph[Atom, Section] =
    if (stack.nonEmpty) throw new Exception(s"Stack non empty $stack")
    ControlGraph(addBlockPairEdges(curg.toSet))

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    assertNoCatching()
    ev match
      case at@BasicControlEvent.Atomic(a) =>
        addNode(Node.Atomic(a)(at.label))
      case BasicControlEvent.Failed() =>
        addNode(Node.Failure())
        predecessors = Set.empty
      case s@BasicControlEvent.BeginSection(sec: Section) =>
        addNode(Node.BlockStart(sec)(s.label))
        stack = Entry.Sec(sec)(s.label) :: stack
      case BasicControlEvent.EndSection() => stack match
        case (s@Entry.Sec(sec)) :: stack_ =>
          stack = stack_
          if (predecessors.isEmpty) {
            // nothing
          } else {
            addNode(Node.BlockEnd(sec)(s.label))
          }
        case _ => error(s"Entry mismatch, expected end of $ev: $stack")

  def isCatching: Boolean = stack match
    case (_: Entry.Catching) :: _ => true
    case _ => false

  private def addNode(node: CNode): Unit =
    predecessors.foreach(predecessor =>
      curg += Edge(predecessor.n, node.n, if predecessor.exc then EdgeType.Exceptional else EdgeType.CF)
    )
    predecessors = Set(node)

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
        stack = Entry.ForkFirst(predecessors, activeExc) :: stack
        activeExc = Map.empty
      case Switch() => stack match
        case Entry.ForkFirst(originTails, originExc) :: stack_ =>
          stack = Entry.ForkSecond(predecessors, mergeAes(originExc, activeExc)) :: stack_
          predecessors = originTails
          activeExc = originExc
        case _ => error(s"Entry mismatch, expected ForkFirst for $ev: $stack")
      case BranchingControlEvent.Join() => stack match
        case Entry.ForkSecond(firstTails, firstOriginExc) :: stack_ =>
          stack = stack_
          predecessors = firstTails ++ predecessors
          activeExc = mergeAes(firstOriginExc, activeExc)
        case _ => error(s"Entry mismatch, expected ForkSecond for $ev: $stack")

  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() =>
        assertNoCatching()
        stack = Entry.Try(outside = activeExc) :: stack
        activeExc = Map.empty
      case Throw(exc: Exc) =>
        assertNoCatching()
        activeExc = mergeAes(activeExc, Map(exc -> predecessors.map(_.copy(exc = true))))
        predecessors = Set.empty
      case Catching() => stack match
        case Entry.Try(outside) :: stack_ =>
          stack = Entry.Catching(
            bodyExc = activeExc,
            outside = outside,
          ) :: stack_
          activeExc = Map.empty
        case _ => error(s"Entry mismatch, expected Try for $ev: $stack")
      case BeginHandle(exc: Exc) => stack match
        case Entry.Catching(bodyExc, outside) :: stack_ =>
          stack = Entry.Handler(exc, predecessors, bodyExc, outside, activeExc) :: stack_
          predecessors = bodyExc.getOrElse(exc, Set.empty)
          activeExc = Map.empty
        case _ => error(s"Entry mismatch, expected Catching for $ev: $stack")
      case EndHandle() => stack match
        case Entry.Handler(hx, tails, bodyExc, outside, resultExc) :: stack_ =>
          stack = Entry.Catching(bodyExc, outside) :: stack_
          predecessors = tails ++ predecessors
          activeExc = mergeAes(resultExc, activeExc)
        case _ => error(s"Entry mismatch, expected Handler for $ev: $stack")
      case EndTry() => stack match
        case Entry.Try(outside) :: stack_ =>
          stack = stack_
          activeExc = outside
        case Entry.Catching(bodyExc, outside) :: stack_ =>
          stack = stack_
          activeExc = mergeAes(outside, activeExc)
        case _ => error(s"Entry mismatch, expected Try or Catching for $ev: $stack")

  override def handle(ev: FixpointControlEvent[Atom,Section,Exc,Fx]): Unit =
    import FixpointControlEvent.*
    assertNoCatching()
    ev match
      case BeginFixpoint(fx) =>
        stack = Entry.Fixpoint(fx) :: stack
      case Recurrent(fx) =>
        val (tails, xs) = fixpoints.getOrElse(fx, (Set.empty, Map.empty))
        predecessors = tails
        activeExc = xs
      case EndFixpoint() => stack match
        case Entry.Fixpoint(fx) :: stack_ =>
          stack = stack_
          fixpoints += fx -> (
            predecessors ++ fixpoints.getOrElse(fx, (Set.empty, Map.empty))._1,
            mergeAes(activeExc, fixpoints.getOrElse(fx, (Set.empty, Map.empty))._2))
        case _ =>
          error(s"Entry mismatch, expected Fixpoint for $ev: $stack")
      case Restart() =>
        predecessors = Set.empty
        activeExc = Map.empty

  private enum Entry:
    case Sec(s: Section)(val label: String)
    case Try(outside: ActiveExc)
    case Catching(bodyExc: ActiveExc, outside: ActiveExc)
    case Handler(exc: Exc, tails: CNodes, bodyExc: ActiveExc, outside: ActiveExc, resultExc: ActiveExc)
    case ForkFirst(originTails: CNodes, originExc: ActiveExc)
    case ForkSecond(firstTails: CNodes, firstOriginExc: ActiveExc)
    case Fixpoint(fx: Fx)

  private def addBlockPairEdges(edges: Set[CEdge]): Set[CEdge] =
    val openedSections = edges.flatMap(e => List(e._1, e._2)).flatMap {
      case s@Node.BlockStart(sec) => List(s)
      case _ => List.empty
    }

    val closedSections = openedSections.filter { s =>
      val sec = s.sec
      edges.exists {
        case Edge(_, Node.BlockEnd(`sec`), _) => true
        case Edge(Node.BlockEnd(`sec`), _, _) => true
        case _ => false
      }
    }

    val blockPairEdges: Set[CEdge] = closedSections.flatMap { s =>
      val sec = s.sec
      if (edges.exists {
        case Edge(Node.BlockStart(`sec`), Node.BlockEnd(`sec`), _) => true
        case _ => false
      })
        List.empty
      else
        List(Edge(s, Node.BlockEnd(sec)(s.label), EdgeType.BlockPair))
    }

    edges ++ blockPairEdges

object ControlEventGraphBuilder:
  private def error(msg: String): Nothing =
    println(s"############ Control Event Error: $msg")
    throw InvalidControlEventSequence(msg)

  case class InvalidControlEventSequence(msg: String) extends Exception(msg)
