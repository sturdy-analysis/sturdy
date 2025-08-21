package sturdy.language.wasm.abstractions

import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.FunctionInstance
import sturdy.values.{Powerset, Structural, given}

trait PowersetReference extends Interpreter:
  override final type Reference = Powerset[FunctionInstance | ExternReference]
  override final type RefV = Reference
  override final type FunV = Powerset[FunctionInstance]

  enum ExternReference:
    case ExternReference
    case Null
  given Structural[FunctionInstance | ExternReference] with {}
