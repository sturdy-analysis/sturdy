package sturdy.effect.symboltable

import sturdy.data.*

trait ConcreteSymbolTable[Key, Symbol, Entry] extends DecidableSymbolTable[Key, Symbol, Entry]:

  protected var tables: Map[Key, Map[Symbol, Entry]] = Map()

  override def tableGet(key: Key, symbol: Symbol): OptionC[Entry] =
    OptionC(tables(key).get(symbol))

  override def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    tables += key -> (tables(key) + (symbol -> newEntry))

  override def addEmptyTable(key: Key): Unit =
    tables += key -> Map()
    
  def getTables: Map[Key, Map[Symbol, Entry]] = tables
