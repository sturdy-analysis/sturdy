package sturdy.language.tip_xdai.core

import sturdy.language.tip_xdai.core.abstractions.TopValue
import sturdy.values.ordering.EqOps

trait Value

trait CoreEqOps[B, V] extends EqOps[Value, Value]:
  def boolToValue(b: B): V