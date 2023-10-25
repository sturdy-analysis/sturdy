package sturdy.language.bytecode.generic


import sturdy.effect.failure.FailureKind
import sturdy.values.Finite

enum BytecodeFailure extends FailureKind:
  case UnboundLocal
  
given Finite[BytecodeFailure] with {}