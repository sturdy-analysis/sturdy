package sturdy.language.tip_xdai.core.abstr

import sturdy.language.tip_xdai.core.Value
import sturdy.values.{Structural, Topped}
import sturdy.values.ordering.EqOps


case object TopValue extends Value


/*trait CoreEqOps extends EqOps[Value, Topped[Boolean]]:
  override def equ(v1: Value, v2: Value): Topped[Boolean] = (v1, v2) match
    case _ => Topped.Top
  override def neq(v1: Value, v2: Value): Topped[Boolean] = (v1, v2) match
    case _ => Topped.Top


trait CoreCombineV:
  def combine(lhs: Value, rhs: Value): Value = (lhs, rhs) match
    case _ => Value.Top

trait CoreJoinV extends CoreCombineV
trait CoreWidenV extends CoreCombineV*/