package sturdy.effect.symboltable

import sturdy.data.Option

trait SymbolTable[Key, Symbol, Entry, MayJoin[_]]:
  def tableGet(key: Key, symbol: Symbol): Option[MayJoin, Entry]
  def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit
  
  def addEmptyTable(key: Key): Unit
