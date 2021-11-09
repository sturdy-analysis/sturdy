package sturdy.effect.symboltable

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.*
import ToppedSymbolTable.*
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.TrySturdy

trait TopSymbolTable[Key, Symbol, Entry](using Top[Entry]) extends SymbolTable[Key, Symbol, Entry, WithJoin], Effectful:

  override def tableGet(key: Key, symbol: Symbol): OptionA[Entry] =
    OptionA.noneSome(Top.top[Entry])

  override def tableSet(key: Key, symbol: Symbol, newEntry: Entry): Unit = {}

  override def addEmptyTable(key: Key): Unit = {}

