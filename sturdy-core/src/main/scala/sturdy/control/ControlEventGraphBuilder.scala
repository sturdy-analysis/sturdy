package sturdy.control

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

enum Node:
  case Start()
  case Atomic(label: String)
  case BlockStart(label: String)
  case BlockEnd(label: String)
  case VirtualFork(label: String)
  case VirtualJoin(label: String)

enum EdgeType:
  case CF
  case BlockPair
  case Thn
  case Els

type Edge = (Node, Option[Node], EdgeType)

class ControlEventGraphBuilder[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import Node.*
  import EdgeType.*

  var stack: mutable.Stack[Edge] = mutable.Stack.empty
  var edges: ListBuffer[Edge] = ListBuffer.empty

  var started: Boolean = false

  override def handle(ev: ControlEvent[Atom, Section, Exc]): Unit =
    if !started then
    ev match
      case ControlEvent.Start() => // PUSH 1
        started = true
        stack.push((Start(), None, CF))
      case _ => throw new Exception("Not started !")
    else
      ev match
        case ControlEvent.Start() => throw new Exception("Duplicate start")
        case ControlEvent.Atomic(a: Atom) => // POP 1 PUSH 1
          val current = Node.Atomic(a.toString)
          edges += completeEdge(current)
          stack.push((current, None, CF))
        case ControlEvent.Begin(sec: Section) => // POP 1 PUSH 2
          val current = Node.BlockStart(sec.toString)
          edges += completeEdge(current)
          stack.push((current, None, BlockPair))
          stack.push((current, None, CF))
        case ControlEvent.End(sec: Section) => // POP 2 PUSH 1
          val current = Node.BlockEnd(sec.toString)
          edges += completeEdge(current)
          edges += completeEdge(current)
          stack.push((current, None, CF))
        case ControlEvent.Fork() => // POP 1 PUSH 2
          val current = VirtualFork(Random.alphanumeric.take(10).mkString)
          edges += completeEdge(current)
          stack.push((current, None, Els))
          stack.push((current, None, Thn))
        case ControlEvent.Switch() => // POP 2 PUSH 2 (swap top 2)
          val lastThn = stack.pop
          val elsEdge = stack.pop
          stack.push(lastThn)
          stack.push(elsEdge)

        case ControlEvent.Join() => // POP 2 PUSH 1
          val current = VirtualJoin(Random.alphanumeric.take(10).mkString)
          edges += completeEdge(current)
          edges += completeEdge(current)
          stack.push((current, None, CF))
        case _ => ??? // TODO

  private inline def completeEdge(node : Node) : Edge =
    val edge = stack.pop()
    (edge._1, Some(node), edge._3)


  def toGraphViz : String = edges.map {
    case ((n1, Some(n2), CF)) => s"\"${n1} (${n1.hashCode()})\" -> \"${n2} (${n2.hashCode()})\""
    case ((n1, Some(n2), BlockPair)) => s"\"${n1} (${n1.hashCode()})\" -> \"${n2} (${n2.hashCode()})\"  [style=dashed]"
    case ((n1, Some(n2), Thn)) => s"\"${n1} (${n1.hashCode()})\" -> \"${n2} (${n2.hashCode()})\"  [label=\"then\" color=green]"
    case ((n1, Some(n2), Els)) => s"\"${n1} (${n1.hashCode()})\" -> \"${n2} (${n2.hashCode()})\"  [label=\"else\" color=blue]"
    case _ => ""
  }.reduce((s1, s2) => s1 + "\n" + s2)