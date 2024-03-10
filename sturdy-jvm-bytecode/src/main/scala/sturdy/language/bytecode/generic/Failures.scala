package sturdy.language.bytecode.generic


import sturdy.effect.failure.FailureKind
import sturdy.values.Finite

enum BytecodeFailure extends FailureKind:
  case UnboundLocal
  case UnboundField
  case UnboundStaticVar
  case IndexOutOfBounds
  case MethodNotFound
  case TypeError
  
given Finite[BytecodeFailure] with {}