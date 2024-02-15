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


  case Seq(body: List[ControlTree[Atom, Sec, Exc]])
  case Fork(branches: List[ControlTree[Atom, Sec, Exc]])

  case Try(body: ControlTree[Atom, Sec, Exc], handlers: List[ControlTree[Atom, Sec, Exc]])
  case Throw(exc: Exc)
  case Handling(exc: Exc, body: ControlTree[Atom, Sec, Exc])

  case Fixpoint(b: ControlTree[Atom, Sec, Exc], repeat: Option[ControlTree[Atom, Sec, Exc]])
  case Recurrent(failing: Boolean)

  @targetName("plusToSeq")
  infix def +(that: ControlTree[Atom, Sec, Exc]): ControlTree[Atom, Sec, Exc] = (this, that) match
    case (Seq(l1), Seq(l2)) => Seq(l1 ++ l2)
    case (Seq(l1), e2) => Seq(l1 :+ e2)
    case (e1, Seq(l2)) => Seq(e1 :: l2)
    case (e1, e2) => Seq(List(e1, e2))

  def print: List[ControlEvent] =
    val buf: ListBuffer[ControlEvent] = ListBuffer.empty
    _print(buf)
    buf.toList

  def toGraphViz: String = this._toGraphViz("Start").fold(s"")(_ + "\n" + _)

  private def _print(buf: ListBuffer[ControlEvent]): Unit = this match
    case ControlTree.Empty() => ()

    case ControlTree.Atomic(a) =>
      buf += BasicControlEvent.Atomic(a)

    case ControlTree.Seq(body) =>
      body.foreach(_._print(buf))

    case ControlTree.Section(section, body) =>
      buf += BasicControlEvent.Begin(section)
      body._print(buf)
      buf += BasicControlEvent.End(section)

    case ControlTree.Fork(branches) =>

      branches.init.foreach { b =>
        buf += BranchingControlEvent.Fork()
        b._print(buf)
        buf += BranchingControlEvent.Switch()
      }

      branches.last._print(buf)

      branches.init.foreach { b =>
        buf += BranchingControlEvent.Join()
      }

    case ControlTree.Failed() =>
      buf += BasicControlEvent.Failed()

    case ControlTree.Try(body, handlers) =>
      buf += ExceptionControlEvent.BeginTry()
      body._print(buf)
      buf += ExceptionControlEvent.EndTry()
      handlers.foreach(_._print(buf))

    case ControlTree.Throw(exc) =>
      buf += ExceptionControlEvent.Throw(exc)

    case ControlTree.Handling(exc, body) =>
      buf += ExceptionControlEvent.Handle(exc)
      body._print(buf)

    case ControlTree.Fixpoint(b, repeat) =>
      buf += FixpointControlEvent.BeginFixpoint()
      b._print(buf)
      repeat match
        case None => ()
        case Some(repeatCt) =>
          buf += FixpointControlEvent.RepeatFixpoint()
          repeatCt._print(buf)
      buf += FixpointControlEvent.EndFixpoint()

    case ControlTree.Recurrent(failing) =>
      buf += FixpointControlEvent.RecurrentCall(failing)


  private def _toGraphViz(p: String): Set[String] = this match
    case ControlTree.Empty() =>
      val name = s"Empty(${randomString})"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Atomic(a) =>
      val name = s"$a (${randomString})"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Seq(body) =>
      val name = s"Seq (${randomString})"
      body.flatMap(_._toGraphViz(name)).toSet + toGraphVizEdge(p, name)

    case ControlTree.Section(section, body) =>
      val name = s"Section $section (${randomString})"
      body._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Fork(branches) =>
      val name = s"Fork (${randomString})"
      branches.flatMap(_._toGraphViz(name)).toSet + toGraphVizEdge(p, name)

    case ControlTree.Failed() =>
      val name = s"Failed(${randomString})"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Try(body, handlers) =>
      val name = s"Try (${randomString})"
      body._toGraphViz(name) ++ handlers.flatMap(_._toGraphViz(name)).toSet + toGraphVizEdge(p, name)

    case ControlTree.Throw(exc) =>
      val name = s"Throw $exc (${randomString})"
      Set(toGraphVizEdge(p, name))

    case ControlTree.Handling(exc, body) =>
      val name = s"Handle $exc (${randomString})"
      body._toGraphViz(name) + toGraphVizEdge(p, name)

    case ControlTree.Fixpoint(b, repeat) =>
      ???

    case ControlTree.Recurrent(failing) =>
      val name = s"Recurrent (${randomString})"
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

        var branches: List[CT] = List.empty

        b2 match
          case Some(Fork(branches2)) => branches = branches2
          case _ => branches = b2.getOrElse(Empty()) :: branches

        b1 match
          case Some(Fork(branches1)) => branches = branches1 ++ branches
          case _ => branches = b1.getOrElse(Empty()) :: branches

        (ControlTree.Fork(branches), i + 1)

      case _ => throw new Exception("Error in dispatch")

    def _buildTry(index: Int): (CT, Int) = events(index) match
      case ExceptionControlEvent.BeginTry() =>
        var i = index + 1
        var cache: Option[CT] = None
        var body: Option[CT] = None
        var handlers: List[CT] = List.empty

        while (events(i) != ExceptionControlEvent.EndTry())
          val (t, skip) = _dispatch(i)
          if (cache.isDefined)
            body = concatenate(body, cache.get)
          cache = Some(t)
          i = skip

        if (cache match
          case Some(ControlTree.Fork(branches)) => branches.forall {
            case Handling(_, _) => true
            case Empty() => true
            case _ => false
          }
          case Some(ControlTree.Handling(exc, body)) => true
          case _ => false) {
          handlers = List(cache.get)
        }
        else {
          if (cache.isDefined)
            body = concatenate(body, cache.get)
        }

        (ControlTree.Try(body.getOrElse(Empty()), handlers), i + 1)


      case _ => throw new Exception("Error in dispatch")

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

    def _buildFixpoint(index: Int): (CT, Int) =
      ???


    build()