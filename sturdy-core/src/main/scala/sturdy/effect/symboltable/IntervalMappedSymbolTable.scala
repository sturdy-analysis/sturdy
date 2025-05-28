package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, JOptionA, MayJoin}
import sturdy.effect.symboltable.IntervalMappedSymbolTable.IntervalMap
import sturdy.effect.symboltable.SizedSymbolTable.Limit
import sturdy.values.Topped.Top
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin}
import sturdy.values.{*, given}

class IntervalMappedSymbolTable[Value, Key, Entry](using Finite[Key], Join[Entry])(extractor: Value => NumericInterval[Int]) extends SizedSymbolTable[Value, Key, Topped[Int], Entry, Topped[Int], WithJoin] {
  var tables: Map[Key, IntervalMap[Entry]] = Map()

  override def get(key: Key, symbol: Topped[Int]): JOption[MayJoin.WithJoin, Entry] =
    val boundCheck = inBounds(key, symbol)
    if (boundCheck.isDefined && !boundCheck.get) {
      return JOptionA.none
    }
    tables.get(key) match {
      case Some(intervalMap) =>
        intervalMap.getSingle(symbol) match {
          case Some(entry) =>
            JOptionA.noneSome(entry)
          case None =>
            JOptionA.none
        }
      case None =>
        JOptionA.none
    }

  override def set(key: Key, symbol: Topped[Int], newEntry: Entry): JOption[MayJoin.WithJoin, Unit] =
    val boundCheck = inBounds(key, symbol)
    if (boundCheck.isDefined && !boundCheck.get) {
      return JOptionA.none
    }
    tables.get(key) match {
      case Some(intervalMap) =>
        val updatedMap = intervalMap.addInterval(newEntry, symbol, overrideExisting = true)
        tables += (key -> updatedMap)
        JOptionA.noneSome(())
      case None =>
        JOptionA.none
    }

  override def size(key: Key): Topped[Int] =
    tables.get(key) match {
      case Some(intervalMap) => intervalMap.size
      case None => Topped.Actual(0)
    }

  override def grow(key: Key, newSize: Topped[Int], initEntry: Entry): JOption[MayJoin.WithJoin, Topped[Int]] = {
    tables.get(key) match {
      case Some(intervalMap) =>
        intervalMap.limit.max match {
          case Some(Topped.Actual(max)) if newSize.isActual && newSize.get > max =>
            return JOptionA.none
          case _ => ()
        }
        val oldSize = intervalMap.size
        var newIntervalMap = intervalMap.setSize(newSize)
        val initEntryInterval = (oldSize, newSize) match {
          case (Topped.Actual(oldS), Topped.Actual(newS)) => NumericInterval(oldS, newS)
          case (Topped.Actual(oldS), Topped.Top) => NumericInterval(oldS, Int.MaxValue)
          case (Topped.Top, Topped.Actual(newS)) => NumericInterval(Int.MinValue, newS)
          case (Topped.Top, Topped.Top) => NumericInterval(Int.MinValue, Int.MaxValue)
        }
        newIntervalMap = newIntervalMap.addInterval(initEntry, initEntryInterval)
        tables += (key -> newIntervalMap)
        JOptionA.some(oldSize)
      case None =>
        JOptionA.none
    }
  }

  override def init(key: Key, entries: Vector[Entry], entryOffset: Value, tableOffset: Value, amount: Value): JOption[MayJoin.WithJoin, Unit] = init(key, entries, extractor(entryOffset), extractor(tableOffset), extractor(amount))

  private def init(key: Key, entries: Vector[Entry], entryOffset: NumericInterval[Int], tableOffset: NumericInterval[Int], amount: NumericInterval[Int]): JOption[MayJoin.WithJoin, Unit] = {
    if (amount.isConstant && amount.toConstant.get == 0) {
      return JOptionA.some(())
    }
    tables.get(key) match {
      case Some(intervalMap) =>
        // elem bounds check
        if (entryOffset.low < 0 || entryOffset.high > entries.size || amount.low < 0 || amount.high < 0) {
          return JOptionA.none
        }
        var updatedMap = intervalMap
        // table bounds check
        val boundCheck = inBounds(updatedMap, tableOffset, amount)
        if (boundCheck.isDefined && !boundCheck.get) {
          return JOptionA.none
        }
        val newEntries = if (containsTop(entryOffset)) entries else entries.slice(entryOffset.low, entryOffset.high + amount.high)
        if (containsTop(tableOffset)) {
          // if tableOffset is top, we add all entries to the top interval
          newEntries.foreach { entry =>
            updatedMap = updatedMap.addInterval(entry, Topped.Top, overrideExisting = false)
          }
        } else {
          // otherwise we add the ordered entries starting from the tableOffset
          for (i <- newEntries.indices) {
            val entry = newEntries(i)
            val offset = NumericInterval(tableOffset.low + i, tableOffset.low + i)
            updatedMap = updatedMap.addInterval(entry, offset, overrideExisting = true)
          }
        }
        tables += (key -> updatedMap)
        JOptionA.some(())
      case None =>
        JOptionA.none
    }
  }

