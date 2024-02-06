package sturdy.control

import sturdy.data.combineMaps

class ControlTreeGraphBuilder[Atom, Sec, Exc] {

  private type CT = ControlTree[Atom, Sec, Exc]
  private type CNode = Node[Atom, Sec]
  private type CEdge = Edge[Atom, Sec]

  def rec(ct: CT) : List[CEdge] = rec(ct, List.empty)._2

  private def rec(ct: CT, prev: List[CNode]) : (List[CNode], List[CEdge], Map[Exc, Set[CNode]]) = ct match
    case ControlTree.Empty() =>
      (prev, List.empty, Map.empty)

    case ControlTree.Atomic(a) =>
      val node : CNode = Node.Atomic(a)
      (List(node), addEdges(prev, node), Map.empty)

    case ControlTree.Seq(x, xs) =>
      val (prevAfter1, edges1, exc1) = rec(x, prev)
      val (prevAfter2, edges2, exc2) = rec(xs, prevAfter1)
      (prevAfter2, edges1 ++ edges2, combineMaps(exc1, exc2, _++_))

    case ControlTree.Section(section, body) =>
      val nodeStart : CNode = Node.BlockStart(section)
      val nodeEnd : CNode = Node.BlockEnd(section)
      val edgesToStart = addEdges(prev, nodeStart)
      val (prevEndBlock, edgesInBlock, excBlock) = rec(body, List(nodeStart))
      val edgesToEnd = addEdges(prevEndBlock, nodeEnd)
      (List(nodeEnd), edgesToStart ++ edgesInBlock ++ edgesToEnd, excBlock)

    case ControlTree.Fork(b1, b2) =>
      val (lastBlock1, edgesBlock1, exc1) = rec(b1, prev)
      val (lastBlock2, edgesBlock2, exc2) = rec(b2, prev)
      (lastBlock1 ++ lastBlock2, edgesBlock1 ++ edgesBlock2, combineMaps(exc1, exc2, _++_))

    case ControlTree.Failed() =>
      val current : CNode = Node.Failure()
      (List.empty, addEdges(prev, current), Map.empty)

    case ControlTree.Throw(exc) =>
      (List.empty, List.empty, Map(exc -> prev.toSet))

    case ControlTree.Try(body, handlers) =>
      val (endTry, blockEdges, excMap) = rec(body, prev)
      
      val (lastHandlers, edgesHandlers, excHandlers) = excMap.map((exc, throws) => handlers.get(exc) match
        case Some(handle) => rec(handle, throws.toList)
        case None => (List.empty, List.empty, Map(exc -> throws))
      ).unzip3
      (lastHandlers.flatten.toList ++ endTry, edgesHandlers.flatten.toList ++ blockEdges, excHandlers.fold(Map.empty)(combineMaps(_, _, _++_)))


  private inline def addEdges(prev: List[CNode], current: CNode) : List[CEdge] =
    prev.map(p => Edge(p, current, EdgeType.CF))

}

