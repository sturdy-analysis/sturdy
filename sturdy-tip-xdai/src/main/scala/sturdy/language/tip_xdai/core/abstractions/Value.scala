package sturdy.language.tip_xdai.core.abstractions

import sturdy.language.tip_xdai.core.Value
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.Topped.Top
import sturdy.values.{Changed, Finite, Join, MaybeChanged, Structural, Topped, Widen}
import sturdy.values.ordering.EqOps


case object TopValue extends Value

// TODO: If we have a boolean extension we should move this
case class BoolValue(b: Boolean) extends Value

object BoolValue:
  def apply(tb: Topped[Boolean]): Value = tb match
    case Topped.Top => TopValue
    case Topped.Actual(b) => new BoolValue(b)

given FiniteValue: Finite[Value] with {}

trait CoreCombine:
  def combine(lhs: Value, rhs: Value): MaybeChanged[Value] = (lhs, rhs) match
    case (TopValue, _) | (_, TopValue) => Unchanged(TopValue)
    case _ => Changed(TopValue)

trait CoreJoin extends CoreCombine, Join[Value]
trait CoreWiden extends CoreCombine, Widen[Value]