package sturdy.language.bytecode.generic

import org.opalj.br.{ArrayType, ClassFile, ClassType, Method, MethodDescriptor, ReferenceType}
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.language.bytecode.abstractions.Site
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.objects.{Object, ObjectOps}

// class to deal with important native methods, e.g., the class Class
// TODO: clean up, improve, fix
class GenericInterpreterNativeMethods[V, Addr, Idx, ObjType, OID, AID, ObjRep, TypeRep, ExcV, CallData, J[_] <: MayJoin[_]]
  (interpreter: GenericInterpreter[V, Addr, Idx, ObjType, ObjRep, TypeRep, ExcV, J]):
  import interpreter.*
  import interpreter.bytecodeOps.*

  val nativeFunList: List[String] = List(
    //"desiredAssertionStatus",
    "fillInStackTrace",
    "arraycopy",
    "makeConcatWithConstants",
    
    "desiredAssertionStatus0",
    "forName0",
    "getConstantPool",
    "getDeclaredClasses0",
    "getDeclaredConstructors0",
    "getDeclaredFields0",
    "getDeclaredMethods0",
    "getDeclaringClass0",
    "getEnclosingMethod0",
    "getGenericSignature0",
    "getInterfaces0",
    "getModifiers",
    "getNestHost0",
    "getNestMembers0",
    "getPermittedSubclasses0",
    "getPrimitiveClass",
    "getProtectionDomain",
    "getRawAnnotations",
    "getRawTypeAnnotations",
    "getRecordComponents0",
    "getSigners",
    "getSimpleBinaryName0",
    "getSuperclass",
    "initClassName",
    "isArray",
    "isAssignableFrom",
    "isHidden",
    "isInstance",
    "isInterface",
    "isPrimitive",
    "isRecord0",
    "registerNatives",
    "setSigners"
  )

  def evalNative(mth: Method, args: Seq[V]): V =
    mth.name match
      case "desiredAssertionStatus" =>
        bytecodeOps.i32ops.integerLit(1)
      case "fillInStackTrace" =>
        //temporary
        bytecodeOps.i32ops.integerLit(-1)
      case "arraycopy" =>
        val src = args(0)
        val srcPos = args(1)
        val dest = args(2)
        val destPos = args(3)
        val length = args(4)
        arrayOps.arraycopy(src, srcPos, dest, destPos, length)
        //temporary
        bytecodeOps.i32ops.integerLit(-1)

  def invokeClassMethod(mth: Method, args: Seq[V]): V =
    mth.name match
      case "desiredAssertionStatus0" =>
        i32ops.integerLit(1)
      case "forName0" =>
        ???
      case "getConstantPool" =>
        // not in docs
        ???
      case "getDeclaredClasses0" =>
        // returns array of all declared classes in this class
        ???
      case "getDeclaredConstructors0" =>
        // returns array of all constructors declared by the class
        ???
      case "getDeclaredFields0" =>
        // creates a field object of a given string name
        ???
      case "getDeclaredMethods0" =>
        // returns an array of method objects of all declared methods
        ???
      case "getDeclaringClass0" =>
        // if this class is member of another class return class object of that class
        ???
      case "getEnclosingMethod0" =>
        // if this class is local or anonymous within a method, return method object of that method
        ???
      case "getGenericSignature0" =>
        // not in docs
        ???
      case "getInterfaces0" =>
        // array of all implemented classes for objects, of all extended interfaces for interfaces
        ???
      case "getModifiers" =>
        // returns java class modifiers encoed as an integer
        ???
      case "getNestHost0" =>
        ???
      case "getNestMembers0" =>
        ???
      case "getPermittedSubclasses0" =>
        ???
      case "getPrimitiveClass" =>
        // not in docs
        val clsObj = createObject(ClassType("java/lang/Class"), Site.Instruction(mth, 0))
        clsObj
      case "getProtectionDomain" =>
        // returns protectionDomain of this class
        ???
      case "getRawAnnotations" =>
        // not in docs
        ???
      case "getRawTypeAnnotations" =>
        // not in docs
        ???
      case "getRecordComponents0" =>
        ???
      case "getSigners" =>
        // returns signers of this class
        ???
      case "getSimpleBinaryName0" =>
        ???
      case "getSuperclass" =>
        // returns class of the superclass of the encapsulated object
        ???
      case "initClassName" =>
        ???
      case "isArray" =>
        // true if this class represents an array
        ???
      case "isAssignableFrom" =>
        // true if this class is the same, or super of that class
        ???
      case "isHidden" =>
        ???
      case "isInstance" =>
        // true if that object is assignment compatable with the object represented by this class
        ???
      case "isInterface" =>
        if ??? then
          i32ops.integerLit(1)
        else
          i32ops.integerLit(0)
      case "isPrimitive" =>
        if ??? then
          i32ops.integerLit(1)
        else
          i32ops.integerLit(0)
      case "isRecord0" =>
        ???
      case "registerNatives" =>
        i32ops.integerLit(-1)
      case "setSigners" =>
        // not in docs
        ???

      /*
      case "getComponentType" =>
        // returns class representing the component type of an array, this class must represent an array class
        ???
      case "getName" =>
        // returns the name of the object encapsulated by this class
        ???
      case "getName0" =>
        // returns the name of the object encapsulated by this class
        ???
      */
      case "desiredAssertionStatus" =>
        ???
      case "fillInStackTrace" =>
        //temporary
        bytecodeOps.i32ops.integerLit(-1)
      case "arraycopy" =>
        val src = args.head
        val srcPos = args(1)
        val dest = args(2)
        val destPos = args(3)
        val length = args(4)
        arrayOps.arraycopy(src, srcPos, dest, destPos, length)
        //temporary
        bytecodeOps.i32ops.integerLit(-1)
