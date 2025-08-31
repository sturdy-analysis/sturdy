package sturdy.language.bytecode

import scala.util.boundary
import boundary.break
import org.opalj.br.{ArrayType, ClassFile, ClassType, Method, MethodDescriptor, ReferenceType}
import org.opalj.br.analyses.Project
import sturdy.data.{MayJoin, *, given}
import sturdy.effect.allocation.{Allocator, CAllocatorIntIncrement}
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.{ConcreteExcept, Except}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.store.{CStore, Store}
import sturdy.fix.{ConcreteFixpoint, Fixpoint}
import sturdy.language.bytecode.generic.*
import sturdy.values.booleans.ConcreteBooleanBranching
import sturdy.values.exceptions.ConcreteExceptional
import sturdy.values.floating.given
import sturdy.values.integer.given
import sturdy.values.objects.*
import sturdy.values.arrays.*
import sturdy.fix
import sturdy.language.bytecode.abstractions.Site
import sturdy.values.ordering.EqOps

import java.net.URL

enum ConcreteRefValues[OID, CF, FieldName, FieldAddr, AID, ArrayElemAddr, AType, ASize]:
  case Object(oid: OID, cls: CF, fields: Map[FieldName, FieldAddr])
  case nonNullArray(aid: AID, vals: Vector[ArrayElemAddr], arrayType: AType, arraySize: ASize)
  case NullValue()

