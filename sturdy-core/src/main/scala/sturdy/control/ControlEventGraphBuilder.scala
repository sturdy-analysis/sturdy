package sturdy.control

import org.eclipse.collections.api.factory.Maps
import org.eclipse.collections.api.map.MutableMap
import org.eclipse.collections.api.multimap.MutableMultimap
import org.eclipse.collections.api.multimap.set.{ImmutableSetMultimap, MutableSetMultimap}
import org.eclipse.collections.api.set.{ImmutableSet, MutableSet}
import org.eclipse.collections.impl.factory.{Multimaps, Sets}
import sturdy.control.EdgeType.Exceptional
import sturdy.control.FixpointControlEvent.BeginFixpoint

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.jdk.CollectionConverters.*
import scala.jdk.FunctionConverters.*

/**
 * Observes an interpreter and the control event it sends to build a ICFG graph.
 * Only observer needed for this purpose.
 */
class ControlEventGraphBuilder[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:
  import ControlEventGraphBuilder.*

  private case class CNode(n: Node[Atom, Section], exc: Boolean)
  private implicit def cnode[A <: Atom,S <: Section](n: Node[A, S]): CNode = CNode(n, false)
  private type CEdge = Edge[Atom, Section]

  private type CNodes = ImmutableSet[CNode]
  private type ActiveExc = ImmutableSetMultimap[Exc, CNode]
  private type Fixpoints = MutableMap[Fx, (CNodes, ActiveExc)]

  /** A multimap of _backward_ edges */
  private type Graph = MutableSetMultimap[CNode, CNode]

  private val emptyPredecessors: CNodes = Sets.immutable.empty()
  private val emptyActiveExc: ActiveExc = Multimaps.immutable.set.empty()

  private var stack: List[Entry] = List()
  private val curg: Graph = Multimaps.mutable.set.empty()
  private val fixpoints: Fixpoints = Maps.mutable.empty()
  private var predecessors: CNodes = Sets.immutable.of(Node.Start())
  private var activeExc: ActiveExc = emptyActiveExc

  private def mergeAes(aes1: ActiveExc, aes2: ActiveExc): ActiveExc =
    if (aes1.isEmpty)
      aes2
    else if (aes2.isEmpty)
      aes1
    else if (aes1.size >= aes2.size) {
      val map = aes1.toMutable
      map.putAll(aes2)
      map.toImmutable
    } else {
      val map = aes2.toMutable
      map.putAll(aes1)
      map.toImmutable
    }

  def get: ControlGraph[Atom, Section] =
    if (stack.nonEmpty) throw new Exception(s"Stack non empty $stack")
    val set = mutable.Set.empty[CEdge]
    curg.forEachKeyValue((to, from) =>
      set += Edge(from.n, to.n, if (from.exc) EdgeType.Exceptional else EdgeType.CF)
      to.n match
        case n@Node.BlockEnd(sec) => set += Edge(Node.BlockStart(sec)(n.label), n, EdgeType.BlockPair)
        case _ => // nothing
    )
    ControlGraph(set.toSet)

  override def handle(ev: BasicControlEvent[Atom,Section,Exc,Fx]): Unit =
    import BasicControlEvent.*
    assertNoCatching()
    ev match
      case at@BasicControlEvent.Atomic(a) =>
        addNode(Node.Atomic(a)(at.label))
      case BasicControlEvent.Failed() =>
        addNode(Node.Failure())
        predecessors = emptyPredecessors
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
    curg.putAll(node, predecessors)
    predecessors = Sets.immutable.of(node)

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
        activeExc = emptyActiveExc
      case Switch() => stack match
        case Entry.ForkFirst(originTails, originExc) :: stack_ =>
          stack = Entry.ForkSecond(predecessors, mergeAes(originExc, activeExc)) :: stack_
          predecessors = originTails
          activeExc = emptyActiveExc
        case _ => error(s"Entry mismatch, expected ForkFirst for $ev: $stack")
      case BranchingControlEvent.Join() => stack match
        case Entry.ForkSecond(firstTails, firstOriginExc) :: stack_ =>
          stack = stack_
          predecessors = firstTails.newWithAll(predecessors)
          activeExc = mergeAes(firstOriginExc, activeExc)
        case _ => error(s"Entry mismatch, expected ForkSecond for $ev: $stack")

  override def handle(ev: ExceptionControlEvent[Atom,Section,Exc,Fx]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() =>
        assertNoCatching()
        stack = Entry.Try(outside = activeExc) :: stack
        activeExc = emptyActiveExc
      case Throw(exc: Exc) =>
        assertNoCatching()
        val predsExc = predecessors.collect(_.copy(exc = true))
        activeExc = activeExc.newWithAll(exc, predsExc)
        predecessors = emptyPredecessors
      case Catching() => stack match
        case Entry.Try(outside) :: stack_ =>
          stack = Entry.Catching(
            bodyExc = activeExc,
            outside = outside,
          ) :: stack_
          activeExc = emptyActiveExc
        case _ => error(s"Entry mismatch, expected Try for $ev: $stack")
      case BeginHandle(exc: Exc) => stack match
        case Entry.Catching(bodyExc, outside) :: stack_ =>
          stack = Entry.Handler(exc, predecessors, bodyExc, outside, activeExc) :: stack_
          predecessors = bodyExc.get(exc)
          activeExc = emptyActiveExc
        case _ => error(s"Entry mismatch, expected Catching for $ev: $stack")
      case EndHandle() => stack match
        case Entry.Handler(hx, tails, bodyExc, outside, resultExc) :: stack_ =>
          stack = Entry.Catching(bodyExc, outside) :: stack_
          predecessors = tails.newWithAll(predecessors)
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
        val (tails, xs) = fixpoints.getOrDefault(fx, (emptyPredecessors, emptyActiveExc))
        predecessors = tails
        activeExc = xs
      case EndFixpoint() => stack match
        case Entry.Fixpoint(fx) :: stack_ =>
          stack = stack_
          val (tails, xs) = fixpoints.getOrDefault(fx, (emptyPredecessors, emptyActiveExc))
          fixpoints.put(fx, (predecessors.newWithAll(tails), mergeAes(activeExc, xs)))
        case _ =>
          error(s"Entry mismatch, expected Fixpoint for $ev: $stack")
      case Restart() =>
        predecessors = emptyPredecessors
        activeExc = emptyActiveExc

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
