package sturdy.control


import java.util.Optional
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
 * Alternative representation of the control trace of an interpreter.
 * Has to be parsed from control events using ControlEventParser.
 * Can be used to build a tree using ControlTreeGraphBuilder via the toGraph method, but prefer direct building with ControlEventGraphBuilder, which is more efficient.
 * Storing traces as trees can take a lot of space in memory.
 */
enum ControlTree[Atom, Sec, Exc, Fx]:
  case Empty()

  case Atomic(a: Atom)(val label: String)
  case Failed()
  case Section(section: Sec, body: ControlTree[Atom, Sec, Exc, Fx])(val label: String)

  case Seq(t1: ControlTree[Atom, Sec, Exc, Fx], t2: ControlTree[Atom, Sec, Exc, Fx])
  case Fork(t1: ControlTree[Atom, Sec, Exc, Fx], t2: ControlTree[Atom, Sec, Exc, Fx])

  case Try(body: ControlTree[Atom, Sec, Exc, Fx], handlers: List[(Exc, ControlTree[Atom, Sec, Exc, Fx])])
  case Throw(exc: Exc)

  case Fix(fx: Fx, b: ControlTree[Atom, Sec, Exc, Fx])
  case Recurrent(fx: Fx)
  case Restart() // restart control at the next subtree

  def size: Int = this match
    case ControlTree.Empty() => 1
    case ControlTree.Atomic(a) => 1
    case ControlTree.Failed() => 1
    case ControlTree.Section(section, body) => 1 + body.size
    case ControlTree.Seq(t1, t2) => 1 + t1.size + t2.size
    case ControlTree.Fork(t1, t2) => 1 + t1.size + t2.size
    case ControlTree.Try(body, handlers) => 1 + body.size + handlers.map(_._2.size).sum
    case ControlTree.Throw(exc) => 1
    case ControlTree.Fix(fx, b) => 1 + b.size
    case ControlTree.Recurrent(fx) => 1
    case ControlTree.Restart() => 1

  @targetName("plusToSeq")
  infix def +(that: ControlTree[Atom, Sec, Exc, Fx]): ControlTree[Atom, Sec, Exc, Fx] = (this, that) match
    case (_, Empty()) => this
    case (Empty(), _) => that
    case (Seq(_, Empty()), _) => assert(false)
    case (Seq(b1, b2), _) => Seq(b1, Seq(b2, that))
    case (_, _) => Seq(this, that)

  def print: List[ControlEvent[Atom,Sec,Exc,Fx]] =
    val buf: ListBuffer[ControlEvent[Atom,Sec,Exc,Fx]] = ListBuffer.empty
    _print(buf)
    buf.toList

  lazy val toGraph: ControlGraph[Atom, Sec] =
    val builder = new ControlTreeGraphBuilder[Atom, Sec, Exc, Fx]
    builder.build(this)

  private def _print(buf: ListBuffer[ControlEvent[Atom,Sec,Exc,Fx]]): Unit = this match
    case ControlTree.Empty() => ()

    case ControlTree.Failed() =>
      buf += BasicControlEvent.Failed()

    case at@ControlTree.Atomic(a) =>
      buf += BasicControlEvent.Atomic(a)(at.label)

    case st@ControlTree.Section(section, body) =>
      buf += BasicControlEvent.BeginSection(section)(st.label)
      body._print(buf)
      buf += BasicControlEvent.EndSection()

    case ControlTree.Seq(x, xs) =>
      x._print(buf)
      xs._print(buf)

    case ControlTree.Fork(b1, b2) =>
      buf += BranchingControlEvent.Fork()
      b1._print(buf)
      buf += BranchingControlEvent.Switch()
      b2._print(buf)
      buf += BranchingControlEvent.Join()

    case ControlTree.Throw(exc) =>
      buf += ExceptionControlEvent.Throw(exc)

    case ControlTree.Try(body, handlers) =>
      buf += ExceptionControlEvent.BeginTry()
      body._print(buf)
      buf += ExceptionControlEvent.Catching()

      handlers.foreach((ex,t) =>
        buf += ExceptionControlEvent.BeginHandle(ex)
        t._print(buf)
        buf += ExceptionControlEvent.EndHandle()
      )

      buf += ExceptionControlEvent.EndTry()

    case ControlTree.Fix(fx, b) =>
      buf += FixpointControlEvent.BeginFixpoint(fx)
      b._print(buf)
      buf += FixpointControlEvent.EndFixpoint()

    case ControlTree.Recurrent(fx) =>
      buf += FixpointControlEvent.Recurrent(fx)

    case ControlTree.Restart() =>
      buf += FixpointControlEvent.Restart()

  private def _toGraphViz(p: String): Set[String] = this match
    case ControlTree.Empty() =>
      val name = s"Empty($randomString)"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Atomic(a) =>
      val name = s"$a ($randomString)"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Seq(x, xs) =>
      val name = s"Seq ($randomString)"
      x._toGraphViz(name) ++ xs._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Section(section, body) =>
      val name = s"Section $section ($randomString)"
      body._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Fork(b1, b2) =>
      val name = s"Fork ($randomString)"
      b1._toGraphViz(name) ++ b2._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Failed() =>
      val name = s"Failed($randomString)"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Try(body, handlers) =>
      val rand = randomString
      val tryName = s"Try ($rand)"
      body._toGraphViz(tryName)
        ++ handlers.flatMap((ex,t) => {
          val handleName = s"Handle $ex ($randomString)"
          t._toGraphViz(handleName) + toGraphVizEdge(tryName, handleName)
        }
      ).toSet
        + toGraphVizEdge(p, tryName)

    case ControlTree.Throw(exc) =>
      val name = s"Throw $exc ($randomString)"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Fix(fx, body) =>
      val name = s"Fixpoint ($randomString)"
      body._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Recurrent(fx) =>
      val name = s"Recurrent ($randomString)"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Restart() =>
      val name = s"Restart ($randomString)"
      Set(toGraphVizEdge(p, name))

  private def randomString: String = Random.alphanumeric.take(10).mkString
  private def toGraphVizEdge(n1: String, n2: String): String = s"\"$n1\" -> \"$n2\""

