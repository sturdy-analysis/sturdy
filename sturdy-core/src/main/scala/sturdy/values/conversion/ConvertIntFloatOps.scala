package sturdy.values.conversion

trait ConvertIntFloatOps[I, F]:
  def floatToInt(f: F): I
  def floatToIntUnsigned(f: F): I
  def floatToRawInt(f: F): I
  def floatToIntSaturating(f: F): I
  def floatToIntSaturatingUnsigned(f: F): I

  def intToFloat(i: I): F
  def intToFloatUnsigned(i: I): F
  def intToRawFloat(i: I): F