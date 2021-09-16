package sturdy.language.wasm.abstractions

import sturdy.language.wasm.Interpreter
import sturdy.values.Topped

trait ConstantAddressMemory extends Interpreter:
  type Addr = Topped[Int]
  type Bytes = Vector[Topped[Byte]]
  type Size = Topped[Int]
  
  