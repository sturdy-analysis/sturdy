package sturdy.fix.cfg

import sturdy.fix.cfg.BlockControlFlowGraph.BlockNode
import sturdy.fix.cfg.ControlFlowGraph.*
import sturdy.fix.cfg.BlockControlFlowGraph.*
import sturdy.fix.cfg.BlockControlFlowGraph.BlockNode.*

import scala.collection.mutable.ListBuffer

object BlockControlFlowGraph:
  enum BlockNode[N <: ControlFlowGraph.Node] extends ControlFlowGraph.Node:
    case Block(id: Int, nodes: Vector[N])(val blockNodes: N => Option[BlockNode[N]])
    case Simple(node: N)(val blockNodes: N => Option[BlockNode[N]])

    override def isStartNode: Boolean = this match
      case Simple(n) => n.isStartNode
      case _ => false
    override def isImportantControlNode: Boolean = this match
      case Simple(n) => n.isImportantControlNode
      case _ => false
    override def getBeginNode: Option[BlockNode[N]] = this match
      case bl@Block(_, nodes) =>
        for (b <- nodes.head.getBeginNode)
          yield bl.blockNodes(b.asInstanceOf[N]) match
            case Some(block) => block
            case None => Simple(b.asInstanceOf[N])(_ => None)
      case simp@Simple(node) =>
        for (b <- node.getBeginNode)
          yield simp.blockNodes(b.asInstanceOf[N]) match
            case Some(block) => block
            case None => Simple(b.asInstanceOf[N])(_ => None)

    override def toString: String = this match
      case Block(id, _) => id.toString
      case Simple(node) => node.toString


