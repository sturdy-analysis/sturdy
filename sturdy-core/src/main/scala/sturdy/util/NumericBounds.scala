package sturdy.util

trait Bounded[N]:
  def minValue: N
  def maxValue: N

object Bounded:
  def apply[N: Bounded]: Bounded[N] = implicitly

given ByteBounds: Bounded[Byte] with
  override def minValue = Byte.MinValue
  override def maxValue = Byte.MaxValue

given ShortBounds: Bounded[Short] with
  override def minValue = Short.MinValue
  override def maxValue = Short.MaxValue

given IntBounds: Bounded[Int] with
  override def minValue = Int.MinValue
  override def maxValue = Int.MaxValue

given LongBounds: Bounded[Long] with
  override def minValue = Long.MinValue
  override def maxValue = Long.MaxValue

given FloatBounds: Bounded[Float] with
  override def minValue = Float.MinValue
  override def maxValue = Float.MaxValue

given DoubleBounds: Bounded[Double] with
  override def minValue = Double.MinValue
  override def maxValue = Double.MaxValue