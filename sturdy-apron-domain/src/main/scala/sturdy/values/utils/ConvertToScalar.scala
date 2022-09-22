package sturdy.values.utils

import apron.Interval
import gmp.{Mpfr, Mpq}
import sturdy.values.Topped

def convertToScalarMpq[B: Numeric](using conv: ConvertMpq[B])(from: Interval): Topped[B] =
    if from.isScalar then
      val a = new Mpq()
      from.inf.toMpq(a, 0)
      Topped.Actual(conv(a))
    else
      Topped.Top

def convertToScalarMpfr[B: Numeric](using conv: ConvertMpfr[B])(from: Interval): Topped[B] =
      if from.isScalar then
            val a = new Mpfr()
            from.inf.toMpfr(a, 0)
            Topped.Actual(conv(a))
      else
            Topped.Top
