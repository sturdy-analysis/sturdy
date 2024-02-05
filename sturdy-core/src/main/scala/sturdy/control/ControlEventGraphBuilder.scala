package sturdy.control

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.util.Random

class ControlEventGraphBuilder[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:

  enum Node:
    case Atomic(atom: Atom)
    case BlockStart(sec: Section)
    case BlockEnd(label: Section)

  private case class NodeProperties(mayFail: Boolean)

  enum EdgeType:
    case CF
    case BlockPair

  private type Edge = (Node, Node, EdgeType)

  private enum ProgramStructure:
    case Block(start: Node)
    case Fork(origin: List[Node], lastFirstBranch: List[Node])

  val edges: mutable.Set[Edge] = mutable.Set.empty
  val nodes: mutable.Set[Node] = mutable.Set.empty

  private val nodesProperties: mutable.Map[Node, NodeProperties] = mutable.Map.empty

  private var structureStack: List[ProgramStructure] = List.empty
  private var ancestors: List[Node] = List.empty

  private type BuilderState = (List[ProgramStructure], List[Node])
  private var fixpoint: List[Node] = List.empty

  var checker: ControlEventChecker[Atom, Section, Exc] = new ControlEventChecker

  private def update(node : Node) : List[Node] =
    val previous = ancestors
    ancestors.foreach(n => edges += ((n, node, EdgeType.CF)))
    ancestors = List(node)
    nodes += node
    nodesProperties.getOrElseUpdate(node, NodeProperties(mayFail = false))
    previous

  override def handle(ev: BasicControlEvent[Atom, Section]): Unit =
    import BasicControlEvent.*
    if (checker.failing)
      ancestors = List.empty
    checker.handle(ev)
    ev match
      case Atomic(a: Atom) => update(Node.Atomic(a))
      case Begin(sec: Section) =>
        val current = Node.BlockStart(sec)
        update(current)
        structureStack = ProgramStructure.Block(current) :: structureStack
      case End(sec: Section) =>
        structureStack.head match
          case ProgramStructure.Block(start) =>
            val end = Node.BlockEnd(sec)
            val prev = update(end) // check to print helper BlockPair edge only if there is no direct CF edge between the two Block nodes
            structureStack = structureStack.tail
            if (prev.nonEmpty && !prev.contains(Node.BlockStart(sec)))
              edges += ((start, end, EdgeType.BlockPair))
          case _ => throw new Exception("Illegal control event sequence")
      case BasicControlEvent.Failed() => ancestors.foreach(n => nodesProperties += n -> NodeProperties(mayFail = true)) // Change to creating a Failure node, deleting the properties and coloring the node via post processing


  override def handle(ev: ExceptionControlEvent[Exc]): Unit = ()

  override def handle(ev: BranchingControlEvent): Unit =
    if (checker.failing)
      ancestors = List.empty
    checker.handle(ev)
    ev match
      case BranchingControlEvent.Fork() => structureStack = ProgramStructure.Fork(ancestors, List.empty) :: structureStack
      case BranchingControlEvent.Switch() =>
        structureStack.head match
          case ProgramStructure.Fork(origin, _) =>
            structureStack = ProgramStructure.Fork(origin, ancestors) :: structureStack.tail
            ancestors = origin
          case _ => throw new Exception("Illegal control event sequence")
      case BranchingControlEvent.Join() =>
        structureStack.head match
          case ProgramStructure.Fork(_, lastFirstBranch) =>
            ancestors = ancestors ++ lastFirstBranch
            structureStack = structureStack.tail
          case _ => throw new Exception("Illegal control event sequence")

  override def handle(ev: FixpointControlEvent): Unit =
    if (checker.failing)
      ancestors = List.empty
    checker.handle(ev)
    ev match
      case FixpointControlEvent.BeginFixpoint() =>
        fixpoint = ancestors
      case FixpointControlEvent.RecurrentCall(_) =>
        ancestors = List.empty
      case FixpointControlEvent.RepeatFixpoint() =>
        ancestors = fixpoint
      case FixpointControlEvent.EndFixpoint() => ()


  def toGraphViz : String =
    if(structureStack.nonEmpty) throw new Exception(s"Stack non empty $structureStack")
    edges.map {
      case (n1, n2, EdgeType.CF) => s"\"$n1\" -> \"$n2\""
      case (n1, n2, EdgeType.BlockPair) => s"\"$n1\" -> \"$n2\"  [style=dashed]"
    }.reduce((s1, s2) => s1 + "\n" + s2)
    + "\n\n\n\n" +
    nodes.filter(node => edges.exists(e => e._1 == node || e._2 == node)).map { node => (node, nodesProperties.get(node)) match
      case (n : Node.Atomic, Some(NodeProperties(true))) => s"\"$n\" [style=filled, fillcolor=\"#FFBBBB\"]"
      case (n : Node.Atomic, Some(NodeProperties(false))) => s"\"$n\" [style=filled, fillcolor=\"#BBFFBB\"]"
      case (n : Node.BlockStart, _) => s"\"$n\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
      case (n : Node.BlockEnd, _) => s"\"$n\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
      case _ => ""
    }.reduce((s1, s2) => s1 + "\n" + s2)