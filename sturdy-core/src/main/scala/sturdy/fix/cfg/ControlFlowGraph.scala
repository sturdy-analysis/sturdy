package sturdy.fix.cfg

import scala.collection.mutable
import ControlFlowGraph.*
import sturdy.fix.cfg.BlockControlFlowGraph.BlockNode

object ControlFlowGraph:
  trait Node:
    def isStartNode: Boolean
    def isImportantControlNode: Boolean
    def isEndNode: Boolean = getBeginNode.isDefined
    def getBeginNode: Option[Node]

  case class CNode[+N <: Node, +Ctx](node: N, ctx: Ctx):
    def emptyCtx: Boolean = ctx == null || ctx.isInstanceOf[Unit]
    override def toString: String =
      if (emptyCtx)
        node.toString
      else
        s"$node | $ctx"

  case class EdgeAttrib(exceptional: Boolean):
    def combine(other: EdgeAttrib): EdgeAttrib = EdgeAttrib(exceptional = this.exceptional || other.exceptional)
  object EdgeAttrib:
    def default: EdgeAttrib = EdgeAttrib(false)

trait ControlFlowGraph[N <: ControlFlowGraph.Node, Ctx]:
  def getNodes: List[CNode[N, Ctx]]
  def getEdges: Map[CNode[N, Ctx], Map[CNode[N, Ctx], EdgeAttrib]]
  def getEdgesFlat: List[(CNode[N, Ctx], CNode[N, Ctx], EdgeAttrib)] =
    for ((from, tos) <- getEdges.toList; (to, attrib) <- tos) yield (from, to, attrib)
  def getReverseEdges: Map[CNode[N, Ctx], Map[CNode[N, Ctx], EdgeAttrib]] =
    val revEdges: mutable.Map[CNode[N, Ctx], Map[CNode[N, Ctx], EdgeAttrib]] = mutable.Map()
    for ((from, tos) <- getEdges; (to, attrib) <- tos) revEdges.get(to) match
      case None => revEdges += to -> Map(from -> attrib)
      case Some(map) => revEdges += to -> (map + (from -> attrib))
    revEdges.toMap

  def withBlocks(shortLabels: Boolean): ControlFlowGraph[BlockNode[N], Ctx] = new BlockControlFlowGraph(this, shortLabels)

  def filterDeadNodes(programNodes: Set[N]): Set[N] =
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
      from.node.getBeginNode match
        case Some(begin) =>
          val startNode = CNode(begin.asInstanceOf[N], from.ctx)
          val edge = s"\t${nodeToGraphViz(startNode)} -> ${nodeToGraphViz(from)} [${callReturnEdgeGraphVizAttributes(startNode, from)}];\n"
          sb ++= edge
        case _ => // nothing
    }

    s"""strict digraph {
       |  ${sb.toString()}
       |}
       |""".stripMargin

  def nodeToGraphViz(n: CNode[N, Ctx]): String =
    n.toString.replaceAll("[^a-zA-Z0-9]", "_")
  def nodeGraphVizAttributes(from: CNode[N, Ctx]): String =
    if (from.node.isStartNode)
      s"fillcolor=red, style=filled, fontcolor=black"
    else
      (from.node match
        case node if node.isImportantControlNode => s"fillcolor=black, style=filled, fontcolor=white"
        case _ => s"fillcolor=white, style=filled, fontcolor=black"
        ) + s", label=\"${from.toString}\""

  def edgeGraphVizAttributes(from: CNode[N, Ctx], to: CNode[N, Ctx], attrib: EdgeAttrib): String =
    if (attrib.exceptional)
      "color=purple"
    else
      "color=black"

  def callReturnEdgeGraphVizAttributes(from: CNode[N, Ctx], to: CNode[N, Ctx]): String = "color=black, style=dashed"

