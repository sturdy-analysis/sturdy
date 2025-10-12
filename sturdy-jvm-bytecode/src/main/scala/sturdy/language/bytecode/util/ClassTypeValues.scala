package sturdy.language.bytecode.util

import org.opalj.br.ClassType

// class types used in this project that are not provided by opal
object ClassTypeValues:
  val AbstractMethodError: ClassType = ClassType("java/lang/AbstractMethodError")
  val IllegalAccessError: ClassType = ClassType("java/lang/IllegalAccessError")
  val IncompatibleClassChangeError: ClassType = ClassType("java/lang/IncompatibleClassChangeError")
  val InstantiationError: ClassType = ClassType("java/io/PrintStream")
  val LinkageError: ClassType = ClassType("java/lang/LinkageError")
  val NoClassDefFoundError: ClassType = ClassType("java/lang/NoClassDefFoundError")
  val NoSuchMethodError: ClassType = ClassType("java/lang/NoSuchMethodError")
  val NoSuchFieldError: ClassType = ClassType("java/lang/NoSuchFieldError")
  val PrintStream: ClassType = ClassType("java/io/PrintStream")
