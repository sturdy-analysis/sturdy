package sturdy.fix

import sturdy.effect.JoinObserver
import sturdy.effect.ObservableJoin

import collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import ControlFlowGraph.*

import sturdy.data.combineMaps
import sturdy.effect.TrySturdy
import sturdy.effect.except.ExceptObserver
import sturdy.effect.except.LanguageException
import sturdy.effect.except.ObservableExcept
import sturdy.util.Exact

import scala.runtime.BoxesRunTime

def control[Ctx, Dom, Codom, Exc <: LanguageException, Node]
  (contextSensitive: Boolean, startNode: Node & StartNode)
  (getDomNode: Dom => Option[Node])
  (getCodomNode: (Dom, Codom) => Option[Node])
  (using obsJoin: ObservableJoin, obsExcept: ObservableExcept[Exc])
  : ControlLogger[Ctx, Dom, Codom, Exc, Node] =
  new ControlLogger(contextSensitive, startNode, getDomNode, getCodomNode, obsJoin, obsExcept)

object ControlFlowGraph:
  case class CNode[Node, Ctx](node: Node, ctx: Ctx):
    override def toString: String =
      if (ctx == null)
        node.toString
      else
        s"$node | $ctx"

  case class EdgeAttrib(exceptional: Boolean):
    def combine(other: EdgeAttrib): EdgeAttrib = EdgeAttrib(exceptional = this.exceptional || other.exceptional)
  object EdgeAttrib:
    def default: EdgeAttrib = EdgeAttrib(false)

