package sturdy.language.bytecode.abstractions

import org.opalj.br.Method

// enum to represent different sites, replacing the old case classes
enum Site:
  case Instruction(mth: Method, pc: Int)
  case ArrayElementInitialization(s: Site, ix: Int)
  case FieldInitialization(s: Site, ident: FieldIdent)
  case StaticInitialization(ident: FieldIdent)
  // not created within the program
  case External
