package sturdy.language.bytecode.generic

import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ClassType, ReferenceType}
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.objects.{Object, ObjectOps}



class JavaNativeFunctions[V, Addr, Idx, OID, AID, ObjRep, TypeRep, J[_] <: MayJoin[_]]
  (bytecodeOps: BytecodeOps[Idx, V, ReferenceType],
   objectOps: ObjectOps[(ClassType, String), OID, V, ClassFile, V, _, Method, String, MethodDescriptor, V, J],
   arrayOps: ArrayOps[AID, V, V, V, ArrayType, _, J]):


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
