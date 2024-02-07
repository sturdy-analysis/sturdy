package sturdy.control

import sturdy.data.combineMaps

class ControlTreeGraphBuilder[Atom, Sec, Exc] {

  private type CT = ControlTree[Atom, Sec, Exc]
  private type CNode = Node[Atom, Sec]
  private type CEdge = Edge[Atom, Sec]

  private var edges : Set[CEdge] = Set.empty

  def build(ct: CT) : Set[CEdge] =
    rec(ct, Set.empty)
    edges

  private def rec(ct: CT, prev: Set[CNode]) : (Set[CNode], Map[Exc, Set[CNode]], Boolean) = ct match
    case ControlTree.Empty() =>
      (prev, Map.empty, false)

    case ControlTree.Atomic(a) =>
      val node : CNode = Node.Atomic(a)
      addEdges(prev, node)
      (Set(node), Map.empty, false)

    case ControlTree.Seq(x, xs) =>
      val (prevAfter1, exc1, failing1) = rec(x, prev)
      val (prevAfter2, exc2, failing2) = rec(xs, prevAfter1)
      (prevAfter2, combineMaps(exc1, exc2, _++_), failing1 || failing2)

    case ControlTree.Section(section, body) =>
      val nodeStart : CNode = Node.BlockStart(section)
      val nodeEnd : CNode = Node.BlockEnd(section)
      val (prevEndBlock, excBlock, failing) = rec(body, Set(nodeStart))
      addEdges(prev, nodeStart)
      if(!failing)
        addEdges(prevEndBlock, nodeEnd)
      if(!edges.contains(Edge(nodeStart, nodeEnd, EdgeType.CF)) && edges.exists(e => e.to == nodeEnd))
        edges += Edge(nodeStart, nodeEnd, EdgeType.BlockPair)
      (Set(nodeEnd), excBlock, failing)

    case ControlTree.Fork(b1, b2) =>
      val (lastBlock1, exc1, failing1) = rec(b1, prev)
      val (lastBlock2, exc2, failing2) = rec(b2, prev)
      (lastBlock1 ++ lastBlock2, combineMaps(exc1, exc2, _++_), failing1 && failing2)

    case ControlTree.Failed() =>
      val current : CNode = Node.Failure()
      addEdges(prev, current)
      (Set.empty, Map.empty, true)

    case ControlTree.Throw(exc) =>
      (Set.empty, Map(exc -> prev), false)

    case ControlTree.Try(body, handlers) =>
      val (endTry, excMap, failingBody) = rec(body, prev)

      val (lastHandlers, excHandlers, failing) = excMap.map((exc, throws) => handlers.get(exc) match
        case Some(handle) => rec(handle, throws)
        case None => (List.empty, Map(exc -> throws), true)
      ).unzip3
      (lastHandlers.flatten.toSet ++ endTry, excHandlers.fold(Map.empty)(combineMaps(_, _, _++_)), failing.reduce(_||_))

    case ControlTree.Fixpoint(b, rep) =>
      val (endBody, excBody) = rec(b, prev)
      rep match
        case None => (endBody, excBody)
        case Some(next) => rec(next, prev)

  private inline def addEdges(prev: Set[CNode], current: CNode) : Set[CEdge] =
    prev.map(p => Edge(p, current, EdgeType.CF))

}

