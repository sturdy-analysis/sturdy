package sturdy.language.bytecode.abstractions

import org.opalj.br.{ClassType, MethodDescriptor}

// static declaration of a dynamically resolved method
// consists of declared class, name and its descriptor
case class StaticMethodDeclaration(declaringClass: ClassType, name: String, descriptor: MethodDescriptor)
