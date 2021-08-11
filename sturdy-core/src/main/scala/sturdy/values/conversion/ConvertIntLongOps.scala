package sturdy.values.conversion

trait ConvertIntLongOps[I, L]:
  def longToInt(l: L): I
  def intToLong(i: I): L
  def intToLongUnsigned(i: I): L

given concreteConvertIntLongOps: ConvertIntLongOps[Int, Long] with
  def longToInt(l: Long): Int = (l % (1L << 32)).toInt
  def intToLong(i: Int): Long = i.toLong
  def intToLongUnsigned(i: Int): Long = i & 0X00000000FFFFFFFFL

