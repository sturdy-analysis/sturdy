package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy

class UpperBoundSymbolTable[Key, Symbol, Entry](emptyEntry: Entry)(using Join[Entry]) extends SymbolTable[Key, Symbol, Entry, WithJoin], Effectful:

  protected var tables: Map[Key, Entry] = Map()

  override def tableGet(key: Key, symbol: Symbol): JOptionA[Entry] =
    JOptionA.noneSome(tables(key))

  override def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    Join(tables(key), newEntry).ifChanged(tables += key -> _)

  override def addEmptyTable(key: Key): Unit =
    tables += key -> emptyEntry

  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new UpperBoundSymbolTableJoiner[A])
  private class UpperBoundSymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: Map[Key, Entry] = _
    private var fDirty: Set[Key] = _

    override def inbetween(): Unit =
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
    c.getState.foreachEntry { (key, cTab) =>
      val aEntry = tables.getOrElse(key, { return IsSound.NotSound(s"Key $key not present in topped symbol table.") })
      for (cEntry <- cTab.values)
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          return eSound
    }
    IsSound.Sound

  type State = Map[Key, Entry]
  override def getState: Map[Key, Entry] = tables
  override def setState(s: Map[Key, Entry]): Unit = tables = s
