package sturdy.values.conversion

trait ConvertIntLongOps[I, L]:
  def longToInt(l: L): I
  def intToLong(i: I): L
  def intToLongUnsigned(i: I): L