class BlockControlFlowGraph[N <: ControlFlowGraph.Node, Ctx](g: ControlFlowGraph[N, Ctx], shortLabels: Boolean) extends ControlFlowGraph[BlockNode[N], Ctx]:
  override val (getNodes, getEdges) = {
    val edges = g.getEdges
    val revEdges = g.getReverseEdges

    def blockCandidate(cnode: CNode[N, Ctx], atStart: Boolean, atEnd: Boolean): Boolean = cnode.node match
      case node if node.isStartNode || node.isImportantControlNode => false
      case node =>
        if (!atStart) {
          val preds = revEdges(cnode)
          if (node.isEndNode || preds.size != 1 || preds.head._1.ctx != cnode.ctx)
            return false
        }
        if (!atEnd) {
          val nexts = edges.getOrElse(cnode, Map())
          if (nexts.size != 1 || !blockCandidate(nexts.head._1, atStart = false, atEnd = true))
            return false
        }
        true

    val startNode = g.getNodes.find(_.node.isStartNode).get
    val myNodes: ListBuffer[CNode[BlockNode[N], Ctx]] = ListBuffer(CNode(Simple(startNode.node)(_ => None), startNode.ctx))
    val myEdges: ListBuffer[(CNode[BlockNode[N], Ctx], CNode[BlockNode[N], Ctx], EdgeAttrib)] = ListBuffer()

    var openBlocks: Map[CNode[N, Ctx], ListBuffer[N]] = Map()
    var closedBlocks: Map[CNode[N, Ctx], BlockNode[N]] = Map()
    def getPredecessors(node: N, ctx: Ctx): Iterable[(CNode[BlockNode[N], Ctx], EdgeAttrib)] =
      for ((pred, attrib) <- revEdges(CNode(node, ctx)))
        yield CNode(closedBlocks.getOrElse(pred, mkSimple(pred.node, pred.ctx)), pred.ctx) -> attrib
    def addEdges(oldTo: N, ctx: Ctx, to: CNode[BlockNode[N], Ctx]): Unit =
      getPredecessors(oldTo, ctx).foreach { case (from, attrib) => myEdges += ((from, to, attrib)) }
    def mkBlockNodes(ctx: Ctx)(n: N): Option[BlockNode[N]] = closedBlocks.get(CNode(n, ctx))
    def mkSimple(node: N, ctx: Ctx): Simple[N] =
      Simple(node)(mkBlockNodes(ctx))

    var blockCount = 0
    def closeBlock(block: ListBuffer[N], ctx: Ctx): Unit =
      val blockNode = CNode(Block(blockCount, block.toVector)(mkBlockNodes(ctx)), ctx)
      blockCount += 1
      myNodes += blockNode
      addEdges(block.head, ctx, blockNode)
      closedBlocks += CNode(block.head, ctx) -> blockNode.node
      closedBlocks += CNode(block.last, ctx) -> blockNode.node

    val queue: ListBuffer[CNode[N, Ctx]] = ListBuffer() ++ edges(startNode).keys
    var visited = Set[CNode[N, Ctx]](startNode)

    while (queue.nonEmpty) {
      val current = queue.head
      queue.remove(0)

      val preds = revEdges.getOrElse(current, Map())
      val nexts = edges.getOrElse(current, Map())
      val openBlock = openBlocks.get(preds.head._1)


      if (visited.contains(current)) {
        assert(openBlock.isEmpty)
        val node = CNode(closedBlocks.getOrElse(current, mkSimple(current.node, current.ctx)), current.ctx)
        addEdges(current.node, current.ctx, node)
      } /*else if (current.node.isEndNode && !visited.contains(CNode(current.node.getBeginNode.get.asInstanceOf[N], current.ctx))) {
        // delay processing of end node until begin node has been processed
        queue.append(current)
      }*/ else {
        visited += current
        queue.prependAll(nexts.keys)

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
          val simpleNode = CNode(mkSimple(current.node, current.ctx), current.ctx)
          myNodes += simpleNode
          addEdges(current.node, current.ctx, simpleNode)
        }
      }
    }


    val blockNodes: List[CNode[BlockNode[N], Ctx]] =
      myNodes.toList
    val blockEdges: Map[CNode[BlockNode[N], Ctx], Map[CNode[BlockNode[N], Ctx], EdgeAttrib]] =
      myEdges.groupMap(_._1)(e => (e._2, e._3)).view.mapValues(_.toMap).toMap
    (blockNodes, blockEdges)
  }

  override def nodeToGraphViz(n: CNode[BlockNode[N], Ctx]): String = n.node match
    case Block(id, _) => s"Block_$id"
    case _ => super.nodeToGraphViz(n)

  override def nodeGraphVizAttributes(from: CNode[BlockNode[N], Ctx]): String = from.node match
    case Block(id, block) =>
      val label = shortLabels match
        case false if from.emptyCtx => block.mkString("\n")
        case false => s"${from.ctx}:\n${block.mkString("\n")}"
        case true =>
          val first = block.head.toString
          val last = block.last.toString
          val l = s"$first\n...${block.size - 2} more instructions...\n$last"
          if (from.ctx == null)
            l
          else
            s"${from.ctx}:\n" + l
      s"shape=box, fillcolor=white, style=filled, fontcolor=black, label=\"$label\""
    case Simple(node) =>
      if (node.isStartNode)
        s"fillcolor=red, style=filled, fontcolor=black, label=\"${from.toString}\""
      else if (node.isImportantControlNode)
        s"fillcolor=black, style=filled, fontcolor=white, label=\"${from.toString}\""
      else
        s"shape=box, fillcolor=white, style=filled, fontcolor=black, label=\"${from.toString}\""

  override def edgeGraphVizAttributes(from: CNode[BlockNode[N], Ctx], to: CNode[BlockNode[N], Ctx], attrib: EdgeAttrib): String =
    var s = ", "
    from.node match
      case Block(_, _) =>
//        s += "tailport=s, "
      case _ =>
    to.node match
      case Block(_, _) =>
//        s += "headport=n, "
      case _ =>
    s += super.edgeGraphVizAttributes(from, to, attrib)
    s.substring(2)