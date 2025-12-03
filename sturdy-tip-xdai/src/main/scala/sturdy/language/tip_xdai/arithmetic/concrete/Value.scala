package sturdy.language.tip_xdai.arithmetic.concrete

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.{CoreEqOps, CoreJoinV, Value}
import sturdy.values.{Structural, Topped}

case class IntValue(value: Int) extends Value:
  override def toString: String = value.toString

given Structural[IntValue] with {}


