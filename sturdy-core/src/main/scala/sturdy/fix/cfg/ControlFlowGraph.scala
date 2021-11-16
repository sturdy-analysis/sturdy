package sturdy.fix.cfg

import scala.collection.mutable
import ControlFlowGraph.*
import sturdy.fix.cfg.BlockControlFlowGraph.BlockNode

/** Marker trait for important control nodes, used during GrpahViz generation */
trait StartNode
trait ImportantControlNode
trait EndNode[Node]:
  val startNode: Node

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

  def withBlocks(shortLabels: Boolean): ControlFlowGraph[BlockNode[Node], Ctx] = new BlockControlFlowGraph(this, shortLabels)

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
          val startNode = CNode(cr.startNode.asInstanceOf[Node], from.ctx)
          val edge = s"\t${nodeToGraphViz(startNode)} -> ${nodeToGraphViz(from)} [${callReturnEdgeGraphVizAttributes(startNode, from)}];\n"
          sb ++= edge
        case _ => // nothing
    }

    s"""strict digraph {
       |  ${sb.toString()}
       |}
       |""".stripMargin

  def nodeToGraphViz(n: CNode[Node, Ctx]): String =
    n.toString.replaceAll("[^a-zA-Z0-9]", "_")
  def nodeGraphVizAttributes(from: CNode[Node, Ctx]): String =
    if (from.node.isInstanceOf[StartNode])
      s"fillcolor=red, style=filled, fontcolor=black"
    else
      (from.node match
        case _: ImportantControlNode => s"fillcolor=black, style=filled, fontcolor=white"
        case _ => s"fillcolor=white, style=filled, fontcolor=black"
        ) + s", label=\"${from.toString}\""

  def edgeGraphVizAttributes(from: CNode[Node, Ctx], to: CNode[Node, Ctx], attrib: EdgeAttrib): String =
    if (attrib.exceptional)
      "color=purple"
    else
      "color=black"

  def callReturnEdgeGraphVizAttributes(from: CNode[Node, Ctx], to: CNode[Node, Ctx]): String = "color=black, style=dashed"

