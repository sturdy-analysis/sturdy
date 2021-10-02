package sturdy.fix

import sturdy.effect.JoinObserver
import sturdy.effect.ObservableJoin

import collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

def control[Ctx, Dom, Codom, Node]
  (contextSensitive: Boolean)
  (getDomNode: Dom => Option[Node])
  (getCodomNode: (Dom, Codom) => Option[Node])
  (using effect: ObservableJoin)
  : ControlLogger[Ctx, Dom, Codom, Node] =
  new ControlLogger(contextSensitive, getDomNode, getCodomNode, effect)

class ControlLogger[Ctx, Dom, Codom, Node]
  (contextSensitive: Boolean,
   getDomNode: Dom => Option[Node],
   getCodomNode: (Dom, Codom) => Option[Node],
   effect: ObservableJoin
  )
  extends JoinObserver:

  effect.addJoinObserver(this)

  val ROOT = CNode(null.asInstanceOf[Node], null.asInstanceOf[Ctx])
  case class CNode(node: Node, ctx: Ctx):
    override def toString: String =
      if (this == ROOT)
        "Root"
      else if (ctx == null)
        node.toString
      else
        s"$node | $ctx"
  
  private var predecessors: Set[CNode] = Set(ROOT)
  private var trace: List[Set[CNode]] = List()

  override def joinStart(): Unit =
    println(s"Join start $predecessors :: $trace")
    trace = predecessors :: trace

  override def joinSwitch(): Unit =
    val others = trace.head
    println(s"Join switch $predecessors <-> $trace")
    trace = predecessors :: trace.tail
    predecessors = others

  override def joinEnd(): Unit =
    val others = trace.head
    trace = trace.tail
    predecessors = predecessors ++ others
    println(s"Join end $predecessors :: $trace")

  override def repeating(): Unit =
    predecessors = Set()

  private val nodes: mutable.Set[CNode] = mutable.Set(ROOT)
  private val edges: mutable.Map[CNode, Set[CNode]] = mutable.Map()

  inline private def addEdgeFromPredecessors(to: CNode): Unit =
    predecessors.foreach(from => addEdge(from, to))

  private def addEdge(from: CNode, to: CNode): Unit =
//    println(s"Edge $from -> $to")
    edges.get(from) match
      case None => edges += from -> Set(to)
      case Some(set) => edges += from -> (set + to)

  def getNodes: List[CNode] = nodes.toList.sortBy(_.toString)
  def getEdges: Map[CNode, Set[CNode]] = edges.toMap
  def getEdgesFlat: List[(CNode, CNode)] = edges.toList.flatMap((from, tos) => tos.map(to => from -> to)).sortBy(_.toString)

  def logger(using contextual: Contextual[Ctx, Dom, Codom]): Logger[Dom, Codom] = new Logger {
    private def getContext: Ctx =
      if (contextSensitive)
        contextual.getCurrentContext
      else
        null.asInstanceOf[Ctx]

    override def enter(dom: Dom): Unit =
      println(s"Enter $dom  ---  $predecessors")
      getDomNode(dom) match
        case Some(node) =>
          val cnode = CNode(node, getContext)
  //        println(s"Visit $cnode")
          nodes += cnode
          addEdgeFromPredecessors(cnode)
          predecessors = Set(cnode)
        case None => // nothing

    override def exit(dom: Dom, codom: Try[Codom]): Unit =
      codom match
        case Success(cod) => getCodomNode(dom, cod.asInstanceOf[Codom]) match
          case Some(node) =>
            val cnode = CNode(node, getContext)
//            println(s"Visit $cnode")
            nodes += cnode
            addEdgeFromPredecessors(cnode)
            predecessors = Set(cnode)
          case None => // nothing
        case Failure(_) =>
//          println(s"Failure, clearing predecessors")
          predecessors = Set()
      println(s"Exit $dom : $codom  ---  $predecessors")
  }


  def toGraphViz: String = {
    val sb = new StringBuilder()
    nodes.foreach { from =>
      sb ++= s"\t${nodeToGraphViz(from)} [${nodeGraphVizAttributes(from)}];\n"
      edges.getOrElse(from, Set()).foreach { to =>
        val edge = s"\t${nodeToGraphViz(from)} -> ${nodeToGraphViz(to)} [${edgeGraphVizAttributes(from, to)}];\n"
        sb ++= edge
      }
      from.node match
        case cr: CallReturnNode[_] =>
          val callNode = CNode(cr.callNode.asInstanceOf[Node], from.ctx)
          val edge = s"\t${nodeToGraphViz(callNode)} -> ${nodeToGraphViz(from)} [${callReturnEdgeGraphVizAttributes(callNode, from)}];\n"
          sb ++= edge
        case _ => // nothing
    }

    s"""strict digraph {
       |  ${sb.toString()}
       |}
       |""".stripMargin
  }

  protected def nodeToGraphViz(n: CNode): String =
    n.toString.replaceAll("@|\\(|\\)|\\ |,|\\|", "_")
  protected def nodeGraphVizAttributes(from: CNode): String =
    if (from == ROOT)
      s"fillcolor=red, style=filled, fontcolor=black"
    else
      (from.node match
        case _: ImportantControlNode => s"fillcolor=black, style=filled, fontcolor=white"
        case _ => s"fillcolor=white, style=filled, fontcolor=black"
      ) + s", label=\"${from.toString}\""

  protected def edgeGraphVizAttributes(from: CNode, to: CNode): String = "color=black"
  protected def callReturnEdgeGraphVizAttributes(from: CNode, to: CNode): String = "color=black, style=dashed"

/** Marker trait for important control nodes, used during GrpahViz generation */
trait ImportantControlNode
trait CallReturnNode[Node](val callNode: Node)
