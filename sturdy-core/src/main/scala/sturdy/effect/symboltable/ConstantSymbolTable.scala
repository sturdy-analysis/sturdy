package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.{*, given}

import scala.Either as Eith

import ConstantSymbolTable.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy

class ConstantSymbolTable[Key, Symbol, Entry](using Join[Entry]) extends SymbolTable[Key, Topped[Symbol], Entry, WithJoin], Effectful:

  protected var tables: Map[Key, Eith[Table[Symbol, Entry], Entry]] = Map()
  private var dirtyTables = Set[Key]()

  override def tableGet(key: Key, symbol: Topped[Symbol]): JOptionA[Entry] =
    tables(key) match
      case Right(entry) => JOptionA.NoneSome(entry)
      case Left(tab) => symbol match
        case Topped.Top =>
          if (tab.underlying.isEmpty)
            JOptionA.none
          else
            JOptionA.NoneSome(tab.entries.reduce(Join(_, _).get))
        case Topped.Actual(sym) => tab.underlying.get(sym) match
          case None => JOptionA.None()
          case Some(MayMust.Must(entry)) => JOptionA.some(entry)
          case Some(MayMust.May(entry)) => JOptionA.noneSome(entry)

  override def tableSet(key: Key, symbol: Topped[Symbol], newEntry: Entry): Unit =
    dirtyTables += key
    tables(key) match
      case Right(entry) =>
        Join(entry, newEntry).ifChanged(tables += key -> Right(_))
      case Left(tab) => symbol match
        case Topped.Top =>
          tables += key -> Right((tab.entries + newEntry).reduce(Join(_, _).get))
        case Topped.Actual(sym) =>
          tables += key -> Left(tab.updated(sym, newEntry))

  override def addEmptyTable(key: Key): Unit =
    tables += key -> Left(Table(Map(), Set()))
    dirtyTables += key

  override type State = Tables[Key, Symbol, Entry]
  def getState: Tables[Key, Symbol, Entry] = tables
  def setState(s: Tables[Key, Symbol, Entry]): Unit = tables = s


  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ToppedSymbolTableJoiner[A])
  class ToppedSymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private val snapDirtyTables = dirtyTables
    dirtyTables = Set()
    private var fTables: Tables[Key, Symbol, Entry] = _
    private var fDirty: Set[Key] = _

    override def inbetween(): Unit =
      fTables = tables
      fDirty = dirtyTables
      tables = snapshot
      dirtyTables = Set()

    override def retainNone(): Unit =
      tables = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      tables = fTables
      dirtyTables = snapDirtyTables ++ fDirty

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      dirtyTables ++= snapDirtyTables

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for (fkey <- fDirty) fTables(fkey) match
        case Right(fEntry) => tables.get(fkey) match
          case None => tables += fkey -> Right(fEntry)
          case Some(Right(gEntry)) => Join(fEntry, gEntry).ifChanged(tables += fkey -> Right(_))
          case Some(Left(gTab)) => tables += fkey -> Right((gTab.entries + fEntry).reduce(Join(_, _).get))
        case Left(fTab) => tables.get(fkey) match
          case None => tables += fkey -> Left(fTab.allMay)
          case Some(Right(gEntry)) => tables += fkey -> Right((fTab.entries + gEntry).reduce(Join(_, _).get))
          case Some(Left(gTab)) => Join(gTab, fTab).ifChanged(tables += fkey -> Left(_))
      for (tkey <- dirtyTables if !fTables.isDefinedAt(tkey)) {
        tables(tkey)  match
          case Left(tab) => tables += tkey -> Left(tab.allMay)
          case _ =>
      }

      dirtyTables ++= fDirty
      dirtyTables ++= snapDirtyTables
  }

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    // - all tables in c are present in the abstract
    // - all abstract tables with at least one 'must' entry have a concrete counterpart
    // - for each key in c, tabs(key) is sound
    val cTables = c.getState
    tables.filterNot { (key,_) => cTables.isDefinedAt(key) }.foreachEntry { (k, aTab) => aTab match
      case Left(tab) =>
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

  def tabInstanceIsSound[cEntry](c: Map[Symbol, cEntry], a: Eith[Table[Symbol,Entry], Entry])(using entrySound: Soundness[cEntry, Entry]): IsSound =
    // all entries in c are approximated by corresponding entry in a
    // all abstract symbols not defined in c point to a 'may' entry
    a match
      case Right(aEntry) =>
        c.foreachEntry { (key, cEntry) =>
          val eSound = entrySound.isSound(cEntry, aEntry)
          if (!eSound.isSound)
            return IsSound.NotSound(s"Concrete entry $cEntry with symbol $key not approximated by abstract entry $aEntry.")
        }
        IsSound.Sound
      case Left(aTab) =>
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

object ConstantSymbolTable:
  type Tables[Key, Symbol, Entry] = Map[Key, Eith[Table[Symbol, Entry], Entry]]

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

  case class Table[Symbol, Entry](underlying: Map[Symbol, MayMust[Entry]], val dirtySymbols: Set[Symbol]):
    def entries: Set[Entry] = underlying.values.map(_.get).toSet
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


