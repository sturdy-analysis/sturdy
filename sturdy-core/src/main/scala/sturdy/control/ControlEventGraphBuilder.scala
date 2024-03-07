package sturdy.control

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.util.Random

class ControlEventGraphBuilder[Atom,Section,Exc,Fx] extends ControlObserver[Atom,Section,Exc,Fx]:

  type CNode = Node[Atom, Section]
  type CEdge = Edge[Atom, Section]

  private enum ProgramStructure:
    case Block(start: CNode)
    case Fork(origin: List[CNode], lastFirstBranch: List[CNode])

  val edges: mutable.Set[CEdge] = mutable.Set.empty
  val nodes: mutable.Set[CNode] = mutable.Set.empty

  private var structureStack: List[ProgramStructure] = List.empty
  private var predecessors: List[CNode] = List.empty

  private var fixpointAncestors: List[CNode] = List.empty

  var checker: ControlEventChecker[Atom, Section, Exc, Fx] = new ControlEventChecker

  private def addNode(node : CNode) : List[CNode] =
    val previous = predecessors
    predecessors.foreach(n => edges += Edge(n, node, EdgeType.CF))
    predecessors = List(node)
    nodes += node
    previous

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    ev match
      case Atomic(a: Atom) => addNode(Node.Atomic(a))
      case BeginSection(sec: Section) =>
        val current : CNode = Node.BlockStart(sec)
        addNode(current)
        structureStack = ProgramStructure.Block(current) :: structureStack
      case EndSection() =>
        structureStack.head match
          case ProgramStructure.Block(start@Node.BlockStart(sec)) =>
            val end : CNode = Node.BlockEnd(sec)
            val prev = addNode(end) // check to print helper BlockPair edge only if there is no direct CF edge between the two Block nodes
            structureStack = structureStack.tail
            if (prev.nonEmpty && !prev.contains(Node.BlockStart(sec)))
              edges += Edge(start, end, EdgeType.BlockPair)
          case _ => throw new Exception("Illegal control event sequence")
      case BasicControlEvent.Failed() =>
        addNode(Node.Failure())
        predecessors = List()


  override def handle(ev: ExceptionControlEvent[Exc]): Unit =
    import ExceptionControlEvent.*
    ev match
      case BeginTry() => ()
      case Throw(exc) => ()
      case Catching() => ()
      case Handle(exc) => ()
      case EndTry() => ()


  override def handle(ev: BranchingControlEvent): Unit =
    import BranchingControlEvent.*
    ev match
      case Fork() => structureStack = ProgramStructure.Fork(predecessors, List.empty) :: structureStack
      case Switch() =>
        structureStack.head match
          case ProgramStructure.Fork(origin, _) =>
            structureStack = ProgramStructure.Fork(origin, predecessors) :: structureStack.tail
            predecessors = origin
          case _ => throw new Exception("Illegal control event sequence")
      case Join() =>
        structureStack.head match
          case ProgramStructure.Fork(_, lastFirstBranch) =>
            predecessors = predecessors ++ lastFirstBranch
            structureStack = structureStack.tail
          case _ => throw new Exception("Illegal control event sequence")

  override def handle(ev: FixpointControlEvent[Fx]): Unit =
    import FixpointControlEvent.*
    ev match
      case BeginFixpoint(fx) =>
        fixpointAncestors = predecessors
      case Recurrent(_) =>
        predecessors = List.empty
      case RepeatFixpoint() =>
        predecessors = fixpointAncestors
      case EndFixpoint() => ()


  def toGraphViz : String =
    if(structureStack.nonEmpty) throw new Exception(s"Stack non empty $structureStack")
    ControlGraph.toGraphViz(edges.toSet)