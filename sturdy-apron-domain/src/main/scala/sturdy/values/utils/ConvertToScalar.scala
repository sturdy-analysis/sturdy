package sturdy.values.utils

import apron.Interval
import gmp.Mpq
import sturdy.values.Topped

def convertToScalar[B: Numeric](using conv: ConvertMpq[B])(from: Interval): Topped[B] =
    if from.isScalar then
      val a = new Mpq()
      from.inf.toMpq(a, 0)
      Topped.Actual(conv(a))
    else
      Topped.Top

