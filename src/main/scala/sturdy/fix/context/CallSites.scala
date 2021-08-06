package sturdy.fix.context

import sturdy.fix.Logger

def callSites[Dom, Call](call: Dom => Option[Call]): CallSiteLogger[Dom, Call] = new CallSiteLogger(call)
class CallSiteLogger[Dom, Call](getCall: Dom => Option[Call]) extends Logger[Dom]:
  private var calls = List[Call]()

  def enter(d: Dom): Unit = getCall(d) match
    case Some(c) => calls = c :: calls
    case _ => // nothing

  def exit(d: Dom): Unit = getCall(d) match
    case Some(c) => calls = calls.tail
    case _ => // nothing

  def callString[In](k: Int) = new Sensitivity[Dom, List[Call]] {
    override def emptyContext: List[Call] = List()
    override def apply(dom: Dom) = Some(calls.take(k))
  }
