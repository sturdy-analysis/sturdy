package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.values.MayMust.*

final class FiniteSymbolTableWithDrop[Key, Symbol, Entry](droppedEntry: Entry)(using Join[Entry], Widen[Entry], Finite[Key], Finite[Symbol])
      extends SymbolTableWithDrop[Key, Symbol, Entry, WithJoin]:

  var tables: Map[Key, Map[Symbol, MayMust[Entry]]] = Map()
  private var dropped: Map[Key, Set[Symbol]] = Map()

  override def putNew(key: Key): Unit =
    tables += key -> Map()

  override def get(key: Key, symbol: Symbol): JOption[WithJoin, Entry] = {
    if (dropped.getOrElse(key, Set()).contains(symbol)) {
      JOptionA.Some(droppedEntry)
    } else {
    tables(key).get(symbol) match
      case None => JOptionA.None()
      case Some(MayMust.May(entry)) => JOptionA.NoneSome(entry)
      case Some(MayMust.Must(entry)) => JOptionA.Some(entry)
    }
  }

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOption[WithJoin, Unit] =
    tables(key).get(symbol) match
      case None =>
        tables += key -> (tables(key) + (symbol -> Must(newEntry)))
      case Some(mayEntry) =>
        Join(mayEntry, Must(newEntry)).ifChanged(entry =>
          tables += key -> (tables(key) + (symbol -> entry))
        )
    if (dropped.getOrElse(key, Set()).contains(symbol))
      dropped -= key
    JOptionA.Some(())

  override def drop(key: Key, symbol: Symbol): Unit =
    dropped = dropped + (key -> (dropped.getOrElse(key, Set()) + symbol))

  override type State = (Map[Key, Map[Symbol, MayMust[Entry]]], Map[Key, Set[Symbol]])

  override def getState: State = (tables, dropped)

  override def setState(st: State): Unit = {
    tables = st._1
    dropped = st._2
  }

  override def join: Join[State] =
    (v1: State, v2: State) => {
      val joinedTables = JoinMap(using CombineMayMustMap[Symbol, Entry, Widening.No]).apply(v1._1, v2._1)
      val joinedDropped = v1._2 ++ v2._2.map { case (k, s) => k -> (s ++ v1._2.getOrElse(k, Set.empty)) }
      if (joinedTables.hasChanged || v1._2 != v2._2) MaybeChanged.Changed((joinedTables.get, joinedDropped))
      else MaybeChanged.Unchanged(v1)
    }

  override def widen: Widen[State] =
    (v1: State, v2: State) => {
      val widenedTables = CombineFiniteKeyMap(using CombineMayMustMap[Symbol, Entry, Widening.Yes], summon[Finite[Key]]).apply(v1._1, v2._1)
      val widenedDropped = v1._2 ++ v2._2.map { case (k, s) => k -> (s ++ v1._2.getOrElse(k, Set.empty)) }
      if (widenedTables.hasChanged || v1._2 != v2._2) MaybeChanged.Changed((widenedTables.get, widenedDropped))
      else MaybeChanged.Unchanged(v1)
    }
