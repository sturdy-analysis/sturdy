package sturdy.language.bytecode.abstractions

import org.opalj.br.ClassFile

enum InvokeType:
  case Interface
  case Special(isInterfaceCall: Boolean)
  case Virtual

// type of invocation and class file the invocation is located in
type InvokeContext = (InvokeType, ClassFile)
