package sturdy.util
import scala.collection.mutable

object Profiler:
  val measuredTimes: mutable.Map[String, Long] = mutable.Map()

  private val startTimes: mutable.Map[String, Long] = mutable.Map()

  def addTime[R](name: String)(block: => R): R = {
    val t0: Long = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    if (!measuredTimes.contains(name)) measuredTimes(name) = 0
    measuredTimes(name) += t1 - t0
    result
  }

  def print(): Unit =
    println("Elapsed times:")
    measuredTimes.keys.foreach(print)

  def print(name: String): Unit =
    println(s"\t$name:${measuredTimes(name) / 1000000000.0}s")

  def reset(): Unit =
    measuredTimes.clear()
    assert(startTimes.isEmpty)


  def start(name: String): Unit =
    assert(!startTimes.contains(name))
    startTimes(name) = System.nanoTime()

  def end(name: String): Unit =
    assert(startTimes.contains(name))
    if (!measuredTimes.contains(name)) measuredTimes(name) = 0
    measuredTimes(name) += System.nanoTime() - startTimes(name)
    startTimes -= name
