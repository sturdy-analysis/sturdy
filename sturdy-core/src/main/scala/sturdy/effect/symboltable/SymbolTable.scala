package sturdy.effect.symboltable

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.data.NoJoin
import sturdy.effect.Effect

/**
  * Map[Key, Table]
  * Table = Map[Symbol, Entry]
  *
  * @tparam Key
  * @tparam Symbol
  * @tparam Entry
  * @tparam J
  */
trait SymbolTable[Key, Symbol, Entry, J[_] <: MayJoin[_]] extends Effect:
  def get(key: Key, symbol: Symbol): JOption[J, Entry]
  def set(key: Key, symbol: Symbol, newEntry: Entry): Unit
  def size(key: Key, symbol: Symbol): Int
  /*def grow(key: Key, delta: Int, initEntry: Entry): Int
  def fill(key: Key, range: Int, newEntry: Entry): Unit
  def copy(key: Key, range: Int, dest: Addr): Unit
  def init(key: Key, newEntry: Entry): Unit
  def drop(key: Key, symbol: Symbol): Unit
*/
  def putNew(key: Key): Unit

  final def getOrElse(key: Key, symbol: Symbol, default: => Entry)(using J[Entry]): Entry =
    get(key, symbol).getOrElse(default)

