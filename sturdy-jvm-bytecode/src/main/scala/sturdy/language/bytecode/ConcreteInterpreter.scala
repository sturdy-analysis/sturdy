package sturdy.language.bytecode

import org.opalj.br.{ArrayType, ClassFile, FieldType, Method, MethodDescriptor, ObjectType, ReferenceType}
import org.opalj.br.analyses.Project
import sturdy.data.{MayJoin, *, given}
import sturdy.effect.allocation.{Allocator, CAllocatorIntIncrement}
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.{ConcreteExcept, Except}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.store.{CStore, Store}
import sturdy.fix.Fixpoint
import sturdy.language.bytecode.Interpreter
import sturdy.language.bytecode.generic.*
import sturdy.values.booleans.{BooleanBranching, ConcreteBooleanBranching}
import sturdy.values.exceptions.ConcreteExceptional
import sturdy.values.floating.FloatOps
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.objects.{*, given}
import sturdy.values.arrays.{*, given}
import sturdy.fix
import sturdy.language.bytecode.AuxillaryFunctions
import sturdy.language.bytecode.ConcreteRefValues.NullValue
import sturdy.values.ordering.EqOps

import java.io.File
import java.net.URL

enum ConcreteRefValues:
  case nonNullObject[OID, CF, FieldAddr, FieldName](oid: OID, cls: CF, fields: Map[FieldName, FieldAddr])
  case nonNullArray[AID, ArrayElemAddr, AType, ASize](aid: AID, vals: Vector[ArrayElemAddr], arrayType: AType, arraySize: ASize)
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

  override type Mth = Method
  override type MthName = String
  override type MthSig = MethodDescriptor
  override type ObjType = ClassFile
  override type FieldAddr = Int
  override type StaticAddr = Int
  override type Idx = Int
  override type TypeRep = ReferenceType
  //override type NullVal = Null
  override type ObjAddr = Int
  override type FieldName = (ObjectType, String)
  //override type ObjRep = Object[ObjAddr, ClassFile, FieldAddr, FieldName]
  override type ArrayAddr = Int
  override type AType = ArrayType
  //override type ArrayRep = Array[ArrayAddr, FieldAddr, ArrayType, Value]
  override type ArrayElemAddr = Int

  override type RefValue = ConcreteRefValues


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
  override def topRef: ConcreteRefValues = throw new UnsupportedOperationException
  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0

  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

/*  given objectTypeOps[OID, Addr, FieldName](using project: Project[URL]): TypeOps[Object[OID, ClassFile, Addr, FieldName], TypeRep, Bool] =
    new ConcreteObjectTypeOps({ (cls, target) =>
      if (target == null)
        false
      else
        cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy)
    })

  given arrayTypeOps[AID, Addr]: TypeOps[Array[AID, Addr, AType, Value], ReferenceType, Boolean] =
    new ConcreteArrayTypeOps((atype, target) => target != null && atype == target.asArrayType)

  given nullTypeOps: TypeOps[NullVal, TypeRep, Bool] with
    override def instanceOf(v: NullVal, target: ReferenceType): Boolean =
      if (target == null) {
        true
      }
      else {
        false
      }
*/

  given concreteTypeOps[OID, AID](using project: Project[URL]): TypeOps[RefValue, TypeRep, Bool] with
    override def instanceOf(v: ConcreteRefValues, target: TypeRep): Boolean = v match
      case v: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName] =>
        if(target == null)
          false
        else
          v.cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy)
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


  given intSizeOps: SizeOps[I32, Boolean] with
    override def is32Bit(v: I32): Boolean = true

  given floatSizeOps: SizeOps[F32, Boolean] with
    override def is32Bit(v: F32): Boolean = true

  given longSizeOps: SizeOps[I64, Boolean] with
    override def is32Bit(v: I64): Boolean = false

  given doubleSizeOps: SizeOps[F64, Boolean] with
    override def is32Bit(v: F64): Boolean = false

/*  given objectSizeOps[OID, Addr, FieldName]: SizeOps[Object[OID, ClassFile, Addr, FieldName], Boolean] with
    override def is32Bit(v: Object[OID, ClassFile, Addr, FieldName]): Boolean = true

  given arraySizeOps[AID, Addr, ArrayType]: SizeOps[Array[AID, Addr, ArrayType, Value], Boolean] with
    override def is32Bit(v: Array[AID, Addr, ArrayType, Value]): Boolean = true
*/

  given refSizeOps: SizeOps[ConcreteRefValues, Boolean] with
    override def is32Bit(v: ConcreteRefValues): Boolean = true

