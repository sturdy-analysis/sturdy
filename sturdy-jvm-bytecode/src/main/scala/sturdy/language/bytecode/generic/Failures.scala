package sturdy.language.bytecode.generic

import sturdy.effect.failure.FailureKind
import sturdy.language.bytecode.abstractions.Site
import sturdy.values.Finite

enum BytecodeFailure extends FailureKind:
  // failed to get/set local variable in frame
  case UnboundLocal
  // field missing from store
  case UnboundField
  // static field missing from store
  case UnboundStaticVar
  // unexpected type
  case TypeError
  // a different variant of the Addr enum was expected
  case IncorrectAddrVariant
  // a different variant of the Site enum was expected
  case IncorrectSiteVariant(incorrectSite: Site)
  // field missing in a context where invariants dictate that it shouldn't
  case FieldNotFound

given Finite[BytecodeFailure] with {}
