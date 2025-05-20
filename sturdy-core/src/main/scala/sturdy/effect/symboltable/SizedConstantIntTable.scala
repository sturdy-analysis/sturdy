package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.effect.Effect
import sturdy.values.{*, given}
import SizedConstantIntTable.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy

import scala.util.boundary, boundary.break
import Either as Eith


class SizedConstantIntTable[Key, Entry](using Finite[Key], Join[Entry]) extends SizedSymbolTable[Key, Topped[Int], Entry, Topped[Int], WithJoin], Effect:

  protected var tables: Map[Key, Either[Table[Int, Entry], Entry]] = Map()
  private var dirtyTables = Set[Key]()

  override def get(key: Key, symbol: Topped[Int]): JOptionA[Entry] =
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

  override def set(key: Key, symbol: Topped[Int], newEntry: Entry): JOption[WithJoin, Unit] =
    symbol match {
      case Topped.Actual(i) if i < 0 =>
        return JOptionA.none
      case _ => ()
    }
    dirtyTables += key
    val tab = tables(key)
    val length =
      tab match {
        case Right(_) => Topped.Top
        case Left(t) => t.limit.min match {
          case Topped.Actual(min) => Topped.Actual(math.max(min, t.underlying.size))
          case Topped.Top => Topped.Top
        }
      }
    length match
      case Topped.Actual(len) => symbol match {
        case Topped.Actual(i) if (i >= len) => return JOptionA.none
        case _ => ()
      }
      case _ => ()

    tables(key) match
      case Right(entry) =>
        Join(entry, newEntry).ifChanged(tables += key -> Right(_))
      case Left(tab) => symbol match
        case Topped.Top =>
          tables += key -> Right((tab.entries + newEntry).reduce(Join(_, _).get))
        case Topped.Actual(sym) =>
          tables += key -> Left(tab.updated(sym, newEntry))

    length match
      case Topped.Top => JOptionA.noneSome(())
      case Topped.Actual(len) => symbol match
        case Topped.Top => JOptionA.noneSome(())
        case Topped.Actual(i) if (i < len) => JOptionA.some(())
        case _ => JOptionA.noneSome(())


  override def size(key: Key): Topped[Int] = tables(key) match
    case Left(table) => table.limit.min
    case _ => Topped.Top

  override def grow(key: Key, newSize: Topped[Int], initEntry: Entry): JOption[WithJoin, Topped[Int]] = {
    // TODO: finalize implementation
    tables(key) match {
      case Right(entry) =>
        Join(entry, initEntry).ifChanged(tables += key -> Right(_))
        JOptionA.noneSome(Topped.Top)
      case Left(tab) =>
        val oldSize = tab.entries.size
        // TODO: change this check to MayMust logic
        if (tab.dirtySymbols.isEmpty) {
          newSize match
            case Topped.Actual(newSz) =>
              // assumption: fully concrete table
              tab.limit.max match
                case Some(Topped.Actual(maxLimit)) =>
                  if (newSz > maxLimit) {
                    return JOptionA.none
                  }
                  for (i <- oldSize until newSz) {
                    tab.updated(i, initEntry)
                  }
                  return JOptionA.some(Topped.Actual(oldSize))
                case Some(Topped.Top) =>
                  tables += key -> Right((tab.entries + initEntry).reduce(Join(_, _).get))
                  return JOptionA.noneSome(Topped.Actual(oldSize))
                case None => ()
              JOptionA.none
        }
        JOptionA.noneSome(Topped.Top)
    }
  }

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[Topped[Int]] = SizedSymbolTable.Limit(Topped.Actual(0), None)): Unit =
    tables += key -> Left(Table(Map(), Set(), limit))
    dirtyTables += key

  override def putNew(key: Key): Unit =
    putNew(key, SizedSymbolTable.Limit(Topped.Actual(0), None))

  override type State = Tables[Key, Entry]
  override def getState: State = tables
  override def setState(s: State): Unit = tables = s
  override def join: Join[State] = JoinMap(using {
    case (Right(a), Right(b)) => Join(a, b).map(Right.apply)
    case (v1@Right(_), Left(_)) => Unchanged(v1)
    case (Left(_), v2@Right(_)) => Changed(v2)
    case (Left(t1), Left(t2)) => Join(t1, t2).map(Left.apply)
  })
  override def widen: Widen[State] = CombineFiniteKeyMap(using {
    case (Right(a), Right(b)) => Join(a, b).map(Right.apply)
    case (v1@Right(_), Left(_)) => Unchanged(v1)
    case (Left(_), v2@Right(_)) => Changed(v2)
    case (Left(t1), Left(t2)) => Join(t1, t2).map(Left.apply)
  })


  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ToppedSymbolTableJoiner[A])
  class ToppedSymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private val snapDirtyTables = dirtyTables
    dirtyTables = Set()
    private var fTables: Tables[Key, Entry] = _
    private var fDirty: Set[Key] = _

    override def inbetween(fFailed: Boolean): Unit =
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

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Int, cEntry])(using Soundness[cEntry, Entry]): IsSound = boundary:
    // - all tables in c are present in the abstract
    // - all abstract tables with at least one 'must' entry have a concrete counterpart
    // - for each key in c, tabs(key) is sound
    val cTables = c.entries
    tables.filterNot { (key,_) => cTables.isDefinedAt(key) }.foreachEntry { (k, aTab) => aTab match
      case Left(tab) =>
        if (!tab.isAllMay)
          break(IsSound.NotSound(s"Definite table with key $k not present in concrete tables."))
      case _ =>
    }
    cTables.foreachEntry { (key, cTab) =>
      val aTab = tables.getOrElse(key, { break(IsSound.NotSound(s"Key $key not present in topped symbol table.")) })
      val tabSound = tabInstanceIsSound(cTab, aTab)
      if (tabSound.isNotSound)
        break(tabSound)
    }
    IsSound.Sound

  def tabInstanceIsSound[cEntry](c: Map[Int, cEntry], a: Either[Table[Int,Entry], Entry])(using entrySound: Soundness[cEntry, Entry]): IsSound = boundary:
    // all entries in c are approximated by corresponding entry in a
    // all abstract symbols not defined in c point to a 'may' entry
    a match
      case Right(aEntry) =>
        c.foreachEntry { (key, cEntry) =>
          val eSound = entrySound.isSound(cEntry, aEntry)
          if (!eSound.isSound)
            break(IsSound.NotSound(s"Concrete entry $cEntry with symbol $key not approximated by abstract entry $aEntry."))
        }
        IsSound.Sound
      case Left(aTab) =>
        aTab.underlying.filterNot { (symbol,_) => c.isDefinedAt(symbol) }.foreachEntry { (s, aEntry) =>
          if (aEntry.isMust)
            break(IsSound.NotSound(s"Definite entry $aEntry with symbol $s not present in concrete table."))
        }
        c.foreachEntry { (key, cEntry) =>
          val aEntry = aTab.underlying.getOrElse(key,
            { break(IsSound.NotSound(s"Concrete entry $cEntry with symbol $key not present in abstract table.")) })
          val eSound = entrySound.isSound(cEntry, aEntry.get)
          if (!eSound.isSound)
            break(IsSound.NotSound(s"Concrete entry $cEntry with symbol $key not approximated by abstract entry ${aEntry.get}."))
        }
        IsSound.Sound


