package sturdy.fix.cfg

import sturdy.effect.JoinObserver
import sturdy.effect.ObservableJoin

import collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import ControlFlowGraph.*
import sturdy.data.combineMaps
import sturdy.effect.TrySturdy
import sturdy.effect.except.ExceptObserver
import sturdy.effect.except.ObservableExcept
import sturdy.fix.Contextual
import sturdy.fix.Logger
import sturdy.fix.cfg.ControlFlowGraph.*
import sturdy.util.Exact

import scala.runtime.BoxesRunTime


class ControlLogger[Ctx, Dom, Codom, Exc, N <: ControlFlowGraph.Node]
  (contextSensitive: Boolean,
   startNode: N,
   getDomNode: Dom => Option[N],
   getCodomNode: (Dom, Codom) => Option[N],
   obsJoin: ObservableJoin,
   obsExcept: ObservableExcept[Exc]
  )
  extends JoinObserver, ExceptObserver[Exc], ControlFlowGraph[N, Ctx]:

  obsJoin.addJoinObserver(this)
  obsExcept.addExceptObserver(this)

  private case class PredNode(cnode: CNode[N, Ctx], exceptional: Boolean)

  type Predecessors = Map[CNode[N, Ctx], EdgeAttrib]
  type Exceptions = Map[Any, Predecessors]

  private val startCNode: CNode[N, Ctx] = CNode(startNode, null.asInstanceOf[Ctx])

  private var predecessors: Predecessors = Map(startCNode -> EdgeAttrib.default)
  private var joinStack: List[Predecessors] = List()

  /** Currently active exceptions and the CFG nodes that triggered them. */
  private var exceptions: Exceptions = Map()
  private var exceptStack: List[Exceptions] = List()
  /** Exceptions currently handled in a catch block. Uncaught exceptions are propagated outwards. */
  private var catchExceptions: Exceptions = Map()

  private val nodes: mutable.Set[CNode[N, Ctx]] = mutable.Set(startCNode)
  private val edges: mutable.Map[CNode[N, Ctx], Map[CNode[N, Ctx], EdgeAttrib]] = mutable.Map()

  override def throwing(exc: Exc): Unit =
    val exceptionalPredecessors = predecessors.map(_.copy(_2 = EdgeAttrib(true)))
    exceptions.get(exc) match
      case None =>
        exceptions += exc -> exceptionalPredecessors
      case Some(preds) =>
        val combinedPreds = combinePredecessors(preds, exceptionalPredecessors)
        exceptions += exc -> combinedPreds
    predecessors = Map()

  override def handlingStart(exc: Exc): Unit =
    catchExceptions.get(exc) match
      case None => predecessors = Map()
      case Some(preds) =>
        predecessors = preds
        catchExceptions -= exc

  override def handlingEnd(): Unit = ()

  override def tryStart(): Unit =
    exceptStack = exceptions :: exceptStack
    exceptions = Map()

  override def tryEnd(): Unit =
    val fallthrough = exceptStack.head
    exceptions = combineExceptions(exceptions, fallthrough)
    exceptStack = exceptStack.tail

  override def catchStart(): Unit =
    catchExceptions = exceptions
    exceptions = Map()

  override def catchEnd(): Unit =
    exceptions = combineExceptions(exceptions, catchExceptions)

  override def joinStart(): Unit =
    joinStack = predecessors :: joinStack
//    exceptions = Map()

  override def joinSwitch(leftFailed: Boolean): Unit =
    val otherPredecessors = joinStack.head
    if (leftFailed)
      joinStack = Map() :: joinStack.tail
    else
      joinStack = predecessors :: joinStack.tail
    predecessors = otherPredecessors

  override def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit =
    val otherPredecessors = joinStack.head
    joinStack = joinStack.tail
    if (rightFailed) {
      predecessors = otherPredecessors
    } else if (leftFailed) {
      // keep predecessors
    } else {
      predecessors = combinePredecessors(predecessors, otherPredecessors)
    }

  private inline def combinePredecessors(preds1: Predecessors, preds2: Predecessors): Predecessors =
    combineMaps(preds1, preds2, _.combine(_))

  private inline def combineExceptions(excs1: Exceptions, excs2: Exceptions): Exceptions =
    combineMaps(excs1, excs2, combinePredecessors(_, _))

  // TODO
//  override def recurrent(in: In, result: Option[TrySturdy[Out]]): Unit = result match
//    case None =>
//      // recurrent call yields bottom
//      predecessors = Set()
//    case Some(out) =>
//      enterNode(in)
//      exitNode(in, out)

  override def repeating(): Unit =
    // We have already logged the edges to `in` from its original predecessor.
    predecessors = Map()

  def clear(): Unit =
    predecessors = Map(startCNode -> EdgeAttrib.default)
    joinStack = List()
    nodes.clear()
    nodes += startCNode
    edges.clear()

  inline private def addEdgeFromPredecessors(to: CNode[N, Ctx]): Unit =
    predecessors.foreach((from, attrib) => addEdge(from, to, attrib))

  private def addEdge(from: CNode[N, Ctx], to: CNode[N, Ctx], attrib: EdgeAttrib): Unit =
    edges.get(from) match
      case None => edges += from -> Map(to -> attrib)
      case Some(map) => edges += from -> (map + (to -> attrib))

  def getNodes: List[CNode[N, Ctx]] = nodes.toList.sortBy(_.toString)
  def getEdges: Map[CNode[N, Ctx], Map[CNode[N, Ctx], EdgeAttrib]] = edges.toMap
  def getEdgeCount: Int = getEdges.map(kv => kv._2.size).sum

  def logger(using contextual: Contextual[Ctx, Dom, Codom]): Logger[Dom, Codom] = new Logger {
    private def getContext: Ctx =
      if (contextSensitive)
        contextual.getCurrentContext
      else
        null.asInstanceOf[Ctx]

    override def enter(dom: Dom): Unit = getDomNode(dom) match
      case Some(node) =>
        val cnode = CNode(node, getContext)
        nodes += cnode
        addEdgeFromPredecessors(cnode)
        predecessors = Map(cnode -> EdgeAttrib.default)
      case None => // nothing

    override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit = codom.get match
      case Some(cod) => getCodomNode(dom, cod) match
        case Some(node) =>
          val cnode = CNode(node, getContext)
          nodes += cnode
          addEdgeFromPredecessors(cnode)
          predecessors = Map(cnode -> EdgeAttrib.default)
        case None => // nothing
      case None =>
        predecessors = Map()
  }


