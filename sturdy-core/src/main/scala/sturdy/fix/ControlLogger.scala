package sturdy.fix

import sturdy.effect.JoinObserver
import sturdy.effect.ObservableJoin

import collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import ControlFlowGraph.*
import sturdy.effect.TrySturdy
import sturdy.effect.except.ExceptObserver
import sturdy.effect.except.ObservableExcept
import sturdy.util.Exact

def control[Ctx, Dom, Codom, Exc, Node]
  (contextSensitive: Boolean)
  (getDomNode: Dom => Option[Node])
  (getCodomNode: (Dom, Codom) => Option[Node])
  (using obsJoin: ObservableJoin, obsExcept: ObservableExcept[Exc])
  : ControlLogger[Ctx, Dom, Codom, Exc, Node] =
  new ControlLogger(contextSensitive, getDomNode, getCodomNode, obsJoin, obsExcept)

object ControlFlowGraph:
  def startCNode[Node, Ctx] = CNode(null.asInstanceOf[Node], null.asInstanceOf[Ctx])
  case class CNode[Node, Ctx](node: Node, ctx: Ctx, exceptional: Boolean = false):
    def isStartNode = node == null && ctx == null

    override def toString: String =
      if (this.isStartNode)
        "Start"
      else if (ctx == null)
        node.toString
      else
        s"$node | $ctx"

trait ControlFlowGraph[Node, Ctx]:
  def getNodes: List[CNode[Node, Ctx]]
  def getEdges: Map[CNode[Node, Ctx], Set[CNode[Node, Ctx]]]
  def getEdgesFlat: List[(CNode[Node, Ctx], CNode[Node, Ctx])] = getEdges.toList.flatMap((from, tos) => tos.map(to => from -> to)).sortBy(_.toString)

  def filterDeadNodes(programNodes: Set[Node]): Set[Node] =
    programNodes.removedAll(getNodes.map(_.node))

  def toGraphViz: String =
    val nodes = getNodes
    val edges = getEdges

    val sb = new StringBuilder()
    nodes.foreach { from =>
      sb ++= s"\t${nodeToGraphViz(from)} [${nodeGraphVizAttributes(from)}];\n"
      edges.getOrElse(from, Set()).foreach { to =>
        val edge = s"\t${nodeToGraphViz(from)} -> ${nodeToGraphViz(to)} [${edgeGraphVizAttributes(from, to)}];\n"
        sb ++= edge
      }
      from.node match
        case cr: EndNode[_] =>
          val callNode = CNode(cr.startNode.asInstanceOf[Node], from.ctx)
          val edge = s"\t${nodeToGraphViz(callNode)} -> ${nodeToGraphViz(from)} [${callReturnEdgeGraphVizAttributes(callNode, from)}];\n"
          sb ++= edge
        case _ => // nothing
    }

    s"""strict digraph {
       |  ${sb.toString()}
       |}
       |""".stripMargin

  protected def nodeToGraphViz(n: CNode[Node, Ctx]): String =
    n.toString.replaceAll("[^a-zA-Z0-9]", "_")
  protected def nodeGraphVizAttributes(from: CNode[Node, Ctx]): String =
    if (from.isStartNode)
      s"fillcolor=red, style=filled, fontcolor=black"
    else
      (from.node match
        case _: ImportantControlNode => s"fillcolor=black, style=filled, fontcolor=white"
        case _ => s"fillcolor=white, style=filled, fontcolor=black"
        ) + s", label=\"${from.toString}\""
  protected def edgeGraphVizAttributes(from: CNode[Node, Ctx], to: CNode[Node, Ctx]): String =
    if (from.exceptional)
      "color=purple"
    else
      "color=black"
  protected def callReturnEdgeGraphVizAttributes(from: CNode[Node, Ctx], to: CNode[Node, Ctx]): String = "color=black, style=dashed"


class ControlLogger[Ctx, Dom, Codom, Exc, Node]
  (contextSensitive: Boolean,
   getDomNode: Dom => Option[Node],
   getCodomNode: (Dom, Codom) => Option[Node],
   obsJoin: ObservableJoin,
   obsExcept: ObservableExcept[Exc]
  )
  extends JoinObserver, ExceptObserver[Exc], ControlFlowGraph[Node, Ctx]:

  obsJoin.addJoinObserver(this)
  obsExcept.addExceptObserver(this)

  private var predecessors: Set[CNode[Node, Ctx]] = Set(startCNode)
  private var trace: List[Set[CNode[Node, Ctx]]] = List()
  private var exceptions: Map[Exact[Exc], Set[CNode[Node, Ctx]]] = Map()

  override def handled(exc: Exc): Unit = exceptions.get(new Exact(exc)) match
    case Some(preds) => predecessors ++= preds
    case None => // nothing

  override def joinStart(): Unit =
    trace = predecessors :: trace

  override def joinSwitch(): Unit =
    val others = trace.head
    trace = predecessors :: trace.tail
    predecessors = others

  override def joinEnd(): Unit =
    val others = trace.head
    trace = trace.tail
    predecessors = predecessors ++ others

  override def repeating(): Unit =
    predecessors = Set()

  private val nodes: mutable.Set[CNode[Node, Ctx]] = mutable.Set(startCNode)
  private val edges: mutable.Map[CNode[Node, Ctx], Set[CNode[Node, Ctx]]] = mutable.Map()

  def clear(): Unit =
    predecessors = Set(startCNode)
    trace = List()
    nodes.clear()
    nodes += startCNode
    edges.clear()

  inline private def addEdgeFromPredecessors(to: CNode[Node, Ctx]): Unit =
    predecessors.foreach(from => addEdge(from, to))

  private def addEdge(from: CNode[Node, Ctx], to: CNode[Node, Ctx]): Unit =
    edges.get(from) match
      case None => edges += from -> Set(to)
      case Some(set) => edges += from -> (set + to)

  def getNodes: List[CNode[Node, Ctx]] = nodes.toList.sortBy(_.toString)
  def getEdges: Map[CNode[Node, Ctx], Set[CNode[Node, Ctx]]] = edges.toMap

  def logger(using contextual: Contextual[Ctx, Dom, Codom]): Logger[Dom, Codom] = new Logger {
    private def getContext: Ctx =
      if (contextSensitive)
        contextual.getCurrentContext
      else
        null.asInstanceOf[Ctx]

    override def enter(dom: Dom): Unit =
      getDomNode(dom) match
        case Some(node) =>
          val cnode = CNode(node, getContext)
          nodes += cnode
          addEdgeFromPredecessors(cnode)
          predecessors = Set(cnode)
        case None => // nothing

    override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
      codom match
        case TrySturdy.Success(cod) => getCodomNode(dom, cod) match
          case Some(node) =>
            val cnode = CNode(node, getContext)
            nodes += cnode
            addEdgeFromPredecessors(cnode)
            predecessors = Set(cnode)
          case None => // nothing
        case TrySturdy.Failure(ex) if ex.isBottom =>
          predecessors = Set()
        case TrySturdy.Failure(ex) =>
          val exceptionalPredecessors = predecessors.map(_.copy(exceptional = true))
          nodes ++= exceptionalPredecessors
          obsExcept.foreachException(ex) { exc =>
            val key = new Exact(exc)
            if (!exceptions.contains(key))
              exceptions += key -> exceptionalPredecessors
            predecessors = Set()
          }
  }


/** Marker trait for important control nodes, used during GrpahViz generation */
trait ImportantControlNode
trait EndNode[Node]:
  val startNode: Node
