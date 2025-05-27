package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, Concrete, TrySturdy}
import sturdy.values.*
import sturdy.{IsSound, Soundness}

import scala.util.boundary
import scala.util.boundary.break

trait SizedDecidableSymbolTable[Value, Key, Entry] extends SizedSymbolTable[Value, Key, Int, Entry, Int, NoJoin]:
  protected var tables: Map[Key, Table] = Map()

  def entries: Map[Key, Table] = tables

  override def get(key: Key, symbol: Int): JOptionC[Entry] = {
    if (!inBounds(key, symbol))
      return JOptionC.none
    JOptionC(tables(key).entries.get(symbol))
  }

  override def set(key: Key, symbol: Int, newEntry: Entry): JOptionC[Unit] = {
    if (!inBounds(key, symbol))
      return JOptionC.none
    val tab = tables(key)
    val newTable = Table(tab.entries + (symbol -> newEntry), tab.limit)
    tables += key -> newTable
    JOptionC.some(())
  }

  override def size(key: Key): Int =
    tables(key).entries.size

  override def grow(key: Key, newSize: Int, initEntry: Entry): JOptionC[Int] =
    val oldTable = tables(key)
    val oldSize = oldTable.entries.size
    val upperLimit = oldTable.limit.max
    if (upperLimit.isDefined && newSize > upperLimit.get)
      JOptionC.none
    else
      val added = (oldSize until newSize).map(i => i -> initEntry).toMap
      val newTable = Table(oldTable.entries ++ added, oldTable.limit)

      tables += key -> newTable
      JOptionC.Some(oldSize)

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[Int] = SizedSymbolTable.Limit(0, None)): Unit =
    tables += key -> Table(Map(), limit)

  override def putNew(key: Key): Unit =
    putNew(key, SizedSymbolTable.Limit(0, None))

  // TODO: check if this has to be implemented here
  /*def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound = boundary:
    c.tables.foreachEntry { (key, cTab) =>
      val aTab = tables.getOrElse(key, break(IsSound.NotSound(s"Key $key not present in topped symbol table.")))
      for ((sym, cEntry) <- cTab.entries)
        val aEntry = aTab.entries.getOrElse(sym, break(IsSound.NotSound(s"Table $key misses symbol $sym, bound to $cEntry in the concrete table.")))
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          break(eSound)
    }
    IsSound.Sound*/

  private def inBounds(key: Key, symbol: Int): Boolean =
    val tab = tables(key)
    val length = math.max(tab.entries.size, tab.limit.min)
    !(symbol >= length || symbol < 0)

  case class Table(entries: Map[Int, Entry], limit: SizedSymbolTable.Limit[Int])

  given JoinTable(using je: Join[Entry], jl: Join[SizedSymbolTable.Limit[Int]]): Join[Table] with
    def apply(t1: Table, t2: Table): MaybeChanged[Table] =
      val entriesJoined = Join(t1.entries, t2.entries)
      val limitJoined = jl(t1.limit, t2.limit)
      MaybeChanged(Table(entriesJoined.get, limitJoined.get),
        entriesJoined.hasChanged || limitJoined.hasChanged)


trait DecidableSymbolTable[Key, Symbol, Entry] extends SymbolTable[Key, Symbol, Entry, NoJoin]:
  protected var tables: Map[Key, Map[Symbol, Entry]] = Map()

  def entries: Map[Key, Map[Symbol, Entry]] = tables

  override def get(key: Key, symbol: Symbol): JOptionC[Entry] =
    JOptionC(tables(key).get(symbol))

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOptionC[Unit] =
    tables += key -> (tables(key) + (symbol -> newEntry))
    JOptionC.some(())

  override def putNew(key: Key): Unit =
    tables += key -> Map()

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound = boundary:
    c.tables.foreachEntry { (key, cTab) =>
      val aTab = tables.getOrElse(key, break(IsSound.NotSound(s"Key $key not present in topped symbol table.")))
      for ((sym, cEntry) <- cTab)
        val aEntry = aTab.getOrElse(sym, break(IsSound.NotSound(s"Table $key misses symbol $sym, bound to $cEntry in the concrete table.")))
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          break(eSound)
    }
    IsSound.Sound


class ConcreteSymbolTable[Key, Symbol, Entry] extends DecidableSymbolTable[Key, Symbol, Entry], Concrete

