package sturdy.language.wasm.control

import sturdy.control.{ControlTree, Edge, Node}
import sturdy.data.combineMaps
import sturdy.language.wasm.abstractions.Control
import sturdy.language.wasm.generic.FuncId

case class CallEdge(from: FuncId, to: FuncId)

object CallGraph {

  def toGraphViz(edges: Set[CallEdge]): String =
    edges.toList.map(e => s"\"${e.from.funcIx}\" -> \"${e.to.funcIx}\"").sorted.fold("")((s1, s2) => s1 + "\n" + s2)

  def buildFromControlTree(tree: ControlTree[_, Control.Section, Control.Exc, Control.Fx]): Set[CallEdge] = _rec(tree, None)._1

  private def _rec(tree: ControlTree[_, Control.Section, Control.Exc, Control.Fx], funcId: Option[FuncId]): (Set[CallEdge], Map[Control.Exc, Set[FuncId]]) = tree match

    case ControlTree.Section(f: FuncId, body) =>
      val (edges, exc) = _rec(body, Some(f))
      if (funcId.isDefined)
        (edges + CallEdge(from = funcId.get, to = f), exc)
      else
        (edges, exc)

    case ControlTree.Empty() => (Set.empty, Map.empty)
    case ControlTree.Atomic(a) => (Set.empty, Map.empty)
    case ControlTree.Failed() => (Set.empty, Map.empty)
    case ControlTree.Recurrent(failing) => (Set.empty, Map.empty)

    case ControlTree.Section(_, body) => _rec(body, funcId)

    case ControlTree.Seq(x, xs) =>
      val (x_edges, x_exc) = _rec(x, funcId)
      val (xs_edges, xs_exc) = _rec(xs, funcId)
      (x_edges ++ xs_edges, combineMaps(x_exc, xs_exc, _ ++ _))

    case ControlTree.Fork(b1, b2) =>
      val (b1_edges, b1_exc) = _rec(b1, funcId)
      val (b2_edges, b2_exc) = _rec(b2, funcId)
      (b1_edges ++ b2_edges, combineMaps(b1_exc, b2_exc, _ ++ _))

    case ControlTree.Fix(fx, b) =>
      _rec(b, funcId)
//      val (repeat_edges, repeat_exc) = repeat.map(_rec(_, funcId)).getOrElse((Set.empty, Map.empty))
      // (b_edges ++ repeat_edges, combineMaps(b_exc, repeat_exc, _ ++ _))

    case ControlTree.Restart() => (Set.empty, Map.empty)

    case ControlTree.Throw(exc) =>
      if (funcId.isDefined)
        (Set.empty, Map(exc -> Set(funcId.get)))
      else
        (Set.empty, Map.empty)

    case ControlTree.Try(body, handlers) =>
      val (body_edges, body_exc) = _rec(body, funcId)
      val (handlers_edges, handlers_exc, handled_exc) = handlers.map(handler => _recCatch(handler._1, handler._2, body_exc))
        .foldLeft((Set[CallEdge](), Map[Control.Exc, Set[FuncId]](), Set[Control.Exc]())) { (a, b) => (a._1 ++ b._1, combineMaps(a._2, b._2, _ ++ _), b._3.map(a._3 + _).getOrElse(a._3)) }

      (body_edges ++ handlers_edges, combineMaps(handlers_exc, body_exc.filterNot((k, _) => handled_exc.contains(k)), _ ++ _))

  private def _recCatch(exc: Control.Exc, tree: ControlTree[_, Control.Section, Control.Exc, Control.Fx], activeExc: Map[Control.Exc, Set[FuncId]]): (Set[CallEdge], Map[Control.Exc, Set[FuncId]], Option[Control.Exc]) =
    activeExc.get(exc) match
      case Some(funcIds) => funcIds.map(funcId => _rec(tree, Some(funcId))).fold((Set.empty, Map.empty)) { (a, b) => (a._1 ++ b._1, combineMaps(a._2, b._2, _ ++ _)) } ++ Tuple1(Some(exc))
      case None => throw new Exception("...")

}