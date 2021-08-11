package sturdy.values.conversion

trait ConvertIntDoubleOps[I, D]:
  def doubleToInt(d: D): I
  def doubleToIntUnsigned(d: D): I
  def doubleToIntSaturating(d: D): I
  def doubleToIntSaturatingUnsigned(d: D): I

  def intToDouble(i: I): D
  def intToDoubleUnsigned(i: I): D
