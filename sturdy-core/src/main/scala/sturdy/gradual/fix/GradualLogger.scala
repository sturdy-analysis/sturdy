package sturdy.gradual.fix

import sturdy.effect.TrySturdy
import sturdy.fix.Logger
import sturdy.util.Label

class Check[T](val unsafe: T, val safe: T)
trait GradualLogger[T, -Dom, -Codom] extends Logger[Dom, Codom]:
  def insertCheck(uv: T, v: T): Unit
  def getCheck(l: Label): Option[Check[T]]