object SizedConstantIntTable:
  type Tables[Key, Entry] = Map[Key, Either[Table[Int, Entry], Entry]]

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
      // combine limits
      val limit1 = tab1.limit
      val limit2 = tab2.limit
      val finalLimit = if (limit1 != limit2) {
        changed = true
        (limit1, limit2) match
          case (SizedSymbolTable.Limit(l1, max1), SizedSymbolTable.Limit(l2, max2)) =>
            val joinedMin = Join(l1, l2).get
            // Join the max values if both are defined
            val joinedMax = (max1, max2) match
              case (Some(m1), Some(m2)) => Some(Join(m1, m2).get)
              case (Some(m), None) => Some(m)
              case (None, Some(m)) => Some(m)
              case (None, None) => None
            SizedSymbolTable.Limit(joinedMin, joinedMax)
      } else {
        limit1
      }
      MaybeChanged(Table(tab, dirty, finalLimit), changed)

  case class Table[Symbol, Entry](underlying: Map[Symbol, MayMust[Entry]], dirtySymbols: Set[Symbol], limit: SizedSymbolTable.Limit[Topped[Int]]):
    def entries: Set[Entry] = underlying.values.map(_.get).toSet
    inline def updated(symbol: Symbol, entry: Entry): Table[Symbol, Entry] =
      Table(underlying.updated(symbol, MayMust.Must(entry)), dirtySymbols + symbol, limit)
    inline def updated(symbol: Symbol, entry: MayMust[Entry]): Table[Symbol, Entry] =
      Table(underlying.updated(symbol, entry), dirtySymbols + symbol, limit)
    def allMay: Table[Symbol, Entry] =
      var newUnderlying = underlying
      var newDirtySymbols = dirtySymbols
      for ((s, mentry) <- underlying if mentry.isMust) {
        newDirtySymbols += s
        newUnderlying += s -> mentry.asMay
      }
      Table(newUnderlying, newDirtySymbols, limit)

    def isAllMay: Boolean = underlying.forall((_,e) => !e.isMust)

    def map[Symbol1, Entry1](mapSym: Symbol => Symbol1, mapEntry: Entry => Entry1): Table[Symbol1, Entry1] =
      Table(underlying.map((sym, entry) => (mapSym(sym), entry.map(mapEntry))), dirtySymbols.map(mapSym), limit)
