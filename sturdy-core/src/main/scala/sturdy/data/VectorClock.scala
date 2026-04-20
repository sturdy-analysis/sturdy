package sturdy.data

import sturdy.values.{*, given}

import scala.collection.immutable.HashMap

trait VectorClock[Key: Finite]:
  var clocks: HashMap[Key, Int] = HashMap.empty

  final case class Timestamp(timestamp: HashMap[Key, Int]):
    override def toString: String = s"t${timestamp.values.sum}"
  def timestamp: Timestamp = Timestamp(clocks)
  def setTimestamp(newTimestamp: Timestamp): Unit = clocks = newTimestamp.timestamp

  given JoinTimeStamp: Join[Timestamp] with
    override def apply(v1: Timestamp, v2: Timestamp): MaybeChanged[Timestamp] =
      given Join[Int] = (x,y) => MaybeChanged(math.max(x,y), x < y)
      Join(v1.timestamp, v2.timestamp).map(Timestamp(_))

  given PartialOrderTimeStamp: PartialOrder[Timestamp] with
    override def lteq(x: Timestamp, y: Timestamp): Boolean =
      val xs = x.timestamp; val ys = y.timestamp
      xs.forall((k, xTime) =>
        ys.get(k).exists(xTime <= _)
      )

  def increment(key: Key): Unit =
    clocks.get(key) match
      case None          => clocks += key -> 1
      case Some(oldTime) => clocks += key -> (oldTime + 1)