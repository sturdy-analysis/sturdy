package sturdy.values.void

import sturdy.effect.failure.Failure

import scala.util.Random

trait VoidOps[V]:
  def void(): V