class ConcreteSizedTable[Value, Key, Entry](extractor: Value => Int) extends SizedDecidableSymbolTable[Value, Key, Entry], Concrete {
  private def inBounds(offset: Int, amount: Int, table: Key): Boolean = offset >= 0 && offset + amount <= this.size(table)

  override def init(key: Key, entries: Vector[Entry], entryOffset: Value, tableOffset: Value, amount: Value): JOption[NoJoin, Unit] = init(key, entries, extractor(entryOffset), extractor(tableOffset), extractor(amount))

  def init(key: Key, entries: Vector[Entry], entryOffset: Int, tableOffset: Int, amount: Int): JOption[NoJoin, Unit] =
    // elem bounds check
    if (entryOffset < 0 || entryOffset + amount > entries.size) {
      return JOptionC.none
    }
    // table bounds check
    if (!inBounds(tableOffset, amount, key)) {
      return JOptionC.none
    }
    val newEntries = entries.slice(entryOffset, entryOffset + amount)
    for ((entry, index) <- newEntries.zipWithIndex) {
      this.set(key, tableOffset + index, entry)
    }
    JOptionC.some(())

  override def fillTable(key: Key, entry: Entry, tableOffset: Value, amount: Value): JOption[NoJoin, Unit] = fillTable(key, entry, extractor(tableOffset), extractor(amount))

  def fillTable(key: Key, entry: Entry, tableOffset: Int, amount: Int): JOption[NoJoin, Unit] =
    // table bounds check
    if (!inBounds(tableOffset, amount, key)) {
      return JOptionC.none
    }
    for (index <- tableOffset until tableOffset + amount) {
      this.set(key, index, entry)
    }
    JOptionC.some(())

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Value, srcOffset: Value, amount: Value): JOption[NoJoin, Unit] = copy(dstKey, srcKey, extractor(dstOffset), extractor(srcOffset), extractor(amount))

  def copy(dstKey: Key, srcKey: Key, dstOffset: Int, srcOffset: Int, amount: Int): JOption[NoJoin, Unit] =
    // dst table bounds check
    if (!inBounds(dstOffset, amount, dstKey)) {
      return JOptionC.none
    }
    // src table bounds check
    if (!inBounds(srcOffset, amount, srcKey)) {
      return JOptionC.none
    }
    // copy entries to Vector
    var entries: Vector[Entry] = Vector.empty
    for (index <- 0 until amount) {
      val entry = this.get(srcKey, srcOffset + index).getOrElse(return JOptionC.none)
      entries = entries :+ entry
    }
    for ((entry, index) <- entries.zipWithIndex) {
      this.set(dstKey, dstOffset + index, entry)
    }
    JOptionC.some(())
}

class JoinableDecidableSymbolTable[Key, Symbol, Entry](using Join[Entry], Widen[Entry], Finite[Key], Finite[Symbol]) extends DecidableSymbolTable[Key, Symbol, Entry]:
  override type State = Map[Key, Map[Symbol, Entry]]

  override def getState: State = tables

  override def setState(st: State): Unit = tables = st

  override def join: Join[State] = implicitly

  override def widen: Widen[State] = implicitly

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new SymbolTableJoiner[A])

  class SymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: Map[Key, Map[Symbol, Entry]] = null

    override def inbetween(fFailed: Boolean): Unit =
      fTables = tables
      tables = snapshot

    override def retainNone(): Unit =
      tables = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      tables = fTables

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}
    // nothing

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (fTables.size != tables.size)
        throw new IllegalStateException()
      var joined = Map[Key, Map[Symbol, Entry]]()
      for ((key, fmap) <- fTables) {
        var joinedMap = Map[Symbol, Entry]()
        val gmap = tables(key)
        if (fmap.size != gmap.size)
          throw new IllegalStateException()
        for ((sym, fentry) <- fmap) {
          val gentry = gmap(sym)
          joinedMap += sym -> Join(fentry, gentry).get
        }
        joined += key -> joinedMap
      }
      tables = joined
  }



//  override type State = Map[Key, Map[Symbol, Entry]]
//  override def getState: Map[Key, Map[Symbol, Entry]] = tables
//  override def setState(tables: Map[Key, Map[Symbol, Entry]]): Unit = this.tables = tables
