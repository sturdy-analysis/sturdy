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
