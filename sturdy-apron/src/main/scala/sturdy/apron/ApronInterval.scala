package sturdy.apron

import apron.{Coeff, Interval}
import gmp.Mpfr
import sturdy.values.integer.IntervalRange
import sturdy.values.{Join, MaybeChanged, Top}


given Join[Coeff] = (c1,c2) =>
  val inf1 = c1.inf()
  val inf2 = c2.inf()
  val newInf =
    if(inf1.cmp(inf2) <= 0)
      inf1
    else
      inf2

  val sup1 = c1.sup()
  val sup2 = c2.sup()
  val newSup =
    if(sup1.cmp(sup2) <= 0)
      sup2
    else
      sup1

  val upperBound = apron.Interval(newInf, newSup)

  MaybeChanged(upperBound, ! (upperBound.isEqual(c1) || upperBound.isEqual(c2)))

given Top[Interval] with
  def top: Interval =
    val iv = Interval()
    iv.setTop()
    iv

given IntervalRange[Interval] =
  (iv: Interval) =>
    val maybeLower =
      if (iv.inf().isInfty() != 0)
        None
      else
        val mpq = gmp.Mpq()
        iv.inf().toMpq(mpq, Mpfr.RNDD)
        try {
          Some(mpq.getNum.bigIntegerValue().intValueExact())
        } catch {
          case exc: ArithmeticException => None
        }

    val maybeUpper =
      if (iv.sup().isInfty() != 0)
        None
      else
        val mpq = gmp.Mpq()
        iv.inf().toMpq(mpq, Mpfr.RNDU)
        try {
          Some(mpq.getNum.bigIntegerValue().intValueExact())
        } catch {
          case exc: ArithmeticException => None
        }

    for(lower <- maybeLower;
        upper <- maybeUpper)
      yield(Range.inclusive(lower,upper))