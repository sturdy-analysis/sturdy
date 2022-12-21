package sturdy.effect.assert

import sturdy.effect.Effect

trait Assert[A, -Context] extends Effect:
  def apply(a: A, ctx: Context): Unit
