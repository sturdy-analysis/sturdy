package sturdy.language.bytecode.algorithms.java6

import org.opalj.bi.{ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC}
import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, ClassType, Field, Method, ReferenceType}
import sturdy.language.bytecode.abstractions.{FieldIdent, StaticMethodDeclaration}
import sturdy.language.bytecode.util.ClassTypeValues

import java.net.URL
import scala.annotation.tailrec

// note that these algorithms incorporate some of the semantics defined in §5.3 of the jvm specification

// §5.4.3.1 class and interface resolution
def resolveClass(c: ReferenceType, d: ClassType)(using Project[URL])(using throwClass: ClassType => Nothing): ReferenceType =
  val resC = c match
    case c: ArrayType if c.componentType.isReferenceType => ArrayType(resolveClass(c.componentType.asReferenceType, d))
    case c => c
  if !accessControl(resC, d) then
    throwClass(ClassTypeValues.IllegalAccessError)
  resC

// §5.4.3.2 field resolution
def resolveField(field: FieldIdent, d: ClassType)(using project: Project[URL], throwClass: ClassType => Nothing): Field =
  resolveClass(field.declaringClass, d)
  val c = getCF(field.declaringClass)
  (c.fields.find(field.matchesField).orElse:
    project.classHierarchy.directSuperinterfacesOf(c.thisType).flatMap(getCF(_).fields).find(field.matchesField).orElse:
      c.superclassType.map(superC => resolveField(FieldIdent(superC, field.name, field.fieldType), d))) match
    case None =>
      throwClass(ClassTypeValues.NoSuchFieldError)
    case Some(f) =>
      if !accessControl(f, d) then
        throwClass(ClassTypeValues.IllegalAccessError)
      f

// §5.4.3.3 method resolution
def resolveMethod(staticMethod: StaticMethodDeclaration, d: ClassType)(using project: Project[URL], throwClass: ClassType => Nothing): Method =
  resolveClass(staticMethod.declaringClass, d)
  val cf = getCF(staticMethod.declaringClass)
  if cf.isInterfaceDeclaration then
    throwClass(ClassTypeValues.IncompatibleClassChangeError)
  (superClassFileIterator(cf).flatMap(_.methods).find: m =>
    m.name == staticMethod.name && m.descriptor == staticMethod.descriptor
  .orElse:
    project.classHierarchy.superinterfaceTypes(staticMethod.declaringClass).get.flatMap(getCF(_).methods).find: m =>
      m.name == staticMethod.name && m.descriptor == staticMethod.descriptor) match
    case None =>
      throwClass(ClassTypeValues.NoSuchMethodError)
    case Some(method) =>
      if method.isAbstract && !cf.isAbstract then
        throwClass(ClassTypeValues.AbstractMethodError)
      if !accessControl(method, d) then
        throwClass(ClassTypeValues.IllegalAccessError)
      method

// §5.4.3.4 interface method resolution
def resolveInterfaceMethod(staticMethod: StaticMethodDeclaration, d: ClassType)(using project: Project[URL], throwClass: ClassType => Nothing): Method =
  resolveClass(staticMethod.declaringClass, d)
  val cf = getCF(staticMethod.declaringClass)
  if !cf.isInterfaceDeclaration then
    throwClass(ClassTypeValues.IncompatibleClassChangeError)
  (project.classHierarchy.superinterfaceTypes(staticMethod.declaringClass).get.flatMap(getCF(_).methods).find: m =>
    m.name == staticMethod.name && m.descriptor == staticMethod.descriptor) match
    case None =>
      throwClass(ClassTypeValues.NoSuchMethodError)
    case Some(method) =>
      method

// §5.4.4
@tailrec
def accessControl(e: ReferenceType | Field | Method, d: ClassType)(using project: Project[URL]): Boolean = e match
  case c: ReferenceType => c match
    case classType: ClassType =>
      getCF(classType).isPublic || classType.packageName == d.packageName
    case ArrayType(c) if c.isReferenceType =>
      accessControl(c.asReferenceType, d)
    case c: ArrayType =>
      true // primitive types are always accessible
  // c.isPublic || c.thisType.packageName == d.packageName
  case r: (Field | Method) =>
    val c = (r match
      case r: Field => r.classFile
      case r: Method => r.classFile
      ).thisType
    r.visibilityModifier match
      case Some(ACC_PUBLIC) =>
        true
      case Some(ACC_PROTECTED) =>
        c.packageName == d.packageName || d.isSubtypeOf(c)(using project.classHierarchy)
      case Some(ACC_PRIVATE) =>
        c == d
      case None => // package-private
        c.packageName == d.packageName

def getCF(c: ReferenceType)(using project: Project[URL]): ClassFile =
  project.classFile(c.mostPreciseClassType).get

def superClassFileIterator(cf: ClassFile)(using Project[URL]): Iterator[ClassFile] = new Iterator[ClassFile]:
  var state: Option[ClassFile] = Some(cf)

  override def hasNext: Boolean = state.isDefined

  override def next(): ClassFile =
    val cf = state.get
    state = cf.superclassType.map(getCF)
    cf