object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]
  //override type I8  = Byte
  //override type I16 = Short
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean

  override type Addr = (Site, Int)

  override type Mth = Method
  override type MthName = String
  override type MthSig = MethodDescriptor
  override type ObjType = ClassFile
  override type Idx = Int
  override type TypeRep = ReferenceType
  //override type NullVal = Null
  override type FieldName = (ClassType, String)
  //override type ObjRep = Object[ObjAddr, ClassFile, FieldAddr, FieldName]
  override type AType = ArrayType
  //override type ArrayRep = Array[ArrayAddr, FieldAddr, ArrayType, Value]

  override type RefValue = ConcreteRefValues[Addr, ClassFile, FieldName, Addr, Addr, Addr, AType, Value]

  override type ExcV = JvmExcept[Value]

  //override def topI8: Byte = throw new UnsupportedOperationException
  //override def topI16: Short = throw new UnsupportedOperationException
  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException

  //override def topObj: Object[ConcreteInterpreter.ObjAddr, ClassFile, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.FieldName] = throw new UnsupportedOperationException
  //override def topArray: Array[ConcreteInterpreter.ArrayAddr, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.AType, ConcreteInterpreter.Value] = throw new UnsupportedOperationException
  //override def topNull: Null = throw new UnsupportedOperationException
  override def topRef: RefValue = throw new UnsupportedOperationException
  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0

  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

  given concreteTypeOps[OID, AID](using project: Project[URL]): TypeOps[RefValue, TypeRep, Bool] with
    /* rewritten below to no longer cause warnings
    override def instanceOf(v: ConcreteRefValues, target: TypeRep): Boolean = v match
      case v: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName] =>
        if(target == null)
          false
        else
          v.cls.thisType.isSubtypeOf(target.mostPreciseClassType)(project.classHierarchy)
      case v: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, I32] =>
        if(target == null)
          false
        else
          v.arrayType == target.asArrayType
      case v: ConcreteRefValues.NullValue =>
        if(target == null)
          true
        else
          false
    */
    override def instanceOf(v: RefValue, target: TypeRep): Boolean = v match
      case ConcreteRefValues.Object(_, cf: ClassFile, _) =>
        if (target == null)
          false
        else
          cf.thisType.isSubtypeOf(target.mostPreciseClassType)(project.classHierarchy)
      case ConcreteRefValues.nonNullArray(_, _, arrayType: AType, _) =>
        if (target == null)
          false
        else
          arrayType == target.asArrayType
      case ConcreteRefValues.NullValue() => target == null

  given intSizeOps: SizeOps[I32, Boolean] with
    override def is32Bit(v: I32): Boolean = true

  given floatSizeOps: SizeOps[F32, Boolean] with
    override def is32Bit(v: F32): Boolean = true

  given longSizeOps: SizeOps[I64, Boolean] with
    override def is32Bit(v: I64): Boolean = false

  given doubleSizeOps: SizeOps[F64, Boolean] with
    override def is32Bit(v: F64): Boolean = false

  given refSizeOps: SizeOps[RefValue, Boolean] with
    override def is32Bit(v: RefValue): Boolean = true

  given ConcreteObjectOps[FieldAddr, FieldName, OID, V, Site]
  (using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, V, NoJoin], project: Project[URL], f: Failure): ObjectOps[FieldName, OID, V, ClassFile, ConcreteRefValues[OID, ClassFile, FieldName, FieldAddr, Addr, Addr, AType, Value], Site, Method, String, MethodDescriptor, I32, NoJoin] with
    type RefValue = ConcreteRefValues[OID, ClassFile, FieldName, FieldAddr, Addr, Addr, AType, Value]
    override def makeObject(oid: OID, cfs: ClassFile, vals: Seq[(V, Site, FieldName)]): ConcreteRefValues[OID, ClassFile, FieldName, FieldAddr, Addr, Addr, AType, Value] =
      val fieldAddrs = vals.map { (v, site, name) =>
        val addr = alloc(site)
        store.write(addr, v)
        (name, addr)
      }.toMap
      ConcreteRefValues.Object(oid, cfs, fieldAddrs)

    override def getField(obj: RefValue, name: FieldName)(using failure: Failure): V = obj match
      case ConcreteRefValues.Object(_, _, fields: Map[FieldName, FieldAddr]) =>
        store.read(fields.getOrElse(name, failure.fail(BytecodeFailure.FieldNotFound, s"field $name not found"))).getOrElse(failure.fail(BytecodeFailure.UnboundField, s"$name not bound"))
      case ConcreteRefValues.NullValue() => except.throws(JvmExcept.Throw(ClassType("java/lang/NullPointerException")))
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def setField(obj: RefValue, name: FieldName, v: V): JOptionC[Unit] = obj match
      case ConcreteRefValues.Object(_, _, fields: Map[FieldName, FieldAddr]) =>
        if !fields.contains(name) then
          JOptionC.none
        else
          store.write(fields(name), v)
          JOptionC.some(())
      case ConcreteRefValues.NullValue() => except.throws(JvmExcept.Throw(ClassType("java/lang/NullPointerException")))
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def invokeFunctionCorrect(obj: RefValue, mthName: String, sig: MthSig, args: Seq[V])(invoke: (RefValue, Mth, Seq[V]) => V): V = obj match
      case ConcreteRefValues.Object(_, cls: ClassFile, _) =>
        val mth = AuxiliaryFunctions.findMethodOfSuperclass(cls, mthName, sig, project)
        invoke(obj, mth, args)
      case ConcreteRefValues.NullValue() => except.throws(JvmExcept.Throw(ClassType("java/lang/NullPointerException")))
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def makeNull(): RefValue = ConcreteRefValues.NullValue()

    override def isNull(obj: RefValue): I32 = obj match
      case ConcreteRefValues.NullValue() => 1
      case _ => 0

  given ConcreteArrayOps[AID, ArrayElemAddr, V, AType, Site]
    (using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, V, NoJoin]): ArrayOps[AID, Int, V, ConcreteRefValues[Addr, ClassFile, FieldName, Addr, AID, ArrayElemAddr, AType, V], AType, Site, NoJoin] with
    type RefValue = ConcreteRefValues[Addr, ClassFile, FieldName, Addr, AID, ArrayElemAddr, AType, V]
    override def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType, arraySize: V): RefValue =
      val valAddrs = vals.map { (v, site) =>
        val addr = alloc(site)
        store.write(addr, v)
        addr
      }.toVector
      ConcreteRefValues.nonNullArray(aid, valAddrs, arrayType, arraySize)

    override def getVal(array: RefValue, idx: Int): JOption[NoJoin, V] = array match
      case ConcreteRefValues.nonNullArray(_, vals: Vector[ArrayElemAddr], _, _) =>
        if idx >= vals.size then
          JOptionC.none
        else
          store.read(vals(idx))
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def setVal(array: RefValue, idx: Int, v: V): JOptionC[Unit] = array match
      case ConcreteRefValues.nonNullArray(_, vals: Vector[ArrayElemAddr], _, _) =>
        if (idx >= vals.size)
          JOptionC.none
        else {
          store.write(vals(idx), v)
          JOptionC.some(())
        }
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def arrayLength(array: RefValue): V = array match
      case ConcreteRefValues.nonNullArray(_, _, _, size: V) =>
        size
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def initArray(size: Int): Seq[Any] =
      Seq.fill(size){}

    override def arraycopy(src: RefValue, srcPos: Int, dest: RefValue, destPos: Int, length: Int): JOption[MayJoin.NoJoin, Unit] = (src, dest) match
      case (ConcreteRefValues.nonNullArray(_, srcVals: Vector[ArrayElemAddr], _, _), ConcreteRefValues.nonNullArray(_, destVals: Vector[ArrayElemAddr], _, _)) =>
        boundary:
          for (i <- 0 until length) do
            if srcPos+i >= srcVals.size || destPos+i >= destVals.size then
              break(JOptionC.none)
            else
              val toCopy = store.read(srcVals(srcPos + i)).get
              store.write(destVals(destPos + i), toCopy)
        JOptionC.some(())
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $src, $dest")

    override def getArray(array: RefValue): Seq[JOption[NoJoin, V]] = array match
      case ConcreteRefValues.nonNullArray(_, vals, _, _) =>
        val arrayVals = vals.map(addr => getVal(array, vals.indexOf(addr)))
        arrayVals
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $array")

    override def printString(letters: Seq[Int]): Unit =
      println(letters.map(l => l.toChar))

  given RefEqOps[AID, OID, ASize]: EqOps[RefValue, Boolean] with
    override def equ(v1: RefValue, v2: RefValue): Boolean = (v1, v2) match
      case (ConcreteRefValues.Object(oid1, _, _), ConcreteRefValues.Object(oid2, _, _)) =>
        oid1 == oid2
      case (ConcreteRefValues.nonNullArray(aid1, _, _, _), ConcreteRefValues.nonNullArray(aid2, _, _, _)) =>
        aid1 == aid2
      case (ConcreteRefValues.NullValue(), ConcreteRefValues.NullValue()) =>
        true
      case _ =>
        throw new IllegalArgumentException(s"trying to compare values $v1 and $v2")

    override def neq(v1: RefValue, v2: RefValue): Boolean = (v1, v2) match
      case (ConcreteRefValues.Object(oid1, _, _), ConcreteRefValues.Object(oid2, _, _)) =>
        oid1 != oid2
      case (ConcreteRefValues.nonNullArray(aid1, _, _, _), ConcreteRefValues.nonNullArray(aid2, _, _, _)) =>
        aid1 != aid2
      case (ConcreteRefValues.NullValue(), ConcreteRefValues.NullValue()) =>
        false
      case _ =>
        throw new IllegalArgumentException(s"trying to compare values $v1 and $v2")

  private type InitialStore = Map[Addr, Value]

  class Instance(files: Project[URL], path: String, initStore: InitialStore) extends GenericInstance:
    val newFrameData: FrameData = 0
    val args: List[Value] = List()

    val joinUnit: MayJoin.NoJoin[Unit] = implicitly
    val jvV: MayJoin.NoJoin[Value] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val failure: ConcreteFailure = new ConcreteFailure
    val frame: ConcreteCallFrame[FrameData, Int, Value, Site] = ConcreteCallFrame[FrameData, Int, Value, Site](newFrameData, args.view.zipWithIndex.map((x,y) => (y, Some(x))))
    val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = new ConcreteExcept
    val objAlloc: Allocator[Addr, Site] = new CAllocatorIntIncrement
    val objFieldAlloc: CAllocatorIntIncrement[Site] = new CAllocatorIntIncrement
    val arrayAlloc: CAllocatorIntIncrement[Site] = new CAllocatorIntIncrement
    val arrayValAlloc: CAllocatorIntIncrement[Site] = new CAllocatorIntIncrement
    val staticAlloc: CAllocatorIntIncrement[Site] = new CAllocatorIntIncrement
    override val store: CStore[Addr, Value] = new CStore(initStore)

    val staticAddrMap: scala.collection.mutable.Map[(ClassType, String), Addr] = scala.collection.mutable.Map()

    override val project: Project[URL] = files
    given Project[URL] = project
    val projectSource: String = path

    private given Failure = failure

    val bytecodeOps: BytecodeOps[Idx, Value, TypeRep] = implicitly
    val objectOps: ObjectOps[FieldName, Addr, Value, ObjType, Value, Site, Mth, MthName, MthSig, Value, MayJoin.NoJoin] =
      LiftedObjectOps[FieldName, Addr, Value, ObjType, Value, Site, Mth, MthName, MthSig, Value, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using ConcreteObjectOps[Addr, FieldName, Addr, Value, Site](using objFieldAlloc, store, project)
      )
    val arrayOps: ArrayOps[Addr, Value, Value, Value, AType, Site, MayJoin.NoJoin] =
      LiftedArrayOps[Addr, Value, Value, Value, AType, Site, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using ConcreteArrayOps[Addr, Addr, Value, AType, Site](using arrayValAlloc, store)
      )

    val fixpoint: ConcreteFixpoint[FixIn, FixOut] = ConcreteFixpoint[FixIn, FixOut]
    override val fixpointSuper: Fixpoint[FixIn, FixOut] = fixpoint
