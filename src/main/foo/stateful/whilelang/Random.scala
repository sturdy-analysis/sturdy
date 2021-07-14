package stateful.whilelang

trait Random[V] {
  def randomNum(): V
}

trait RandomImpl extends Random[ValImpl.Value] {
  import ValImpl._
  override def randomNum(): Value = DoubleValue(util.Random.nextDouble())
}

trait RandomAbs extends Random[ValAbs.Value] {
  import ValAbs._
  override def randomNum(): Value = NumValue(Actual(Interval(0, 1)))
}