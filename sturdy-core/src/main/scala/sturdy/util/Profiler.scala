package sturdy.util
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Profiler:
  private var measuredTimes: Map[String, Long] = Map()
  private var callCount: Map[String, Int] = Map()
  private var enableMeasurement: Boolean = true
  
  private var logPrefix: String = ""
  private var loggedData: Map[String, Any] = Map()

  private var startTimes: Map[String, Long] = Map()

  private var savedTimes: Map[String, ListBuffer[Long]] = Map()

  def addTime[R](name: String, enableSubmeasurements: Boolean = true)(block: => R): R = {
    if(enableMeasurement && !startTimes.contains(name)) {
      start(name)
      try block finally end(name)
    } else {
      block
    }
  }

  inline def disableMeasurement[R](block: => R): R = {
    val snapshotEnableMeasurement = enableMeasurement
    try {
      enableMeasurement = false
      block
    }
    finally {
      enableMeasurement = snapshotEnableMeasurement
    }
  }

  def setLogPrefix(prefix: String): Unit =
    this.logPrefix = prefix
  
  def addData[R](name: String, default: R)(f: R => R): Unit = {
    val key = logPrefix + name
    loggedData.get(key) match {
      case Some(value) => loggedData = loggedData + (key -> f(value.asInstanceOf[R]))
      case None => loggedData = loggedData + (key -> default)
    }
  }

  def addTimeBestOf[R](name: String, repeat: Int)(block: => R): R = {
    if (measuredTimes.contains(name))
      throw new IllegalArgumentException()
    
    var previous: Long = 0
    val times = ListBuffer.empty[Long]
    val res = for (i <- 1 to repeat) yield {
      val r = addTime(name)(block)
      val t = measuredTimes(name) - previous
      previous += t
      times += t
      r
    }
    val best = times.min
    measuredTimes += name -> best
    res.head
  }

  def get(name: String): Option[Long] =
    measuredTimes.get(name)

  def getData(name: String, default: => Any): Any =
    loggedData.getOrElse(logPrefix + name, default)

  def getAllData: Map[String, Any] = loggedData
  
  def printLastMeasured(): Unit = {
    if (measuredTimes.nonEmpty) {
      println("Elapsed times:")
      val maxNameLength = measuredTimes.keys.map(_.length).max
      measuredTimes.keys.toList.sorted.foreach(printByName(paddingName = maxNameLength + 1))
    }
    if (loggedData.nonEmpty) {
      println("Logged data:")
      println(loggedData.toList.sortBy(_._1))
    }
  }

  def printByName(paddingName: Int)(name: String): Unit = {
    println(s"\t${name.padTo(paddingName, ' ')} ${"%7d".format(callCount(name))}  ${nanoToFormattedSeconds(measuredTimes(name))}")
  }

  private def nanoToFormattedSeconds(nanoseconds: Long): String =
    val seconds: Double = nanoseconds / 1000000000.0
    f"$seconds%3.3fs"
//    s"$seconds"

  def printSavedTimes(): Unit =
    savedTimes.foreach{
      (name, listOfTimes: ListBuffer[Long]) =>
        print(s"$name: ")
        println(listOfTimes.map(nano => s"${nanoToFormattedSeconds(nano)}").mkString(", "))
    }

  def reset(): Unit =
    measuredTimes = Map()
    callCount = Map()
    enableMeasurement = true
    logPrefix = ""
    loggedData = Map()
    assert(startTimes.isEmpty)


  def start(name: String): Unit =
    startTimes += name -> System.nanoTime()
    callCount += name -> (callCount.getOrElse(name, 0) + 1)

  def end(name: String): Unit =
    measuredTimes += name -> (measuredTimes.getOrElse(name,0L) + System.nanoTime() - startTimes(name))
    startTimes -= name

  def saveTimesAndReset(): Unit =
    measuredTimes.foreach {
      (name, time) =>
        if (!savedTimes.contains(name))
          savedTimes += name -> ListBuffer()
        savedTimes(name).append(time)
    }
    measuredTimes = Map()
