package sturdy.effect.symboltable

import sturdy.data.*

import scala.collection.mutable

trait ConcreteSymbolTable[Key, Symbol, Entry] extends SymbolTable[Key, Symbol, Entry, NoJoin]:

  protected val tables: mutable.Map[Key, mutable.Map[Symbol, Entry]] = mutable.Map()

  override def tableGet(key: Key, symbol: Symbol): OptionC[Entry] =
    OptionC(tables(key).get(symbol))

  override def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    (tables(key))(symbol) = newEntry

  override def addEmptyTable(key: Key): Unit =
    tables(key) = mutable.Map()
    
  def getTables: mutable.Map[Key, mutable.Map[Symbol, Entry]] = tables
