package sturdy.utils

import org.scalacheck.{Gen, Shrink}

object GenInterval:
  case class Interval(low: Int, included: Int, high: Int)

  def genInterval(size: Int): Gen[Interval] =
    for {
      low <- Gen.choose(-size, size)
      high <- Gen.choose(low, low + size)
      included <- Gen.choose(low, high)
    }
    yield Interval(low, included, high)

  given shrinkInterval: Shrink[Interval] = Shrink.xmap[(Int, Int, Int), Interval](
    from = (low, included, high) => Interval(low, included, high),
    to = iv => (iv.low, iv.included, iv.high)
  ).suchThat { case Interval(low, included, high) =>
    low <= included && included <= high
  }
