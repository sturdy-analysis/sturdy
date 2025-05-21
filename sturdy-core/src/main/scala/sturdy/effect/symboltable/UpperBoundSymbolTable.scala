package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.effect.Effect
import sturdy.values.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy
import sturdy.values.types.BaseType

import scala.util.boundary
import boundary.break

class SizedUpperBoundSymbolTable[Key, Symbol, Entry](emptyEntry: Entry)(using Join[Entry], Widen[Entry], Finite[Key]) extends UpperBoundSymbolTable[Key, Symbol, Entry](emptyEntry), SizedSymbolTable[Key, Symbol, Entry, BaseType[Int], WithJoin] {
  override def size(key: Key): BaseType[Int] = ???

  override def grow(key: Key, newSize: BaseType[Int], initEntry: Entry): JOption[WithJoin, BaseType[Int]] = ???

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[BaseType[Int]]): Unit = ???
}

class UpperBoundSymbolTable[Key, Symbol, Entry](emptyEntry: Entry)(using Join[Entry], Widen[Entry], Finite[Key]) extends SymbolTable[Key, Symbol, Entry, WithJoin], Effect:

  protected var tables: Map[Key, Entry] = Map()

  override def get(key: Key, symbol: Symbol): JOptionA[Entry] = {
    // TODO: implement size chck
    JOptionA.noneSome(tables(key))
  }

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOption[WithJoin, Unit] = {
    // TODO: implement size check
    Join(tables(key), newEntry).ifChanged(tables += key -> _)
    JOptionA.some(())
  }


  override def putNew(key: Key): Unit =
    tables += key -> emptyEntry

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new UpperBoundSymbolTableJoiner[A])
  private class UpperBoundSymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: Map[Key, Entry] = _
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

  def tableIsSound[cSymbol, cEntry](c: ConcreteSymbolTable[Key, cSymbol, cEntry])(using Soundness[cEntry, Entry]): IsSound = boundary:
    c.entries.foreachEntry { (key, cTab) =>
      val aEntry = tables.getOrElse(key, { break(IsSound.NotSound(s"Key $key not present in topped symbol table.")) })
      for (cEntry <- cTab.values)
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          break(eSound)
    }
    IsSound.Sound

  type State = Map[Key, Entry]
  override def getState: State = tables
  override def setState(s: State): Unit = tables = s
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly
