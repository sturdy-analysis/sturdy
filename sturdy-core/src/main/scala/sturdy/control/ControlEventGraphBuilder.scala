package sturdy.control

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.util.Random

class ControlEventGraphBuilder[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:

  type CNode = Node[Atom, Section]
  type CEdge = Edge[Atom, Section]

  private case class NodeProperties(mayFail: Boolean)

  private enum ProgramStructure:
    case Block(start: CNode)
    case Fork(origin: List[CNode], lastFirstBranch: List[CNode])

  val edges: mutable.Set[CEdge] = mutable.Set.empty
  val nodes: mutable.Set[CNode] = mutable.Set.empty

  private val nodesProperties: mutable.Map[CNode, NodeProperties] = mutable.Map.empty

  private var structureStack: List[ProgramStructure] = List.empty
  private var ancestors: List[CNode] = List.empty

  private var fixpointAncestors: List[CNode] = List.empty

  var checker: ControlEventChecker[Atom, Section, Exc] = new ControlEventChecker

  private def addNode(node : CNode) : List[CNode] =
    val previous = ancestors
    ancestors.foreach(n => edges += (Edge(n, node, EdgeType.CF)))
    ancestors = List(node)
    nodes += node
    nodesProperties.getOrElseUpdate(node, NodeProperties(mayFail = false))
    previous

  private def checkFail(ev : ControlEvent) : Unit =
    if (checker.failing)
      ancestors = List.empty
    checker.handle(ev)

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    checkFail(ev)
    ev match
      case Atomic(a: Atom) => addNode(Node.Atomic(a))
      case Begin(sec: Section) =>
        val current : CNode = Node.BlockStart(sec)
        addNode(current)
        structureStack = ProgramStructure.Block(current) :: structureStack
      case End(sec: Section) =>
        structureStack.head match
          case ProgramStructure.Block(start) =>
            val end : CNode = Node.BlockEnd(sec)
            val prev = addNode(end) // check to print helper BlockPair edge only if there is no direct CF edge between the two Block nodes
            structureStack = structureStack.tail
            if (prev.nonEmpty && !prev.contains(Node.BlockStart(sec)))
              edges += (Edge(start, end, EdgeType.BlockPair))
          case _ => throw new Exception("Illegal control event sequence")
      case BasicControlEvent.Failed() => ancestors.foreach(n => nodesProperties += n -> NodeProperties(mayFail = true)) // Change to creating a Failure node, deleting the properties and coloring the node via post processing


  override def handle(ev: ExceptionControlEvent[Exc]): Unit =
    import ExceptionControlEvent.*
    checkFail(ev)
    ev match
      case BeginTry() => ()
      case Throw(exc) => ()
      case Catching() => ()
      case Handle(exc) => ()
      case EndTry() => ()


  override def handle(ev: BranchingControlEvent): Unit =
    import BranchingControlEvent.*
    checkFail(ev)
    ev match
      case Fork() => structureStack = ProgramStructure.Fork(ancestors, List.empty) :: structureStack
      case Switch() =>
        structureStack.head match
          case ProgramStructure.Fork(origin, _) =>
            structureStack = ProgramStructure.Fork(origin, ancestors) :: structureStack.tail
            ancestors = origin
          case _ => throw new Exception("Illegal control event sequence")
      case Join() =>
        structureStack.head match
          case ProgramStructure.Fork(_, lastFirstBranch) =>
            ancestors = ancestors ++ lastFirstBranch
            structureStack = structureStack.tail
          case _ => throw new Exception("Illegal control event sequence")

  override def handle(ev: FixpointControlEvent): Unit =
    import FixpointControlEvent.*
    checkFail(ev)
    ev match
      case BeginFixpoint() =>
        fixpointAncestors = ancestors
      case RecurrentCall(_) =>
        ancestors = List.empty
      case RepeatFixpoint() =>
        ancestors = fixpointAncestors
      case EndFixpoint() => ()


  def toGraphViz : String =
    if(structureStack.nonEmpty) throw new Exception(s"Stack non empty $structureStack")
    ControlGraph.toGraphViz(edges.toList)