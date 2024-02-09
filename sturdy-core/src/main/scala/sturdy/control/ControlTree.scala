package sturdy.control

import sturdy.control.FixpointControlEvent.RepeatFixpoint

import java.util.Optional
import scala.annotation.targetName
import scala.collection.mutable.ListBuffer

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
        handle.foreach { (exc, handler) =>
          buf += ExceptionControlEvent.Handle(exc)
          handler._toEvents(buf)
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
      case Try(body: Option[CT], handlers: Map[Exc, Option[CT]], currentHandling: Option[Exc])
      case Fixpoint(body: Option[CT], repeat: Option[CT], afterRepeat: Boolean, ended: Boolean)

    var stack: List[Structure] = List(Structure.VirtualBlock(None))

    inline def concatenate(a: Option[CT], b: CT): Option[CT] = (a, b) match
      case (None, _) => Some(b)
      case (Some(aa), _) => Some(aa + b)

    def addToStack(ct: CT): Unit =
      stack = (stack.head match
        case Structure.VirtualBlock(program) => Structure.VirtualBlock(concatenate(program, ct))
        case Structure.Block(section, body) => Structure.Block(section, concatenate(body, ct))
        case Structure.Fork(branch1, branch2, afterSwitch) =>
          if (afterSwitch)
            Structure.Fork(concatenate(branch1, ct), branch2, afterSwitch)
          else
            Structure.Fork(branch1, concatenate(branch2, ct), afterSwitch)
        case Structure.Try(body, handlers, currentHandling) => currentHandling match
          case None => Structure.Try(concatenate(body, ct), handlers, currentHandling)
          case Some(exc) =>
            Structure.Try(body, handlers + (exc -> concatenate(handlers(exc), ct)), currentHandling)
        case Structure.Fixpoint(body, repeat, afterRepeat, ended) =>
          if (afterRepeat)
            Structure.Fixpoint(body, concatenate(repeat, ct), afterRepeat, ended)
          else
            Structure.Fixpoint(concatenate(body, ct), repeat, afterRepeat, ended)) :: stack.tail

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

        case BranchingControlEvent.Fork() =>
          stack = Structure.Fork(None, None, false) :: stack
        case BranchingControlEvent.Switch() =>
          stack.head match
            case Structure.Fork(branch1, branch2, false) => stack = Structure.Fork(branch1, branch2, true) :: stack.tail
            case _ => throw new Exception("...")
        case BranchingControlEvent.Join() =>
          stack.head match
            case Structure.Fork(branch1, branch2, true) =>
              stack = stack.tail
              addToStack(Fork(branch1.getOrElse(Empty()), branch2.getOrElse(Empty())))
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
          stack = Structure.Try(None, Map.empty, None) :: stack
        case ExceptionControlEvent.Throw(exc: Exc) =>
          addToStack(ControlTree.Throw(exc))
        case ExceptionControlEvent.Catching() => ()
        case ExceptionControlEvent.Handle(exc: Exc) => stack.head match
          case Structure.Try(body, handlers, _) =>
            stack = Structure.Try(body, handlers, Some(exc)) :: stack.tail
          case _ => throw new Exception("...")
        case ExceptionControlEvent.EndTry() => stack.head match
          case Structure.Try(body, handlers, _) =>
            stack = stack.tail
            addToStack(Try(body.getOrElse(Empty()), handlers.map((k, v) => (k, v.getOrElse(Empty())))))
          case _ => throw new Exception("...")
    }

    stack.head match
      case Structure.VirtualBlock(program) => program.getOrElse(Empty())
      case _ => throw new Exception("...")