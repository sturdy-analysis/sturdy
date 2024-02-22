package sturdy.control


import java.util.Optional
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer
import scala.util.Random

enum ControlTree[Atom, Sec, Exc]:
  case Empty()

  case Atomic(a: Atom)
  case Failed()
  case Section(section: Sec, body: ControlTree[Atom, Sec, Exc])

  case Seq(x: ControlTree[Atom, Sec, Exc], xs: ControlTree[Atom, Sec, Exc])
  case Fork(b1: ControlTree[Atom, Sec, Exc], b2: ControlTree[Atom, Sec, Exc])

  case Try(body: ControlTree[Atom, Sec, Exc], handlers: List[ControlTree[Atom, Sec, Exc]])
  case Throw(exc: Exc)
  case Handling(exc: Exc, body: ControlTree[Atom, Sec, Exc])

  case Fixpoint(b: ControlTree[Atom, Sec, Exc], repeat: Option[ControlTree[Atom, Sec, Exc]])
  case Recurrent(failing: Boolean)

  @targetName("plusToSeq")
  infix def +(that: ControlTree[Atom, Sec, Exc]): ControlTree[Atom, Sec, Exc] = (this, that) match
    case (_, Empty()) => this
    case (Empty(), _) => that
    case (Seq(_, Empty()), _) => assert(false)
    case (Seq(b1, b2), _) => Seq(b1, Seq(b2, that))
    case (_, _) => Seq(this, that)

  def print: List[ControlEvent] =
    val buf: ListBuffer[ControlEvent] = ListBuffer.empty
    _print(buf)
    buf.toList

  def toGraphViz: String = this._toGraphViz("Start").toList.sorted.fold(s"")(_ + "\n" + _)

  private def _print(buf: ListBuffer[ControlEvent]): Unit = this match
    case ControlTree.Empty() => ()

    case ControlTree.Atomic(a) =>
      buf += BasicControlEvent.Atomic(a)

    case ControlTree.Seq(x, xs) =>
      x._print(buf)
      xs._print(buf)

    case ControlTree.Section(section, body) =>
      buf += BasicControlEvent.Begin(section)
      body._print(buf)
      buf += BasicControlEvent.End(section)

    case ControlTree.Fork(b1, b2) =>
      buf += BranchingControlEvent.Fork()
      b1._print(buf)
      buf += BranchingControlEvent.Switch()
      b2._print(buf)
      buf += BranchingControlEvent.Join()

    case ControlTree.Failed() =>
      buf += BasicControlEvent.Failed()

    case ControlTree.Try(body, handlers) =>
      buf += ExceptionControlEvent.BeginTry()
      body._print(buf)

      if (handlers.nonEmpty) {
        handlers.tail.foreach(_ =>
          buf += BranchingControlEvent.Fork()
        )

        handlers.head._print(buf)

        handlers.tail.foreach(handler =>
          buf += BranchingControlEvent.Switch()
          handler._print(buf)
          buf += BranchingControlEvent.Join()
        )
      }

      buf += ExceptionControlEvent.EndTry()

    case ControlTree.Throw(exc) =>
      buf += ExceptionControlEvent.Throw(exc)

    case ControlTree.Handling(exc, body) =>
      buf += ExceptionControlEvent.Handle(exc)
      body._print(buf)

    case ControlTree.Fixpoint(b, repeat) =>
      buf += FixpointControlEvent.BeginFixpoint()
      b._print(buf)
      buf += FixpointControlEvent.EndFixpoint()
      repeat match
        case None => ()
        case Some(repeatCt) =>
          buf += FixpointControlEvent.RepeatFixpoint()
          repeatCt._print(buf)

    case ControlTree.Recurrent(failing) =>
      buf += FixpointControlEvent.RecurrentCall(failing)


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
      val name = s"Try ($randomString)"
      body._toGraphViz(name)
        ++ handlers.flatMap(
        _._toGraphViz(name)
      ).toSet
        + toGraphVizEdge(p, name)

    case ControlTree.Throw(exc) =>
      val name = s"Throw $exc ($randomString)"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Handling(exc, body) =>
      val name = s"Handle $exc ($randomString)"
      body._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Fixpoint(body, repeat) =>
      val name = s"Fixpoint ($randomString)"
      body._toGraphViz(name) ++ repeat.map(_._toGraphViz(name)).getOrElse(Set.empty) + toGraphVizEdge(p, name)

    case ControlTree.Recurrent(_) =>
      val name = s"Recurrent ($randomString)"
      Set(toGraphVizEdge(p, name))

  private def randomString: String = Random.alphanumeric.take(10).mkString
  private def toGraphVizEdge(n1: String, n2: String): String = s"\"$n1\" -> \"$n2\""

