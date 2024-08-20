package sturdy.apron

import apron.*
import gmp.Mpfr
import sturdy.values.floating.FloatSpecials
import sturdy.values.integer.IntervalRange
import sturdy.values.{*,given}

given intervalOrdering: PartialOrder[Interval] with
  override def lteq(iv1: Interval, iv2: Interval): Boolean = iv1.isLeq(iv2)

given JoinInterval: Join[Interval] with
  def apply(iv1: Interval, iv2: Interval): MaybeChanged[Interval] =
    val inf1 = iv1.inf()
    val inf2 = iv2.inf()
    val newInf =
      if(inf1.cmp(inf2) <= 0)
        inf1
      else
        inf2

    val sup1 = iv1.sup()
    val sup2 = iv2.sup()
    val newSup =
      if(sup1.cmp(sup2) <= 0)
        sup2
      else
        sup1

    val joined = apron.Interval(newInf, newSup)

    MaybeChanged(joined, ! joined.isLeq(iv1))

given WidenInterval: Widen[Interval] with
  def apply(iv1: Interval, iv2: Interval): MaybeChanged[Interval] =
    val inf1 = iv1.inf()
    val inf2 = iv2.inf()
    val newInf =
      if (inf1.cmp(inf2) <= 0)
        inf1
      else
        DoubleScalar(Double.NegativeInfinity)

    val sup1 = iv1.sup()
    val sup2 = iv2.sup()
    val newSup =
      if (sup2.cmp(sup1) <= 0)
        sup1
      else
        DoubleScalar(Double.PositiveInfinity)

    val joined = apron.Interval(newInf, newSup)

    MaybeChanged(joined, !joined.isLeq(iv1))

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