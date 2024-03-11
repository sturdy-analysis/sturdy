package sturdy.control

import sturdy.data.combineMaps

import scala.language.implicitConversions

class ControlTreeGraphBuilder[Atom, Sec, Exc, Fx] {

  private type CT = ControlTree[Atom, Sec, Exc, Fx]
  private case class CNode(n: Node[Atom, Sec], exc: Boolean)
  private implicit def cnode[A <: Atom,S <: Sec](n: Node[A, S]): CNode = CNode(n, false)
  private type CEdge = Edge[Atom, Sec]
  private case class Result(tails: Set[CNode], xs: List[(Exc, Set[CNode])]):
    def ||(that: Result): Result =
      Result(this.tails ++ that.tails, this.xs ++ that.xs)
  private object Result:
    val empty: Result = Result(Set(), List())

  private var edges: Set[CEdge] = Set.empty
  private var fixpoints: Map[Fx, Result] = Map()

  def build(ct: CT): ControlGraph[Atom, Sec] =
    _build(ct, Set(Node.Start()))
    ControlGraph(edges)

  private def _build(ct: CT, pred: Set[CNode]): Result = ct match
    case ControlTree.Empty() =>
      Result(pred, List())

    case ControlTree.Atomic(a) =>
      val current = Node.Atomic(a)
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
      if (tails.isEmpty)
        Result(Set.empty, xs)
      else {
        addBlockPairEdges(section)
        Result(Set(end), xs)
      }

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
            xpred.map(_.copy(exc = true))
          else
            Set()
        }.toSet
        _build(ht, hpred)
      }
      val result = rs.foldRight(Result(lastBody, List()))(_ || _)
      result

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
    edges ++= prev.map(p =>
      Edge(p.n, current.n, if (p.exc) EdgeType.Exceptional else EdgeType.CF)
    )

  private def addBlockPairEdges(sec: Sec): Unit =
    if (!edges.contains(Edge(Node.BlockStart(sec), Node.BlockEnd(sec), EdgeType.CF)))
      edges += Edge(Node.BlockStart(sec), Node.BlockEnd(sec), EdgeType.BlockPair)
}

