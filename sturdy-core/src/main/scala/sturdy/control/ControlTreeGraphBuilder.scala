package sturdy.control

import sturdy.data.combineMaps

class ControlTreeGraphBuilder[Atom, Sec, Exc, Fx] {

  private type CT = ControlTree[Atom, Sec, Exc, Fx]
  private type CNode = Node[Atom, Sec]
  private type CEdge = Edge[Atom, Sec]
  private case class Result(tails: Set[CNode], xs: List[(Exc, Set[CNode])]):
    def ||(that: Result): Result =
      Result(this.tails ++ that.tails, this.xs ++ that.xs)
  private object Result:
    val empty: Result = Result(Set(), List())

  private var edges: Set[CEdge] = Set.empty
  private var fixpoints: Map[Fx, Result] = Map()

  def build(ct: CT): Set[CEdge] =
    _build(ct, Set(Node.Start()))
    edges

  private def _build(ct: CT, pred: Set[CNode]): Result = ct match
    case ControlTree.Empty() =>
      Result(pred, List())

    case ControlTree.Atomic(a) =>
      val current: CNode = Node.Atomic(a)
      addEdges(pred, current)
      Result(Set(current), List())

    case ControlTree.Failed() =>
      addEdges(pred, Node.Failure())
      Result.empty

    case ControlTree.Section(section, body) =>
      val begin: CNode = Node.BlockStart(section)
      val end: CNode = Node.BlockEnd(section)

      val Result(tails, xs) = _build(body, Set(begin))
      addEdges(pred, begin)
      addEdges(tails, end)
      if (!edges.contains(Edge(begin, end, EdgeType.CF)) && tails.nonEmpty)
        addEdges(Set(begin), end, EdgeType.BlockPair)
      Result(Set(end), xs)

    case ControlTree.Seq(t1, t2) =>
      val Result(tails1, xs1) = _build(t1, pred)
      val Result(tails2, xs2) = _build(t2, tails1)
      Result(tails2, xs1 ++ xs2)

    case ControlTree.Fork(b1, b2) =>
      val Result(tails1, xs1) = _build(b1, pred)
      val Result(tails2, xs2) = _build(b2, pred)
      Result(tails1 ++ tails2, xs1 ++ xs2)

    case ControlTree.Try(body, handlers) =>
      val Result(lastBody, excs) = _build(body, pred)

      val rs = for ((hx, ht) <- handlers) yield {
        val hpred = excs.flatMap { case (x, xpred) =>
          if (x == hx)
            xpred
          else
            Set()
        }.toSet
        _build(ht, hpred)
      }
      rs.foldRight(Result.empty)(_||_)

    case ControlTree.Throw(exc) =>
      Result(Set(), List(exc -> pred))

    case ControlTree.Fix(fx, b) =>
      val r = _build(b, pred)
      // register result of `b`
      fixpoints += fx -> (fixpoints.getOrElse(fx, Result.empty) || r)
      r

    case ControlTree.Recurrent(fx) =>
      fixpoints.getOrElse(fx, Result.empty)

    case ControlTree.Restart() =>
      // restart in t without predecessors
      Result.empty

  private def addEdges(prev: Set[CNode], current: CNode): Unit =
    edges ++= prev.map(p => Edge(p, current, EdgeType.CF))

  private def addEdges(prev: Set[CNode], current: CNode, edgeType: EdgeType): Unit =
    edges ++= prev.map(p => Edge(p, current, edgeType))
}

