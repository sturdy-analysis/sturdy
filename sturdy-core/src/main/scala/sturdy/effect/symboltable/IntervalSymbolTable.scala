package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.symboltable.ConstantSymbolTable.Tables
import sturdy.values.*
import sturdy.values.integer.NumericInterval

import Numeric.Implicits.infixNumericOps
import Ordering.Implicits.infixOrderingOps

class IntervalSymbolTable[Key, I, Entry](rangeLimit: Int)(using Finite[Key], Join[Entry], Numeric[I]) extends SymbolTable[Key, NumericInterval[I], Entry, WithJoin], Effect:
  private val constantSymbolTable: ConstantSymbolTable[Key, I, Entry] = new ConstantSymbolTable

  private val one = summon[Numeric[I]].one

  def get(key: Key, symbol: NumericInterval[I]): JOptionA[Entry] =
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

  def set(key: Key, symbol: NumericInterval[I], newEntry: Entry): Unit =
    if (symbol.countOfNumsInInterval <= rangeLimit)
      var i = symbol.low
      while (i <= symbol.high)
        constantSymbolTable.set(key, Topped.Actual(i), newEntry)
        i += one
    else
      constantSymbolTable.set(key, Topped.Top, newEntry)

  def size(key: Key, symbol: NumericInterval[I]): Int = ???

  override def grow(key: Key, delta: Byte, initEntry: Entry): Byte = ???

  override def fill(key: Key, range: Byte, newEntry: Entry, length: Byte): Unit = ???

  override def copy(key: Key, range: Byte, dest: Key): Unit = ???

  override def init(key: Key, newEntry: Entry): Unit = ???

  //override def drop(key: Key, symbol: Topped[Symbol]): Unit = ???
  //override def drop(key: Key, symbol: Symbol): Unit = ???

  def putNew(key: Key): Unit =
    constantSymbolTable.putNew(key)

  override type State = constantSymbolTable.State
  override def getState: State =
    constantSymbolTable.getState
  def setState(state: State): Unit =
    constantSymbolTable.setState(state)
  override def join: Join[State] = constantSymbolTable.join
  override def widen: Widen[State] = constantSymbolTable.widen

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    constantSymbolTable.makeComputationJoiner

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, I, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    constantSymbolTable.tableIsSound(c)
