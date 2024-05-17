package sturdy.util
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Profiler:
  private var measuredTimes: Map[String, Long] = Map()

  private var startTimes: Map[String, Long] = Map()

  private var savedTimes: Map[String, ListBuffer[Long]] = Map()

  def addTime[R](name: String)(block: => R): R = {
    start(name)
    try block    // call-by-name
    finally end(name)
  }

  def printLastMeasured(): Unit =
    println("Elapsed times:")
    measuredTimes.keys.foreach(printByName)

  def printByName(name: String): Unit =
    println(s"\t$name:${nanoToFormattedSeconds(measuredTimes(name))}")

  private def nanoToFormattedSeconds(nanoseconds: Long): String =
    val seconds: Double = nanoseconds / 1000000000.0
    f"$seconds%1.4fs"
//    s"$seconds"


  def printSavedTimes(): Unit =
    savedTimes.foreach{
      (name, listOfTimes: ListBuffer[Long]) =>
        print(s"$name: ")
        println(listOfTimes.map(nano => s"${nanoToFormattedSeconds(nano)}").mkString(", "))
    }

  def reset(): Unit =
    measuredTimes = Map()
    assert(startTimes.isEmpty)


  def start(name: String): Unit =
    assert(!startTimes.contains(name))
    startTimes += name -> System.nanoTime()

  def end(name: String): Unit =
    assert(startTimes.contains(name))
    if (!measuredTimes.contains(name)) measuredTimes += name -> 0
    measuredTimes += name -> (measuredTimes(name) + System.nanoTime() - startTimes(name))
    startTimes -= name

  def saveTimesAndReset(): Unit =
    measuredTimes.foreach {
      (name, time) =>
        if (!savedTimes.contains(name))
          savedTimes += name -> ListBuffer()
        savedTimes(name).append(time)
    }
    measuredTimes = Map()
