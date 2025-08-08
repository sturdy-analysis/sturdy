package sturdy.util

trait Enumerable[F]:
  def nextUp(f: F): F
  def nextDown(f: F): F

object Enumerable:
  def apply[F: Enumerable]: Enumerable[F] = implicitly

given Enumerable[Float] with
  override def nextDown(f: Float): Float = Math.nextDown(f)

  override def nextUp(f: Float): Float = Math.nextUp(f)


given Enumerable[Double] with
  override def nextDown(f: Double): Double = Math.nextDown(f)

  override def nextUp(f: Double): Double = Math.nextUp(f)