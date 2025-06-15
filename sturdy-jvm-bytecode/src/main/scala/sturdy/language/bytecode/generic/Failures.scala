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
  // a different variant of the Addr enum was expected
  case IncorrectAddrVariant
  // a different variant of the Site enum was expected
  case IncorrectSiteVariant

given Finite[BytecodeFailure] with {}
