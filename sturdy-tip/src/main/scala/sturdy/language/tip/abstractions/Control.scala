package sturdy.language.tip.abstractions

import sturdy.effect.EffectStack
import sturdy.effect.except.ObservableExcept
import sturdy.fix
import sturdy.fix.cfg.{ControlFlowGraph, ControlLogger}
import sturdy.language.tip.*
import sturdy.util.Labeled

trait Control extends Interpreter:
  import TipControl.*

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

object TipControl:
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
