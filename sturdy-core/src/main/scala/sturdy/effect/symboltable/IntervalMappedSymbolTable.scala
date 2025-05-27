package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, JOptionA, MayJoin}
import sturdy.effect.symboltable.IntervalMappedSymbolTable.IntervalMap
import sturdy.effect.symboltable.SizedSymbolTable.Limit
import sturdy.values.Topped.Top
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin}
import sturdy.values.*

class IntervalMappedSymbolTable[Value, Key, Entry](using Finite[Key], Join[Entry])(extractor: Value => NumericInterval[Int]) extends SizedSymbolTable[Value, Key, Topped[Int], Entry, Topped[Int], WithJoin] {
  var tables: Map[Key, IntervalMap[Entry]] = Map()

  override def get(key: Key, symbol: Topped[Int]): JOption[MayJoin.WithJoin, Entry] =
    val boundCheck = inBounds(key, symbol)
    if (boundCheck.isDefined && !boundCheck.get) {
      return JOptionA.none
    }
    tables.get(key) match {
      case Some(intervalMap) =>
        intervalMap.get(symbol) match {
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
        val updatedMap = intervalMap.addInterval(newEntry, symbol)
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
        val newIntervalMap = intervalMap.increaseSize(newSize)
        val initEntryInterval = (oldSize, newSize) match {
          case (Topped.Actual(oldS), Topped.Actual(newS)) => NumericInterval(oldS, newS)
          case (Topped.Actual(oldS), Topped.Top) => NumericInterval(oldS, Int.MaxValue)
          case (Topped.Top, Topped.Actual(newS)) => NumericInterval(Int.MinValue, newS)
          case (Topped.Top, Topped.Top) => NumericInterval(Int.MinValue, Int.MaxValue)
        }
        newIntervalMap.addInterval(initEntry, initEntryInterval)
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
        var resizedMap = intervalMap.increaseSize(amount.toConstant)
        // table bounds check
        val boundCheck = inBounds(resizedMap, tableOffset, amount)
        if (boundCheck.isDefined && !boundCheck.get) {
          return JOptionA.none
        }
        val newEntries = if(containsTop(entryOffset)) entries else entries.slice(entryOffset.low, entryOffset.high + amount.high)
        if (containsTop(tableOffset)) {
          // if tableOffset is top, we add all entries to the top interval
          newEntries.foreach { entry =>
            resizedMap = resizedMap.addInterval(entry, Topped.Top)
          }
        } else {
          // otherwise we add entries to the specified interval
          newEntries.foreach { entry =>
            resizedMap = resizedMap.addInterval(entry, NumericInterval(tableOffset.low, tableOffset.high + amount.high - 1))
          }
        }
        tables += (key -> resizedMap)
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
            tables += (key -> intervalMap.addInterval(entry, Topped.Top))
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
            // all intervals are finite -> copy finite intervals
            val elementsToCopy = srcIntervalMap.get(NumericInterval(srcOffset.low, srcOffset.high + amount.high - 1))
            elementsToCopy.foreach { entry =>
              tables += (dstKey -> dstIntervalMap.addInterval(entry, NumericInterval(dstOffset.low, dstOffset.high + amount.high - 1)))
            }
          case (true, false, false) =>
            // dst is top but elements are finite -> copy them to top interval
            val elementsToCopy = srcIntervalMap.get(NumericInterval(srcOffset.low, srcOffset.high + amount.high - 1))
            elementsToCopy.foreach { entry =>
              tables += (dstKey -> dstIntervalMap.addInterval(entry, Topped.Top))
            }
          case (_, _, true) | (_, true, _) =>
            // elements are top in at least one direction -> copy all to top interval
            val elementsToCopy = srcIntervalMap.get(NumericInterval(srcOffset.low, Int.MaxValue))
            elementsToCopy.foreach { entry =>
              tables += (dstKey -> dstIntervalMap.addInterval(entry, NumericInterval(dstOffset.low, Int.MaxValue)))
            }
        }
    }

    JOptionA.none
  }

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[Topped[Int]]): Unit = tables += (key -> IntervalMap[Entry](Map.empty, limit, limit.min))
  override def putNew(key: Key): Unit = putNew(key, SizedSymbolTable.Limit(Topped.Actual(0), None))

  private def containsTop(interval: NumericInterval[Int]): Boolean = {
    interval.low == Int.MinValue && interval.high == Int.MaxValue
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
    case(a, b) => Join(a, b)
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
      MaybeChanged(IntervalMap(joinedMap, v1.limit, v1.size), changed)
    }
  }
  
  case class IntervalMap[Entry](map: Map[Entry, NumericInterval[Int]], limit: Limit[Topped[Int]], size: Topped[Int])(using Join[Entry]) {
    def addInterval(entry: Entry, interval: NumericInterval[Int]): IntervalMap[Entry] = {
      map.get(entry) match {
        case Some(existingInterval) => Join.apply(existingInterval, interval) match {
          case MaybeChanged.Changed(a) => IntervalMap(map.updated(entry, a), limit, size)
          case MaybeChanged.Unchanged(_) => IntervalMap(map, limit, size)
        }
        case None => IntervalMap(map.updated(entry, interval), limit, size)
      }
    }

    def addInterval(entry: Entry, interval: Topped[Int]): IntervalMap[Entry] = {
      interval match {
        case Topped.Actual(i) => addInterval(entry, NumericInterval(i, i))
        case Topped.Top => addInterval(entry, NumericInterval(Int.MinValue, Int.MaxValue))
      }
    }

    def get(index: Topped[Int]): Option[Entry] = {
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
        get(Topped.Actual(i)) match {
          case Some(entry) => entries = entries :+ entry
          case None => ()
        }
      }
      entries
    }

    def increaseSize(amount: Topped[Int]): IntervalMap[Entry] = {
      val newSize = (size, amount) match {
        case (Topped.Actual(s), Topped.Actual(a)) => Topped.Actual(s + a)
        case (Topped.Top, _) => Topped.Top
        case (_, Topped.Top) => Topped.Top
      }
      IntervalMap(map, limit, newSize)
    }

  }
