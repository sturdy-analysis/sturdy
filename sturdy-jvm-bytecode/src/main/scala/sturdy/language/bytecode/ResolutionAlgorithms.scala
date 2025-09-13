package sturdy.language.bytecode

import org.opalj.br.analyses.Project
import org.opalj.br.{ClassFile, ClassHierarchy, ClassType, Field, Method, MethodDescriptor}
import org.opalj.collection.immutable.UIDSet
import sturdy.data.MayJoin
import sturdy.effect.except.Except
import sturdy.language.bytecode.generic.JvmExcept

import java.net.URL
import scala.annotation.tailrec

// functions that implement the algorithms defined in chapter 5.4 of the jvm specification
// see https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-5.html#jvms-5.4

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

// attempt to resolve a static method reference consisting of a static callee, a name, and a descriptor
def resolveMethod[Value, ExcV, J[_] <: MayJoin[_]](caller: ClassType, calleeStatic: ClassType, name: String, descriptor: MethodDescriptor)(using hierarchy: ClassHierarchy, project: Project[URL], except: Except[JvmExcept[Value], ExcV, J]): Method =
  if hierarchy.isInterface(calleeStatic).isYesOrUnknown then
    except.throws(JvmExcept.Throw(ClassType("java/lang/IncompatibleClassChangeError")))
  val resolved = project.resolveMethodReference(calleeStatic, name, descriptor).getOrElse:
    except.throws(JvmExcept.Throw(ClassType("java/lang/NoSuchMethodError")))
  // access control
  if resolved.isAccessibleBy(caller, project.nests) then
    resolved
  else
    except.throws(JvmExcept.Throw(ClassType("java/lang/IllegalAccessError")))

// attempt to resolve a static method reference consisting of a static callee, a name, and a descriptor
def resolveInterfaceMethod[Value, ExcV, J[_] <: MayJoin[_]](caller: ClassType, calleeStatic: ClassType, name: String, descriptor: MethodDescriptor)(using hierarchy: ClassHierarchy, project: Project[URL], except: Except[JvmExcept[Value], ExcV, J]): Method =
  if hierarchy.isInterface(calleeStatic).isNoOrUnknown then
    except.throws(JvmExcept.Throw(ClassType("java/lang/IncompatibleClassChangeError")))
  val resolved = project.resolveInterfaceMethodReference(calleeStatic, name, descriptor).getOrElse:
    except.throws(JvmExcept.Throw(ClassType("java/lang/NoSuchMethodError")))
  // access control
  if resolved.isAccessibleBy(caller, project.nests) then
    resolved
  else
    except.throws(JvmExcept.Throw(ClassType("java/lang/IllegalAccessError")))

def canOverride(mc: Method, ma: Method): Boolean =
  !mc.isStatic && !ma.isStatic && mc.name == ma.name && mc.descriptor == ma.descriptor && !mc.isPrivate &&
    Method.canDirectlyOverride(mc.classFile.thisType.packageName, ma.visibilityModifier, ma.classFile.thisType.packageName)

// method selection algorithm for invokeinterface and invokevirtual
def selectMethod[Value, ExcV, J[_] <: MayJoin[_]](dynamicType: ClassType, resolvedMethod: Method)(using hierarchy: ClassHierarchy, project: Project[URL], except: Except[JvmExcept[Value], ExcV, J]): Method =
  if resolvedMethod.isPrivate then return resolvedMethod

  val dynCF = project.classFile(dynamicType).get
  val candidate = dynCF.methods.find(canOverride(_, resolvedMethod))
  if candidate.isDefined then return candidate.get

  var superType = dynCF.superclassType
  var result: Option[Method] = None
  while superType.isDefined do
    val cf = project.classFile(superType.get).get
    if result.isEmpty then
      result = cf.methods.find(canOverride(_, resolvedMethod))
    superType = cf.superclassType
  if result.isDefined then return result.get

  val maxSpecificMethods = project.findMaximallySpecificSuperinterfaceMethods(hierarchy.superinterfaceTypes(dynamicType).get, resolvedMethod.name, resolvedMethod.descriptor, UIDSet.empty)._2
  if maxSpecificMethods.size == 1 then
    maxSpecificMethods.head
  else if maxSpecificMethods.isEmpty then
    except.throws(JvmExcept.Throw(ClassType("java/lang/AbstractMethodError")))
  else
    except.throws(JvmExcept.Throw(ClassType("java/lang/IncompatibleClassChangeError")))

// method selection algorithm for invokespecial
def selectSpecial[Value, ExcV, J[_] <: MayJoin[_]](c: ClassType, resolvedMethod: Method)(using hierarchy: ClassHierarchy, project: Project[URL], except: Except[JvmExcept[Value], ExcV, J]): Method =
  val cf = project.classFile(c).get
  val candidate = cf.methods.find: mth =>
    !mth.isStatic && mth.name == resolvedMethod.name && mth.descriptor == resolvedMethod.descriptor
  if candidate.isDefined then return candidate.get

  var superType = cf.superclassType
  var result: Option[Method] = None
  while superType.isDefined && result.isEmpty do
    val cf = project.classFile(superType.get).get
    if result.isEmpty then
      result = cf.methods.find: mth =>
        !mth.isStatic && mth.name == resolvedMethod.name && mth.descriptor == resolvedMethod.descriptor
    superType = cf.superclassType
  if result.isDefined then return result.get

  if hierarchy.isInterface(c).isYes then
    val m = project.classFile(ClassType.Object).get.methods.find: mth =>
      !mth.isStatic && mth.isPublic && mth.name == resolvedMethod.name && mth.descriptor == resolvedMethod.descriptor
    if m.isDefined then return m.get

  val maxSpecificMethods = project.findMaximallySpecificSuperinterfaceMethods(hierarchy.superinterfaceTypes(c).get, resolvedMethod.name, resolvedMethod.descriptor, UIDSet.empty)._2.filter(_.isNotAbstract)
  if maxSpecificMethods.size == 1 then
    maxSpecificMethods.head
  else if maxSpecificMethods.isEmpty then
    except.throws(JvmExcept.Throw(ClassType("java/lang/AbstractMethodError")))
  else
    except.throws(JvmExcept.Throw(ClassType("java/lang/IncompatibleClassChangeError")))
