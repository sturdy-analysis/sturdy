package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.reader.Java8Framework
import org.opalj.br.{ClassFile, ClassType, Method, MethodDescriptor}
import sturdy.effect.failure.Failure
import sturdy.language.bytecode.generic.BytecodeFailure.*

import java.net.URL

object AuxiliaryFunctions:
  def findMethodOfSuperclass(classFile: ClassFile, name: String, sig: MethodDescriptor, project: Project[URL])(using f: Failure): Method =
    classFile.findMethod(name, sig).getOrElse:
      if (classFile.thisType == ClassType.Object)
        f.fail(MethodNotFound, s"Method $name, $sig not found")
      else
        val nextInherit = classFile.superclassType.get
        findInheritedMethodOfSuperclass(classFile, name, sig, nextInherit, project)

  def javaLibClassFileWrapper(obj: ClassType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source

  def findInheritedMethodOfSuperclass(obj: ClassFile, name: String, sig: MethodDescriptor, inheritedObj: ClassType, project: Project[URL])(using f: Failure): Method =
    val libSource = org.opalj.bytecode.RTJar
    if inheritedObj == ClassType.Object then
      // TODO: test
      val objectCF = Java8Framework.ClassFile(libSource, "classes/java/lang/Object.class").head
      objectCF.findMethod(name, sig).getOrElse:
        obj.interfaceTypes.flatMap(interface => project.classFile(interface).get.findMethod(name, sig)).headOption
          .getOrElse(f.fail(MethodNotFound, s"Method $name, $sig not found"))
    else
      val cf = if project.isLibraryType(inheritedObj) then
        val source = javaLibClassFileWrapper(inheritedObj)
        Java8Framework.ClassFile(libSource, source).head
      else
        project.classFile(inheritedObj).get
      val nextInherit = project.classHierarchy.supertypeInformation(inheritedObj).get.classTypes.last
        cf.findMethod(name, sig)
          .getOrElse(findInheritedMethodOfSuperclass(obj, name, sig, nextInherit, project))
