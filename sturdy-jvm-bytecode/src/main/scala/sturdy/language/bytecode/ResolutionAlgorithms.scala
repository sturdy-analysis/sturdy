package sturdy.language.bytecode

import org.opalj.bi.{ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC}
import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, ClassHierarchy, ClassType, Field, Method, MethodDescriptor, ReferenceType}
import org.opalj.collection.immutable.UIDSet
import sturdy.data.MayJoin
import sturdy.effect.except.Except
import sturdy.language.bytecode.abstractions.FieldIdent
import sturdy.language.bytecode.generic.JvmExcept

import java.net.URL
import scala.annotation.tailrec

// functions that implement the algorithms defined in chapter 5.4 of the jvm specification
// see https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-5.html#jvms-5.4

// 5.4.3.1 class and interface resolution
def resolveClass[Value, ExcV, J[_] <: MayJoin[_]](c: ReferenceType, d: ClassType)(using hierarchy: ClassHierarchy, project: Project[URL], except: Except[JvmExcept[Value], ExcV, J]): ClassType =
  val resC = c match
    case classType: ClassType =>
      classType
    case arrayType: ArrayType =>
      if arrayType.componentType.isReferenceType then
        resolveClass(arrayType.componentType.asReferenceType, d)
      else
        // TODO: not sure whether this is correct
        arrayType.mostPreciseClassType
  if !accessControl(project.classFile(resC).get, d) then
    except.throws(JvmExcept.Throw(ClassType("java/lang/IllegalAccessError")))
  resC

@tailrec
def resolveField[Value, ExcV, J[_] <: MayJoin[_]](d: ClassType, ident: FieldIdent)(using project: Project[URL], except: Except[JvmExcept[Value], ExcV, J]): Field =
  val c = project.classFile(ident.declaringClass).getOrElse:
    except.throws(JvmExcept.Throw(ClassType("java/lang/NoClassDefFoundError")))
  var candidate: Option[Field] = None
  candidate = c.fields.find(ident.matchesField).orElse:
    project.classHierarchy.directSuperinterfacesOf(c.thisType).flatMap(project.classFile(_).get.fields).find(ident.matchesField)
  candidate match
    case Some(field) =>
      if accessControl(field, d)(using project.classHierarchy) then
        field
      else
        except.throws(JvmExcept.Throw(ClassType("java/lang/IllegalAccessError")))
    case None =>
      if c.superclassType.isEmpty then
        except.throws(JvmExcept.Throw(ClassType("java/lang/NoSuchFieldError")))
      resolveField(d, FieldIdent(c.superclassType.get, ident.name, ident.fieldType))

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

def accessControl(e: Field | Method | ClassFile, d: ClassType)(using hierarchy: ClassHierarchy, project: Project[URL]): Boolean =
  e match
    case e: (Field | Method) => fieldOrMethodAccessControl(e, d)
    case c: ClassFile => classAccessControl(c, d)

private def fieldOrMethodAccessControl(e: Field | Method, d: ClassType)(using hierarchy: ClassHierarchy, project: Project[URL]): Boolean =
  val c = (e match
    case f: Field => f.classFile
    case m: Method => m.classFile
    ).thisType
  // adapted from https://github.com/opalj/opal/blob/1cdb64f98d166f8bc3c08e501aeffa2bf2ef659d/OPAL/br/src/main/scala/org/opalj/br/Method.scala#L487
  e.visibilityModifier match
    // TODO Respect Java 9 modules
    case Some(ACC_PUBLIC) =>
      true
    case Some(ACC_PROTECTED) =>
      c.packageName == d.packageName || d.isASubtypeOf(c).isNotNo
    case Some(ACC_PRIVATE) =>
      c == d || project.nests.getOrElse(c, c) == project.nests.getOrElse(d, d)
    case None =>
      c.packageName == d.packageName

// classes or interfaces
private def classAccessControl(c: ClassFile, d: ClassType)(using hierarchy: ClassHierarchy, project: Project[URL]): Boolean =
  // TODO Respect Java 9 modules
  c.isPublic || c.thisType.packageName == d.packageName
