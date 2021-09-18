package sturdy.effect.symboltable

import sturdy.data.Option

trait SymbolTable[Key, Symbol, Entry]:
  type TableJoin[A]

  def tableGet(key: Key, symbol: Symbol): Option[TableJoin, Entry]
  def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit
  
  def addEmptyTable(key: Key): Unit
