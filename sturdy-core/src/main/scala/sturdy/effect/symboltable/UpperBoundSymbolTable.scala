package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.effect.Effect
import sturdy.values.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy

class UpperBoundSymbolTable[Key, Symbol, Entry](emptyEntry: Entry)(using Join[Entry], Widen[Entry], Finite[Key]) extends SymbolTable[Key, Symbol, Entry, WithJoin], Effect:

  protected var tables: MayMap[Key, Entry] = MayMap()

  override def get(key: Key, symbol: Symbol): JOptionA[Entry] =
    JOptionA.noneSome(tables(key))

  override def set(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    Join(tables(key), newEntry).ifChanged(tables += key -> _)

  override def putNew(key: Key): Unit =
    tables += key -> emptyEntry

  private class UpperBoundSymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: MayMap[Key, Entry] = _
    private var fDirty: Set[Key] = _

    override def inbetween(fFailed: Boolean): Unit =
      fTables = tables
      tables = snapshot

    override def retainNone(): Unit =
      tables = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      tables = fTables

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for ((fKey, fEntry) <- fTables)
        tables.get(fKey) match
          case None => tables += fKey -> fEntry
          case Some(gEntry) => tables += fKey -> Join(fEntry, gEntry).get
  }

  def tableIsSound[cSymbol, cEntry](c: ConcreteSymbolTable[Key, cSymbol, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    c.entries.foreachEntry { (key, cTab) =>
      val aEntry = tables.getOrElse(key, { return IsSound.NotSound(s"Key $key not present in topped symbol table.") })
      for (cEntry <- cTab.values)
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          return eSound
    }
    IsSound.Sound

  type State = MayMap[Key, Entry]
  override def getState: MayMap[Key, Entry] = tables
  override def setState(s: MayMap[Key, Entry]): Unit = tables = s
  override def join: Join[MayMap[Key, Entry]] = implicitly
  override def widen: Widen[MayMap[Key, Entry]] = implicitly
