package sturdy.control

import sturdy.data.combineMaps

class ControlTreeGraphBuilder[Atom, Sec, Exc] {

  private type CT = ControlTree[Atom, Sec, Exc]
  private type CNode = Node[Atom, Sec]
  private type CEdge = Edge[Atom, Sec]

  private var edges: Set[CEdge] = Set.empty

  def build(ct: CT): Set[CEdge] =
    rec(ct, Set.empty)
    edges

  private def rec(ct: CT, prev: Set[CNode]): (Set[CNode], Map[Exc, Set[CNode]]) = ct match
    case ControlTree.Empty() =>
      (prev, Map.empty)

    case ControlTree.Atomic(a) =>
      val node: CNode = Node.Atomic(a)
      addEdges(prev, node)
      (Set(node), Map.empty)

    case ControlTree.Seq(x, xs) =>
      val (prevAfter1, exc1) = rec(x, prev)
      assert(prevAfter1.nonEmpty)
      val (prevAfter2, exc2) = rec(xs, prevAfter1)
      (prevAfter2, combineMaps(exc1, exc2, _ ++ _))

    case ControlTree.Section(section, body) =>
      val nodeStart: CNode = Node.BlockStart(section)
      val nodeEnd: CNode = Node.BlockEnd(section)
      val (prevEndBlock, excBlock) = rec(body, Set(nodeStart))
      addEdges(prev, nodeStart)
      if (prevEndBlock.nonEmpty) {
        addEdges(prevEndBlock, nodeEnd)
        if (!edges.contains(Edge(nodeStart, nodeEnd, EdgeType.CF)) && edges.exists(e => e.to == nodeEnd))
          edges += Edge(nodeStart, nodeEnd, EdgeType.BlockPair)
        (Set(nodeEnd), excBlock)
      } else {
        (Set.empty, excBlock)
      }

    case ControlTree.Fork(b1, b2) =>
      val (lastBlock1, exc1) = rec(b1, prev)
      val (lastBlock2, exc2) = rec(b2, prev)
      (lastBlock1 ++ lastBlock2, combineMaps(exc1, exc2, _ ++ _))

    case ControlTree.Failed() =>
      val current: CNode = Node.Failure()
      addEdges(prev, current)
      (Set.empty, Map.empty)

    case ControlTree.Throw(exc) =>
      (Set.empty, Map(exc -> prev))

    case ControlTree.Try(body, handlers) =>
      val (endTry, throwers) = rec(body, prev)

      val (lastHandlers, excHandlers) = throwers.map((exc, thrower) => handlers.get(exc) match
        case Some(handle) => rec(handle, thrower)
        case None => (Set.empty, Map(exc -> thrower))
      ).unzip
      (lastHandlers.flatten.toSet ++ endTry, excHandlers.fold(Map.empty)(combineMaps(_, _, _ ++ _)))

    case ControlTree.Fixpoint(b, rep) =>
      val (endBody, excBody) = rec(b, prev)
      rep match
        case None => (endBody, excBody)
        case Some(next) => rec(next, prev)

    case ControlTree.Recurrent(_) =>
      (Set.empty, Map.empty)

  private inline def addEdges(prev: Set[CNode], current: CNode): Unit =
    edges ++= prev.map(p => Edge(p, current, EdgeType.CF))

}

