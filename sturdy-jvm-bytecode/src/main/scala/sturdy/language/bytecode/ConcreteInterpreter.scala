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
import scala.collection.mutable

enum ConcreteRefValues[ObjectAddr, Class, FieldName, ObjFieldAddr, ArrayAddr, ArrayElemAddr, AType, ASizeType]:
  case Object(oid: ObjectAddr, cls: Class, fields: Map[FieldName, ObjFieldAddr])
  case nonNullArray(aid: ArrayAddr, vals: Vector[ArrayElemAddr], arrayType: AType, arraySize: ASizeType)
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
  override def topI32: Int = throw UnsupportedOperationException()

  override def topI64: Long = throw UnsupportedOperationException()

  override def topF32: Float = throw UnsupportedOperationException()

  override def topF64: Double = throw UnsupportedOperationException()

  //override def topObj: Object[ConcreteInterpreter.ObjAddr, ClassFile, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.FieldName] = throw new UnsupportedOperationException
  //override def topArray: Array[ConcreteInterpreter.ArrayAddr, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.AType, ConcreteInterpreter.Value] = throw new UnsupportedOperationException
  //override def topNull: Null = throw new UnsupportedOperationException
  override def topRef: RefValue = throw UnsupportedOperationException()

  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0

  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

  given ConcreteTypeOps(using project: Project[URL]): TypeOps[RefValue, TypeRep, Bool] with
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

  given ConcreteObjectOps
  (using alloc: Allocator[Addr, Site], store: Store[Addr, Value, NoJoin], project: Project[URL], f: Failure): ObjectOps[FieldName, Addr, Value, ClassFile, RefValue, Site, Method, String, MethodDescriptor, I32, NoJoin] with
    override def makeObject(oid: Addr, cfs: ClassFile, vals: Seq[(Value, Site, FieldName)]): RefValue =
      val fieldAddrs = vals.map { (v, site, name) =>
        val addr = alloc(site)
        store.write(addr, v)
        (name, addr)
      }.toMap
      ConcreteRefValues.Object(oid, cfs, fieldAddrs)

    override def getField(obj: RefValue, name: FieldName)(using failure: Failure): Value = obj match
      case ConcreteRefValues.Object(_, _, fields: Map[FieldName, Addr]) =>
        store.read(fields.getOrElse(name, failure.fail(BytecodeFailure.FieldNotFound, s"field $name not found"))).getOrElse(failure.fail(BytecodeFailure.UnboundField, s"$name not bound"))
      case ConcreteRefValues.NullValue() => except.throws(JvmExcept.Throw(ClassType("java/lang/NullPointerException")))
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def setField(obj: RefValue, name: FieldName, v: Value): JOptionC[Unit] = obj match
      case ConcreteRefValues.Object(_, _, fields: Map[FieldName, Addr]) =>
        if !fields.contains(name) then
          JOptionC.none
        else
          store.write(fields(name), v)
          JOptionC.some(())
      case ConcreteRefValues.NullValue() => except.throws(JvmExcept.Throw(ClassType("java/lang/NullPointerException")))
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def invokeFunctionCorrect(obj: RefValue, mthName: String, sig: MthSig, args: Seq[Value])(invoke: (RefValue, Mth, Seq[Value]) => Value): Value = obj match
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

  given ConcreteArrayOps
  (using alloc: Allocator[Addr, Site], store: Store[Addr, Value, NoJoin]): ArrayOps[Addr, Int, Value, RefValue, AType, Site, NoJoin] with
    override def makeArray(aid: Addr, vals: Seq[(Value, Site)], arrayType: AType, arraySize: Value): RefValue =
      val valAddrs = vals.map { (v, site) =>
        val addr = alloc(site)
        store.write(addr, v)
        addr
      }.toVector
      ConcreteRefValues.nonNullArray(aid, valAddrs, arrayType, arraySize)

    override def getVal(array: RefValue, idx: Int): JOption[NoJoin, Value] = array match
      case ConcreteRefValues.nonNullArray(_, vals: Vector[Addr], _, _) =>
        if idx >= vals.size then
          JOptionC.none
        else
          store.read(vals(idx))
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def setVal(array: RefValue, idx: Int, v: Value): JOptionC[Unit] = array match
      case ConcreteRefValues.nonNullArray(_, vals: Vector[Addr], _, _) =>
        if (idx >= vals.size)
          JOptionC.none
        else {
          store.write(vals(idx), v)
          JOptionC.some(())
        }
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def arrayLength(array: RefValue): Value = array match
      case ConcreteRefValues.nonNullArray(_, _, _, size: Value) =>
        size
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def initArray(size: Int): Seq[Any] =
      Seq.fill(size) {}

    override def arraycopy(src: RefValue, srcPos: Int, dest: RefValue, destPos: Int, length: Int): JOption[MayJoin.NoJoin, Unit] = (src, dest) match
      case (ConcreteRefValues.nonNullArray(_, srcVals: Vector[Addr], _, _), ConcreteRefValues.nonNullArray(_, destVals: Vector[Addr], _, _)) =>
        boundary:
          for (i <- 0 until length) do
            if srcPos + i >= srcVals.size || destPos + i >= destVals.size then
              break(JOptionC.none)
            else
              val toCopy = store.read(srcVals(srcPos + i)).get
              store.write(destVals(destPos + i), toCopy)
        JOptionC.some(())
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $src, $dest")

    override def getArray(array: RefValue): Seq[JOption[NoJoin, Value]] = array match
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
        throw IllegalArgumentException(s"trying to compare values $v1 and $v2")

    override def neq(v1: RefValue, v2: RefValue): Boolean = (v1, v2) match
      case (ConcreteRefValues.Object(oid1, _, _), ConcreteRefValues.Object(oid2, _, _)) =>
        oid1 != oid2
      case (ConcreteRefValues.nonNullArray(aid1, _, _, _), ConcreteRefValues.nonNullArray(aid2, _, _, _)) =>
        aid1 != aid2
      case (ConcreteRefValues.NullValue(), ConcreteRefValues.NullValue()) =>
        false
      case _ =>
        throw IllegalArgumentException(s"trying to compare values $v1 and $v2")

  private type InitialStore = Map[Addr, Value]

  class Instance(files: Project[URL], path: String, initStore: InitialStore) extends GenericInstance:
    val newFrameData: FrameData = 0
    val args: List[Value] = List()

    override val joinUnit: MayJoin.NoJoin[Unit] = implicitly
    override val jvV: MayJoin.NoJoin[Value] = implicitly

    override val stack: ConcreteOperandStack[Value] = ConcreteOperandStack[Value]
    override implicit val failure: ConcreteFailure = ConcreteFailure()
    override val frame: ConcreteCallFrame[FrameData, Int, Value, Site] = ConcreteCallFrame[FrameData, Int, Value, Site](newFrameData, args.view.zipWithIndex.map((x, y) => (y, Some(x))))
    override val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = ConcreteExcept[JvmExcept[Value]]
    override val objAlloc: Allocator[Addr, Site] = CAllocatorIntIncrement[Site]
    override val objFieldAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val arrayAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val arrayValAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val staticAlloc: CAllocatorIntIncrement[Site] = CAllocatorIntIncrement[Site]
    override val store: CStore[Addr, Value] = CStore(initStore)

    override val staticAddrMap: mutable.Map[(ClassType, String), Addr] = mutable.Map()

    override implicit val project: Project[URL] = files

    override val projectSource: String = path

    override val bytecodeOps: BytecodeOps[Idx, Value, TypeRep] = implicitly
    override val objectOps: ObjectOps[FieldName, Addr, Value, ObjType, Value, Site, Mth, MthName, MthSig, Value, MayJoin.NoJoin] =
      LiftedObjectOps[FieldName, Addr, Value, ObjType, Value, Site, Mth, MthName, MthSig, Value, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using ConcreteObjectOps(using objFieldAlloc, store, project)
      )
    override val arrayOps: ArrayOps[Addr, Value, Value, Value, AType, Site, MayJoin.NoJoin] =
      LiftedArrayOps[Addr, Value, Value, Value, AType, Site, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using ConcreteArrayOps(using arrayValAlloc, store)
      )

    override val fixpoint: ConcreteFixpoint[FixIn, FixOut] = ConcreteFixpoint[FixIn, FixOut]
    override val fixpointSuper: Fixpoint[FixIn, FixOut] = fixpoint
