package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, Concrete, TrySturdy}
import sturdy.values.*
import sturdy.{IsSound, Soundness}

import scala.reflect.ClassTag
import scala.util.boundary
import scala.util.boundary.break

trait SizedDecidableSymbolTable[Key, Entry] extends SizedSymbolTable[Key, Int, Entry, Int, NoJoin]:
  var tables: Map[Key, Table] = Map()

  def entries: Map[Key, Table] = tables

  override def putNew(key: Key, limit: SizedSymbolTable.Limit[Int] = SizedSymbolTable.Limit(0, None)): Unit =
    tables += key -> Table(Map(), limit)

  override def get(key: Key, symbol: Int): JOptionC[Entry] =
    JOptionC(tables(key).entries.get(symbol))

  override def set(key: Key, symbol: Int, newEntry: Entry): JOptionC[Unit] = {
    val tab = tables(key)
    if(0 <= symbol && symbol < tab.limit.min) {
      val newTable = Table(tab.entries + (symbol -> newEntry), tab.limit)
      tables += key -> newTable
      JOptionC.some(())
    } else {
      JOptionC.none
    }
  }

  override def size(key: Key): Int =
    tables(key).limit.min

  override def grow(key: Key, newSize: Int, initEntry: Entry): JOptionC[Int] =
    val oldTable = tables(key)
    oldTable.limit.max match
      case Some(upperLimit) if newSize > upperLimit => JOptionC.none
      case _ =>
        val newLimit = SizedSymbolTable.Limit(newSize, oldTable.limit.max)
        tables += key -> Table(oldTable.entries, newLimit)
        fill(key, initEntry, oldTable.limit.min, newSize - oldTable.limit.min)
        JOptionC.Some(oldTable.limit.min)

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

class ConcreteSymbolDropTable[Key, Symbol, Entry](droppedEntry: Entry) extends DecidableSymbolTable[Key, Symbol, Entry], SymbolTableWithDrop[Key, Symbol, Entry, NoJoin], Concrete:
  var dropped: Map[Key, Set[Symbol]] = Map()
  
  override def drop(key: Key, symbol: Symbol): Unit =
    dropped += key -> (dropped.getOrElse(key, Set()) + symbol)

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOptionC[Unit] = { 
    if (dropped.getOrElse(key, Set()).contains(symbol))
      dropped -= key
    super.set(key, symbol, newEntry)
  }

  override def get(key: Key, symbol: Symbol): JOptionC[Entry] = { 
    if (dropped.getOrElse(key, Set()).contains(symbol))
      return JOptionC.Some(droppedEntry)
    super.get(key, symbol)
  }

class ConcreteSizedTable[Key, Entry] extends SizedDecidableSymbolTable[Key, Entry], Concrete {
  override def init(key: Key, entries: Seq[Entry], entryOffset: Int, tableOffset: Int, amount: Int): JOption[NoJoin, Unit] =
    if(amount >= 0 && entryOffset >= 0 && entryOffset + amount <= entries.size && tableOffset >= 0 && tableOffset + amount <= size(key)) {
      val newEntries = entries.slice(entryOffset, entryOffset + amount)
      for ((entry, index) <- newEntries.zipWithIndex) {
        this.set(key, tableOffset + index, entry)
      }
      JOptionC.Some(())
    } else {
      JOptionC.none
    }

  override def fill(key: Key, entry: Entry, tableOffset: Int, amount: Int): JOption[NoJoin, Unit] =
    val Table(_, limit) = tables(key)
    if (tableOffset + amount <= limit.min) {
      for (index <- tableOffset until tableOffset + amount) {
        this.set(key, index, entry)
      }
      JOptionC.some(())
    } else {
      JOptionC.none
    }

  override def copy(dstKey: Key, srcKey: Key, dstOffset: Int, srcOffset: Int, amount: Int): JOption[NoJoin, Unit] =
    boundary:
      val srcTab = tables(srcKey)
      val dstTab = tables(dstKey)
      if (amount >= 0 && srcOffset >= 0 && srcOffset + amount <= size(srcKey) && dstOffset >= 0 && dstOffset + amount <= size(dstKey)) {
        val srcEntries = (0 until amount).map(i => srcTab.entries.getOrElse(srcOffset + i, break(JOptionC.none)))
        var newEntries = dstTab.entries
        for ((entry, i) <- srcEntries.zipWithIndex) newEntries = newEntries.updated(dstOffset + i, entry)
        tables += dstKey -> Table(newEntries, dstTab.limit)
        JOptionC.some(())
      } else {
        JOptionC.none
      }
}

class JoinableDecidableSymbolTable[Key, Symbol, Entry](using Join[Entry], Widen[Entry], Finite[Key], Finite[Symbol]) extends DecidableSymbolTable[Key, Symbol, Entry]:
  override type State = JoinableDecidableSymbolTableState

  override def getState: State = JoinableDecidableSymbolTableState(tables)

  override def setState(st: State): Unit = tables = st.tables

  override def join: Join[State] = implicitly

  override def widen: Widen[State] = implicitly

  case class JoinableDecidableSymbolTableState(tables: Map[Key, Map[Symbol, Entry]]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: JoinableDecidableSymbolTableState => MapEquals(this.tables, other.tables, (table1, table2) => MapEquals(table1, table2))
        case _ => false

  given CombineJoinableDecidableSymbolTableState[W <: Widening](using combineMaps: Combine[Map[Key, Map[Symbol, Entry]],W]): Combine[JoinableDecidableSymbolTableState, W] with
    override def apply(v1: JoinableDecidableSymbolTableState, v2: JoinableDecidableSymbolTableState): MaybeChanged[JoinableDecidableSymbolTableState] =
      combineMaps(v1.tables, v2.tables).map(JoinableDecidableSymbolTableState.apply)


  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    for(tab <- tables.values.iterator;
        entry <- tab.values.iterator;
        addr <- valueIterator(entry))
      yield(addr)