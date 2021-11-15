package sturdy.effect.symboltable

import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.Effectful
import sturdy.effect.TrySturdy
import sturdy.values.Join

trait JoinedSymbolTable[Key, Symbol, Entry](using Join[Entry]) extends ConcreteSymbolTable[Key, Symbol, Entry]:
  override def makeComputationJoiner[A]: ComputationJoiner[A] = new SymbolTableJoiner[A] 
  class SymbolTableJoiner[A] extends ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    private val snapshot = tables
    private var fTables: Map[Key, Map[Symbol, Entry]] = null

    override def inbetween_(): Unit =
      fTables = tables
      tables = snapshot

    override def retainNone_(): Unit =
      tables = snapshot

    override def retainFirst_(fRes: TrySturdy[A]): Unit =
      tables = fTables

    override def retainSecond_(gRes: TrySturdy[A]): Unit = {}
      // nothing

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
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