package sturdy.util

trait IsInterval[L, IV]:
  def constant(i: L): IV

  def interval(low: L, high: L): IV