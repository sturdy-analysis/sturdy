package sturdy.apron

import apron.{Coeff, Interval}
import gmp.Mpfr
import sturdy.values.integer.IntervalRange
import sturdy.values.{Join, MaybeChanged, Top}

object IntervalLattice:
  def meet(iv1: Interval, iv2: Interval): Interval =
    val res = Interval()
    if (iv1.sup.cmp(iv2.inf) < 0 || iv2.sup.cmp(iv1.inf) < 0) // no overlap
      res.setBottom()
    else
      if (iv1.inf.cmp(iv2.inf) >= 0)
        res.setInf(iv1.inf)
      else
        res.setInf(iv2.inf)
      if (iv1.sup.cmp(iv2.sup) <= 0)
        res.setSup(iv1.sup)
      else
        res.setSup(iv2.sup)
    res

given joinInterval: Join[Interval] = joinCoeff.apply(_,_)

given joinCoeff: Join[Coeff] with
  def apply(c1: Coeff, c2: Coeff): MaybeChanged[Interval] =
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
        iv.sup().toMpq(mpq, Mpfr.RNDU)
        try {
          Some(mpq.getNum.bigIntegerValue().intValueExact())
        } catch {
          case exc: ArithmeticException => None
        }

    for(lower <- maybeLower;
        upper <- maybeUpper)
      yield(Range.inclusive(lower,upper))