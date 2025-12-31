package sturdy.language.bytecode.abstractions

import org.opalj.br.{ClassFile, ClassType, MethodDescriptor}

enum InvokeType:
  case Interface
  case Special(isInterfaceCall: Boolean)
  case Virtual

// site of invocation, type of invocation and class file the invocation is located in
type InvokeContext = (Site, InvokeType, ClassFile)

// static declaration of a dynamically resolved method
// consists of declared class, name and its descriptor
case class StaticMethodDeclaration(declaringClass: ClassType, name: String, descriptor: MethodDescriptor)

// site of access and class file it is located in
type FieldAccessContext = (Site, ClassFile)

// site of operation
type ArrayOpContext = Site
