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

class IntervalSymbolTable[Key, I, Entry](rangeLimit: I)(using Finite[Key], Join[Entry], Numeric[I]) extends SymbolTable[Key, NumericInterval[I], Entry, WithJoin], Effect:
  private val constantSymbolTable: ConstantSymbolTable[Key, I, Entry] = new ConstantSymbolTable

  private val one = summon[Numeric[I]].one

  def get(key: Key, symbol: NumericInterval[I]): JOptionA[Entry] = symbol match
    case NumericInterval.Bounded(low, high) if high - low <= rangeLimit =>
      var result = constantSymbolTable.get(key, Topped.Actual(low))
      var i = low + one
      while (i <= high)
        result = Join(result, constantSymbolTable.get(key, Topped.Actual(i))).get
        i += one
      result
    case _ =>
      constantSymbolTable.get(key, Topped.Top)

  def set(key: Key, symbol: NumericInterval[I], newEntry: Entry): Unit = symbol match
    case NumericInterval.Bounded(low, high) if high - low <= rangeLimit =>
      var i = low
      while (i <= high)
        constantSymbolTable.set(key, Topped.Actual(i), newEntry)
        i += one
    case _ =>
      constantSymbolTable.set(key, Topped.Top, newEntry)

  def putNew(key: Key): Unit =
    constantSymbolTable.putNew(key)

  override type State = constantSymbolTable.State
  override def getState: Tables[Key, I, Entry] =
    constantSymbolTable.getState
  def setState(state: Tables[Key, I, Entry]): Unit =
    constantSymbolTable.setState(state)
  override def join: Join[Tables[Key, I, Entry]] = constantSymbolTable.join
  override def widen: Widen[Tables[Key, I, Entry]] = constantSymbolTable.widen

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    constantSymbolTable.makeComputationJoiner

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, I, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    constantSymbolTable.tableIsSound(c)
