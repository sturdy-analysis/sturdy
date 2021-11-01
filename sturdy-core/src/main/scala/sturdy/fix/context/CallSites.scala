package sturdy.fix.context

import sturdy.effect.TrySturdy
import sturdy.fix.Logger
import sturdy.values.Finite

import scala.util.Try

def surroundingCallSites[Dom, Call](call: Dom => Option[Call]): SurroundingCallSiteLogger[Dom, Call] = new SurroundingCallSiteLogger(call)
class SurroundingCallSiteLogger[Dom, Call](getCall: Dom => Option[Call]) extends Logger[Dom, Any]:
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


def previousCallSites[Dom, Call](k: Int)(call: Dom => Option[Call]): PreviousCallSiteLogger[Dom, Call] = new PreviousCallSiteLogger(k, call)
class PreviousCallSiteLogger[Dom, Call](k: Int, getCall: Dom => Option[Call]) extends Logger[Dom, Any]:
  private var calls = Vector[Call]()

  def enter(dom: Dom): Unit = getCall(dom) match
    case Some(c) =>
      if (calls.size < k)
        calls = calls :+ c
      else
        calls = calls.init :+ c
    case _ => // nothing

  def exit(dom: Dom, codom: TrySturdy[Any]): Unit = {}

  def callString[In] = new Sensitivity[Dom, CallString[Call]] {
    override def emptyContext: CallString[Call] = CallString(Vector())
    override def switchCall(dom: Dom): Boolean = getCall(dom).isDefined
    override def apply(dom: Dom) = CallString(calls)
  }


case class CallString[Call](calls: Seq[Call])
given FiniteCallString[Call]: Finite[CallString[Call]] with {}