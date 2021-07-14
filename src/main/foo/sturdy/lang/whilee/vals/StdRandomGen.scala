package sturdy.lang.whilee.vals

import java.util.Formatter.DateTime

import sturdy.lang.whilee.HasRandomGen

import scala.compat.Platform
import scala.util.Random

trait StdRandomGen extends HasRandomGen[Double] {
  val random = new Random(Platform.currentTime)

  override def nextRandom: Double = random.nextDouble()
}
