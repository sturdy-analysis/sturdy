package sturdy.fix.context

import sturdy.effect.TrySturdy
import sturdy.fix.Logger
import sturdy.values.Finite

import scala.util.Try

def callSites[Dom, Call](call: Dom => Option[Call]): CallSiteLogger[Dom, Call] = new CallSiteLogger(call)
class CallSiteLogger[Dom, Call](getCall: Dom => Option[Call]) extends Logger[Dom, Any]:
  private var calls = List[Call]()

  def enter(dom: Dom): Unit = getCall(dom) match
    case Some(c) => calls = c :: calls
    case _ => // nothing

  def exit(dom: Dom, codom: TrySturdy[Any]): Unit = getCall(dom) match
    case Some(c) => calls = calls.tail
    case _ => // nothing

  def callString[In](k: Int) = new Sensitivity[Dom, CallString[Call]] {
    override def emptyContext: CallString[Call] = CallString(List())
    override def switchCall(dom: Dom): Boolean = getCall(dom).isDefined
    override def apply(dom: Dom) = CallString(calls.take(k))
  }

case class CallString[Call](calls: List[Call])
given FiniteCallString[Call]: Finite[CallString[Call]] with {}