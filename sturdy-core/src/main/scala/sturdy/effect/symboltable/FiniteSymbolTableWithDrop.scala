package sturdy.effect.symboltable

import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.values.MayMust.*

final class FiniteSymbolTableWithDrop[Key, Symbol, Entry](using Join[Entry], Widen[Entry], Finite[Key], Finite[Symbol])
      extends SymbolTableWithDrop[Key, Symbol, Entry, WithJoin]:
  var tables: Map[Key, Map[Symbol, MayMust[Entry]]] = Map()

  override def putNew(key: Key): Unit =
    tables += key -> Map()

  override def get(key: Key, symbol: Symbol): JOption[WithJoin, Entry] =
    tables(key).get(symbol) match
      case None => JOptionA.None()
      case Some(MayMust.May(entry)) => JOptionA.NoneSome(entry)
      case Some(MayMust.Must(entry)) => JOptionA.Some(entry)

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOption[WithJoin, Unit] =
    tables(key).get(symbol) match
      case None =>
        tables += key -> (tables(key) + (symbol -> Must(newEntry)))
      case Some(mayEntry) =>
        Join(mayEntry, Must(newEntry)).ifChanged(entry =>
          tables += key -> (tables(key) + (symbol -> entry))
        )
    JOptionA.Some(())

  override def drop(key: Key, symbol: Symbol): Unit =
    tables += key -> (tables(key) - symbol)

  override type State = Map[Key, Map[Symbol, MayMust[Entry]]]

  override def getState: State = tables

  override def setState(st: State): Unit = tables = st

  override def join: Join[State] = JoinMap(using CombineMayMustMap[Symbol, Entry, Widening.No])

  override def widen: Widen[State] = CombineFiniteKeyMap(using CombineMayMustMap[Symbol, Entry, Widening.Yes], implicitly)