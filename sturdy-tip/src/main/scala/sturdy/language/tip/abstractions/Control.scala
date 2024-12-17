package sturdy.language.tip.abstractions

import sturdy.gradual.fix.{Check, GradualLogger}
import sturdy.control.{BasicControlEvent, ControlEvent, ControlObservable}
import sturdy.effect.{EffectStack, TrySturdy}
import sturdy.effect.except.ObservableExcept
import sturdy.fix
import sturdy.fix.Logger
import sturdy.fix.cfg.{ControlFlowGraph, ControlLogger}
import sturdy.gradual.GradualLoggerOps
import sturdy.language.tip.*
import sturdy.util.{Label, Labeled}
import sturdy.values.PartialOrder
import sturdy.values.booleans.ObservedBooleanBranching

import scala.collection.mutable.ListBuffer

object Control:
  type Atom = Stm
  type Section = Function | Exp.Call | Stm.If
  type Exc = Unit
  type Fx = (FixIn, List[Any])

  def getInNode(l: Labeled | Function): Option[TipControlNode] = l match
    case f: Function => Some(TipControlNode.Enter(f.name))
    case c: Exp.Call => Some(TipControlNode.Call(c))
    case _: (Stm.Block | Stm.Assign) => None
    case s: Stm => Some(TipControlNode.Stm(s))
    //      case e: (Exp.Alloc | Exp.Deref) => Some(TipControlNode.MemExp(e))
    case _ => None

  def getOutNode(l: Labeled | Function): Option[TipControlNode] = l match
    case f: Function => Some(TipControlNode.Exit(f.name))
    case c: Exp.Call => Some(TipControlNode.CallReturn(TipControlNode.Call(c)))
    case a: Stm.Assign => Some(TipControlNode.Stm(a))
    case _ => None

class TipGradualLogger[T,V] extends GradualLogger[T, FixIn, FixOut[V]]:
  var current: ListBuffer[FixIn] = ListBuffer[FixIn]()
  val m = scala.collection.mutable.Map[Label, List[Check[T]]]()

  override def enter(dom: FixIn): Unit =
    current += dom

    dom match
      case FixIn.Eval(e) => println(s"Eval $e"); e.label
      case FixIn.Run(s) => println(s"Run $s")
      case FixIn.EnterFunction(f) => println(s"Enter $f")


  override def exit(dom: FixIn, codom: TrySturdy[FixOut[V]]): Unit =
    //remove element from mutable list
    current.dropRightInPlace(1)

    dom match
      case FixIn.Eval(e) => println(s"Eval $e -> $codom")
      case FixIn.Run(s) => println(s"Run $s -> $codom")
      case FixIn.EnterFunction(f) => println(s"Enter $f -> $codom")

  override def insertCheck(uv: T, v: T): Unit = current.last match
    case FixIn.Eval(e) =>
      m += (e.label -> (m.getOrElse(e.label, Nil) :+ Check(uv, v)))
      println(s"inserting check at ${e.label}")
    case FixIn.Run(s) =>
      m += (s.label -> (m.getOrElse(s.label, Nil) :+ Check(uv, v)))
      println(s"inserting check at ${s.label}")
    case FixIn.EnterFunction(f) =>
      println(s"What to do here? $f")

  override def getCheck(l: Label): Option[Check[T]] = m.get(l).flatMap(_.headOption)

type TipGradualOps[T, V] = GradualLoggerOps[T, FixIn, FixOut[V]]

trait Control extends Interpreter:
  import Control.*

  def controlEventLogger(observable: ControlObservable[Atom, Section, Exc, Fx])(using effects: EffectStack): Logger[FixIn, FixOut[Value]] =
    effects.addJoinObserver(observable)
    new Logger[FixIn, FixOut[Value]]:
      //var vurrent = _


      override def enter(dom: FixIn): Unit = dom match
        case FixIn.EnterFunction(f) => observable.triggerControlEvent(BasicControlEvent.BeginSection(f))
        case FixIn.Eval(c: Exp.Call) => observable.triggerControlEvent(BasicControlEvent.BeginSection(c))
        case FixIn.Run(s: Stm.If) => observable.triggerControlEvent(BasicControlEvent.BeginSection(s))
        case FixIn.Run(s: Stm.While) => observable.triggerControlEvent(BasicControlEvent.Atomic(s))
        case FixIn.Run(s: (Stm.Assign | Stm.Output)) => observable.triggerControlEvent(BasicControlEvent.Atomic(s))
        case _ => // nothing

      override def exit(dom: FixIn, codom: TrySturdy[FixOut[Value]]): Unit = dom match
        case FixIn.EnterFunction(f) => observable.triggerControlEvent(BasicControlEvent.EndSection())
        case FixIn.Eval(c: Exp.Call) => observable.triggerControlEvent(BasicControlEvent.EndSection())
        case FixIn.Run(s: Stm.If) => observable.triggerControlEvent(BasicControlEvent.EndSection())
        case _ => // nothing



  def controlLogger[Ctx](ctxSensitive: Boolean)(using effects: EffectStack)
             : ControlLogger[Ctx, FixIn, FixOut[Value], Nothing, TipControlNode] =
    fix.control[Ctx, FixIn, FixOut[Value], Nothing, TipControlNode](ctxSensitive, TipControlNode.Start)
      {
        case FixIn.Eval(e) => getInNode(e)
        case FixIn.Run(s) => getInNode(s)
        case FixIn.EnterFunction(f) => getInNode(f)
      }
      {
        case (FixIn.Eval(e),_) => getOutNode(e)
        case (FixIn.Run(s),_) => getOutNode(s)
        case (FixIn.EnterFunction(f),_) => getOutNode(f)
      }
      (using effects, ObservableExcept.None)



  def gradualLogger[T, V](): TipGradualLogger[T, V] = new TipGradualLogger()






enum TipControlNode extends ControlFlowGraph.Node:
  case Start
  case Enter(fun: String)
  case Exit(fun: String)
  case Call(c: Exp.Call)
  case CallReturn(from: TipControlNode.Call)
  case Stm(s: sturdy.language.tip.Stm)
  case MemExp(e: Exp.Alloc | Exp.Deref)

  override def isStartNode: Boolean = this == Start
  override def isImportantControlNode: Boolean = this match
    case Enter(_) | Exit(_) => true
    case _ => false
  override def getBeginNode: Option[ControlFlowGraph.Node] = this match
    case CallReturn(c) => Some(c)
    case _ => None

  override def toString: String = this match
    case Start => "Start"
    case Enter(f) => s"enter $f"
    case Exit(f) => s"exit $f"
    case Call(c) => c.toString
    case CallReturn(c) => "return " + c.toString
    case MemExp(e) => e.toString
    case Stm(s) => s.toString
