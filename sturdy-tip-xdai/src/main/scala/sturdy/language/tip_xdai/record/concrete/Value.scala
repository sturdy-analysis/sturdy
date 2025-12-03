package sturdy.language.tip_xdai.record.concrete

import sturdy.effect.except.Except
import sturdy.language.tip_xdai.core.{CoreEqOps, CoreJoinV, Value}
import sturdy.values.{Finite, Structural, Topped}
import sturdy.language.tip_xdai.record.Field

case class RecordV(value: Map[Field, Value]) extends Value:
  override def toString: String = value.toString

given Structural[RecordV] with {}


