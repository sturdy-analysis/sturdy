package sturdy.effect.symboltable

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.data.NoJoin
import sturdy.effect.Effect

trait SymbolTable[Key, Symbol, Entry, J[_] <: MayJoin[_]] extends Effect:
  def get(key: Key, symbol: Symbol): JOption[J, Entry]
  def set(key: Key, symbol: Symbol, newEntry: Entry): Unit
  def putNew(key: Key): Unit

