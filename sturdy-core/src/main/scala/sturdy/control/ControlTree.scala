package sturdy.control

import sturdy.control.FixpointControlEvent.RepeatFixpoint

import java.util.Optional
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer
import scala.util.Random

enum ControlTree[Atom, Sec, Exc]:
  case Empty()
  case Atomic(a: Atom)
  case Seq(a: ControlTree[Atom, Sec, Exc], b: ControlTree[Atom, Sec, Exc])
  case Section(section: Sec, body: ControlTree[Atom, Sec, Exc])
  case Fork(b1: ControlTree[Atom, Sec, Exc], b2: ControlTree[Atom, Sec, Exc])
  case Failed()

  case Try(body: ControlTree[Atom, Sec, Exc], handle: Map[Exc, ControlTree[Atom, Sec, Exc]])
  case Throw(exc: Exc)

  case Fixpoint(b: ControlTree[Atom, Sec, Exc], repeat: Option[ControlTree[Atom, Sec, Exc]])
  case Recurrent(failing: Boolean)

  @targetName("plusToSeq")
  infix def +(that: ControlTree[Atom, Sec, Exc]): ControlTree[Atom, Sec, Exc] =
    Seq(this, that)

  def toEvents: List[ControlEvent] =
    val buf: ListBuffer[ControlEvent] = ListBuffer.empty
    _toEvents(buf)
    buf.toList

  def toGraphViz: String = this._toGraphViz("Start").fold(s"")(_ + "\n" + _)

  private def _toGraphViz(p: String): Set[String] = this match
    case ControlTree.Empty() =>
      val name = s"Empty(${randomString})"
      Set(toGraphVizEdge(p, name))
    case ControlTree.Atomic(a) =>
      val name = s"$a (${randomString})"
      Set(toGraphVizEdge(p, name))
    case ControlTree.Seq(a, b) =>
      val name = s"Seq (${randomString})"
      a._toGraphViz(name) ++ b._toGraphViz(name) + toGraphVizEdge(p, name)
    case ControlTree.Section(section, body) =>
      val name = s"Section $section (${randomString})"
      body._toGraphViz(name) + toGraphVizEdge(p, name)
    case ControlTree.Fork(b1, b2) =>
      val name = s"Fork (${randomString})"
      b1._toGraphViz(name) ++ b2._toGraphViz(name) + toGraphVizEdge(p, name)
    case ControlTree.Failed() =>
      val name = s"Failed(${randomString})"
      Set(toGraphVizEdge(p, name))
    case ControlTree.Try(body, handle) =>
      val name = s"Try (${randomString})"
      body._toGraphViz(name) ++ handle.flatMap((exc, ct) => {
        val nameExc = s"Handle $exc (${randomString})"
        Set(toGraphVizEdge(name, nameExc)) ++ ct._toGraphViz(nameExc)
      }) + toGraphVizEdge(p, name)
    case ControlTree.Throw(exc) =>
      val name = s"Throw $exc (${randomString})"
      Set(toGraphVizEdge(p, name))
    case ControlTree.Fixpoint(b, repeat) => ???
    case ControlTree.Recurrent(failing) =>
      val name = s"Recurrent (${randomString})"
      Set(toGraphVizEdge(p, name))

  private def randomString: String = Random.alphanumeric.take(10).mkString

  private def toGraphVizEdge(n1: String, n2: String): String = s"\"$n1\" -> \"$n2\""

  private def _toEvents(buf: ListBuffer[ControlEvent]): Unit = this match
    case ControlTree.Empty() => ()
    case ControlTree.Atomic(a) =>
      buf += BasicControlEvent.Atomic(a)

    case ControlTree.Seq(a, b) =>
      a._toEvents(buf)
      b._toEvents(buf)

    case ControlTree.Section(section, body) =>
      buf += BasicControlEvent.Begin(section)
      body._toEvents(buf)
      buf += BasicControlEvent.End(section)

    case ControlTree.Fork(b1, b2) =>
      buf += BranchingControlEvent.Fork()
      b1._toEvents(buf)
      buf += BranchingControlEvent.Switch()
      b2._toEvents(buf)
      buf += BranchingControlEvent.Join()

    case ControlTree.Failed() =>
      buf += BasicControlEvent.Failed()

    case ControlTree.Try(body, handle) =>
      buf += ExceptionControlEvent.BeginTry()
      body._toEvents(buf)
      if (handle.nonEmpty)
        buf += ExceptionControlEvent.Catching()

      if (handle.size == 1) {
        handle.foreach { (exc, handler) =>
          buf += ExceptionControlEvent.Handle(exc)
          handler._toEvents(buf)
        }
      }
      else {
        var first = true
        for (_ <- 0 until handle.size - 1)
          buf += BranchingControlEvent.Fork()
        handle.foreach { (exc, handler) =>
          if (!first) buf += BranchingControlEvent.Switch()
          buf += ExceptionControlEvent.Handle(exc)
          handler._toEvents(buf)
          if (!first) buf += BranchingControlEvent.Join()
          first = false
        }
      }


      buf += ExceptionControlEvent.EndTry()

    case ControlTree.Throw(exc) =>
      buf += ExceptionControlEvent.Throw(exc)

    case ControlTree.Fixpoint(b, repeat) =>
      buf += FixpointControlEvent.BeginFixpoint()
      b._toEvents(buf)
      repeat match
        case None => ()
        case Some(repeatCt) =>
          buf += FixpointControlEvent.RepeatFixpoint()
          repeatCt._toEvents(buf)
      buf += FixpointControlEvent.EndFixpoint()

    case ControlTree.Recurrent(failing) =>
      buf += FixpointControlEvent.RecurrentCall(failing)


