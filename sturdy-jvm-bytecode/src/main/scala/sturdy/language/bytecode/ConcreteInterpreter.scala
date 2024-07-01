package sturdy.language.bytecode

import org.opalj.br.{ArrayType, ClassFile, FieldType, Method, MethodDescriptor, ObjectType, ReferenceType}
import org.opalj.br.analyses.Project
import sturdy.data.{MayJoin, *, given}
import sturdy.effect.allocation.{Allocation, CAllocationIntIncrement}
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

import java.io.File
import java.net.URL

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
  override type Idx = Int
  override type TypeRep = ReferenceType
  override type NullVal = Null
  override type ObjAddr = Int
  override type FieldName = String
  override type ObjRep = Object[ObjAddr, ClassFile, FieldAddr, FieldName]
  override type ArrayAddr = Int
  override type AType = ArrayType
  override type ArrayRep = Array[ArrayAddr, FieldAddr, ArrayType]
  override type ArrayElemAddr = Int

  override type ExcV = JvmExcept[Value]

  //override def topI8: Byte = throw new UnsupportedOperationException
  //override def topI16: Short = throw new UnsupportedOperationException
  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException
  override def topObj: Object[ConcreteInterpreter.ObjAddr, ClassFile, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.FieldName] = throw new UnsupportedOperationException
  override def topArray: Array[ConcreteInterpreter.ArrayAddr, ConcreteInterpreter.FieldAddr, ConcreteInterpreter.AType] = throw new UnsupportedOperationException
  override def topNull: Null = throw new UnsupportedOperationException
  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0

  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

  given objectTypeOps[OID, Addr, FieldName](using project: Project[URL]): TypeOps[Object[OID, ClassFile, Addr, FieldName], TypeRep, Bool] =
    new ConcreteObjectTypeOps({ (cls, target) =>
      if (target == null)
        false
      else
        cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy)
    })

  given arrayTypeOps[AID, Addr]: TypeOps[Array[AID, Addr, AType], ReferenceType, Boolean] =
    new ConcreteArrayTypeOps((atype, target) => target != null && atype == target.asArrayType)

  given nullTypeOps: TypeOps[NullVal, TypeRep, Bool] with
    override def instanceOf(v: NullVal, target: ReferenceType): Boolean =
      if (target == null) {
        true
      }
      else {
        false
      }

  given intSizeOps: SizeOps[I32, Boolean] with
    override def is32Bit(v: I32): Boolean = true

  given floatSizeOps: SizeOps[F32, Boolean] with
    override def is32Bit(v: F32): Boolean = true

  given longSizeOps: SizeOps[I64, Boolean] with
    override def is32Bit(v: I64): Boolean = false

  given doubleSizeOps: SizeOps[F64, Boolean] with
    override def is32Bit(v: F64): Boolean = false

  given objectSizeOps[OID, Addr, FieldName]: SizeOps[Object[OID, ClassFile, Addr, FieldName], Boolean] with
    override def is32Bit(v: Object[OID, ClassFile, Addr, FieldName]): Boolean = true

  given arraySizeOps[AID, Addr, ArrayType]: SizeOps[Array[AID, Addr, ArrayType], Boolean] with
    override def is32Bit(v: Array[AID, Addr, ArrayType]): Boolean = true

  given TestConcObjectOps[FieldAddr, FieldName, OID, V, Site]
  (using alloc: Allocation[FieldAddr, Site], store: Store[FieldAddr, V, NoJoin], project: Project[URL], f: Failure): ObjectOps[FieldName, OID, V, ClassFile, Object[OID, ClassFile, FieldAddr, FieldName], Object[OID, ClassFile, FieldAddr, FieldName], Site, Method, String, MethodDescriptor, Null, NoJoin] with
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


  type varStore = Map[FieldAddr, Value]
  type StaticStore = Map[(ObjectType, String), Value]

  class Instance(files: Project[URL], path: String, initStore: varStore, initArrayValStore: varStore, initStaticStore: StaticStore) extends GenericInstance:
    val newFrameData: FrameData = 0
    val args: List[Value] = List()

    val joinUnit: MayJoin.NoJoin[Unit] = implicitly
    val jvV: MayJoin.NoJoin[Value] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val failure: ConcreteFailure = new ConcreteFailure
    val frame: ConcreteCallFrame[FrameData, Int, Value] = new ConcreteCallFrame[FrameData, Int, Value](newFrameData, args.view.zipWithIndex.map(_.swap))
    val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = new ConcreteExcept
    val objAlloc: CAllocationIntIncrement[InstructionSite] = new CAllocationIntIncrement
    val objFieldAlloc: CAllocationIntIncrement[FieldInitSite] = new CAllocationIntIncrement
    val arrayAlloc: CAllocationIntIncrement[InstructionSite] = new CAllocationIntIncrement
    val arrayValAlloc: CAllocationIntIncrement[ArrayElemInitSite] = new CAllocationIntIncrement
    val objFieldStore: CStore[FieldAddr, Value] = new CStore(initStore)
    val arrayValStore: CStore[FieldAddr, Value] = new CStore(initArrayValStore)
    val staticVarStore: CStore[(ObjectType, String), Value] = new CStore(initStaticStore)

    override val project: Project[URL] = files
    given Project[URL] = project
    val projectSource: String = path

    private given Failure = failure

    val bytecodeOps: BytecodeOps[Idx, Value, TypeRep] = implicitly
    val objectOps: ObjectOps[FieldName, ObjAddr, Value, ObjType, ObjRep, Value, FieldInitSite, Mth, MthName, MthSig, Value, MayJoin.NoJoin] =
      new LiftedObjectOps[FieldName, ObjAddr, Value, ObjType, ObjRep, Value, FieldInitSite, Mth, MthName, MthSig, Value, MayJoin.NoJoin, ObjRep, NullVal](_.asObj, Value.Obj.apply, _.asNull, Value.Null.apply)(
        using new TestConcObjectOps(using objFieldAlloc, objFieldStore, project)
      )
    val arrayOps: ArrayOps[ArrayAddr, Value, Value, Value, AType, ArrayElemInitSite, MayJoin.NoJoin] =
      new LiftedArrayOps[ArrayAddr, Value, Value, Value, AType, ArrayElemInitSite, MayJoin.NoJoin, ArrayRep, Int](_.asArray, Value.Array.apply, _.asInt32, Value.Int32.apply)(
        using new ConcreteArrayOps(using arrayValAlloc, arrayValStore)
      )

    val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut]
    override val fixpointSuper = fixpoint


