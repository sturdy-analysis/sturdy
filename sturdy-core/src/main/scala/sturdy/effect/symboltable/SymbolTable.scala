package sturdy.effect.symboltable

import sturdy.data.Option
import sturdy.data.NoJoin
import sturdy.effect.Effectful

trait SymbolTable[Key, Symbol, Entry, MayJoin[_]] extends Effectful:
  def tableGet(key: Key, symbol: Symbol): Option[MayJoin, Entry]
  def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit
  def addEmptyTable(key: Key): Unit

trait DecidableSymbolTable[Key, Symbol, Entry] extends SymbolTable[Key, Symbol, Entry, NoJoin]:
  def getTables: Map[Key, Map[Symbol, Entry]]
  def setTables(tables: Map[Key, Map[Symbol, Entry]]): Unit
