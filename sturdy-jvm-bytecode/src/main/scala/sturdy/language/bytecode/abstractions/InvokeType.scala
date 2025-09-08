package sturdy.language.bytecode.abstractions

enum InvokeType:
  case Interface
  case Special(isInterfaceCall: Boolean)
  case Virtual
