package sturdy.effect.symboltable

import sturdy.{IsSound, Soundness, data}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, JOptionA, MayJoin}
import sturdy.effect.symboltable.IntervalMappedSymbolTable.IntervalMap
import sturdy.effect.symboltable.SizedSymbolTable.Limit
import sturdy.values.Topped.Top
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin}
import sturdy.values.{*, given}

import scala.util.boundary
import scala.util.boundary.break

class IntervalMappedSymbolTable[Value, Key, Entry](using Finite[Key], Join[Entry])(rangeLimit: Int, extractor: Value => NumericInterval[Int])
  extends SizedSymbolTable[Value, Key, NumericInterval[Int], Entry, Topped[Int], WithJoin] {

  var tables: Map[Key, IntervalMap[Entry]] = Map()

  override def get(key: Key, symbol: NumericInterval[Int]): JOptionA[Entry] =
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

  override def set(key: Key, symbol: NumericInterval[Int], newEntry: Entry): JOptionA[Unit] =
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
      case Some(intervalMap) => intervalMap.size.toConstant
      case None => Topped.Top
    }

  override def grow(key: Key, newSize: Topped[Int], initEntry: Entry): JOptionA[Topped[Int]] = {
    tables.get(key) match {
      case Some(intervalMap) =>
        intervalMap.limit.max match {
          case Some(iv@NumericInterval[Int](_, _)) if newSize.isActual && iv.isConstant && newSize.get > iv.toConstant.get =>
            return JOptionA.none
          case _ => ()
        }
        val oldSize = intervalMap.size.toConstant
        val newSizeIv = newSize match {
          case Topped.Actual(i) => NumericInterval(i, i)
          case Topped.Top => NumericInterval(0, Int.MaxValue)
        }
        var newIntervalMap = intervalMap.setSize(newSizeIv)
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

  // Todo: reimplement when IntervalAnalysis has been updated such that Size=NumericInterval[Int] (has to update Memory)
  /*override def grow(key: Key, newSize: Topped[Int], initEntry: Entry): JOption[MayJoin.WithJoin, NumericInterval[Int]] = {
    tables.get(key) match {
      case Some(intervalMap) =>
        intervalMap.limit.max match {
          case Some(limitIv@NumericInterval(_, _)) if limitIv.isConstant && newSize.isConstant && limitIv.toConstant.get < newSize.toConstant.get =>
            return JOptionA.none
          case _ => ()
        }
        val oldSize = intervalMap.size
        var newIntervalMap = intervalMap.setSize(newSize)
        val initEntryInterval = if (oldSize.isConstant) {
          NumericInterval(oldSize.toConstant.get, newSize.high)
        } else {
          NumericInterval(oldSize.low, newSize.high)
        }
        newIntervalMap = newIntervalMap.addInterval(initEntry, initEntryInterval)
        tables += (key -> newIntervalMap)
        JOptionA.some(oldSize)
      case None =>
        JOptionA.none
    }
  }*/

  override def init(key: Key, entries: Vector[Entry], entryOffset: Value, tableOffset: Value, amount: Value): JOption[MayJoin.WithJoin, Unit] = init(key, entries, extractor(entryOffset), extractor(tableOffset), extractor(amount))

  private def init(key: Key, entries: Vector[Entry], entryOffset: NumericInterval[Int], tableOffset: NumericInterval[Int], amount: NumericInterval[Int]): JOption[MayJoin.WithJoin, Unit] = {
    if (amount.isConstant && amount.toConstant.get == 0) {
      return JOptionA.some(())
    }
    tables.get(key) match {
      case Some(intervalMap) =>
        // elem bounds check
        if ((!containsTop(entryOffset) && (entryOffset.low < 0 || entryOffset.high > entries.size)) || (!containsTop(amount) && (amount.low < 0 || amount.high < 0))) {
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
        if(boundCheck.isEmpty) JOptionA.noneSome(()) else JOptionA.some(())
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
              val srcSymbol = NumericInterval(i, i)
              val symbols = srcIntervalMap.get(srcSymbol)
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

  override def putNew(key: Key, limit: Limit[Topped[Int]]): Unit = {
    val ivLimit = limit match {
      case Limit(min, Some(max)) => (min, max) match {
        case (Topped.Actual(i), Topped.Actual(j)) => Limit(NumericInterval(i, i), Some(NumericInterval(j, j)))
        case (Topped.Actual(i), Topped.Top) => Limit(NumericInterval(i, i), Some(NumericInterval(i, Int.MaxValue)))
        case (Topped.Top, Topped.Actual(j)) => Limit(NumericInterval(0, Int.MaxValue), Some(NumericInterval(j, j)))
        case (Topped.Top, Topped.Top) => Limit(NumericInterval(0, Int.MaxValue), Some(NumericInterval(0, Int.MaxValue)))
      }
      case Limit(min, None) => min match {
        case Topped.Actual(i) => Limit(NumericInterval(i, i), None)
        case Topped.Top => Limit(NumericInterval(0, Int.MaxValue), None)
      }
    }
    val size = limit.min match {
      case Topped.Actual(i) => NumericInterval(i, i)
      case Topped.Top => NumericInterval(0, Int.MaxValue)
    }
    tables += (key -> IntervalMap[Entry](Map.empty, ivLimit, size))
  }

  override def putNew(key: Key): Unit = putNew(key, SizedSymbolTable.Limit(Topped.Actual(0), None))

  private def containsTop(interval: NumericInterval[Int]): Boolean = {
    interval.low <= 0 && interval.high >= rangeLimit
  }

  private def inBounds(intervalMap: IntervalMap[Entry], offset: NumericInterval[Int], amount: NumericInterval[Int]): Option[Boolean] = {
    val constOffset = offset.toConstant
    val constAmount = amount.toConstant
    if (constOffset.isTop || constAmount.isTop) return None
    val off = constOffset.get + constAmount.get - 1
    inBounds(intervalMap, NumericInterval(off, off))
  }

  private def inBounds(key: Key, offset: NumericInterval[Int], amount: NumericInterval[Int]): Option[Boolean] = {
    val constOffset = offset.toConstant
    val constAmount = amount.toConstant
    if (constOffset.isTop || constAmount.isTop) return None
    val off = constOffset.get + constAmount.get - 1
    inBounds(key, NumericInterval(off, off))
  }

  private def inBounds(key: Key, symbol: NumericInterval[Int]): Option[Boolean] =
    tables.get(key) match {
      case Some(intervalMap) => inBounds(intervalMap, symbol)
      case None => None
    }

  private def inBounds(intervalMap: IntervalMap[Entry], symbol: NumericInterval[Int]): Option[Boolean] = {
    if (containsTop(symbol)) return None
    val ivSize = intervalMap.size
    Some(!(ivSize.low <= symbol.low && ivSize.high >= symbol.high))
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

  def tableIsSound[cValue, cEntry](c: ConcreteSizedTable[cValue, Key, cEntry])(using Soundness[Limit[Int], Limit[NumericInterval[Int]]]): IsSound =
    boundary[IsSound]:
      for ((key, cTab) <- c.tables) {
        val aTab = tables.getOrElse(key, break(IsSound.NotSound(s"Key $key not present in interval mapped symbol table.")))

        if (!Soundness.isSound(cTab.limit, aTab.limit).isSound)
          break(IsSound.NotSound(s"Table $key has mismatched limits: concrete ${cTab.limit} vs abstract ${aTab.limit}."))

        for ((sym, cEntry) <- cTab.entries) {
          val matching = aTab.get(NumericInterval(sym, sym)).map(aEntry => aEntry == cEntry)
          if (matching.isEmpty)
            break(IsSound.NotSound(s"Table $key misses symbol $sym, bound to $cEntry in the concrete table."))
        }
      }
      IsSound.Sound
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

  case class IntervalMap[Entry](map: Map[Entry, NumericInterval[Int]], limit: Limit[NumericInterval[Int]], size: NumericInterval[Int])(using Join[Entry]) {
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
        case Topped.Top => addInterval(entry, NumericInterval(0, Int.MaxValue), overrideExisting)
      }
    }

    def getSingle(index: NumericInterval[Int]): Option[Entry] = {
        map.collect {
          case (entry, interval) if interval.low <= index.high && index.low <= interval.high => entry
        }.reduceOption(Join(_, _).get)
    }

    def get(index: NumericInterval[Int]): Vector[Entry] = {
      map.collect {
        case (entry, interval) if interval.low <= index.high && index.low <= interval.high => entry
      }.toVector
    }

    def getAll: Vector[Entry] = {
      map.keys.toVector
    }

    def setSize(newSize: NumericInterval[Int]): IntervalMap[Entry] = {
      IntervalMap(map, limit, newSize)
    }

  }

  given limitIsSound: Soundness[Limit[Int], Limit[NumericInterval[Int]]] with {
    override def isSound(c: Limit[Int], a: Limit[NumericInterval[Int]]): IsSound = {
      (c.max, a.max) match {
        case (Some(cMax), Some(aMax)) =>
          if (aMax.containsNum(cMax) && a.min.containsNum(c.min)) IsSound.Sound
          else IsSound.NotSound(s"Limit max mismatch: concrete $cMax vs abstract $aMax.")
        case (None, None) =>
          if (a.min.containsNum(c.min)) IsSound.Sound
          else IsSound.NotSound(s"Limit min mismatch: concrete ${c.min} vs abstract ${a.min}.")
        case _ => IsSound.NotSound(s"Limit max mismatch: concrete $c vs abstract $a.")
      }
    }
  }

class ConstantIntervalMappedSymbolTable[Value, Key, Entry](using Finite[Key], Join[Entry])(extractor: Value => NumericInterval[Int])
  extends SizedSymbolTable[Value, Key, Topped[Int], Entry, Topped[Int], WithJoin] {

  private val intervalTables = new IntervalMappedSymbolTable[Value, Key, Entry](Int.MaxValue, extractor)

  private def topToInterval(top: Topped[Int]): NumericInterval[Int] = {
    top match {
      case Topped.Actual(i) => NumericInterval(i, i)
      case Topped.Top => NumericInterval(0, Int.MaxValue)
    }
  }

  override def get(key: Key, symbol: Topped[Int]): JOption[data.MayJoin.WithJoin, Entry] =
    intervalTables.get(key, topToInterval(symbol))

  override def set(key: Key, symbol: Topped[Int], newEntry: Entry): JOption[data.MayJoin.WithJoin, Unit] =
    intervalTables.set(key, topToInterval(symbol), newEntry)

  override def size(key: Key): Topped[Int] = intervalTables.size(key)

  override def grow(key: Key, newSize: Topped[Int], initEntry: Entry): JOption[data.MayJoin.WithJoin, Topped[Int]] = intervalTables.grow(key, newSize, initEntry)

  override def init(key: Key, entries: Vector[Entry], entryOffset: Value, tableOffset: Value, amount: Value): JOption[data.MayJoin.WithJoin, Unit] =
    intervalTables.init(key, entries, entryOffset, tableOffset, amount)

  override def fillTable(key: Key, entry: Entry, tableOffset: Value, amount: Value): JOption[data.MayJoin.WithJoin, Unit] =
    intervalTables.fillTable(key, entry, tableOffset, amount)

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Value, srcOffset: Value, amount: Value): JOption[data.MayJoin.WithJoin, Unit] =
    intervalTables.copy(dstKey, srcKey, dstOffset, srcOffset, amount)

  override def putNew(key: Key): Unit =
    intervalTables.putNew(key)

  override def putNew(key: Key, limit: Limit[Topped[Int]]): Unit =
    intervalTables.putNew(key, Limit(limit.min, limit.max))


  override type State = intervalTables.State
  override def getState: State = intervalTables.getState
  override def setState(st: State): Unit = intervalTables.setState(st)
  override def join: Join[State] = intervalTables.join
  override def widen: Widen[State] = intervalTables.widen

  def tableIsSound[cValue, cEntry](c: ConcreteSizedTable[cValue, Key, cEntry])(using Soundness[Limit[Int], Limit[NumericInterval[Int]]]): IsSound = {
    intervalTables.tableIsSound(c)
  }
}