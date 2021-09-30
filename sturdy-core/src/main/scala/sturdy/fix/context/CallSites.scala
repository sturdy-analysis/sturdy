package sturdy.fix.context

import sturdy.fix.Logger

import scala.util.Try

def callSites[Dom, Call](call: Dom => Option[Call]): CallSiteLogger[Dom, Call] = new CallSiteLogger(call)
class CallSiteLogger[Dom, Call](getCall: Dom => Option[Call]) extends Logger[Dom, Any]:
  private var calls = List[Call]()

  def enter(dom: Dom): Unit = getCall(dom) match
    case Some(c) => calls = c :: calls
    case _ => // nothing

  def exit(dom: Dom, codom: Try[Any]): Unit = getCall(dom) match
    case Some(c) => calls = calls.tail
    case _ => // nothing

  def callString[In](k: Int) = new Sensitivity[Dom, List[Call]] {
    override def emptyContext: List[Call] = List()
    override def switchCall(dom: Dom): Boolean = getCall(dom).isDefined
    override def apply(dom: Dom) = calls.take(k)
  }
