package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.Concrete
import sturdy.effect.TrySturdy
import sturdy.values.*

trait DecidableSymbolTable[Key, Symbol, Entry] extends SymbolTable[Key, Symbol, Entry, NoJoin]:
  protected var tables: MustMap[Key, MustMap[Symbol, Entry]] = MustMap()

  def entries: MustMap[Key, MustMap[Symbol, Entry]] = tables
  
  override def get(key: Key, symbol: Symbol): JOptionC[Entry] =
    JOptionC(tables(key).get(symbol))

  override def set(key: Key, symbol: Symbol, newEntry: Entry): Unit =
    tables += key -> (tables(key) + (symbol -> newEntry))

  override def putNew(key: Key): Unit =
    tables += key -> MustMap()

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
  override type State =  MustMap[Key, MustMap[Symbol, Entry]]
  override def getState: MustMap[Key, MustMap[Symbol, Entry]] = tables
  override def setState(st: MustMap[Key, MustMap[Symbol, Entry]]): Unit = tables = st
  override def join: Join[MustMap[Key, MustMap[Symbol, Entry]]] = implicitly
  override def widen: Widen[MustMap[Key, MustMap[Symbol, Entry]]] = implicitly

  class SymbolTableJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = tables
    private var fTables: MustMap[Key, MustMap[Symbol, Entry]] = MustMap(null)

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
      var joined = MustMap[Key, MustMap[Symbol, Entry]]()
      for ((key,fmap) <- fTables) {
        var joinedMap = MustMap[Symbol, Entry]()
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
