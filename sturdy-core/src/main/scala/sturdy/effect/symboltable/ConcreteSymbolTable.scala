package sturdy.effect.symboltable

import sturdy.data.*

class ConcreteSymbolTable[Key, Symbol, Entry] extends DecidableSymbolTable[Key, Symbol, Entry]:

  protected var tables: Map[Key, Map[Symbol, Entry]] = Map()

  override def get(key: Key, symbol: Symbol): JOptionC[Entry] =
    JOptionC(tables(key).get(symbol))

  override def set(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    tables += key -> (tables(key) + (symbol -> newEntry))

  override def putNew(key: Key): Unit =
    tables += key -> Map()
    
  override def getState: Map[Key, Map[Symbol, Entry]] = tables
  override def setState(tables: Map[Key, Map[Symbol, Entry]]): Unit = this.tables = tables