object ControlTree:

  def buildControlTree[Atom, Sec, Exc](events: List[ControlEvent]): ControlTree[Atom, Sec, Exc] =

    type CT = ControlTree[Atom, Sec, Exc]

    enum Structure:
      case VirtualBlock(program: Option[CT])
      case Block(section: Sec, body: Option[CT])
      case Fork(branch1: Option[CT], branch2: Option[CT], afterSwitch: Boolean)
      case Try(body: Option[CT])
      case Catching(handlers: Map[Exc, Option[CT]], currentHandling: Option[Exc])
      case Handling(body: Option[CT])
      case Fixpoint(body: Option[CT], repeat: Option[CT], afterRepeat: Boolean, ended: Boolean)

    var stack: List[Structure] = List(Structure.VirtualBlock(None))

    inline def concatenate(a: Option[CT], b: CT): Option[CT] = (a, b) match
      case (None, _) => Some(b)
      case (Some(aa), _) => Some(aa + b)

    def addToStack(ct: CT): Unit = stack.head match
      case Structure.VirtualBlock(program) => stack = Structure.VirtualBlock(concatenate(program, ct)) :: stack.tail
      case Structure.Block(section, body) => stack = Structure.Block(section, concatenate(body, ct)) :: stack.tail
      case Structure.Fork(branch1, branch2, afterSwitch) =>
        if (!afterSwitch)
          stack = Structure.Fork(concatenate(branch1, ct), branch2, afterSwitch) :: stack.tail
        else
          stack = Structure.Fork(branch1, concatenate(branch2, ct), afterSwitch) :: stack.tail
      case Structure.Try(body) =>
        stack = Structure.Try(concatenate(body, ct)) :: stack.tail
      case Structure.Catching(_, _) => throw new Exception("...")
      case Structure.Handling(body) => stack = Structure.Handling(concatenate(body, ct)) :: stack.tail
      case Structure.Fixpoint(body, repeat, afterRepeat, ended) =>
        if (afterRepeat)
          stack = Structure.Fixpoint(body, concatenate(repeat, ct), afterRepeat, ended) :: stack.tail
        else
          stack = Structure.Fixpoint(concatenate(body, ct), repeat, afterRepeat, ended) :: stack.tail

    for (ev <- events) {

      stack.head match
        case Structure.Fixpoint(body, repeat, _, true) => ev match
          case RepeatFixpoint() => ()
          case _ =>
            stack = stack.tail
            addToStack(Fixpoint(body.getOrElse(Empty()), repeat))
        case _ => ()

      ev match
        case BasicControlEvent.Atomic(a: Atom) =>
          addToStack(ControlTree.Atomic(a))

        case BasicControlEvent.Failed() =>
          addToStack(ControlTree.Failed())

        case BasicControlEvent.Begin(sec: Sec) =>
          stack = Structure.Block(sec, None) :: stack

        case BasicControlEvent.End(_: Sec) =>
          stack.head match
            case Structure.Block(sec, body) =>
              stack = stack.tail
              addToStack(Section(sec, body.getOrElse(Empty())))
            case _ => throw new Exception("...")

        case BranchingControlEvent.Fork() => stack.head match
          case Structure.Catching(_, _) => ()
          case _ => stack = Structure.Fork(None, None, false) :: stack

        case BranchingControlEvent.Switch() =>
          stack.head match
            case Structure.Fork(branch1, branch2, false) => stack = Structure.Fork(branch1, branch2, true) :: stack.tail
            case Structure.Catching(_, _) => ()
            case Structure.Handling(_) => ()
            case _ => throw new Exception("...")

        case BranchingControlEvent.Join() =>
          stack.head match
            case Structure.Fork(branch1, branch2, true) =>
              stack = stack.tail
              addToStack(Fork(branch1.getOrElse(Empty()), branch2.getOrElse(Empty())))
            case Structure.Catching(_, _) => ()
            case Structure.Handling(_) => ()
            case _ => throw new Exception("...")

        case FixpointControlEvent.BeginFixpoint() =>
          stack = Structure.Fixpoint(None, None, false, false) :: stack

        case FixpointControlEvent.RecurrentCall(failing) =>
          addToStack(Recurrent(failing))

        case FixpointControlEvent.EndFixpoint() => stack.head match
          case Structure.Fixpoint(body, repeat, afterRepeat, _) =>
            stack = Structure.Fixpoint(body, repeat, afterRepeat, true) :: stack.tail
          case _ => throw new Exception("...")

        case FixpointControlEvent.RepeatFixpoint() => stack.head match
          case Structure.Fixpoint(body, repeat, false, true) => stack = Structure.Fixpoint(body, repeat, true, false) :: stack
          case _ => throw new Exception("...")

        case ExceptionControlEvent.BeginTry() =>
          stack = Structure.Try(None) :: stack

        case ExceptionControlEvent.Throw(exc: Exc) =>
          addToStack(ControlTree.Throw(exc))

        case ExceptionControlEvent.Catching() => stack.head match
          case Structure.Try(_) => stack = Structure.Catching(Map.empty, None) :: stack

          case _ => throw new Exception("...")

        case ExceptionControlEvent.Handle(exc: Exc) => stack.head match
          case Structure.Handling(body) =>
            stack = stack.tail
            stack.head match
              case Structure.Catching(handlers, Some(currentHandling)) =>
                stack = Structure.Catching(handlers + (currentHandling -> body), Some(exc)) :: stack.tail
                stack = Structure.Handling(None) :: stack
              case _ => throw new Exception("...")
          case Structure.Catching(handlers, _) =>
            stack = Structure.Catching(handlers, Some(exc)) :: stack.tail
            stack = Structure.Handling(None) :: stack
          case _ => throw new Exception("...")

        case ExceptionControlEvent.EndTry() => stack.take(3) match
          case List(Structure.Handling(body), Structure.Catching(handlers, Some(currentHandling)), Structure.Try(mainBody)) =>
            stack = stack.drop(3)
            addToStack(Try(mainBody.getOrElse(Empty()), handlers.map((k, v) => (k, v.getOrElse(Empty()))) + (currentHandling -> body.getOrElse(Empty()))))
          case List(Structure.Try(mainBody), _, _) =>
            stack = stack.drop(1)
            addToStack(Try(mainBody.getOrElse(Empty()), Map.empty))
          case _ => throw new Exception("...")
    }

    stack.head match
      case Structure.VirtualBlock(program) => program.getOrElse(Empty())
      case _ => throw new Exception("...")