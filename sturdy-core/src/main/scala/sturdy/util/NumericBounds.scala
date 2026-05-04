package sturdy.util

trait Bounded[N]:
  def minValue: N
  def maxValue: N
  def numBytes: N

object Bounded:
  def apply[N: Bounded]: Bounded[N] = implicitly

given ByteBounds: Bounded[Byte] with
  override def minValue = Byte.MinValue
  override def maxValue = Byte.MaxValue
  override def numBytes: Byte = java.lang.Byte.BYTES

given ShortBounds: Bounded[Short] with
  override def minValue = Short.MinValue
  override def maxValue = Short.MaxValue
  override def numBytes: Short = java.lang.Short.BYTES

given IntBounds: Bounded[Int] with
  override def minValue = Int.MinValue
  override def maxValue = Int.MaxValue
  override def numBytes: Int = java.lang.Integer.BYTES

given LongBounds: Bounded[Long] with
  override def minValue = Long.MinValue
  override def maxValue = Long.MaxValue
  override def numBytes: Long = java.lang.Long.BYTES

given FloatBounds: Bounded[Float] with
  override def minValue = Float.MinValue
  override def maxValue = Float.MaxValue
  override def numBytes: Float = java.lang.Float.BYTES

given DoubleBounds: Bounded[Double] with
  override def minValue = Double.MinValue
  override def maxValue = Double.MaxValue
  override def numBytes: Double = java.lang.Double.BYTES