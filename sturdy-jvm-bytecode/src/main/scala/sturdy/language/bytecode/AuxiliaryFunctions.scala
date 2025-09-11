package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.reader.Java8Framework
import org.opalj.br.{ClassFile, ClassType, Field, Method, MethodDescriptor}
import sturdy.effect.failure.Failure
import sturdy.language.bytecode.generic.BytecodeFailure.*

import java.net.URL
import scala.annotation.tailrec

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
    val libSource = org.opalj.bytecode.JavaBase
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

  // predicate to check whether a field matches a fieldname
  private def nameMatches(name: (ClassType, String))(field: Field): Boolean =
    field.name == name._2

  // TODO: access control
  // TODO: field type checking (currently, only name and class are considered)
  // - consider implementing a field descriptor type to facilitate this
  @tailrec
  def resolveField(c: ClassFile, name: (ClassType, String))(using project: Project[URL]): Option[Field] =
    var candidate: Option[Field] = None
    candidate = c.fields.find(nameMatches(name)).orElse:
      project.classHierarchy.directSuperinterfacesOf(c.thisType).flatMap(project.classFile(_).get.fields).find(nameMatches(name))
    if candidate.isDefined then return candidate
    if c.superclassType.isEmpty then return None
    resolveField(project.classFile(c.superclassType.get).get, name)
