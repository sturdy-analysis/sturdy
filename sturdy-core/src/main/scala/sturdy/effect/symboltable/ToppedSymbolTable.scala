package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.{*, given}

import scala.collection.mutable
import ToppedSymbolTable.*

trait ToppedSymbolTable[Key, Symbol, Entry](using Join[Entry], Top[Entry]) extends SymbolTable[Key, Topped[Symbol], Entry, WithJoin], Effectful:

  protected var tables: Map[Key, Topped[Table[Symbol, Entry]]] = Map()
  private var dirtyTables = Set[Key]()

  def getSymbolTables: Tables[Key, Symbol, Entry] = tables
  def setSymbolTables(s: Tables[Key, Symbol, Entry]): Unit =
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
    tables += key -> Topped.Actual(Table(Map(), Set()))
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
            case Some(Topped.Actual(gTab)) => Join(gTab, fTab).ifChanged(tables += fkey -> Topped.Actual(_))

        dirtyTables ++= fDirty
        dirtyTables ++= snapDirtyTables
      }
    }

object ToppedSymbolTable:
  type Tables[Key, Symbol, Entry] = Map[Key, Topped[Table[Symbol, Entry]]]

  given CombineTable[Symbol, Entry, W <: Widening](using Combine[Entry, W]): Combine[Table[Symbol, Entry], W] with
    override def apply(old: Table[Symbol, Entry], now: Table[Symbol, Entry]): MaybeChanged[Table[Symbol, Entry]] =
      if (old.dirtySymbols.size >= now.dirtySymbols.size)
        combineFrom(old, now)
      else
        combineFrom(now, old)

    private def combineFrom(tab1: Table[Symbol, Entry], tab2: Table[Symbol, Entry]): MaybeChanged[Table[Symbol, Entry]] =
      var tab = tab1.underlying
      var dirty = tab1.dirtySymbols
      var changed = false
      for (s <- tab2.dirtySymbols) {
        val now = tab2.underlying(s)
        tab.get(s) match {
          case None =>
            tab += s -> now
            dirty += s
            changed = true
          case Some(old) =>
            Combine[MayMust[Entry], W](old, now).ifChanged { joined =>
              tab += s -> joined
              dirty += s
              changed = true
            }
        }
      }
      MaybeChanged(Table(tab, dirty), changed)

  case class Table[Symbol, Entry](val underlying: Map[Symbol, MayMust[Entry]], val dirtySymbols: Set[Symbol]):
    inline def updated(symbol: Symbol, entry: Entry): Table[Symbol, Entry] =
      Table(underlying.updated(symbol, MayMust.Must(entry)), dirtySymbols + symbol)
    inline def updated(symbol: Symbol, entry: MayMust[Entry]): Table[Symbol, Entry] =
      Table(underlying.updated(symbol, entry), dirtySymbols + symbol)
    def allMay: Table[Symbol, Entry] =
      var newUnderlying = underlying
      var newDirtySymbols = dirtySymbols
      for ((s, mentry) <- underlying if mentry.isMust) {
        newDirtySymbols += s
        newUnderlying += s -> mentry.asMay
      }
      Table(newUnderlying, newDirtySymbols)


