package sturdy.language.wasm.generic

trait ValueOps[V]:
  def i32NeqZero[A](v: V, notZero: => A, isZero: => A): A