  override def fillTable(key: Key, entry: Entry, tableOffset: Value, amount: Value): JOption[MayJoin.WithJoin, Unit] = fillTable(key, entry, extractor(tableOffset), extractor(amount))

  private def fillTable(key: Key, entry: Entry, tableOffset: NumericInterval[Int], amount: NumericInterval[Int]): JOption[MayJoin.WithJoin, Unit] = {
    val boundCheck = inBounds(key, tableOffset, amount)
    if (boundCheck.isDefined && !boundCheck.get) {
      return JOptionA.none
    }

    tables.get(key) match {
      case Some(intervalMap) =>
        (containsTop(tableOffset), containsTop(amount)) match {
          case (true, _) =>
            tables += (key -> intervalMap.addInterval(entry, Topped.Top, overrideExisting = false))
          case (false, true) =>
            tables += (key -> intervalMap.addInterval(entry, NumericInterval(tableOffset.low, Int.MaxValue)))
          case (false, false) =>
            tables += (key -> intervalMap.addInterval(entry, NumericInterval(tableOffset.low, tableOffset.high + amount.high - 1)))
        }
        JOptionA.some(())
      case None => JOptionA.none
    }
  }

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Value, srcOffset: Value, amount: Value): JOption[MayJoin.WithJoin, Unit] = copy(dstKey, srcKey, extractor(dstOffset), extractor(srcOffset), extractor(amount))

  private def copy(dstKey: Key, srcKey: Key, dstOffset: NumericInterval[Int], srcOffset: NumericInterval[Int], amount: NumericInterval[Int]): JOption[MayJoin.WithJoin, Unit] = {
    val dstBoundCheck = inBounds(dstKey, dstOffset, amount)
    if (dstBoundCheck.isDefined && !dstBoundCheck.get) {
      return JOptionA.none
    }
    val srcBoundCheck = inBounds(srcKey, srcOffset, amount)
    if (srcBoundCheck.isDefined && !srcBoundCheck.get) {
      return JOptionA.none
    }

    (tables.get(dstKey), tables.get(srcKey)) match {
      case (None, _) | (_, None) =>
        JOptionA.none
      case (Some(dstIntervalMap), Some(srcIntervalMap)) =>
        (containsTop(dstOffset), containsTop(srcOffset), containsTop(amount)) match {
          case (false, false, false) =>
            // all intervals are finite -> copy finite intervals by iterating over srcOffset + amount and getting the elements at those indices
            var updatedDstMap = dstIntervalMap
            for (i <- srcOffset.low until srcOffset.high + amount.high) {
              val srcSymbol = Topped.Actual(i)
              val symbols = srcIntervalMap.getSingle(srcSymbol)
              symbols.foreach { entry =>
                updatedDstMap = updatedDstMap.addInterval(entry, NumericInterval(dstOffset.low + (i - srcOffset.low), dstOffset.low + (i - srcOffset.low)))
              }
            }
            tables += (dstKey -> updatedDstMap)
            JOptionA.some(())
          case (true, false, false) =>
            // dst is top but elements are finite -> copy them to top interval
            val elementsToCopy = srcIntervalMap.get(NumericInterval(srcOffset.low, srcOffset.high + amount.high - 1))
            elementsToCopy.foreach { entry =>
              tables += (dstKey -> dstIntervalMap.addInterval(entry, Topped.Top, overrideExisting = false))
            }
            JOptionA.some(())
          case (_, _, true) | (_, true, _) =>
            // elements are top in at least one direction -> copy all to top interval
            val iv = NumericInterval(srcOffset.low, Int.MaxValue)
            var elemsToCopy: Vector[Entry] = Vector.empty
            if (containsTop(iv)) {
              elemsToCopy = srcIntervalMap.getAll
            } else {
              elemsToCopy = srcIntervalMap.get(iv)
            }
            elemsToCopy.foreach { entry =>
              tables += (dstKey -> dstIntervalMap.addInterval(entry, NumericInterval(dstOffset.low, Int.MaxValue)))
            }
            JOptionA.some(())
        }
    }
  }

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[Topped[Int]]): Unit = tables += (key -> IntervalMap[Entry](Map.empty, limit, limit.min))
  override def putNew(key: Key): Unit = putNew(key, SizedSymbolTable.Limit(Topped.Actual(0), None))

  private def containsTop(interval: NumericInterval[Int]): Boolean = {
    interval.low <= 0 && interval.high == Int.MaxValue
  }

  private def inBounds(intervalMap: IntervalMap[Entry], offset: NumericInterval[Int], amount: NumericInterval[Int]): Option[Boolean] = {
    val constOffset = offset.toConstant
    val constAmount = amount.toConstant
    if (constOffset.isTop || constAmount.isTop) return None
    inBounds(intervalMap, Topped.Actual(constOffset.get + constAmount.get - 1))
  }

  private def inBounds(key: Key, offset: NumericInterval[Int], amount: NumericInterval[Int]): Option[Boolean] = {
    val constOffset = offset.toConstant
    val constAmount = amount.toConstant
    if (constOffset.isTop || constAmount.isTop) return None
    inBounds(key, Topped.Actual(constOffset.get + constAmount.get - 1))
  }

  private def inBounds(key: Key, symbol: Topped[Int]): Option[Boolean] =
    tables.get(key) match {
      case Some(intervalMap) => inBounds(intervalMap, symbol)
      case None => None
    }

  private def inBounds(intervalMap: IntervalMap[Entry], symbol: Topped[Int]): Option[Boolean] = {
    if (symbol.isTop) return None
    intervalMap.size match {
      case Topped.Actual(size) => Some(!(symbol.get < 0 || symbol.get >= size))
      case Topped.Top => None
    }
  }

  override type State = Map[Key, IntervalMap[Entry]]
  override def getState: State = tables
  override def setState(s: State): Unit = tables = s
  override def join: Join[State] = JoinMap(using {
    case (a, b) => Join(a, b)
  })
  override def widen: Widen[State] = CombineFiniteKeyMap(using {
    case (a, b) => Widen(a, b)
  })
}

