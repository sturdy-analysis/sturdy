package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.symboltable.SizedConstantTable.Tables
import sturdy.values.*
import sturdy.values.integer.NumericInterval

import Numeric.Implicits.infixNumericOps
import Ordering.Implicits.infixOrderingOps

// TODO: replace Size with NumericInterval to improve precision
class IntervalSymbolTable[Value, Key, Entry](rangeLimit: Int)(using Finite[Key], Join[Entry], Numeric[Int]) extends SizedSymbolTable[Value, Key, NumericInterval[Int], Entry, Topped[Int], WithJoin], Effect:
  private val constantSymbolTable: SizedConstantTable[Value, Key, Entry] = new SizedConstantTable

  private val one = summon[Numeric[Int]].one

  def get(key: Key, symbol: NumericInterval[Int]): JOptionA[Entry] =
    if (symbol.countOfNumsInInterval <= rangeLimit) {
      var result = constantSymbolTable.get(key, Topped.Actual(symbol.low))
      var i = symbol.low + one
      while (i <= symbol.high) {
        result = Join(result, constantSymbolTable.get(key, Topped.Actual(i))).get
        i += one
      }
      result
    } else {
      constantSymbolTable.get(key, Topped.Top)
    }

  def set(key: Key, symbol: NumericInterval[Int], newEntry: Entry): JOption[WithJoin, Unit] =
    if (symbol.countOfNumsInInterval <= rangeLimit)
      var i = symbol.low
      while (i <= symbol.high)
        constantSymbolTable.set(key, Topped.Actual(i), newEntry)
        i += one
    else
      constantSymbolTable.set(key, Topped.Top, newEntry)
    // TODO: implement size check logic
    JOptionA.some(())

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[Topped[Int]]): Unit = ???


  def putNew(key: Key): Unit =
    constantSymbolTable.putNew(key)

  override def size(key: Key): Topped[Int] = ???

  override def grow(key: Key, newSize: Topped[Int], initEntry: Entry): JOption[WithJoin, Topped[Int]] = ???

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

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Int, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    constantSymbolTable.tableIsSound(c)


