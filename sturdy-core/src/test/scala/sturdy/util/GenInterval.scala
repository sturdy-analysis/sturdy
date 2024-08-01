package sturdy.util

import org.scalacheck.Gen.Choose
import org.scalacheck.{Gen, Shrink}
import sturdy.util.GenFloatSpecials.genFloatSpecials
import sturdy.values.floating.FloatSpecials

import math.Integral.Implicits.infixIntegralOps
import math.Ordering.Implicits.infixOrderingOps

object GenInterval:
  case class Interval[N](low: N, included: N, high: N, floatSpecials: FloatSpecials)

  def genInterval[N: Numeric: Choose](minValue: N, maxValue: N, specials: N*): Gen[Interval[N]] =
    for {
      low <- Gen.chooseNum(minValue, maxValue, specials*)
      high <- Gen.chooseNum(low, maxValue, specials*)
      included <- Gen.chooseNum(low, high, specials*)
      floatSpecials <- genFloatSpecials
    }
    yield Interval(low, included, high, floatSpecials)

  given shrinkInterval[N: Numeric]: Shrink[Interval[N]] = Shrink.xmap[(N, N, N, FloatSpecials), Interval[N]](
    from = (low, included, high, specials) => Interval(low, included, high, specials),
    to = iv => (iv.low, iv.included, iv.high, iv.floatSpecials)
  ).suchThat { case Interval(low, included, high, specials) =>
    low <= included && included <= high
  }
