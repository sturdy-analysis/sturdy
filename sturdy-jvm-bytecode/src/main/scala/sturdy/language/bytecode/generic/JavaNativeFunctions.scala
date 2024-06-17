package sturdy.language.bytecode.generic

import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ReferenceType}
import sturdy.data.{JOption, JOptionC, MayJoin}
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.objects.{Object, ObjectOps}



class JavaNativeFunctions[V, FieldAddr, ArrayElemAddr, Idx, OID, AID, ObjRep, TypeRep, J[_] <: MayJoin[_]]
  (bytecodeOps: BytecodeOps[Idx, V, ReferenceType],
   objectOps: ObjectOps[String, OID, V, ClassFile, Object[OID, ClassFile, FieldAddr, String], V, _, Method, String, MethodDescriptor, V, J],
   arrayOps: ArrayOps[AID, V, V, V, ArrayType, _, J]):


  val nativeFunList: List[String] = List(
    "desiredAssertionStatus",
    "fillInStackTrace",
    "arraycopy",
    "makeConcatWithConstants"
  )

  def evalNative(obj: V, mth: Method, args: Seq[V]): V =
    mth.name match
      case "desiredAssertionStatus" =>
        bytecodeOps.i32ops.integerLit(1)
      case "fillInStackTrace" =>
        //temporary
        bytecodeOps.i32ops.integerLit(-1)

  def evalNativeStatic(mth: Method, args: Seq[V]): V =
    mth.name match
      case "arraycopy" =>
        val src = args(0)
        val srcPos = args(1)
        val dest = args(2)
        val destPos = args(3)
        val length = args(4)
        arrayOps.arraycopy(src, srcPos, dest, destPos, length)
        //temporary
        bytecodeOps.i32ops.integerLit(-1)



