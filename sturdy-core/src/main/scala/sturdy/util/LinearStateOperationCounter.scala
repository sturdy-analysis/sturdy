package sturdy.util

import scala.collection.mutable.ListBuffer

object LinearStateOperationCounter:
  var wideningCounter = 0
  var lookupCounter = 0
  var lookupStackCounter = 0

  private val wideningCounters: ListBuffer[Int] = ListBuffer()
  private val lookupCounters: ListBuffer[Int] = ListBuffer()
  private val lookupStackCounters: ListBuffer[Int] = ListBuffer()
  override def toString: String =
    s"${wideningCounters.sum} state widenings, ${lookupCounters.sum} state lookups, ${lookupStackCounters.sum} stack lookups"

  def addToListAndReset(): Unit =
    wideningCounters.append(wideningCounter)
    lookupCounters.append(lookupCounter)
    lookupStackCounters.append(lookupStackCounter)
    wideningCounter = 0
    lookupCounter = 0
    lookupStackCounter = 0

  def getSummedOperationsPerTest: ListBuffer[Int] =
    (wideningCounters zip lookupCounters zip lookupStackCounters).map{
      case ((w: Int, l: Int), ls: Int) => w + l + ls
    }

  def getOperationsPerTest: ListBuffer[(Int, Int, Int, Int)] =
    (wideningCounters zip lookupCounters zip lookupStackCounters).map{
      case ((w: Int, l: Int), ls: Int) => (w, l, ls, w + l + ls)
    }

  def clearAll(): Unit =
    wideningCounters.clear()
    lookupCounters.clear()
    lookupStackCounters.clear()
    wideningCounter = 0
    lookupCounter = 0
    lookupStackCounter = 0
