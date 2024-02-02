package sturdy.control

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.util.Random




class ControlEventGraphBuilder[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:

  enum Node:
    case Start()
    case Atomic(atom: Atom)
    case BlockStart(sec: Section)
    case BlockEnd(label: Section)

  private case class NodeProperties(mayFail: Boolean)

  enum EdgeType:
    case CF
    case BlockPair

  type Edge = (Node, Node, EdgeType)

  private enum ProgramStructure:
    case Block(label: String, start: Node, end: Node)
    case Fork(label: String, origin: List[Node], lastFirstBranch: List[Node])


  val edges: mutable.Set[Edge] = mutable.Set.empty
  val nodes: mutable.Set[Node] = mutable.Set.empty

  private val nodesProperties: mutable.Map[Node, NodeProperties] = mutable.Map.empty

  private var structureStack: List[ProgramStructure] = List.empty
  private var ancestors: List[Node] = List.empty
  var started: Boolean = false

  private type BuilderState = (List[ProgramStructure], List[Node], Boolean)
  private val stateStack: mutable.Stack[BuilderState] = mutable.Stack.empty

  var checker: ControlEventChecker[Atom, Section, Exc] = new ControlEventChecker

  private def setToState(state: BuilderState) : Unit =
    structureStack = state._1
    ancestors = state._2
    started = state._3

  private def getCurrentState : BuilderState = (structureStack, ancestors, started)

  private def update(node : Node) : List[Node] =
    val previous = ancestors
    ancestors.foreach(n => edges += ((n, node, EdgeType.CF)))
    ancestors = List(node)
    nodes += node
    nodesProperties.getOrElseUpdate(node, NodeProperties(mayFail = false))
    previous


  override def handle(ev: ControlEvent[Atom, Section, Exc]): Unit =
    val failing = checker.failing
    checker.handle(ev)

    if failing then
      ancestors = List.empty

    if !started then
      ev match
        case ControlEvent.Start() => // PUSH 1
          started = true
          val current = Node.Start()
          ancestors = List(current)
          nodes += current

        case _ => throw new Exception("Not started !")
    else
      ev match
        case ControlEvent.Start() => throw new Exception("Duplicate start")

        case ControlEvent.Atomic(a: Atom) =>
          val current = Node.Atomic(a)
          update(current)

        case ControlEvent.Begin(sec: Section) =>
          val current = Node.BlockStart(sec)
          update(current)
          structureStack = structureStack ++ List(ProgramStructure.Block(sec.toString, current, Node.BlockEnd(sec)))

        case ControlEvent.End(sec: Section) =>
          structureStack.last match
            case ProgramStructure.Block(label, start, end) => // add check match block
              val ancest = update(end)
              structureStack = structureStack.dropRight(1)
              if (ancest.nonEmpty && !ancest.contains(Node.BlockStart(sec)))
                edges += ((start, end, EdgeType.BlockPair))
            case _ => throw new Exception("Illegal control event sequence")

        case ControlEvent.Fork() =>
          structureStack = structureStack ++ List(ProgramStructure.Fork("", ancestors, List.empty))

        case ControlEvent.Switch() =>
          structureStack.last match
            case ProgramStructure.Fork(label, origin, _) =>
              structureStack = structureStack.dropRight(1) ++ List(ProgramStructure.Fork(label, origin, ancestors))
              ancestors = origin
            case _ => throw new Exception("Illegal control event sequence")

        case ControlEvent.Join() =>
          structureStack.last match
            case ProgramStructure.Fork(_, _, lastFirstBranch) =>
              ancestors = ancestors ++ lastFirstBranch
              structureStack = structureStack.dropRight(1)
            case _ => throw new Exception("Illegal control event sequence")

        case ControlEvent.Failed() =>
          ancestors.foreach(n => nodesProperties += n -> NodeProperties(mayFail = true))

        case ControlEvent.FixpointPrepare() =>
          stateStack.push(getCurrentState)

        case ControlEvent.FixpointRecurrent() =>
          ancestors = List.empty

        case ControlEvent.FixpointRepeat() =>
          setToState(stateStack.head)

        case ControlEvent.FixpointRelease() =>
          stateStack.pop()

        case _ => () // TODO


  def toGraphViz : String =
    if(structureStack.nonEmpty) throw new Exception(s"Stack non empty ${structureStack}")
    edges.map {
      case ((n1, n2, EdgeType.CF)) => s"\"${n1}\" -> \"${n2}\""
      case ((n1, n2, EdgeType.BlockPair)) => s"\"${n1}\" -> \"${n2}\"  [style=dashed]"
    }.reduce((s1, s2) => s1 + "\n" + s2)
    + "\n\n\n\n" +
    nodes.filter(node => edges.exists(e => e._1 == node || e._2 == node)).map { node => (node, nodesProperties.get(node)) match
      case (n : Node.Atomic, Some(NodeProperties(true))) => s"\"${n}\" [style=filled, fillcolor=\"#FFBBBB\"]"
      case (n : Node.Atomic, Some(NodeProperties(false))) => s"\"${n}\" [style=filled, fillcolor=\"#BBFFBB\"]"
      case (n : Node.BlockStart, _) => s"\"${n}\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
      case (n : Node.BlockEnd, _) => s"\"${n}\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
      case (n : Node.Start, _) => s"\"${n}\" [shape=circle, style=filled, fillcolor=\"#BBBBBB\"]"
      case _ => ""
    }.reduce((s1, s2) => s1 + "\n" + s2)