package sturdy.control

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

enum Node:
  case Start()
  case Atomic(label: String)
  case BlockStart(label: String)
  case BlockEnd(label: String)
  case Failed(label: String)

enum EdgeType:
  case CF
  case BlockPair
  /*
  case Thn
  case Els
  case JoinThn
  case JoinEls
  case EmptyEls
   */

type ProtoEdge = (Node, EdgeType)

type Edge = (Node, Node, EdgeType)

class ControlEventGraphBuilder[Atom,Section,Exc] extends ControlObserver[Atom,Section,Exc]:
  import Node.*
  import EdgeType.*

  var stack: mutable.Stack[List[ProtoEdge]] = mutable.Stack.empty
  var edges: mutable.Set[Edge] = mutable.Set.empty

  var started: Boolean = false

  override def handle(ev: ControlEvent[Atom, Section, Exc]): Unit =
    if !started then
      ev match
        case ControlEvent.Start() => // PUSH 1
          started = true
          stack.push(List((Start(), CF)))
        case _ => throw new Exception("Not started !")
    else
      ev match
        case ControlEvent.Start() => throw new Exception("Duplicate start")
        
        case ControlEvent.Atomic(a: Atom) => // POP 1 PUSH 1
          val current = Node.Atomic(a.toString)
          completeEdge(current)

          stack.push(List((current, CF)))

        case ControlEvent.Begin(sec: Section) => // POP 1 PUSH 2
          val current = Node.BlockStart(sec.toString)
          completeEdge(current)

          stack.push(List((current, BlockPair)))
          stack.push(List((current, CF)))
          
        case ControlEvent.End(sec: Section) => // POP 2 PUSH 1
          val current = Node.BlockEnd(sec.toString)
          completeEdge(current)
          completeEdge(current)

          stack.push(List((current, CF)))

        case ControlEvent.Fork() => // POP 1 PUSH 2
          val prev = stack.pop

          stack.push(prev)
          stack.push(prev)

          /*
          stack.push(prev.map((n, _) => (n, Els)))
          stack.push(prev.map((n, _) => (n, Thn)))
           */

        case ControlEvent.Switch() => // POP 2 PUSH 2 (swap top 2)
          val lastThn = stack.pop
          val elsEdge = stack.pop

          stack.push(lastThn)
          stack.push(elsEdge)

        case ControlEvent.Join() => // POP 2 PUSH 1
          val lastEls = stack.pop
          val lastThn = stack.pop

          stack.push(lastEls/*.map((n, et) => (n, if et == Els then CF else JoinEls))*/ ++ lastThn/*.map((n, _) => (n, JoinThn))*/)

        case ControlEvent.Failed() =>
          val current = Node.Failed("")
          completeEdge(current)
          stack.push(List.empty)

        case _ => ??? // TODO

  private def completeEdge(current : Node) : Unit = completeEdge(current, None)

  private def completeEdge(current : Node, edgeType: Option[EdgeType]) : Unit =
    val protoEdges : List[ProtoEdge] = stack.pop()
    protoEdges.foreach {
      (prev, et : EdgeType) => edges += ((prev, current, edgeType.getOrElse(et) ))
    }

  def toGraphViz : String =
    if(stack.size != 1) throw new Exception(s"Wrong stack state size ${stack.size}")
    edges.map {
      case ((n1, n2, CF)) => s"\"${n1}\" -> \"${n2}\""
      case ((n1, n2, BlockPair)) => s"\"${n1}\" -> \"${n2}\"  [style=dashed]"
      /*
      case ((n1, n2, Thn)) => s"\"${n1}\" -> \"${n2}\"  [label=\"then\" color=green]"
      case ((n1, n2, Els)) => s"\"${n1}\" -> \"${n2}\"  [label=\"else\" color=blue]"
      case ((n1, n2, JoinThn)) => s"\"${n1}\" -> \"${n2}\"  [label=\"JoinThen\" color=green]"
      case ((n1, n2, JoinEls)) => s"\"${n1}\" -> \"${n2}\"  [label=\"JoinElse\" color=blue]"
      case ((n1, n2, EmptyEls)) => s"\"${n1}\" -> \"${n2}\"  [label=\"EmptyEls\" color=yellow]"
      */
    }.reduce((s1, s2) => s1 + "\n" + s2)