package sturdy.effect.symboltable

import sturdy.data.*

class ConcreteSymbolTable[Key, Symbol, Entry] extends DecidableSymbolTable[Key, Symbol, Entry]:

  protected var tables: Map[Key, Map[Symbol, Entry]] = Map()

  override def tableGet(key: Key, symbol: Symbol): JOptionC[Entry] =
    JOptionC(tables(key).get(symbol))

  override def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    tables += key -> (tables(key) + (symbol -> newEntry))

  override def addEmptyTable(key: Key): Unit =
    tables += key -> Map()
    
  override def getState: Map[Key, Map[Symbol, Entry]] = tables
  override def setState(tables: Map[Key, Map[Symbol, Entry]]): Unit = this.tables = tables
