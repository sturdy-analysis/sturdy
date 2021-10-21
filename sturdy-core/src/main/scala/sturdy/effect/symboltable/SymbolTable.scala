package sturdy.effect.symboltable

import sturdy.data.Option
import sturdy.data.OptionC
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effectful
import sturdy.effect.TrySturdy

trait SymbolTable[Key, Symbol, Entry, MayJoin[_]]:
  def tableGet(key: Key, symbol: Symbol): Option[MayJoin, Entry]
  def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit
  
  def addEmptyTable(key: Key): Unit


trait DecidableSymbolTable[Key, Symbol, Entry] extends Effectful:
  val table: ConcreteSymbolTable[Key, Symbol, Entry]

  def tableGetDecidable(key: Key, symbol: Symbol): OptionC[Entry] =
    table.tableGet(key, symbol)
  def tableSetDecidable(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    table.tableSet(key, symbol, newEntry)

  def addEmptyTableDecidable(key: Key): Unit =
    table.addEmptyTable(key)

  // TODO delegate to table
  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    def inbetween(): Unit = ???
    def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = ???
    def retainOnlyFirst(fRes: TrySturdy[A]): Unit = ???
    def retainOnlySecond(gRes: TrySturdy[A]): Unit = ???
  }