package sturdy.effect.symboltable

import sturdy.effect.PathSensitiveEffect
import sturdy.values.{PathSensitive, assertPath}

trait PathSensitiveSymbolTable[Key, Symbol, Entry : PathSensitive] extends DecidableSymbolTable[Key, Symbol, Entry], PathSensitiveEffect:
  override def assert(cond: Any): Unit =
    tables = tables.view.mapValues(_.view.mapValues(_.assertPath(cond)).toMap).toMap
