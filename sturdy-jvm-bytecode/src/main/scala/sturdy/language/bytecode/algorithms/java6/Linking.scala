package sturdy.language.bytecode.algorithms.java6

import org.opalj.bi.{ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC, ACC_SUPER}
import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, ClassType, Field, Method, ReferenceType}
import sturdy.language.bytecode.abstractions.{FieldIdent, StaticMethodDeclaration}
import sturdy.language.bytecode.util.ClassTypeValues

import java.net.URL
import scala.annotation.tailrec

// note that these algorithms incorporate some of the semantics defined in §5.3 of the jvm specification

// §5.4.3.1 class and interface resolution
def resolveClass(c: ReferenceType, d: ClassType)(using throwClass: ClassType => Nothing)(using Project[URL]): ReferenceType =
  val resC = c match
    case c: ArrayType if c.componentType.isReferenceType => ArrayType(resolveClass(c.componentType.asReferenceType, d))
    case c => c
  if !accessControl(resC, d) then
    throwClass(ClassTypeValues.IllegalAccessError)
  resC

// §5.4.3.2 field resolution
def resolveField(field: FieldIdent, d: ClassType)(using throwClass: ClassType => Nothing)(using project: Project[URL]): Field =
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
def resolveMethod(staticMethod: StaticMethodDeclaration, d: ClassType)(using throwClass: ClassType => Nothing)(using project: Project[URL]): Method =
  resolveClass(staticMethod.declaringClass, d)
  val cf = getCF(staticMethod.declaringClass)
  if cf.isInterfaceDeclaration then
    throwClass(ClassTypeValues.IncompatibleClassChangeError)
  (superClassFileIterator(cf).flatMap(_.methods).find: m =>
    m.name == staticMethod.name && m.descriptor == staticMethod.descriptor
  .orElse:
    val x = // project.classHierarchy.superinterfaceTypes(staticMethod.declaringClass).get
      project.classHierarchy.allSuperclassesIterator(staticMethod.declaringClass).filter(_.isInterfaceDeclaration).flatMap(_.methods)
    x.find: m =>
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
def resolveInterfaceMethod(staticMethod: StaticMethodDeclaration, d: ClassType)(using throwClass: ClassType => Nothing)(using project: Project[URL]): Method =
  resolveClass(staticMethod.declaringClass, d)
  val cf = getCF(staticMethod.declaringClass)
  if !cf.isInterfaceDeclaration then
    throwClass(ClassTypeValues.IncompatibleClassChangeError)
  cf.methods.find: m =>
    m.name == staticMethod.name && m.descriptor == staticMethod.descriptor
  .getOrElse:
    (project.classHierarchy.superinterfaceTypes(staticMethod.declaringClass).get.add(ClassType.Object).flatMap(getCF(_).methods).find: m =>
      m.name == staticMethod.name && m.descriptor == staticMethod.descriptor) match
      case None =>
        throwClass(ClassTypeValues.NoSuchMethodError)
      case Some(method) =>
        method

// §5.4.4
@tailrec
def accessControl(e: ReferenceType | Field | Method, d: ClassType)(using ClassType => Nothing)(using project: Project[URL]): Boolean = e match
  case c: ReferenceType => c match
    case classType: ClassType =>
      getCF(classType).isPublic || classType.packageName == d.packageName
    case ArrayType(c) if c.isReferenceType =>
      accessControl(c.asReferenceType, d)
    case _: ArrayType =>
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
        c == d || getCF(d).nestedClasses.contains(c)
      case None => // package-private
        c.packageName == d.packageName

// selection algorithms as defined in https://docs.oracle.com/javase/specs/jvms/se6/html/Instructions2.doc6.html
def selectInterfaceMethod(resolvedMethod: Method, dynamicClass: ClassFile)(using throwClass: ClassType => Nothing)(using Project[URL]): Method =
  dynamicClass.methods.find: m =>
    m.isNotStatic && m.name == resolvedMethod.name && m.descriptor == resolvedMethod.descriptor
  .orElse:
    dynamicClass.superclassType.map(c => selectInterfaceMethod(resolvedMethod, getCFUnchecked(c)))
  .getOrElse:
    throwClass(ClassTypeValues.AbstractMethodError)


def selectSpecialMethod(resolvedMethod: Method, dynamicClass: ClassFile, currentClass: ClassFile)(using throwClass: ClassType => Nothing)(using project: Project[URL]): Method =
  if ACC_SUPER.isSet(currentClass.accessFlags) && currentClass.thisType.isSubtypeOf(resolvedMethod.classFile.thisType)(using project.classHierarchy) && !resolvedMethod.isInitializer then
    superClassFileIterator(getCF(currentClass.superclassType.get)).flatMap(_.methods).find: m =>
      m.isNotStatic && m.name == resolvedMethod.name && m.descriptor == resolvedMethod.descriptor
    .getOrElse:
      throwClass(ClassTypeValues.AbstractMethodError)
  else
    resolvedMethod

def selectVirtualMethod(resolvedMethod: Method, dynamicClass: ClassFile)(using throwClass: ClassType => Nothing)(using Project[URL]): Method =
  superClassFileIterator(dynamicClass).flatMap: c =>
    c.methods.find: m =>
      m.isNotStatic && m.name == resolvedMethod.name && m.descriptor == resolvedMethod.descriptor && accessControl(resolvedMethod, c.thisType)
  .nextOption().getOrElse:
    throwClass(ClassTypeValues.AbstractMethodError)

// throws scala exception on failure
def getCFUnchecked(c: ReferenceType)(using project: Project[URL]): ClassFile =
  project.classFile(c.mostPreciseClassType).get

// throws NoClassDefFoundError on failure
def getCF(c: ReferenceType)(using project: Project[URL], throwClass: ClassType => Nothing): ClassFile =
  project.classFile(c.mostPreciseClassType).getOrElse:
    throwClass(ClassTypeValues.NoClassDefFoundError)

// the iterator is reflexive
def superClassFileIterator(cf: ClassFile)(using Project[URL], ClassType => Nothing): Iterator[ClassFile] = new Iterator[ClassFile]:
  var state: Option[ClassFile] = Some(cf)

  override def hasNext: Boolean = state.isDefined

  override def next(): ClassFile =
    val cf = state.get
    state = cf.superclassType.map(getCF)
    cf
