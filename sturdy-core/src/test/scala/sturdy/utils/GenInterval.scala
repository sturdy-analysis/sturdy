package sturdy.utils

import org.scalacheck.Gen.Choose
import org.scalacheck.{Gen, Shrink}

import math.Integral.Implicits.infixIntegralOps
import math.Ordering.Implicits.infixOrderingOps

object GenInterval:
  case class Interval[N](low: N, included: N, high: N)

  def genInterval[N: Integral: Choose](minValue: N, maxValue: N): Gen[Interval[N]] =
    for {
      low <- Gen.chooseNum(minValue, maxValue)
      high <- Gen.chooseNum(low, maxValue)
      included <- Gen.chooseNum(low, high)
    }
    yield Interval(low, included, high)

  given shrinkInterval[N: Integral: Ordering]: Shrink[Interval[N]] = Shrink.xmap[(N, N, N), Interval[N]](
    from = (low, included, high) => Interval(low, included, high),
    to = iv => (iv.low, iv.included, iv.high)
  ).suchThat { case Interval(low, included, high) =>
    low <= included && included <= high
  }