/*  given TestConcObjectOps[FieldAddr, FieldName, OID, V, Site]
  (using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, V, NoJoin], project: Project[URL], f: Failure): ObjectOps[FieldName, OID, V, ClassFile, Object[OID, ClassFile, FieldAddr, FieldName], Site, Method, String, MethodDescriptor, Null, NoJoin] with
    override def makeObject(oid: OID, cfs: ClassFile, vals: Seq[(V, Site, FieldName)]): Object[OID, ClassFile, FieldAddr, FieldName] =
      val fieldAddrs = vals.map { (v, site, name) =>
        val addr = alloc(site)
        store.write(addr, v)
        (name, addr)
      }.toVector.toMap
      Object(oid, cfs, fieldAddrs)

    override def getField(obj: Object[OID, ClassFile, FieldAddr, FieldName], name: FieldName): JOption[NoJoin, V] =
      if (!obj.fields.contains(name))
        JOptionC.none
      else
        store.read(obj.fields(name))

    override def setField(obj: Object[OID, ClassFile, FieldAddr, FieldName], name: FieldName, v: V): JOptionC[Unit] =
      if (!obj.fields.contains(name))
        JOptionC.none
      else {
        store.write(obj.fields(name), v)
        JOptionC.some(())
      }

    override def invokeFunctionCorrect(obj: Object[OID, ClassFile, FieldAddr, FieldName], mthName: String, sig: MthSig, args: Seq[V])(invoke: (Object[OID, ClassFile, FieldAddr, FieldName], Mth, Seq[V]) => V): V =
      val mth = AuxillaryFunctions.findMethodOfSuperclass(obj.cls, mthName, sig, project)
      invoke(obj, mth, args)

    override def makeNull(): Null = null

*/
  given TestConcObjectOps[FieldAddr, FieldName, OID, V, Site]
  (using alloc: Allocator[FieldAddr, Site], store: Store[FieldAddr, V, NoJoin], project: Project[URL], f: Failure): ObjectOps[FieldName, OID, V, ClassFile, ConcreteRefValues, Site, Method, String, MethodDescriptor, I32, NoJoin] with
    override def makeObject(oid: OID, cfs: ClassFile, vals: Seq[(V, Site, FieldName)]): RefValue =
      val fieldAddrs = vals.map { (v, site, name) =>
        val addr = alloc(site)
        store.write(addr, v)
        (name, addr)
      }.toVector.toMap
      ConcreteRefValues.nonNullObject(oid, cfs, fieldAddrs)

    override def getField(obj: RefValue, name: FieldName): JOption[NoJoin, V] = obj match
      case obj: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName] =>
        if (!obj.fields.contains(name))
          JOptionC.none
        else
          store.read(obj.fields(name))
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def setField(obj: RefValue, name: FieldName, v: V): JOptionC[Unit] = obj match
      case obj: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName] =>
        if (!obj.fields.contains(name))
          JOptionC.none
        else {
          store.write(obj.fields(name), v)
          JOptionC.some(())
        }
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def invokeFunctionCorrect(obj: RefValue, mthName: String, sig: MthSig, args: Seq[V])(invoke: (RefValue, Mth, Seq[V]) => V): V = obj match
      case obj: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName] =>
        val mth = AuxillaryFunctions.findMethodOfSuperclass(obj.cls, mthName, sig, project)
        invoke(obj, mth, args)
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $obj")

    override def makeNull(): RefValue = ConcreteRefValues.NullValue()

    override def isNull(obj: RefValue): I32 = obj match
      case obj: ConcreteRefValues.NullValue => 1
      case _ => 0

  given ConcreteArrayOps[AID, V, AType, Site]
    (using alloc: Allocator[ArrayElemAddr, Site], store: Store[ArrayElemAddr, V, NoJoin]): ArrayOps[AID, Int, V, RefValue, AType, Site, NoJoin] with
    override def makeArray(aid: AID, vals: Seq[(V, Site)], arrayType: AType, arraySize: V): RefValue =
      val valAddrs = vals.map{ (v, site) =>
        val addr = alloc(site)
        store.write(addr, v)
        addr
      }.toVector
      ConcreteRefValues.nonNullArray(aid, valAddrs, arrayType, arraySize)
    override def getVal(array: RefValue, idx: Int): JOption[NoJoin, V] = array match
      case array: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, I32] =>
        if (idx >= array.vals.size)
          JOptionC.none
        else
          store.read(array.vals(idx))
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")
    override def setVal(array: RefValue, idx: Int, v: V): JOptionC[Unit] = array match
      case array: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, I32] =>
        if (idx >= array.vals.size)
          JOptionC.none
        else {
          store.write(array.vals(idx), v)
          JOptionC.some(())
        }
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def arrayLength(array: RefValue): V = array match
      case array: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, V] =>
        array.arraySize
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $array")

    override def initArray(size: Int): Seq[Any] =
      Seq.fill(size){}

    override def arraycopy(src: RefValue, srcPos: Int, dest: RefValue, destPos: Int, length: Int): JOption[MayJoin.NoJoin, Unit] = (src, dest) match
      case (src, dest): (ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, I32], ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, I32]) =>
        for (i <- 0 until length){
          if(srcPos+i >= src.vals.size || destPos+i >= dest.vals.size){
            return JOptionC.none
          }
          else{
            val toCopy = store.read(src.vals(srcPos + i)).get
            store.write(dest.vals(destPos + i), toCopy)
          }
        }
        JOptionC.some(())
      case _ =>
        throw UnsupportedOperationException(s"attempted array operations on $src, $dest")

    override def getArray(array: RefValue): Seq[JOption[NoJoin, V]] = array match
      case array: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, I32] =>
        val arrayVals = array.vals.map(addr => getVal(array, array.vals.indexOf(addr)))
        arrayVals
      case _ =>
        throw UnsupportedOperationException(s"attempted object operations on $array")

    override def printString(letters: Seq[Int]): Unit =
      println(letters.map(l => l.toChar))

  given RefEqOps[AID, OID, ASize]: EqOps[RefValue, Boolean] with
    override def equ(v1: RefValue, v2: RefValue): Boolean = (v1, v2) match
      case (v1: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName], v2: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName]) =>
        v1.oid == v2.oid
      case (v1: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, ASize], v2: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, ASize]) =>
        v1.aid == v2.aid
      case (v1: ConcreteRefValues.NullValue, v2: ConcreteRefValues.NullValue) =>
        true
      case _ =>
        throw new IllegalArgumentException(s"trying to compare values $v1 and $v2")

    override def neq(v1: ConcreteRefValues, v2: ConcreteRefValues): Boolean = (v1, v2) match
      case (v1: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName], v2: ConcreteRefValues.nonNullObject[OID, ClassFile, FieldAddr, FieldName]) =>
        v1.oid != v2.oid
      case (v1: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, ASize], v2: ConcreteRefValues.nonNullArray[AID, ArrayElemAddr, AType, ASize]) =>
        v1.aid != v2.aid
      case (v1: ConcreteRefValues.NullValue, v2: ConcreteRefValues.NullValue) =>
        false
      case _ =>
        throw new IllegalArgumentException(s"trying to compare values $v1 and $v2")

  type varStore = Map[FieldAddr, Value]
  type StaticStore = Map[StaticAddr, Value]

  class Instance(files: Project[URL], path: String, initStore: varStore, initArrayValStore: varStore, initStaticStore: StaticStore) extends GenericInstance:
    val newFrameData: FrameData = 0
    val args: List[Value] = List()

    val joinUnit: MayJoin.NoJoin[Unit] = implicitly
    val jvV: MayJoin.NoJoin[Value] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val failure: ConcreteFailure = new ConcreteFailure
    val frame: ConcreteCallFrame[FrameData, Int, Value] = new ConcreteCallFrame[FrameData, Int, Value](newFrameData, args.view.zipWithIndex.map((x,y) => (y, Some(x))))
    val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = new ConcreteExcept
    val objAlloc: CAllocatorIntIncrement[InstructionSite] = new CAllocatorIntIncrement
    val objFieldAlloc: CAllocatorIntIncrement[FieldInitSite] = new CAllocatorIntIncrement
    val arrayAlloc: CAllocatorIntIncrement[InstructionSite] = new CAllocatorIntIncrement
    val arrayValAlloc: CAllocatorIntIncrement[ArrayElemInitSite] = new CAllocatorIntIncrement
    val staticAlloc: CAllocatorIntIncrement[StaticInitSite] = new CAllocatorIntIncrement
    val objFieldStore: CStore[FieldAddr, Value] = new CStore(initStore)
    val arrayValStore: CStore[FieldAddr, Value] = new CStore(initArrayValStore)
    val staticVarStore: CStore[StaticAddr, Value] = new CStore(initStaticStore)
    
    val staticAddrMap: scala.collection.mutable.Map[(ObjectType, String), StaticAddr] = scala.collection.mutable.Map()

    override val project: Project[URL] = files
    given Project[URL] = project
    val projectSource: String = path

    private given Failure = failure

    val bytecodeOps: BytecodeOps[Idx, Value, TypeRep] = implicitly
    val objectOps: ObjectOps[FieldName, ObjAddr, Value, ObjType, Value, FieldInitSite, Mth, MthName, MthSig, Value, MayJoin.NoJoin] =
      new LiftedObjectOps[FieldName, ObjAddr, Value, ObjType, Value, FieldInitSite, Mth, MthName, MthSig, Value, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new TestConcObjectOps(using objFieldAlloc, objFieldStore, project)
      )
    val arrayOps: ArrayOps[ArrayAddr, Value, Value, Value, AType, ArrayElemInitSite, MayJoin.NoJoin] =
      new LiftedArrayOps[ArrayAddr, Value, Value, Value, AType, ArrayElemInitSite, MayJoin.NoJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new ConcreteArrayOps(using arrayValAlloc, arrayValStore)
      )

    val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut]
    override val fixpointSuper = fixpoint


