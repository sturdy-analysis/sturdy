package sturdy.language.pcf

import sturdy.control.{BasicControlEvent, ControlObservable}
import sturdy.effect.{EffectStack, TrySturdy}
import sturdy.fix.Logger
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.language
import sturdy.language.pcf
import sturdy.language.pcf.FixIn.{Enter, Eval}
import Exp.*


object Control {
  type Atom = Exp
  type Section = Exp
  type Exc = Unit
  type Fx = (FixIn, List[Any])

  import BasicControlEvent.*

  def triggerEventsIn(observable: ControlObservable[Atom, Section, Exc, Fx])(e: Exp): Unit = e match
    case Var(name) => observable.triggerControlEvent(BeginSection(e)(s"var($name) " ++ e.label.toString))
    case Num(n) => observable.triggerControlEvent(Atomic(e)(s"num($n)" ++ e.label.toString))
    case BinOpApp(op, _, _) => observable.triggerControlEvent(BeginSection(e)(op.toString ++ e.label.toString))
    case Read => observable.triggerControlEvent(BasicControlEvent.Atomic(e)("read " ++ e.label.toString))
    case If(_, _, _) => observable.triggerControlEvent(BasicControlEvent.BeginSection(e)("if " ++ e.label.toString))
    case Lam(x, _) => observable.triggerControlEvent(Atomic(e)(s"lambda($x, ...)" ++ e.label.toString))
    case App(fun, _) => observable.triggerControlEvent(BasicControlEvent.BeginSection(e)("app " ++ fun.toString ++ e.label.toString))
    case Rec(_, _) => ()

  def triggerEventsOut(observable: ControlObservable[Atom, Section, Exc, Fx])(e: Exp): Unit = e match
    case Var(_) => observable.triggerControlEvent(EndSection())
    case Num(_) => ()
    case BinOpApp(_, _, _) => observable.triggerControlEvent(EndSection())
    case Read => ()
    case If(_, _, _) => observable.triggerControlEvent(EndSection())
    case Lam(_, _) => ()
    case App(_, _) => observable.triggerControlEvent(EndSection())
    case Rec(_, _) => ()
}

trait Control extends Interpreter:
  import Control.*

  def controlEventLogger(observable: ControlObservable[Atom, Section, Exc, Fx])(using effects: EffectStack): Logger[FixIn, Value] =
    effects.addJoinObserver(observable)
    new Logger:
      override def enter(dom: FixIn): Unit = dom match
        case Enter(e : Exp) => triggerEventsIn(observable)(e)
        case Eval(e : Exp) => triggerEventsIn(observable)(e)

      override def exit(dom: FixIn, codom: TrySturdy[Value]): Unit = dom match
        case Enter(e : Exp) => triggerEventsOut(observable)(e)
        case Eval(e : Exp) => triggerEventsOut(observable)(e)
