package sturdy.language.tip_xdai.core.sign

import sturdy.language.tip_xdai.core.{CoreEqOps, Function, StructuralFunction, Value}
import sturdy.language.tip_xdai.core.abstractions.CoreJoin
import sturdy.values.{JoinPowerset, MaybeChanged, Powerset, Structural, Topped, powersetCertainEqualOps}
import sturdy.values.ordering.EqOps
import sturdy.values.ordering.StructuralEqOps
import sturdy.language.tip_xdai.core.abstractions.{BoolValue, TopValue}

case class PSetFunValue(f: Powerset[Function]) extends Value:
  override def toString: String = f.toString

trait SignEqOps extends CoreEqOps[Topped[Boolean], Value]:
  override def boolToValue(b: Topped[Boolean]): Value = BoolValue(b)

  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (PSetFunValue(f1), PSetFunValue(f2)) => boolToValue(powersetCertainEqualOps.equ(f1, f2))
    case _ => TopValue

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (PSetFunValue(f1), PSetFunValue(f2)) => boolToValue(powersetCertainEqualOps.neq(f1, f2))
    case _ => TopValue

trait SignJoin extends CoreJoin:
  override def combine(lhs: Value, rhs: Value): MaybeChanged[Value] = (lhs, rhs) match
    case (PSetFunValue(f1), PSetFunValue(f2)) => JoinPowerset(f1, f2).map(PSetFunValue.apply)
    case _ => super.combine(lhs, rhs)

  override def apply(v1: Value, v2: Value): MaybeChanged[Value] = combine(v1, v2)