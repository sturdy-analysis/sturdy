package sturdy.language.bytecode.generic

import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ReferenceType}
import sturdy.data.{JOptionC, MayJoin}
import sturdy.values.arrays.{Array, ArrayOps}
import sturdy.values.objects.{Object, ObjectOps}

class JavaNativeFunctions[V, Addr, Idx, OID, AID, ObjRep, TypeRep, Site, J[_] <: MayJoin[_]]
  (bytecodeOps: BytecodeOps[Addr, Idx, V, ReferenceType])
  (objectOps: ObjectOps[Addr, Int, String, OID, V, ClassFile, Object[OID, ClassFile, Addr, String], V, Site, Method, String, MethodDescriptor, V, J])
  (arrayOps: ArrayOps[Addr, AID, V, V, Array[AID, Addr, ArrayType], V, ArrayType, Site, J]):

  val nativeFunList: List[String] = List(
    "desiredAssertionStatus",
    "fillInStackTrace",
    "arraycopy"
  )

  def evalNative(obj: V, mth: Method, args: Seq[V]): JOptionC[V] =
    mth.name match
      case "desiredAssertionStatus" =>
        JOptionC.some(bytecodeOps.i32ops.integerLit(1))
      case "fillInStackTrace" =>
        JOptionC.some(obj)

  def evalNativeStatic(mth: Method, args: Seq[V]): JOptionC[V] =
    mth.name match
      case "arraycopy" =>
        val src = args(0)
        val srcPos = args(1)
        val dest = args(2)
        val destPos = args(3)
        val length = args(4)
        arrayOps.arraycopy(src, srcPos, dest, destPos, length)
        JOptionC.some(bytecodeOps.i32ops.integerLit(0))

