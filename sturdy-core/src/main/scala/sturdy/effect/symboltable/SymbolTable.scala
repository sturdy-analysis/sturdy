package sturdy.effect.symboltable

import sturdy.data.{JOption, MayJoin, NoJoin}
import sturdy.effect.Effect
import sturdy.values.{Join, MaybeChanged}

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

  def putNew(key: Key): Unit

  final def getOrElse(key: Key, symbol: Symbol, default: => Entry)(using J[Entry]): Entry =
    get(key, symbol).getOrElse(default)

trait SizedSymbolTable[Key, Symbol, Entry, Size, J[_] <: MayJoin[_]] extends SymbolTable[Key, Symbol, Entry, J]:
  
  def size(key: Key): Size
  def grow(key: Key, delta: Size, initEntry: Entry): JOption[J, Size]

  def putNew(key: Key, limit: SizedSymbolTable.Limit[Size]): Unit
  
  
object SizedSymbolTable:  
  case class Limit[Size](min: Size, max: Option[Size]) {}
  
given joinLimit[V](using Join[V]): Join[SizedSymbolTable.Limit[V]] with
  def apply(l1: SizedSymbolTable.Limit[V], l2: SizedSymbolTable.Limit[V]): MaybeChanged[SizedSymbolTable.Limit[V]] =
    val min = Join(l1.min, l2.min)
    val max = l1.max match
      case None => l2.max
      case Some(m) => l2.max match
        case None => Some(m)
        case Some(n) => Some(Join(m, n).get)
    MaybeChanged(SizedSymbolTable.Limit(min.get, max), min.hasChanged || l1.max != l2.max)