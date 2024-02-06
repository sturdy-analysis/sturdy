package sturdy.control

import Node.*

class ControlTreeGraphBuilder[Atom, Sec] {

  private type CT = ControlTree[Atom, Sec]
  private type CNode = Node[Atom, Sec]
  private type CEdge = Edge[Atom, Sec]

  def rec(ct: CT) : List[CEdge] = rec(ct, List.empty)._2

  private def rec(ct: CT, prev: List[CNode] ) : (List[CNode], List[CEdge]) = ct match
    case ControlTree.Empty() =>
      (prev, List.empty)
    case ControlTree.Atomic(a) =>
      val node : CNode = Node.Atomic(a)
      (List(node), addEdges(prev, node))
    case ControlTree.Seq(x, xs) =>
      val (prevAfter1, edges1) = rec(x, prev)
      val (prevAfter2, edges2) = rec(xs, prevAfter1)
      (prevAfter2, edges1 ++ edges2)
    case ControlTree.Section(section, body) =>
      val nodeStart : CNode = Node.BlockStart(section)
      val nodeEnd : CNode = Node.BlockEnd(section)
      val edgesToStart = addEdges(prev, nodeStart)
      val (prevEndBlock, edgesInBlock) = rec(body, List(nodeStart))
      val edgesToEnd = addEdges(prevEndBlock, nodeEnd)
      (List(nodeEnd), edgesToStart ++ edgesInBlock ++ edgesToEnd)
    case ControlTree.Fork(b1, b2) =>
      val (lastBlock1, edgesBlock1) = rec(b1, prev)
      val (lastBlock2, edgesBlock2) = rec(b2, prev)
      (lastBlock1 ++ lastBlock2, edgesBlock1 ++ edgesBlock2)
    case ControlTree.Failed() =>
      val current : CNode = Node.Failure(prev.hashCode().toString)
      (List.empty, addEdges(prev, current))

  private inline def addEdges(prev: List[CNode], current: CNode) : List[CEdge] =
    prev.map(p => Edge(p, current, EdgeType.CF))

}

