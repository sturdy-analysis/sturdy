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

class SizedUpperBoundSymbolTable[Key, Symbol, Entry](using Join[Entry], Widen[Entry], Finite[Key]) extends UpperBoundSymbolTable[Key, Symbol, Entry], SizedSymbolTable[Key, Symbol, Entry, BaseType[Int], WithJoin] {
  override def size(key: Key): BaseType[Int] = new BaseType[Int]()

  override def grow(key: Key, newSize: BaseType[Int], initEntry: Entry): JOption[WithJoin, BaseType[Int]] = JOptionA.noneSome(BaseType[Int]())

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[BaseType[Int]]): Unit = super.putNew(key)

  override def init(key: Key, entries: Seq[Entry], entryOffset: Symbol, tableOffset: Symbol, amount: BaseType[Int]): JOption[WithJoin, Unit] = JOptionA.noneSome(BaseType[Int]())

  override def fill(key: Key, entry: Entry, tableOffset: Symbol, amount: BaseType[Int]): JOption[WithJoin, Unit] = JOptionA.noneSome(BaseType[Int]())

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Symbol, srcOffset: Symbol, amount: BaseType[Int]): JOption[WithJoin, Unit] = JOptionA.noneSome(BaseType[Int]())
}

class UpperBoundSymbolTable[Key, Symbol, Entry](using Join[Entry], Widen[Entry], Finite[Key]) extends SymbolTable[Key, Symbol, Entry, WithJoin], Effect:

  protected var tables: Map[Key, Option[Entry]] = Map()

  override def get(key: Key, symbol: Symbol): JOptionA[Entry] = {
    // TODO: implement size chck
    tables(key) match {
      case Some(entries) => JOptionA.noneSome(entries)
      case None => JOptionA.none
    }
  }

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOption[WithJoin, Unit] = {
    // TODO: implement size check
    Join(tables(key).getOrElse(newEntry), newEntry).ifChanged(tables += key -> Some(_))
    JOptionA.some(())
  }

  override def putNew(key: Key): Unit =
    tables += key -> None

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new UpperBoundSymbolTableJoiner[A])
  private class UpperBoundSymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: Map[Key, Option[Entry]] = _
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
        val eSound = Soundness.isSound(cEntry, aEntry.get)
        if (!eSound.isSound)
          break(eSound)
    }
    IsSound.Sound

  type State = Map[Key, Option[Entry]]
  override def getState: State = tables
  override def setState(s: State): Unit = tables = s
  override def join: Join[State] = implicitly
  override def widen: Widen[State] = implicitly
