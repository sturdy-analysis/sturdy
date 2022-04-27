package sturdy.util

import scala.collection.mutable.ListBuffer

object LinearStateOperationCounter:
  var wideningCounter = 0
  var lookupCounter = 0

  private val wideningCounters: ListBuffer[Int] = ListBuffer()
  private val lookupCounters: ListBuffer[Int] = ListBuffer()

  override def toString: String =
    s"${wideningCounters.sum} state widenings, ${lookupCounters.sum} state lookups"

  def addToListAndReset(): Unit =
    wideningCounters.append(wideningCounter)
    lookupCounters.append(lookupCounter)
    wideningCounter = 0
    lookupCounter = 0

  def getSummedOperationsPerTest: ListBuffer[Int] =
    (wideningCounters zip lookupCounters).map(_+_)
