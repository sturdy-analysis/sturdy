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
      if (!edges.contains(Edge(begin, end, EdgeType.CF)) && edges.exists(_.to == end))
        addEdges(Set(begin), end, EdgeType.BlockPair)
      if (edges.exists(_.to == end))
        (Set(end), excs)
      else
        (Set(), Map())

    case ControlTree.Seq(body) =>
      body.foldLeft((prev, Map[Exc, Set[CNode]]())) { (a, b) =>
        val (last, exc) = _build(b, a._1)
        (last, combineMaps(a._2, exc, _ ++ _))
      }

    case ControlTree.Fork(branches) =>
      branches.map(_build(_, prev)).fold(Set.empty, Map.empty)((a, b) => (a._1 ++ b._1, combineMaps(a._2, b._2, _ ++ _)))

    case ControlTree.Try(body, handlers) =>
      val (last, excBody) = _build(body, prev)


      val (lastHandlers, excEsc) = if (handlers.isEmpty)
        (last, excBody)
      else
        handlers.map(_buildCatch(_, Set.empty, excBody)).fold(Set.empty, Map.empty)((a, b) => (a._1 ++ b._1, combineMaps(a._2, b._2, _ ++ _)))

      (last ++ lastHandlers, excEsc)

    case ControlTree.Throw(exc) =>
      (Set(), Map(exc -> prev))

    case ControlTree.Handling(exc, body) => throw new Exception("...")

    case ControlTree.Fixpoint(b, repeat) =>
      _build(repeat.getOrElse(b), prev)

    case ControlTree.Recurrent(failing) =>
      (if failing then Set.empty else prev, Map.empty)

  private def _buildCatch(ct: CT, prev: Set[CNode], activeExc: Map[Exc, Set[CNode]]): (Set[CNode], Map[Exc, Set[CNode]]) = ct match
    case ControlTree.Fork(branches) => branches.map {
      case ControlTree.Empty() =>
        (prev, activeExc)
      case ControlTree.Handling(exc, body) => activeExc.get(exc) match
        case Some(prevExc) =>
          _build(body, prevExc)
        case None =>
          (Set.empty, Map.empty)
      case _ => throw new Exception("...")
    }.fold(Set.empty, Map.empty)((a, b) => (a._1 ++ b._1, combineMaps(a._2, b._2, _ ++ _)))

    case ControlTree.Handling(exc, body) => activeExc.get(exc) match
      case Some(prevExc) =>
        _build(body, prevExc)
      case None =>
        (Set.empty, Map.empty)
    case _ => throw new Exception("...")

  private def addEdges(prev: Set[CNode], current: CNode): Unit =
    edges ++= prev.map(p => Edge(p, current, EdgeType.CF))

  private def addEdges(prev: Set[CNode], current: CNode, edgeType: EdgeType): Unit =
    edges ++= prev.map(p => Edge(p, current, edgeType))
}

