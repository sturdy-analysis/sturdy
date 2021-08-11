package sturdy.values.conversion

trait ConvertLongDoubleOps[L, D]:
  def doubleToLong(d: D): L
  def doubleToLongUnsigned(d: D): L
  def doubleToRawLong(d: D): L
  def doubleToLongSaturating(d: D): L
  def doubleToLongSaturatingUnsigned(d: D): L

  def longToDouble(l: L): D
  def longToDoubleUnsigned(l: L): D
  def longToRawDoulbe(l: L): D
