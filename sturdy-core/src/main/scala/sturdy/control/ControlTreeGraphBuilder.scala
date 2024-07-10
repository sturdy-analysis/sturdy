package sturdy.control

import sturdy.data.combineMaps

import scala.annotation.tailrec
import scala.language.implicitConversions

class ControlTreeGraphBuilder[Atom, Sec, Exc, Fx] {

  private type CT = ControlTree[Atom, Sec, Exc, Fx]
  private case class CNode(n: Node[Atom, Sec], exc: Boolean)
  private implicit def cnode[A <: Atom,S <: Sec](n: Node[A, S]): CNode = CNode(n, false)
  private type CEdge = Edge[Atom, Sec]

  private type CNodes = Set[CNode]
  private type ActiveExc = Map[Exc, CNodes]
  private type Fixpoints = Map[Fx, (CNodes, ActiveExc)]
  private type Graph = Set[CEdge]

  def build(ct: CT): ControlGraph[Atom, Sec] =
    val init_state = TreeBuilderState(preds = Set(Node.Start()), aes = Map.empty, fixpoints = Map.empty, curg = Set.empty)
    val builded_state = _build(ct, init_state)
//    val helper_edges_state = addBlockPairEdges(builded_state)
    ControlGraph(builded_state.curg)

  private def addNode(state: TreeBuilderState, n: Node[Atom, Sec]): TreeBuilderState =
    state.copy(
      preds = Set(n),
      curg = state.curg ++
        state.preds.map(pred => Edge(pred.n, n, if pred.exc then EdgeType.Exceptional else EdgeType.CF)))

  private def mergeAes(aes1: ActiveExc, aes2: ActiveExc): ActiveExc =
    aes1 ++ aes2.map { case (k, v) => k -> (aes1.getOrElse(k, Set.empty) ++ v) }

  private def addAes(state: TreeBuilderState, exc: Exc): TreeBuilderState =
    state.copy(aes = state.aes + (exc -> (state.preds.map(node => node.copy(exc = true)) ++ state.aes.getOrElse(exc, Set.empty))))

  private def addFixpoint(state: TreeBuilderState, fx: Fx): TreeBuilderState =
    state.copy(fixpoints = state.fixpoints + (fx ->
      (state.preds ++ state.fixpoints.getOrElse(fx, (List.empty, Map.empty))._1,
        mergeAes(state.aes, state.fixpoints.getOrElse(fx, (List.empty, Map.empty))._2)
      )))

  private def restoreFixpoint(state: TreeBuilderState, fx: Fx): TreeBuilderState =
    state.copy(
      preds = state.fixpoints.getOrElse(fx, (Set.empty, Map.empty))._1,
      aes = state.fixpoints.getOrElse(fx, (Set.empty, Map.empty))._2
    )

  private def _build(ct: CT, state: TreeBuilderState): TreeBuilderState = ct match
    case ControlTree.Empty() => state

    case at@ControlTree.Atomic(a) =>
      addNode(state, Node.Atomic(a)(at.label))

    case ControlTree.Failed() =>
      addNode(state, Node.Failure()).copy(preds = Set.empty)

    case st@ControlTree.Section(section, body) =>
      val body_state = _build(body, addNode(state, Node.BlockStart(section)(st.label)))
      if (body_state.preds.isEmpty)
        body_state
      else
        addNode(body_state, Node.BlockEnd(section)(st.label))

    case ControlTree.Seq(t1, t2) =>
      _build(t2, _build(t1, state))

    case ControlTree.Fork(b1, b2) =>
      val state1 = _build(b1, state)
      val state2 = _build(b2, state1.copy(preds = state.preds, aes = state.aes))
      state2.copy(preds = state1.preds ++ state2.preds, aes = mergeAes(state1.aes, state2.aes))

    case ControlTree.Try(body, handlers) =>
      val state_body = _build(body, state.copy(aes = Map.empty))
      val state_handlers = handlers.foldLeft(state_body.copy(aes = Map.empty))((s, handler) =>
        val state_handler = _build(handler._2, s.copy(
          preds = state_body.aes.getOrElse(handler._1, Set.empty),
          aes = Map.empty
        ))
        state_handler.copy(preds = s.preds ++ state_handler.preds, aes = mergeAes(s.aes, state_handler.aes))
      )

      state_handlers.copy(aes = mergeAes(state.aes, state_handlers.aes))

    case ControlTree.Throw(exc) =>
      addAes(state, exc).copy(preds = Set.empty)

    case ControlTree.Fix(fx, b) =>
      val state1 = _build(b, state)
      addFixpoint(state1, fx)

    case ControlTree.Recurrent(fx) =>
      restoreFixpoint(state, fx)

    case ControlTree.Restart() =>
      state.copy(preds = Set.empty, aes = Map.empty)

  private def addBlockPairEdges(state: TreeBuilderState): TreeBuilderState =
    val openedSections = state.curg.flatMap(e => List(e._1, e._2)).flatMap {
      case s@Node.BlockStart(sec) => List(s)
      case _ => List.empty
    }
    val closedSections = openedSections.filter { s =>
      val sec = s.sec
      state.curg.exists {
        case Edge(_, Node.BlockEnd(`sec`), _) => true
        case Edge(Node.BlockEnd(`sec`), _, _) => true
        case _ => false
      }
    }
    val blockPairEdges: Set[CEdge] = closedSections.flatMap { s =>
      val sec = s.sec
      if (state.curg.exists {
        case Edge(Node.BlockStart(`sec`), Node.BlockEnd(`sec`), _) => true
        case _ => false
      })
        List.empty
      else
        List(Edge(s, Node.BlockEnd(sec)(s.label), EdgeType.BlockPair))
    }

    state.copy(curg = state.curg ++ blockPairEdges)

  private case class TreeBuilderState(preds: CNodes, aes: ActiveExc, fixpoints: Fixpoints, curg: Graph)




}

