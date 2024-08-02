package sturdy.util

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen, Shrink}
import sturdy.util.GenFloatSpecials.genFloatSpecials
import sturdy.util.GenInterval.Interval
import sturdy.values.floating.FloatSpecials

import math.Integral.Implicits.infixIntegralOps
import math.Ordering.Implicits.infixOrderingOps

object GenInterval:
  case class Interval[N](low: N, included: N, high: N, floatSpecials: FloatSpecials)

  def genInterval[N: Numeric: Choose](minValue: N, maxValue: N, specials: N*): Gen[Interval[N]] =
    val negInfinity = Numeric[N].parseString(Double.NegativeInfinity.toString)
    val posInfinity = Numeric[N].parseString(Double.PositiveInfinity.toString)
    val negZero = Numeric[N].parseString((-0.0d).toString)
    val nan = Numeric[N].parseString(Double.NaN.toString)
    val isFloat = negInfinity.isDefined && posInfinity.isDefined && negZero.isDefined && nan.isDefined
    for {
      included <-
        if(isFloat)
          Gen.oneOf(
            Gen.chooseNum(minValue, maxValue, specials*),
            Gen.oneOf(negInfinity.get, posInfinity.get, negZero.get, nan.get)
          )
        else
          Gen.chooseNum(minValue, maxValue)
      d = Numeric[N].toDouble(included)
      low <-
        if(d.isInfinity || d.isNaN)
          Gen.chooseNum(minValue, maxValue, specials*)
        else
          Gen.chooseNum(minValue, included, specials*)
      high <-
        if(d.isInfinity || d.isNaN)
          Gen.chooseNum(low, maxValue, specials*)
        else
          Gen.chooseNum(included, maxValue, specials*)
      specials <- genFloatSpecials(d)
    }
    yield Interval(low, included, high, specials)


  given shrinkInterval[N: Numeric]: Shrink[Interval[N]] = Shrink.xmap[(N, N, N, FloatSpecials), Interval[N]](
    from = (low, included, high, specials) => Interval(low, included, high, specials),
    to = iv => (iv.low, iv.included, iv.high, iv.floatSpecials)
  ).suchThat { case Interval(low, included, high, specials) =>
    low <= included && included <= high
  }
