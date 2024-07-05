package sturdy.utils

import org.scalacheck.Gen.Choose
import org.scalacheck.{Gen, Shrink}

import math.Integral.Implicits.infixIntegralOps
import math.Ordering.Implicits.infixOrderingOps

object GenInterval:
  case class Interval[N](low: N, included: N, high: N)

  def genInterval[N: Numeric: Choose](minValue: N, maxValue: N, specials: N*): Gen[Interval[N]] =
    for {
      low <- Gen.chooseNum(minValue, maxValue, specials*)
      high <- Gen.chooseNum(low, maxValue, specials*)
      included <- Gen.chooseNum(low, high, specials*)
    }
    yield Interval(low, included, high)

  given shrinkInterval[N: Numeric]: Shrink[Interval[N]] = Shrink.xmap[(N, N, N), Interval[N]](
    from = (low, included, high) => Interval(low, included, high),
    to = iv => (iv.low, iv.included, iv.high)
  ).suchThat { case Interval(low, included, high) =>
    low <= included && included <= high
  }
