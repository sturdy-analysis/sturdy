package sturdy.fix.cfg

import sturdy.fix.cfg.BlockControlFlowGraph.BlockNode
import sturdy.fix.cfg.ControlFlowGraph.*
import sturdy.fix.cfg.BlockControlFlowGraph.*
import sturdy.fix.cfg.BlockControlFlowGraph.BlockNode.*

import scala.collection.mutable.ListBuffer

object BlockControlFlowGraph:
  enum BlockNode[Node]:
    case Block(id: Int, nodes: Vector[Node])
    case Simple(node: Node)

    override def toString: String = this match
      case Block(id, _) => id.toString
      case Simple(node) => node.toString


class BlockControlFlowGraph[Node, Ctx](g: ControlFlowGraph[Node, Ctx], shortLabels: Boolean) extends ControlFlowGraph[BlockNode[Node], Ctx]:
  override val (getNodes, getEdges) = {
    val edges = g.getEdges
    val revEdges = g.getReverseEdges

    def blockCandidate(node: CNode[Node, Ctx], atStart: Boolean, atEnd: Boolean): Boolean = node.node match
      case _: (EndNode[_] | ImportantControlNode) => false
      case _ =>
        if (!atStart) {
          val preds = revEdges(node)
          if (preds.size != 1 || preds.head._1.ctx != node.ctx)
            return false
        }
        if (!atEnd) {
          val nexts = edges.getOrElse(node, Map())
          if (nexts.size != 1 || !blockCandidate(nexts.head._1, atStart = false, atEnd = true))
            return false
        }
        true

    val startNode = g.getNodes.find(_.node.isInstanceOf[StartNode]).get
    val myNodes: ListBuffer[CNode[BlockNode[Node], Ctx]] = ListBuffer(CNode(Simple(startNode.node), startNode.ctx))
    val myEdges: ListBuffer[(CNode[BlockNode[Node], Ctx], CNode[BlockNode[Node], Ctx], EdgeAttrib)] = ListBuffer()

    var openBlocks: Map[CNode[Node, Ctx], ListBuffer[Node]] = Map()
    var closedBlocks: Map[CNode[Node, Ctx], BlockNode[Node]] = Map()
    def getPredecessors(node: Node, ctx: Ctx): Iterable[(CNode[BlockNode[Node], Ctx], EdgeAttrib)] =
      for ((pred, attrib) <- revEdges(CNode(node, ctx)))
        yield CNode(closedBlocks.getOrElse(pred, Simple(pred.node)), pred.ctx) -> attrib
    def addEdges(oldTo: Node, ctx: Ctx, to: CNode[BlockNode[Node], Ctx]): Unit =
      getPredecessors(oldTo, ctx).foreach { case (from, attrib) => myEdges += ((from, to, attrib)) }

    var blockCount = 0
    def closeBlock(block: ListBuffer[Node], ctx: Ctx): Unit =
      val blockNode = CNode(Block(blockCount, block.toVector), ctx)
      blockCount += 1
      myNodes += blockNode
      addEdges(block.head, ctx, blockNode)
      closedBlocks += CNode(block.head, ctx) -> blockNode.node
      closedBlocks += CNode(block.last, ctx) -> blockNode.node

    var queue = edges(startNode).keys.toList
    var visited = Set[CNode[Node, Ctx]](startNode)

    while (queue.nonEmpty) {
      val current = queue.head
      queue = queue.tail

      val preds = revEdges.getOrElse(current, Map())
      val nexts = edges.getOrElse(current, Map())
      val openBlock = openBlocks.get(preds.head._1)


      if (visited.contains(current)) {
        assert(openBlock.isEmpty)
        val node = CNode(closedBlocks.getOrElse(current, Simple(current.node)), current.ctx)
        addEdges(current.node, current.ctx, node)
      } else {
        visited += current
        queue ++= nexts.keys

        if (openBlock.isDefined && blockCandidate(current, atStart = false, atEnd = true)) {
          // we have a single predecessor with the same context and the predecessor is part of an open block
          //   => add current to open block
          val block = openBlock.get
          if (blockCandidate(current, atStart = false, atEnd = false)) {
            // current may appear in the middle of block
            block += current.node
            openBlocks += current -> block
            openBlocks -= preds.head._1
          } else {
            // current may only appear at the end of block
            block += current.node
            openBlocks -= preds.head._1
            closeBlock(block, current.ctx)
          }
        } else if (blockCandidate(current, atStart = true, atEnd = false)) {
          // current may appear at start of a block
          assert(openBlock.isEmpty)
          openBlocks += current -> ListBuffer(current.node)
        } else {
          openBlock.foreach(closeBlock(_, preds.head._1.ctx))

          // simple node
          val simpleNode = CNode(Simple(current.node), current.ctx)
          myNodes += simpleNode
          addEdges(current.node, current.ctx, simpleNode)
        }
      }
    }


    val blockNodes: List[CNode[BlockNode[Node], Ctx]] =
      myNodes.toList
    val blockEdges: Map[CNode[BlockNode[Node], Ctx], Map[CNode[BlockNode[Node], Ctx], EdgeAttrib]] =
      myEdges.groupMap(_._1)(e => (e._2, e._3)).view.mapValues(_.toMap).toMap
    (blockNodes, blockEdges)
  }

  override def nodeToGraphViz(n: CNode[BlockNode[Node], Ctx]): String = n.node match
    case Block(id, _) => s"Block_$id"
    case Simple(node) => g.nodeToGraphViz(CNode(node, n.ctx))

  override def nodeGraphVizAttributes(from: CNode[BlockNode[Node], Ctx]): String = from.node match
    case Block(id, block) =>
      val label = shortLabels match
        case false if from.ctx == null => block.mkString("\n")
        case false if from.ctx != null => s"${from.ctx}:\n${block.mkString("\n")}"
        case true =>
          val first = block.head.toString
          val last = block.last.toString
          val l = s"$first\n...${block.size - 2} more instructions...\n$last"
          if (from.ctx == null)
            l
          else
            s"${from.ctx}:\n" + l
      s"shape=box, fillcolor=white, style=filled, fontcolor=black, label=\"$label\""
    case Simple(node) => g.nodeGraphVizAttributes(CNode(node, from.ctx))

  override def edgeGraphVizAttributes(from: CNode[BlockNode[Node], Ctx], to: CNode[BlockNode[Node], Ctx], attrib: EdgeAttrib): String =
    var s = ", "
    val gfrom: Node = from.node match
      case Block(_, block) =>
//        s += "tailport=s, "
        block.last
      case Simple(node) => node
    val gto: Node = to.node match
      case Block(_, block) =>
//        s += "headport=n, "
        block.head
      case Simple(node) => node
    s += g.edgeGraphVizAttributes(CNode(gfrom, from.ctx), CNode(gto, to.ctx), attrib)
    s.substring(2)