object IntervalMappedSymbolTable:
  given CombineIntervalMap[Entry, W <: Widening](using Join[Entry]): Combine[IntervalMap[Entry], W] with {
    override def apply(v1: IntervalMap[Entry], v2: IntervalMap[Entry]): MaybeChanged[IntervalMap[Entry]] = {
      var joinedMap = v1.map
      var changed = false
      for ((entry, interval2) <- v2.map) {
        joinedMap.get(entry) match {
          case None =>
            joinedMap += (entry -> interval2)
            changed = true
          case Some(interval1) =>
            Join(interval1, interval2) match {
              case MaybeChanged.Changed(newInterval) =>
                joinedMap += (entry -> newInterval)
                changed = true
              case MaybeChanged.Unchanged(_) => ()
            }
        }
      }
      val limitJoined = Join(v1.limit, v2.limit) match {
        case MaybeChanged.Changed(newLimit) =>
          changed = true
          newLimit
        case MaybeChanged.Unchanged(_) => v1.limit
      }
      val sizeJoined = Join(v1.size, v2.size) match {
        case MaybeChanged.Changed(newSize) =>
          changed = true
          newSize
        case MaybeChanged.Unchanged(_) => v1.size
      }
      MaybeChanged(IntervalMap(joinedMap, limitJoined, sizeJoined), changed)
    }
  }

  case class IntervalMap[Entry](map: Map[Entry, NumericInterval[Int]], limit: Limit[Topped[Int]], size: Topped[Int])(using Join[Entry]) {
    def addInterval(entry: Entry, interval: NumericInterval[Int], overrideExisting: Boolean = false): IntervalMap[Entry] = {
      var updatedMap = map
      if (overrideExisting && interval.isConstant) {
        val conflictingEntries = this.get(interval)
        for (conflictingEntry <- conflictingEntries) {
          val conflictingInterval = map.get(conflictingEntry)
          conflictingInterval match {
            case None => ()
            case Some(conflictingInterval) =>
              if (conflictingInterval.isConstant) {
                updatedMap = updatedMap - conflictingEntry
              } else if (conflictingInterval.low == interval.toConstant.get) {
                updatedMap = updatedMap.updated(conflictingEntry, NumericInterval(conflictingInterval.low + 1, conflictingInterval.high))
              } else if (conflictingInterval.high == interval.toConstant.get) {
                updatedMap = updatedMap.updated(conflictingEntry, NumericInterval(conflictingInterval.low, conflictingInterval.high - 1))
              }
          }
        }
      }

      updatedMap.get(entry) match {
        case Some(existingInterval) => Join.apply(existingInterval, interval) match {
          case MaybeChanged.Changed(a) => IntervalMap(updatedMap.updated(entry, a), limit, size)
          case MaybeChanged.Unchanged(_) => IntervalMap(updatedMap, limit, size)
        }
        case None => IntervalMap(updatedMap.updated(entry, interval), limit, size)
      }
    }

    def addInterval(entry: Entry, interval: Topped[Int], overrideExisting: Boolean): IntervalMap[Entry] = {
      interval match {
        case Topped.Actual(i) => addInterval(entry, NumericInterval(i, i), overrideExisting)
        case Topped.Top => addInterval(entry, NumericInterval(Int.MinValue, Int.MaxValue), overrideExisting)
      }
    }

    def getSingle(index: Topped[Int]): Option[Entry] = {
      index match {
        case Topped.Actual(i) => map.collect {
          case (entry, interval) if interval.containsNum(i) => entry
        }.reduceOption(Join(_, _).get)
        case Topped.Top => map.keys.reduceOption(Join(_, _).get)
      }
    }

    def get(index: NumericInterval[Int]): Vector[Entry] = {
      var entries: Vector[Entry] = Vector.empty
      for (i <- index.low to index.high) {
        getSingle(Topped.Actual(i)) match {
          case Some(entry) => entries = entries :+ entry
          case None => ()
        }
      }
      entries
    }

    def getAll: Vector[Entry] = {
      map.keys.toVector
    }

    def setSize(newSize: Topped[Int]): IntervalMap[Entry] = {
      IntervalMap(map, limit, newSize)
    }

  }
