package sturdy.values.integer

import sturdy.values.Topped

trait IntervalRange[IV]:
  def range(iv: IV): Option[Range]
  def fromInt(l: Int, h: Int): IV
  def fromTop(t: Topped[Int]): IV =
    t match
      case Topped.Actual(x) => fromInt(x, x)
      case Topped.Top => fromInt(Int.MinValue, Int.MaxValue)

object IntervalRange:
  def apply[IV: IntervalRange]: IntervalRange[IV] = summon[IntervalRange[IV]]