package sturdy.util

import sturdy.values.floating.FloatSpecials

trait IsInterval[L, IV]:
  def constant(i: L): IV
  def interval(low: L, high: L, floatSpecials: FloatSpecials = FloatSpecials.Bottom): IV