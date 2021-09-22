package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.{*, given}

import scala.collection.mutable
import ToppedSymbolTable.*
import sturdy.fix.Widening
import sturdy.fix.widenMap

trait ToppedSymbolTable[Key, Symbol, Entry](using JoinValue[Entry], Top[Entry]) extends SymbolTable[Key, Topped[Symbol], Entry], Effectful:

  override type TableJoin[A] = Join[A]

  protected var tables: Map[Key, Topped[Table[Symbol, Entry]]] = Map()
  private var dirtyTables = Set[Key]()

  def getSymbolTables: State[Key, Symbol, Entry] = tables
  def setSymbolTables(s: State[Key, Symbol, Entry]): Unit =
    tables = s
  
  override def tableGet(key: Key, symbol: Topped[Symbol]): OptionA[Entry] =
    tables(key) match
      case Topped.Top => OptionA.NoneSome(Iterable.single(Top.top))
      case Topped.Actual(tab) => symbol match
        case Topped.Top => OptionA.NoneSome(tab.underlying.values.map(_.get))
        case Topped.Actual(sym) => tab.underlying.get(sym) match
          case None => OptionA.None()
          case Some(MayMust.Must(entry)) => OptionA.some(entry)
          case Some(MayMust.May(entry)) => OptionA.noneSome(entry)

  override def tableSet(key: Key, symbol: Topped[Symbol], newEntry: Entry): Unit =
    dirtyTables += key
    tables(key) match
      case Topped.Top => // nothing
      case Topped.Actual(tab) => symbol match
        case Topped.Top => tables += key -> Topped.Top
        case Topped.Actual(sym) =>
          val newTab = Topped.Actual(tab.updated(sym, newEntry))
          tables += key -> newTab

  override def addEmptyTable(key: Key): Unit =
    tables += key -> Topped.Actual(new Table(Map(), Set()))
    dirtyTables += key

  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = tables
    val snapDirtyTables = dirtyTables
    dirtyTables = Set()

    super.joinComputations(f){
      val fTables = tables
      tables = snapshot
      val fDirty = dirtyTables
      dirtyTables = Set()

      try g finally {
        for (fkey <- fDirty) fTables(fkey) match
          case Topped.Top => tables += fkey -> Topped.Top
          case Topped.Actual(fTab) => tables.get(fkey) match
            case None => tables += fkey -> Topped.Actual(fTab.allMay)
            case Some(Topped.Top) => // leave at top
            case Some(Topped.Actual(gTab)) => tables += fkey -> Topped.Actual(fTab.join(gTab))

        dirtyTables ++= fDirty
        dirtyTables ++= snapDirtyTables
      }
    }

object ToppedSymbolTable:
  type State[Key, Symbol, Entry] = Map[Key, Topped[Table[Symbol, Entry]]]

//  given Widen[Key, Symbol, Entry]: Widening[Map[Key, Topped[Table[Symbol, Entry]]]] =
//    new widenMap(using new Finite[Key] {}, new Topped.nestedToppedWidening)
//
//  given WidenTable[Symbol, Entry]: Widening[Table[Symbol, Entry]] with
//    override def widen(old: Table[Symbol, Entry], now: Table[Symbol, Entry]): Table[Symbol, Entry] = ???

  class Table[Symbol, Entry](val underlying: Map[Symbol, MayMust[Entry]], val dirtySymbols: Set[Symbol]):
    inline def updated(symbol: Symbol, entry: Entry): Table[Symbol, Entry] =
      new Table(underlying.updated(symbol, MayMust.Must(entry)), dirtySymbols + symbol)
    inline def updated(symbol: Symbol, entry: MayMust[Entry]): Table[Symbol, Entry] =
      new Table(underlying.updated(symbol, entry), dirtySymbols + symbol)
    def allMay: Table[Symbol, Entry] =
      var newUnderlying = underlying
      var newDirtySymbols = dirtySymbols
      for ((s, mentry) <- underlying if mentry.isMust) {
        newDirtySymbols += s
        newUnderlying += s -> mentry.asMay
      }
      new Table(newUnderlying, newDirtySymbols)

    inline def join(that: Table[Symbol, Entry])(using j: JoinValue[Entry]) = combine(that, j.joinValues)
    inline def widen(that: Table[Symbol, Entry])(using w: Widening[Entry]) = combine(that, w.widen)

    def combine(that: Table[Symbol, Entry], com: (Entry, Entry) => Entry): Table[Symbol, Entry] =
      if (this.dirtySymbols.size >= that.dirtySymbols.size)
        this.combineFrom(that, com)
      else
        that.combineFrom(this, com)

    inline private def combineFrom(that: Table[Symbol, Entry], com: (Entry, Entry) => Entry): Table[Symbol, Entry] =
      var tab = this.underlying
      var dirty = this.dirtySymbols
      for (s <- that.dirtySymbols) {
        val now = that.underlying(s)
        val e = tab.get(s) match {
          case None => now
          case Some(old) => old.combine(now, com)
        }
        tab += s -> e
        dirty += s
      }
      Table(tab, dirty)

