package sturdy.fix

import sturdy.effect.AnalysisState
import sturdy.values.JoinValue

trait ContextSensitive[Dom, In, Ctx] extends Function2[Dom, In, Ctx]

object ContextSensitive:
  def full[Dom, In] = new ContextSensitive[Dom, In, (Dom,In)] {
    override def apply(dom: Dom, in: In): (Dom, In) = (dom,in)
  }

  def none[Dom, In] = new ContextSensitive[Dom, In, Dom] {
    override def apply(dom: Dom, in: In): Dom = dom
  }

  def callSites[Dom, Call](call: Dom => Option[Call]): CallSiteLogger[Dom, Call] = new CallSiteLogger(call)
  class CallSiteLogger[Dom, Call](getCall: Dom => Option[Call]) extends Logger[Dom]:
    private var calls = List[Call]()
    def enter(d: Dom): Unit = getCall(d) match
      case Some(c) => calls = c :: calls
      case _ => // nothing
    def exit(d: Dom): Unit = getCall(d) match
      case Some(c) => calls = calls.tail
      case _ => // nothing
    def callString[In](k: Int) = new ContextSensitive[Dom, In, (Dom, List[Call])] {
      override def apply(dom: Dom, in: In): (Dom, List[Call]) = (dom, calls.take(k))
    }


