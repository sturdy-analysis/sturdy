package sturdy.language.wasm.abstractions

import sturdy.fix
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut

trait ContextInsensitive[V] extends fix.ContextualFixpoint[FixIn, FixOut[V]]:
  override type Ctx = this.type