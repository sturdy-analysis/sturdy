package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effectful
import sturdy.effect.TrySturdy
import sturdy.values.Join

class JoinedSymbolTable[Key, Symbol, Entry](using Join[Entry]) extends ConcreteSymbolTable[Key, Symbol, Entry]:
  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new SymbolTableJoiner[A])
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

  def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound =
    c.getState.foreachEntry { (key, cTab) =>
      val aTab = tables.getOrElse(key, return IsSound.NotSound(s"Key $key not present in topped symbol table."))
      for ((sym, cEntry) <- cTab)
        val aEntry = aTab.getOrElse(sym, return IsSound.NotSound(s"Table $key misses symbol $sym, bound to $cEntry in the concrete table."))
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          return eSound
    }
    IsSound.Sound
