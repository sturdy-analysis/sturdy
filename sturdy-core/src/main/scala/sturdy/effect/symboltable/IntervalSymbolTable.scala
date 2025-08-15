package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.symboltable.SizedConstantTable.Tables
import sturdy.effect.symboltable.SizedSymbolTable.Limit
import sturdy.values.*
import sturdy.values.integer.IntervalRange

import scala.util.boundary
import scala.util.boundary.break

class IntervalSymbolTable[Value, Key: Finite, Symbol: IntervalRange, Entry: Join, Size] extends SizedSymbolTable[Value, Key, Symbol, Entry, Size, WithJoin], Effect:
  private val constantSymbolTable: SizedConstantTable[Value, Key, Entry] = new SizedConstantTable()

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

  private def intersect(range1: Range, range2: Range): Range =
    val inc1 = range1.inclusive
    val inc2 = range2.inclusive
    assert(inc1.step == 1 && inc2.step == 1)
    val start = if(inc1.start <= inc2.start) inc2.start else inc1.start
    val end = if(inc1.end <= inc2.end) inc1.end else inc2.end
    Range.inclusive(start, end)


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

  override def putNew(key: Key, limit: Limit[Size]): Unit = ???

  override def size(key: Key): Size = ???

  override def grow(key: Key, newSize: Size, initEntry: Entry): JOption[WithJoin, Size] = ???

  override def init(key: Key, entries: Vector[Entry], entryOffset: Value, tableOffset: Value, amount: Value): JOption[WithJoin, Unit] = ???

  override def fillTable(key: Key, entry: Entry, tableOffset: Value, amount: Value): JOption[WithJoin, Unit] = ???

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Value, srcOffset: Value, amount: Value): JOption[WithJoin, Unit] = ???

  override type State = constantSymbolTable.State
  override def getState: State =
    constantSymbolTable.getState
  def setState(state: State): Unit =
    constantSymbolTable.setState(state)
  override def join: Join[State] = constantSymbolTable.join
  override def widen: Widen[State] = constantSymbolTable.widen

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    constantSymbolTable.makeComputationJoiner

  def tableIsSound[cValue, cEntry](c: ConcreteSizedTable[cValue, Key, cEntry])(using Soundness[Limit[Int], Limit[Symbol]]): IsSound = ???
