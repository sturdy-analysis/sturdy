package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.{Top, Topped}

import scala.collection.mutable

trait ToppedSymbolTable[Key, Symbol, Entry](using Top[Entry]) extends SymbolTable[Key, Topped[Symbol], Entry], Effectful:

  override type TableJoin[A] = Join[A]

  protected val tables: mutable.Map[Key, Topped[mutable.Map[Symbol, Entry]]] = mutable.Map()

  override def tableGet(key: Key, symbol: Topped[Symbol]): OptionA[Entry] =
    tables(key) match
      case Topped.Top => OptionA.NoneSome(Iterable.single(Top.top))
      case Topped.Actual(tab) => symbol match
        case Topped.Top => OptionA.NoneSome(tab.values)
        case Topped.Actual(sym) => OptionA(tab.get(sym))

  override def tableSet(key: Key, symbol: Topped[Symbol], newEntry: Entry): Unit =
    tables(key) match
      case Topped.Top => // nothing
      case Topped.Actual(tab) => symbol match
        case Topped.Top => tables(key) = Topped.Top
        case Topped.Actual(sym) => tab(sym) = newEntry

  override def addEmptyTable(key: Key): Unit =
    tables(key) = Topped.Actual(mutable.Map())

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    // TODO implement  
    ???