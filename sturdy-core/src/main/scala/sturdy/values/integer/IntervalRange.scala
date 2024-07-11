package sturdy.values.integer

trait IntervalRange[IV]:
  def range(iv: IV): Option[Range]

object IntervalRange:
  def apply[IV: IntervalRange](iv: IV): Option[Range] = summon[IntervalRange[IV]].range(iv)