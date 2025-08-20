package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.symboltable.SizedConstantTable.Tables
import sturdy.effect.symboltable.SizedSymbolTable.Limit
import sturdy.values.*
import sturdy.values.addresses.{AddressLimits, AddressOffset}
import sturdy.values.integer.{IntegerOps, IntervalRange}

import scala.util.boundary
import scala.util.boundary.break

class IntervalSymbolTable[Key: Finite, Symbol: IntervalRange, Entry: Join, Size]
    extends SizedSymbolTable[Key, Symbol, Entry, Size, WithJoin], Effect:

  private val constantSymbolTable: SizedConstantTable[Key, Entry] = new SizedConstantTable()

  override def get(key: Key, symbol: Symbol): JOptionA[Entry] =
    IntervalRange(symbol) match
      case Some(range) =>
        val symbols = constantSymbolTable.symbols(key)
        if(symbols.isEmpty)
          constantSymbolTable.get(key, Topped.Top)
        else
          intersect(range,Range.inclusive(symbols.min,symbols.max)).foldLeft(JOptionA.none)(
            (res, i) => Join(res, constantSymbolTable.get(key, Topped.Actual(i))).get
          )
      case None => constantSymbolTable.get(key, Topped.Top)

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOptionA[Unit] = {
    IntervalRange(symbol) match
      case Some(range) => range.foreach(
        i => constantSymbolTable.set(key, Topped.Actual(i), newEntry)
      )
      case None =>
        constantSymbolTable.set(key, Topped.Top, newEntry)
    JOptionA.Some(())
  }

  override def putNew(key: Key): Unit =
    constantSymbolTable.putNew(key)

  override def putNew(key: Key, limit: Limit[Size]): Unit =
    // TODO: currently ignores limit.
    constantSymbolTable.putNew(key)

  override def size(key: Key): Size = ???

  override def grow(key: Key, newSize: Size, initEntry: Entry): JOption[WithJoin, Size] = ???

  override def init(key: Key, entries: Seq[Entry], entryOffset: Symbol, tableOffset: Symbol, amount: Size): JOption[WithJoin, Unit] =
    (IntervalRange(entryOffset), IntervalRange(tableOffset)) match
      case (_, None) | (None, _) =>
        for(entry <- entries)
          constantSymbolTable.set(key, Topped.Top, entry)
      case (Some(entryOffsetRange), Some(tableOffsetRange)) =>
        if(entryOffsetRange.size == 1 && tableOffsetRange.size == 1)
          val slice = entries.slice(entryOffsetRange.start, entries.size)
          for((entry, idx) <- slice.zipWithIndex) {
            constantSymbolTable.set(key, Topped.Actual(tableOffsetRange.start + idx), entry)
          }
        else
          for(entry <- entries)
            constantSymbolTable.set(key, Topped.Top, entry)

    JOptionA.Some(()) // TODO: Return None, when tableOffset + amount > tableSize


  override def fill(key: Key, entry: Entry, tableOffset: Symbol, amount: Size): JOption[WithJoin, Unit] =
    constantSymbolTable.set(key, Topped.Top, entry)

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Symbol, srcOffset: Symbol, amount: Size): JOption[WithJoin, Unit] = ???


  private def intersect(range1: Range, range2: Range): Range =
    val inc1 = range1.inclusive
    val inc2 = range2.inclusive
    assert(inc1.step == 1 && inc2.step == 1)
    val start = scala.math.max(inc1.start, inc2.start)
    val end = scala.math.min(inc1.end, inc2.end)
    Range.inclusive(start, end)

  private def union(range1: Range, range2: Range): Range =
    val inc1 = range1.inclusive
    val inc2 = range2.inclusive
    assert(inc1.step == 1 && inc2.step == 1)
    val start = scala.math.min(inc1.start, inc2.start)
    val end = scala.math.max(inc1.end, inc2.end)
    Range.inclusive(start, end)


  override type State = constantSymbolTable.State
  override def getState: State =
    constantSymbolTable.getState
  def setState(state: State): Unit =
    constantSymbolTable.setState(state)
  override def join: Join[State] = constantSymbolTable.join
  override def widen: Widen[State] = constantSymbolTable.widen

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    constantSymbolTable.makeComputationJoiner

  def tableIsSound[cEntry](c: ConcreteSizedTable[Key, cEntry])(using Soundness[Limit[Int], Limit[Symbol]]): IsSound = ???
