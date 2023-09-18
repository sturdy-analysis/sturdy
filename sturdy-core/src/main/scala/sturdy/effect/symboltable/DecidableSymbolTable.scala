package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Concrete
import sturdy.effect.TrySturdy
import sturdy.values.*

trait DecidableSymbolTable[Key, Symbol, Entry] extends SymbolTable[Key, Symbol, Entry, NoJoin]:
  protected var tables: Map[Key, Map[Symbol, Entry]] = Map()
  var min = 0
  var max = None

  def entries: Map[Key, Map[Symbol, Entry]] = tables
  
  override def get(key: Key, symbol: Symbol): JOptionC[Entry] =
    JOptionC(tables(key).get(symbol))

  override def set(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    tables += key -> (tables(key) + (symbol -> newEntry))

  override def size(key: Key, symbol: Symbol): Int =
    tables(key).size

  override def grow(key: Key, symbol: Symbol, initEntry: Entry): Int =
    tables += key -> (tables(key) + (symbol -> initEntry))
    tables(key).size


  override def fill(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    tables += key -> (tables(key) + (symbol -> newEntry))

  override def copy(key: Key, symbol: Symbol, dest: Key): Unit = ???

  override def init(key: Key, newEntry: Entry): Unit = ???

  //override def drop(key: Key, symbol: Topped[Symbol]): Unit = ???
  //override def drop(key: Key, symbol: Symbol): Unit = ???

  override def putNew(key: Key): Unit =
    tables += key -> Map()

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    c.tables.foreachEntry { (key, cTab) =>
      val aTab = tables.getOrElse(key, return IsSound.NotSound(s"Key $key not present in topped symbol table."))
      for ((sym, cEntry) <- cTab)
        val aEntry = aTab.getOrElse(sym, return IsSound.NotSound(s"Table $key misses symbol $sym, bound to $cEntry in the concrete table."))
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          return eSound
    }
    IsSound.Sound


class ConcreteSymbolTable[Key, Symbol, Entry] extends DecidableSymbolTable[Key, Symbol, Entry], Concrete


class JoinableDecidableSymbolTable[Key, Symbol, Entry](using Join[Entry], Widen[Entry], Finite[Key], Finite[Symbol]) extends DecidableSymbolTable[Key, Symbol, Entry]:
  override type State =  Map[Key, Map[Symbol, Entry]]
  override def getState: Map[Key, Map[Symbol, Entry]] = tables
  override def setState(st: Map[Key, Map[Symbol, Entry]]): Unit = tables = st
  override def join: Join[Map[Key, Map[Symbol, Entry]]] = implicitly
  override def widen: Widen[Map[Key, Map[Symbol, Entry]]] = implicitly

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new SymbolTableJoiner[A])
  class SymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: Map[Key, Map[Symbol, Entry]] = null

    override def inbetween(): Unit =
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
      for ((key,fmap) <- fTables) {
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
