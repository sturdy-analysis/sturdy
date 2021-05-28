package stateful.whilelang

import scala.util.Random

trait Random[V] {
  def randomNum(): V
}

trait RandomImpl extends Random[ValImpl.Value] {
  import ValImpl._
  override def randomNum(): Value = DoubleValue(Random.nextDouble())
}

trait RandomAbs extends Random[ValAbs.Value] {
  import ValAbs._
  override def randomNum(): Value = NumValue(None)
}