trait ControlFlowGraph[Node, Ctx]:
  def getNodes: List[CNode[Node, Ctx]]
  def getEdges: Map[CNode[Node, Ctx], Map[CNode[Node, Ctx], EdgeAttrib]]
  def getEdgesFlat: List[(CNode[Node, Ctx], CNode[Node, Ctx], EdgeAttrib)] =
    for ((from, tos) <- getEdges.toList; (to, attrib) <- tos) yield (from, to, attrib)
  def getReverseEdges: Map[CNode[Node, Ctx], Map[CNode[Node, Ctx], EdgeAttrib]] =
    val revEdges: mutable.Map[CNode[Node, Ctx], Map[CNode[Node, Ctx], EdgeAttrib]] = mutable.Map()
    for ((from, tos) <- getEdges; (to, attrib) <- tos) revEdges.get(to) match
      case None => revEdges += to -> Map(from -> attrib)
      case Some(map) => revEdges += to -> (map + (from -> attrib))
    revEdges.toMap

  def filterDeadNodes(programNodes: Set[Node]): Set[Node] =
    val liveNodes = getNodes.map(_.node).toSet
    programNodes.removedAll(liveNodes)

  def toGraphViz: String =
    val nodes = getNodes
    val edges = getEdges

    val sb = new StringBuilder()
    nodes.foreach { from =>
      sb ++= s"\t${nodeToGraphViz(from)} [${nodeGraphVizAttributes(from)}];\n"
      edges.getOrElse(from, Map()).foreach { case (to, attrib) =>
        val edge = s"\t${nodeToGraphViz(from)} -> ${nodeToGraphViz(to)} [${edgeGraphVizAttributes(from, to, attrib)}];\n"
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
    if (from.node.isInstanceOf[StartNode])
      s"fillcolor=red, style=filled, fontcolor=black"
    else
      (from.node match
        case _: ImportantControlNode => s"fillcolor=black, style=filled, fontcolor=white"
        case _ => s"fillcolor=white, style=filled, fontcolor=black"
        ) + s", label=\"${from.toString}\""

  protected def edgeGraphVizAttributes(from: CNode[Node, Ctx], to: CNode[Node, Ctx], attrib: EdgeAttrib): String =
    if (attrib.exceptional)
      "color=purple"
    else
      "color=black"
  protected def callReturnEdgeGraphVizAttributes(from: CNode[Node, Ctx], to: CNode[Node, Ctx]): String = "color=black, style=dashed"


class ControlLogger[Ctx, Dom, Codom, Exc <: LanguageException, Node]
  (contextSensitive: Boolean,
   startNode: Node & StartNode,
   getDomNode: Dom => Option[Node],
   getCodomNode: (Dom, Codom) => Option[Node],
   obsJoin: ObservableJoin,
   obsExcept: ObservableExcept[Exc]
  )
  extends JoinObserver, ExceptObserver[Exc], ControlFlowGraph[Node, Ctx]:

  obsJoin.addJoinObserver(this)
  obsExcept.addExceptObserver(this)

  private case class PredNode(cnode: CNode[Node, Ctx], exceptional: Boolean)

  type Predecessors = Map[CNode[Node, Ctx], EdgeAttrib]
  type Exceptions = Map[LanguageException, Predecessors]

  private val startCNode: CNode[Node, Ctx] = CNode(startNode, null.asInstanceOf[Ctx])

  private var predecessors: Predecessors = Map(startCNode -> EdgeAttrib.default)
  private var joinStack: List[Predecessors] = List()

  /** Currently active exceptions and the CFG nodes that triggered them. */
  private var exceptions: Exceptions = Map()
  private var exceptStack: List[Exceptions] = List()
  /** Exceptions currently handled in a catch block. Uncaught exceptions are propagated outwards. */
  private var catchExceptions: Exceptions = Map()

  private val nodes: mutable.Set[CNode[Node, Ctx]] = mutable.Set(startCNode)
  private val edges: mutable.Map[CNode[Node, Ctx], Map[CNode[Node, Ctx], EdgeAttrib]] = mutable.Map()

  override def throwing(exc: Exc): Unit =
    val exceptionalPredecessors = predecessors.map(_.copy(_2 = EdgeAttrib(true)))
    exceptions.get(exc) match
      case None =>
        exceptions += exc -> exceptionalPredecessors
      case Some(preds) =>
        val combinedPreds = combinePredecessors(preds, exceptionalPredecessors)
        exceptions += exc -> combinedPreds
    predecessors = Map()

  override def handling(exc: Exc): Unit =
    predecessors = catchExceptions(exc)
    catchExceptions -= exc

  override def tryStart(): Unit =
    exceptStack = exceptions :: exceptStack
    exceptions = Map()

  override def tryEnd(): Unit =
    val fallthrough = exceptStack.head
    exceptions = combineExceptions(exceptions, fallthrough)
    exceptStack = exceptStack.tail

  override def catchStart(): Unit =
    catchExceptions = exceptions
    exceptions = Map()

  override def catchEnd(): Unit =
    exceptions = combineExceptions(exceptions, catchExceptions)

  override def joinStart(): Unit =
    joinStack = predecessors :: joinStack
//    exceptions = Map()

  override def joinSwitch(): Unit =
    val otherPredecessors = joinStack.head
    joinStack = predecessors :: joinStack.tail
    predecessors = otherPredecessors

  override def joinEnd(): Unit =
    val otherPredecessors = joinStack.head
    joinStack = joinStack.tail
    predecessors = combinePredecessors(predecessors, otherPredecessors)

  private inline def combinePredecessors(preds1: Predecessors, preds2: Predecessors): Predecessors =
    combineMaps(preds1, preds2, _.combine(_))

  private inline def combineExceptions(excs1: Exceptions, excs2: Exceptions): Exceptions =
    combineMaps(excs1, excs2, combinePredecessors(_, _))


  override def repeating(): Unit =
    predecessors = Map()

  def clear(): Unit =
    predecessors = Map(startCNode -> EdgeAttrib.default)
    joinStack = List()
    nodes.clear()
    nodes += startCNode
    edges.clear()

  inline private def addEdgeFromPredecessors(to: CNode[Node, Ctx]): Unit =
    predecessors.foreach((from, attrib) => addEdge(from, to, attrib))

  private def addEdge(from: CNode[Node, Ctx], to: CNode[Node, Ctx], attrib: EdgeAttrib): Unit =
    edges.get(from) match
      case None => edges += from -> Map(to -> attrib)
      case Some(map) => edges += from -> (map + (to -> attrib))

  def getNodes: List[CNode[Node, Ctx]] = nodes.toList.sortBy(_.toString)
  def getEdges: Map[CNode[Node, Ctx], Map[CNode[Node, Ctx], EdgeAttrib]] = edges.toMap

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
          predecessors = Map(cnode -> EdgeAttrib.default)
        case None => // nothing

    override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
      codom match
        case TrySturdy.Success(cod) => getCodomNode(dom, cod) match
          case Some(node) =>
            val cnode = CNode(node, getContext)
            nodes += cnode
            addEdgeFromPredecessors(cnode)
            predecessors = Map(cnode -> EdgeAttrib.default)
          case None => // nothing
        case TrySturdy.Failure(ex) =>
          predecessors = Map()
  }


/** Marker trait for important control nodes, used during GrpahViz generation */
trait StartNode
trait ImportantControlNode
trait EndNode[Node]:
  val startNode: Node
