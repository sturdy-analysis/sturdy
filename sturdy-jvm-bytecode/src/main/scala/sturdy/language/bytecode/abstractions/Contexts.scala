package sturdy.language.bytecode.abstractions

import org.opalj.br.ClassFile

enum InvokeType:
  case Interface
  case Special(isInterfaceCall: Boolean)
  case Virtual

// site of invocation, type of invocation and class file the invocation is located in
type InvokeContext = (Site, InvokeType, ClassFile)

// site of access and class file it is located in
type FieldAccessContext = (Site, ClassFile)

// site of operation
type ArrayOpContext = Site
