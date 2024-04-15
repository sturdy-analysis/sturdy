package sturdy.language.bytecode

import org.opalj.br.{ArrayType, ClassFile, FieldType, Method, MethodDescriptor, ObjectType, ReferenceType}
import org.opalj.br.analyses.Project
import sturdy.data.{MayJoin, *, given}
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.{ConcreteExcept, Except}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.store.CStore
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

import java.io.File
import java.net.URL

object ConcreteInterpreter extends Interpreter:
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
  override type Addr = Int
  override type Idx = Int
  override type TypeRep = ReferenceType
  override type NullVal = Null
  override type OID = Int
  override type FieldName = String
  override type ObjRep = Object[OID, ClassFile, Addr, FieldName]
  override type AID = Int
  override type AType = ArrayType
  override type ArrayRep = Array[AID, Addr, ArrayType]

  //override def topI8: Byte = throw new UnsupportedOperationException
  //override def topI16: Short = throw new UnsupportedOperationException
  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException
  override def topObj: Object[ConcreteInterpreter.OID, ClassFile, ConcreteInterpreter.Addr, ConcreteInterpreter.FieldName] = throw new UnsupportedOperationException
  override def topArray: Array[ConcreteInterpreter.AID, ConcreteInterpreter.Addr, ConcreteInterpreter.AType] = throw new UnsupportedOperationException
  override def topNull: Null = throw new UnsupportedOperationException
  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0
  def asObj(v: Value)(using Failure): ObjRep = v.asObj
  def asArray(v: Value)(using Failure): ArrayRep = v.asArray
  def asInt32(v: Value)(using Failure): I32 = v.asInt32
  def asNull(v: Value)(using Failure): NullVal = v.asNull
  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)


  type Store = Map[Addr, Value]
  type StaticStore = Map[(ObjectType, String), Value]
  class Instance(files: Project[URL], path: String, initStore: Store, initArrayValStore: Store, initStaticStore: StaticStore) extends GenericInstance:
    val newFrameData: FrameData = 0
    val args: List[Value] = List()

    val joinUnit: MayJoin.NoJoin[Unit] = implicitly
    val jvV: MayJoin.NoJoin[Value] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val failure: ConcreteFailure = new ConcreteFailure
    val frame: ConcreteCallFrame[FrameData, Int, Value] = new ConcreteCallFrame[FrameData, Int, Value](newFrameData, args.view.zipWithIndex.map(_.swap))
    val except: Except[JvmExcept[Value], JvmExcept[Value], MayJoin.NoJoin] = new ConcreteExcept
    val objFieldAlloc: CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
    val objAlloc: CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
    val arrayAlloc: CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
    val arrayValAlloc: CAllocationIntIncrement[AllocationSite] = new CAllocationIntIncrement
    val objFieldStore: CStore[Addr, Value] = new CStore(initStore)
    val arrayValStore: CStore[Addr, Value] = new CStore(initArrayValStore)
    val staticVarStore: CStore[(ObjectType, String), Value] = new CStore(initStaticStore)

    val project: Project[URL] = files
    val projectSource: String = path

    private given Failure = failure

    given objectTypeOps: TypeOps[ObjRep, TypeRep, Bool]  with
      override def instanceOf(v: ObjRep, check: ReferenceType): Boolean =
        if (check == null){
          false
        }
        else{
          v.cls.thisType.isSubtypeOf(check.mostPreciseObjectType)(project.classHierarchy)
        }

    given arrayTypeOps: TypeOps[ArrayRep, TypeRep, Bool] with
      override def instanceOf(v: ArrayRep, check: ReferenceType): Boolean =
        if (check == null){
          false
        }
        else{
          v.arrayType == check.asArrayType
        }
        
    given nullTypeOps: TypeOps[NullVal, TypeRep, Bool] with
      override def instanceOf(v: NullVal, check: ReferenceType): Boolean =
        if (check == null){
          true
        }
        else{
          false
        }

    val bytecodeOps: BytecodeOps[Addr, Idx, Value, TypeRep] = implicitly
    val objectOps: ObjectOps[Addr, Idx, FieldName, OID, Value, ObjType, ObjRep, Value, AllocationSite, Mth, MthName, MthSig, Value, MayJoin.NoJoin] =
      new LiftedObjectOps[Addr, Idx, FieldName, OID, Value, ObjType, ObjRep, Value, AllocationSite, Mth, MthName, MthSig, Value, MayJoin.NoJoin, ObjRep, NullVal](asObj, Value.Obj.apply, asNull, Value.Null.apply)(
        using new ConcreteObjectOps(using objFieldAlloc, objFieldStore)
      )
    val arrayOps: ArrayOps[Addr, AID, Value, Value, ArrayRep, Value, AType, AllocationSite, MayJoin.NoJoin] =
      new LiftedArrayOps[Addr, AID, Value, Value, ArrayRep, Value, AType, AllocationSite, MayJoin.NoJoin, ArrayRep, Int](asArray, Value.Array.apply, asInt32, Value.Int32.apply)(
        using new ConcreteArrayOps(using arrayValAlloc, arrayValStore)
      )

    val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut]
    override val fixpointSuper = fixpoint