//object ControlTree:
//
//  def buildControlTree[Atom, Sec, Exc, Fx](events: List[ControlEvent]): ControlTree[Atom, Sec, Exc, Fx] =
//
//    type CT = ControlTree[Atom, Sec, Exc, Fx]
//
//    inline def concatenate(a: Option[CT], b: CT): Option[CT] =
//      Some(a.map(_ + b).getOrElse(b))
//
//    def build(): CT =
//      var index = 0
//      var program: Option[CT] = None
//
//      while (index < events.size)
//        val (t, skipTo) = _dispatch(index)
//        program = concatenate(program, t)
//        index = skipTo
//
//      program.getOrElse(Empty())
//
//    def _dispatch(index: Int): (CT, Int) = events(index) match
//
//      case event: BasicControlEvent[Atom, Sec] => event match
//        case BasicControlEvent.Atomic(a) => (ControlTree.Atomic(a), index + 1)
//        case BasicControlEvent.Failed() => (ControlTree.Failed(), index + 1)
//        case BasicControlEvent.BeginSection(_) => _buildSection(index)
//        case BasicControlEvent.EndSection() => throw new Exception("Invalid Sequence")
//
//      case event: BranchingControlEvent => event match
//        case BranchingControlEvent.Fork() => _buildFork(index)
//        case _ => throw new Exception("Invalid Sequence")
//
//      case event: ExceptionControlEvent[Exc] => event match
//        case ExceptionControlEvent.BeginTry() => _buildTry(index)
//        case ExceptionControlEvent.Throw(exc) => (ControlTree.Throw(exc), index + 1)
//        case ExceptionControlEvent.Handle(_) => ??? // _buildHandle(index)
//        case ExceptionControlEvent.Catching() => throw new Exception("Should not happen")
//        case _ => throw new Exception("Invalid Sequence")
//
//      case event: FixpointControlEvent[Fx] => event match
//        case FixpointControlEvent.BeginFixpoint(fx) => _buildFixpoint(index)
//        case FixpointControlEvent.Recurrent(fx) => (ControlTree.Recurrent(fx), index + 1)
//        case _ => throw new Exception("Invalid Sequence")
//
//    def _buildSection(index: Int): (CT, Int) = events(index) match
//      case BasicControlEvent.BeginSection(sec: Sec) =>
//        var i = index + 1
//        var body: Option[CT] = None
//
//        while (events(i) != BasicControlEvent.EndSection())
//          val (t, skip) = _dispatch(i)
//          body = concatenate(body, t)
//          i = skip
//
//        (ControlTree.Section(sec, body.getOrElse(Empty())), i + 1)
//
//      case _ => throw new Exception("Error in dispatch")
//
//    def _buildFork(index: Int): (CT, Int) = events(index) match
//      case BranchingControlEvent.Fork() =>
//        var i = index + 1
//        var b1: Option[CT] = None
//        var b2: Option[CT] = None
//
//        while (events(i) != BranchingControlEvent.Switch())
//          val (t, skip) = _dispatch(i)
//          b1 = concatenate(b1, t)
//          i = skip
//
//        i += 1
//
//        while (events(i) != BranchingControlEvent.Join())
//          val (t, skip) = _dispatch(i)
//          b2 = concatenate(b2, t)
//          i = skip
//
//        val fork = b1 match
//          case Some(Fork(b11, b12)) => Fork(b11, Fork(b12, b2.getOrElse(Empty())))
//          case _ => Fork(b1.getOrElse(Empty()), b2.getOrElse(Empty()))
//
//        (fork, i + 1)
//
//      case _ => throw new Exception("Error in dispatch")
//
//    def _buildTry(index: Int): (CT, Int) = events(index) match
//      case ExceptionControlEvent.BeginTry() =>
//        var i = index + 1
//
//        var cache: Option[CT] = None
//        var body: Option[CT] = None
//
//        var handlers: List[(Exc, CT)] = List.empty
//
//        while (events(i) != ExceptionControlEvent.EndTry()) {
//          // TODO
//          // case ExceptionControlEvent.Handle(_) => _buildHandle(index)
//
//
//          val (t, skip) = _dispatch(i)
//          if (cache.isDefined && !cache.contains(Empty()))
//            body = concatenate(body, cache.get)
//          cache = Some(t)
//          i = skip
//        }
//
//        cache match
//          case Some(h@ControlTree.Fork(_, _)) if _getHandlers(h).isDefined =>
//            handlers = _getHandlers(h).get
//// TODO         case Some(h@ControlTree.Handling(_, _)) => handlers = List(h)
//          case Some(Empty()) => ()
//          case Some(_) => body = concatenate(body, cache.get)
//          case None => ()
//
//        (ControlTree.Try(body.getOrElse(Empty()), handlers), i + 1)
//
//      case _ => throw new Exception("Error in dispatch")
//
//    def _getHandlers(ct: CT): Option[List[(Exc, CT)]] = ct match
//      case ControlTree.Fork(b1, b2) =>
//        val h1 = _getHandlers(b1)
//        if (h1.isDefined)
//          val h2 = _getHandlers(b2)
//          if (h2.isDefined)
//            Some(h1.get ++ h2.get)
//          else
//            None
//        else
//          None
////      case h@ControlTree.Handling(_, _) => Some(List(h))
////      case ControlTree.Empty() => Some(List(Empty()))
//      case _ => None
//
//
//    def _buildHandle(index: Int): ((Exc, CT), Int) = events(index) match
//      case ExceptionControlEvent.Handle(exc: Exc) =>
//        var i = index + 1
//        var body: Option[CT] = None
//
//        while (events(i) match
//          case ExceptionControlEvent.Handle(_) => false
//          case ExceptionControlEvent.EndTry() => false
//          case BranchingControlEvent.Switch() => false
//          case BranchingControlEvent.Join() => false
//          case _ => true)
//
//          val (t, skip) = _dispatch(i)
//          body = concatenate(body, t)
//          i = skip
//
//        ((exc, body.getOrElse(Empty())), i)
//      case _ => throw new Exception("Error in dispatch")
//
//    def _buildFixpoint(index: Int): (CT, Int) = events(index) match
//      case FixpointControlEvent.BeginFixpoint(fx: Fx) =>
//        var i = index + 1
//        var body: Option[CT] = None
//        var repeat: Option[CT] = None
//
//        while (events(i) != FixpointControlEvent.EndFixpoint())
//          val (t, skip) = _dispatch(i)
//          body = concatenate(body, t)
//          i = skip
//
//        i = i + 1
//        if (i < events.size)
//          if (events(i) == FixpointControlEvent.RepeatFixpoint()) events(i + 1) match
//            case FixpointControlEvent.BeginFixpoint(fx) =>
//              val (repeatSec, skip) = _buildFixpoint(i + 1)
//              repeat = Some(repeatSec)
//              i = skip
//
//        (ControlTree.Fix(fx, body.getOrElse(Empty()), repeat), i)
//
//      case _ => throw new Exception("Error in dispatch")
//
//    build()