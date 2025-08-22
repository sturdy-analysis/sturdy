package sturdy.data

import sturdy.values.{*, given}

trait LogicalClock[Time]:
  def zero: Time
  def increment(oldTime: Time): Time

trait VectorClock[Key, Time: Join](using logicalClock: LogicalClock[Time]):

  var clocks: Map[Key, Time] = Map()

  final case class Timestamp(timestamp: Map[Key, Time])
  def timestamp: Timestamp = Timestamp(clocks)
  def setTimestamp(newTimestamp: Timestamp) = clocks = newTimestamp.timestamp

  given CombineTimeStamp[W <: Widening](using combineTime: Combine[Time, W]): Combine[Timestamp, W] with
    override def apply(v1: Timestamp, v2: Timestamp): MaybeChanged[Timestamp] =
      Combine(v1.timestamp, v2.timestamp).map(Timestamp(_))

  def increment(key: Key): Unit =
    clocks.get(key) match
      case None          => clocks += key -> logicalClock.zero
      case Some(oldTime) => clocks += key -> logicalClock.increment(oldTime)



