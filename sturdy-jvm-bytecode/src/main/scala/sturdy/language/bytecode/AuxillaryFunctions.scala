package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.{ClassFile, Method, MethodDescriptor, ObjectType}
import sturdy.effect.failure.Failure
import sturdy.language.bytecode.generic.BytecodeFailure.*

import java.net.URL

object AuxillaryFunctions {

  def findMethodOfSuperclass(obj: ClassFile, name: String, sig: MethodDescriptor, project: Project[URL])(using f: Failure): Method =
    if (obj.thisType != ObjectType("java/lang/Object")) {
      val nextInherit = project.classHierarchy.supertypeInformation(obj.thisType).get.classTypes.last
      obj.findMethod(name, sig)
        .getOrElse(findInheritedMethodOfSuperclass(obj, name, sig, nextInherit, project))
    }
    else {
      obj.findMethod(name, sig).getOrElse(f.fail(MethodNotFound, s"Method $name, $sig not found"))
    }

  def javaLibClassFileWrapper(obj: ObjectType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source

  def findInheritedMethodOfSuperclass(obj: ClassFile, name: String, sig: MethodDescriptor, inheritedObj: ObjectType, project: Project[URL])(using f: Failure): Method =
    val libSource = org.opalj.bytecode.RTJar
    if (inheritedObj == ObjectType("java/lang/Object")) {
      val objectCF = org.opalj.br.reader.Java8Framework.ClassFile(libSource, "classes/java/lang/Object.class").head
      objectCF.findMethod(name, sig).getOrElse(
        obj.interfaceTypes.map(interfaces => project.classFile(interfaces)).map(file => file.get.findMethod(name, sig)).head
          .getOrElse(f.fail(MethodNotFound, s"Method $name, $sig not found"))
      )
    }
    else {
      if (project.isLibraryType(inheritedObj)) {
        val source = javaLibClassFileWrapper(inheritedObj)
        val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(libSource, source).head
        val nextInherit = project.classHierarchy.supertypeInformation(inheritedObj).get.classTypes.last
        cfs.findMethod(name, sig)
          .getOrElse(findInheritedMethodOfSuperclass(obj, name, sig, nextInherit, project))
      }
      else {
        val cfs = project.classFile(inheritedObj).get
        val nextInherit = project.classHierarchy.supertypeInformation(inheritedObj).get.classTypes.last
        cfs.findMethod(name, sig)
          .getOrElse(findInheritedMethodOfSuperclass(obj, name, sig, nextInherit, project))
      }
    }
}