object ControlTree:

  def buildControlTree[Atom, Sec, Exc](events: List[ControlEvent]): ControlTree[Atom, Sec, Exc] =

    type CT = ControlTree[Atom, Sec, Exc]

    inline def concatenate(a: Option[CT], b: CT): Option[CT] =
      Some(a.map(_ + b).getOrElse(b))

    def build(): CT =
      var index = 0
      var program: Option[CT] = None

      while (index < events.size)
        val (t, skipTo) = _dispatch(index)
        program = concatenate(program, t)
        index = skipTo

      program.getOrElse(Empty())

    def _dispatch(index: Int): (CT, Int) = events(index) match

      case event: BasicControlEvent[Atom, Sec] => event match
        case BasicControlEvent.Atomic(a) => (ControlTree.Atomic(a), index + 1)
        case BasicControlEvent.Failed() => (ControlTree.Failed(), index + 1)
        case BasicControlEvent.Begin(_) => _buildSection(index)
        case BasicControlEvent.End(_) => throw new Exception("Invalid Sequence")

      case event: BranchingControlEvent => event match
        case BranchingControlEvent.Fork() => _buildFork(index)
        case _ => throw new Exception("Invalid Sequence")

      case event: ExceptionControlEvent[Exc] => event match
        case ExceptionControlEvent.BeginTry() => _buildTry(index)
        case ExceptionControlEvent.Throw(exc) => (ControlTree.Throw(exc), index + 1)
        case ExceptionControlEvent.Handle(_) => _buildHandle(index)
        case ExceptionControlEvent.Catching() => throw new Exception("Should not happen")
        case _ => throw new Exception("Invalid Sequence")

      case event: FixpointControlEvent => event match
        case FixpointControlEvent.BeginFixpoint() => _buildFixpoint(index)
        case FixpointControlEvent.RecurrentCall(failing) => (ControlTree.Recurrent(failing), index + 1)
        case _ => throw new Exception("Invalid Sequence")

    def _buildSection(index: Int): (CT, Int) = events(index) match
      case BasicControlEvent.Begin(sec: Sec) =>
        var i = index + 1
        var body: Option[CT] = None

        while (events(i) != BasicControlEvent.End(sec: Sec))
          val (t, skip) = _dispatch(i)
          body = concatenate(body, t)
          i = skip

        (ControlTree.Section(sec, body.getOrElse(Empty())), i + 1)

      case _ => throw new Exception("Error in dispatch")

    def _buildFork(index: Int): (CT, Int) = events(index) match
      case BranchingControlEvent.Fork() =>
        var i = index + 1
        var b1: Option[CT] = None
        var b2: Option[CT] = None

        while (events(i) != BranchingControlEvent.Switch())
          val (t, skip) = _dispatch(i)
          b1 = concatenate(b1, t)
          i = skip

        i += 1

        while (events(i) != BranchingControlEvent.Join())
          val (t, skip) = _dispatch(i)
          b2 = concatenate(b2, t)
          i = skip

        val fork = b1 match
          case Some(Fork(b11, b12)) => Fork(b11, Fork(b12, b2.getOrElse(Empty())))
          case _ => Fork(b1.getOrElse(Empty()), b2.getOrElse(Empty()))

        (fork, i + 1)

      case _ => throw new Exception("Error in dispatch")

    def _buildTry(index: Int): (CT, Int) = events(index) match
      case ExceptionControlEvent.BeginTry() =>
        var i = index + 1

        var cache: Option[CT] = None
        var body: Option[CT] = None

        var handlers: List[CT] = List.empty

        while (events(i) != ExceptionControlEvent.EndTry())
          val (t, skip) = _dispatch(i)
          if (cache.isDefined && !cache.contains(Empty()))
            body = concatenate(body, cache.get)
          cache = Some(t)
          i = skip

        cache match
          case Some(h@ControlTree.Fork(_, _)) if _getHandlers(h).isDefined =>
            handlers = _getHandlers(h).get
          case Some(h@ControlTree.Handling(_, _)) => handlers = List(h)
          case Some(Empty()) => ()
          case Some(_) => body = concatenate(body, cache.get)
          case None => ()

        (ControlTree.Try(body.getOrElse(Empty()), handlers), i + 1)

      case _ => throw new Exception("Error in dispatch")

    def _getHandlers(ct: CT): Option[List[CT]] = ct match
      case ControlTree.Fork(b1, b2) =>
        val h1 = _getHandlers(b1)
        if (h1.isDefined)
          val h2 = _getHandlers(b2)
          if (h2.isDefined)
            Some(h1.get ++ h2.get)
          else
            None
        else
          None
      case h@ControlTree.Handling(_, _) => Some(List(h))
      case ControlTree.Empty() => Some(List(Empty()))
      case _ => None


    def _buildHandle(index: Int): (CT, Int) = events(index) match
      case ExceptionControlEvent.Handle(exc: Exc) =>
        var i = index + 1
        var body: Option[CT] = None

        while (events(i) match
          case ExceptionControlEvent.Handle(_) => false
          case ExceptionControlEvent.EndTry() => false
          case BranchingControlEvent.Switch() => false
          case BranchingControlEvent.Join() => false
          case _ => true)

          val (t, skip) = _dispatch(i)
          body = concatenate(body, t)
          i = skip

        (ControlTree.Handling(exc, body.getOrElse(Empty())), i)
      case _ => throw new Exception("Error in dispatch")

    def _buildFixpoint(index: Int): (CT, Int) = events(index) match
      case FixpointControlEvent.BeginFixpoint() =>
        var i = index + 1
        var body: Option[CT] = None
        var repeat: Option[CT] = None

        while (events(i) != FixpointControlEvent.EndFixpoint())
          val (t, skip) = _dispatch(i)
          body = concatenate(body, t)
          i = skip

        i = i + 1

        if (events(i) == FixpointControlEvent.RepeatFixpoint()) events(i + 1) match
          case FixpointControlEvent.BeginFixpoint() =>
            val (repeatSec, skip) = _buildFixpoint(i + 1)
            repeat = Some(repeatSec)
            i = skip

        (ControlTree.Fixpoint(body.getOrElse(Empty()), repeat), i)

      case _ => throw new Exception("Error in dispatch")

    build()