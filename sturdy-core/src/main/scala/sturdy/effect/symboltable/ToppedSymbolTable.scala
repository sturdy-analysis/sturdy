package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.{*, given}

import ToppedSymbolTable.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.TrySturdy

trait ToppedSymbolTable[Key, Symbol, Entry](using Join[Entry], Top[Entry]) extends SymbolTable[Key, Topped[Symbol], Entry, WithJoin], Effectful:

  protected var tables: Map[Key, Topped[Table[Symbol, Entry]]] = Map()
  private var dirtyTables = Set[Key]()

  def getSymbolTables: Tables[Key, Symbol, Entry] = tables
  def setSymbolTables(s: Tables[Key, Symbol, Entry]): Unit =
    tables = s
  
  override def tableGet(key: Key, symbol: Topped[Symbol]): OptionA[Entry] =
    tables(key) match
      case Topped.Top => OptionA.NoneSome(Top.top)
      case Topped.Actual(tab) => symbol match
        case Topped.Top =>
          val vals = tab.underlying.values.map(_.get)
          OptionA.NoneSome(vals.reduce(Join(_, _).get))
        case Topped.Actual(sym) => tab.underlying.get(sym) match
          case None => OptionA.None()
          case Some(MayMust.Must(entry)) => OptionA.some(entry)
          case Some(MayMust.May(entry)) => OptionA.noneSome(entry)

  override def tableSet(key: Key, symbol: Topped[Symbol], newEntry: Entry): Unit =
    dirtyTables += key
    tables(key) match
      case Topped.Top => // nothing
      case Topped.Actual(tab) => symbol match
        case Topped.Top =>
          tables += key -> Topped.Top
        case Topped.Actual(sym) =>
          val newTab = Topped.Actual(tab.updated(sym, newEntry))
          tables += key -> newTab

  override def addEmptyTable(key: Key): Unit =
    tables += key -> Topped.Actual(Table(Map(), Set()))
    dirtyTables += key

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ToppedSymbolTableJoiner[A] 
  class ToppedSymbolTableJoiner[A] extends ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    val snapshot = tables
    val snapDirtyTables = dirtyTables
    dirtyTables = Set()
    var fTables: Map[Key, Topped[Table[Symbol, Entry]]] = null
    var fDirty: Set[Key] = null

    override def inbetween_(): Unit =
      fTables = tables
      fDirty = dirtyTables
      tables = snapshot
      dirtyTables = Set()

    override def retainOnlyFirst_(fRes: TrySturdy[A]): Unit =
      tables = fTables
      dirtyTables = snapDirtyTables ++ fDirty

    override def retainOnlySecond_(gRes: TrySturdy[A]): Unit =
      dirtyTables ++= snapDirtyTables

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for (fkey <- fDirty) fTables(fkey) match
        case Topped.Top =>
          tables += fkey -> Topped.Top
        case Topped.Actual(fTab) => tables.get(fkey) match
          case None => tables += fkey -> Topped.Actual(fTab.allMay)
          case Some(Topped.Top) => // leave at top
          case Some(Topped.Actual(gTab)) => Join(gTab, fTab).ifChanged(tables += fkey -> Topped.Actual(_))
      for (tkey <- dirtyTables if !fTables.isDefinedAt(tkey)) {
        tables(tkey)  match
          case Topped.Actual(tab) => tables += tkey -> Topped.Actual(tab.allMay)
          case _ =>
      }

      dirtyTables ++= fDirty
      dirtyTables ++= snapDirtyTables
  }

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    // - all tables in c are present in the abstract
    // - all abstract tables with at least one 'must' entry have a concrete counterpart
    // - for each key in c, tabs(key) is sound
    val cTables = c.getTables
    tables.filterNot { (key,_) => cTables.isDefinedAt(key) }.foreachEntry { (k, aTab) => aTab match
      case Topped.Actual(tab) =>
        if (!tab.isAllMay)
          return IsSound.NotSound(s"Definite table with key $k not present in concrete tables.")
      case _ =>
    }
    cTables.foreachEntry { (key, cTab) =>
      val aTab = tables.getOrElse(key, { return IsSound.NotSound(s"Key $key not present in topped symbol table.") })
      val tabSound = tabInstanceIsSound(cTab, aTab)
      if (tabSound.isNotSound)
        return tabSound
    }
    IsSound.Sound

  def tabInstanceIsSound[cEntry](c: Map[Symbol, cEntry], a: Topped[Table[Symbol,Entry]])(using entrySound: Soundness[cEntry, Entry]): IsSound =
    // all entries in c are approximated by corresponding entry in a
    // all abstract symbols not defined in c point to a 'may' entry
    a match
      case Topped.Top => return IsSound.Sound
      case Topped.Actual(aTab) =>
        aTab.underlying.filterNot { (symbol,_) => c.isDefinedAt(symbol) }.foreachEntry { (s, aEntry) =>
          if (aEntry.isMust)
            return IsSound.NotSound(s"Definite entry $aEntry with symbol $s not present in concrete table.")
        }
        c.foreachEntry { (key, cEntry) =>
          val aEntry = aTab.underlying.getOrElse(key,
            { return IsSound.NotSound(s"Concrete entry $cEntry with symbol $key not present in abstract table.") })
          val eSound = entrySound.isSound(cEntry, aEntry.get)
          if (!eSound.isSound)
            return IsSound.NotSound(s"Concrete entry $cEntry with symbol $key not approximated by abstract entry ${aEntry.get}.")
        }
        IsSound.Sound

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

    def isAllMay: Boolean = underlying.forall((_,e) => !e.isMust)


