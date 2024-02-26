package sturdy.control

import sturdy.data.combineMaps

class ControlTreeGraphBuilder[Atom, Sec, Exc] {

  private type CT = ControlTree[Atom, Sec, Exc]
  private type CNode = Node[Atom, Sec]
  private type CEdge = Edge[Atom, Sec]

  private var edges: Set[CEdge] = Set.empty

  def build(ct: CT): Set[CEdge] =
    _build(ct, Set(Node.Start()))
    edges

  private def _build(ct: CT, prev: Set[CNode]): (Set[CNode], Map[Exc, Set[CNode]]) = if prev.isEmpty then (Set(), Map()) else ct match
    case ControlTree.Empty() =>
      (prev, Map.empty)

    case ControlTree.Atomic(a) =>
      val current: CNode = Node.Atomic(a)
      addEdges(prev, current)
      (Set(current), Map.empty)

    case ControlTree.Failed() =>
      val current: CNode = Node.Failure()
      addEdges(prev, current)
      (Set(), Map())

    case ControlTree.Section(section, body) =>
      val begin: CNode = Node.BlockStart(section)
      val end: CNode = Node.BlockEnd(section)

      addEdges(prev, begin)
      val (last, excs) = _build(body, Set(begin))
      addEdges(last, end)
      if (!edges.contains(Edge(begin, end, EdgeType.CF)) && last.nonEmpty)
        addEdges(Set(begin), end, EdgeType.BlockPair)
      if (last.nonEmpty)
        (Set(end), excs)
      else
        (Set(), excs)

    case ControlTree.Seq(x, xs) =>
      val (x_last, x_exc) = _build(x, prev)
      val (xs_last, xs_exc) = _build(xs, x_last)
      (xs_last, combineMaps(x_exc, xs_exc, _ ++ _))

    case ControlTree.Fork(b1, b2) =>
      val (b1_last, b1_exc) = _build(b1, prev)
      val (b2_last, b2_exc) = _build(b2, prev)
      (b1_last ++ b2_last, combineMaps(b1_exc, b2_exc, _ ++ _))

    case ControlTree.Try(body, handlers) =>
      val (lastBody, excs) = _build(body, prev)

      excs.flatMap { exc =>
        handlers.map { handler =>
          _buildCatch(handler, exc._2, exc._1)
        }
      }.fold(lastBody, Map.empty) { (a, b) => (a._1 ++ b._1, combineMaps(a._2, b._2, _ ++ _)) }

    case ControlTree.Throw(exc) =>
      (Set(), Map(exc -> prev))

    case ControlTree.Handling(_, _) => throw new Exception("...")

    case ControlTree.Fixpoint(b, repeat) =>
      _build(repeat.getOrElse(b), prev)

    case ControlTree.Recurrent(failing) =>
      (if failing then Set.empty else prev, Map.empty)

  private def _buildCatch(ct: CT, prev: Set[CNode], activeExc: Exc): (Set[CNode], Map[Exc, Set[CNode]]) = ct match
    case ControlTree.Handling(exc, body) =>
      if (exc == activeExc)
        _build(body, prev)
      else
        (Set.empty, Map.empty)
    case ControlTree.Empty() => (Set.empty, Map.empty)
    case _ => throw new Exception("...")

  private def addEdges(prev: Set[CNode], current: CNode): Unit =
    edges ++= prev.map(p => Edge(p, current, EdgeType.CF))

  private def addEdges(prev: Set[CNode], current: CNode, edgeType: EdgeType): Unit =
    edges ++= prev.map(p => Edge(p, current, edgeType))
}

