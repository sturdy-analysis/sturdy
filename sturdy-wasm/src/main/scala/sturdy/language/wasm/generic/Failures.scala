package sturdy.language.wasm.generic

import sturdy.effect.failure.FailureKind
import sturdy.values.Finite

enum WasmFailure extends FailureKind:
  case UnreachableInstruction
  case UnboundLocal
  case UnboundGlobal
  case UnboundFunctionType
  case UnboundFunctionIndex
  case IndirectCallTypeMismatch
  case MemoryAccessOutOfBounds
  case TableAccessOutOfBounds
  case InvocationError
  case InvalidModule
  case TypeError
  case ProcExit
  case FileError
  case MockError

given Finite[WasmFailure] with {}