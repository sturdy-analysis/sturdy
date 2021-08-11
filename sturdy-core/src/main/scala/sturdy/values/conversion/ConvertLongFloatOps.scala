package sturdy.values.conversion

trait ConvertLongFloatOps[L, F]:
  def floatToLong(f: F): L
  def floatToLongUnsigned(f: F): L
  def floatToLongSaturating(f: F): L
  def floatToLongSaturatingUnsigned(f: F): L

  def longToFloat(l: L): F
  def longToFloatUnsigned(l: L): F
