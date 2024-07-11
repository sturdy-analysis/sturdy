package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effect
import sturdy.effect.symboltable.ConstantSymbolTable.Tables
import sturdy.values.*
import sturdy.values.integer.{IntervalRange}

class IntervalSymbolTable[Key: Finite, IV: IntervalRange, Entry: Join] extends SymbolTable[Key, IV, Entry, WithJoin], Effect:
  private val constantSymbolTable: ConstantSymbolTable[Key, Int, Entry] = new ConstantSymbolTable

  def get(key: Key, symbol: IV): JOptionA[Entry] =
    IntervalRange(symbol) match
      case Some(range) =>
        val symbols = constantSymbolTable.symbols(key)
        if(symbols.isEmpty)
          constantSymbolTable.get(key, Topped.Top)
        else
          range.intersect(Range.inclusive(symbols.min,symbols.max)).foldLeft(JOptionA.none)(
            (res, i) => Join(res, constantSymbolTable.get(key, Topped.Actual(i))).get
          )
      case None => constantSymbolTable.get(key, Topped.Top)

  def set(key: Key, symbol: IV, newEntry: Entry): Unit =
    IntervalRange(symbol) match
      case Some(range) => range.foreach(
        i => constantSymbolTable.set(key, Topped.Actual(i), newEntry)
      )
      case None =>
        constantSymbolTable.set(key, Topped.Top, newEntry)

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

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Int, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    constantSymbolTable.tableIsSound(c)
