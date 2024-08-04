package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ObjectType, ReferenceType}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.EffectStack
import sturdy.effect.allocation.{AAllocationFromContext, Allocation}
import sturdy.effect.callframe.{DecidableMutableCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.except.{Except, JoinedExcept}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack}
import sturdy.effect.store.{AStoreMultiAddrThreadded, AStoreSingleAddrThreadded, Store, TopStore}
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.context.Sensitivity
import sturdy.fix.{ContextualFixpoint, Fixpoint}
import sturdy.language.bytecode.ConcreteInterpreter.{Bool, NullVal, TypeRep}
import sturdy.language.bytecode.{ConcreteInterpreter, Interpreter}
import sturdy.language.bytecode.abstractions.{ConstantObjects, Exceptions, Numbers}
import sturdy.language.bytecode.generic.{ArrayElemInitSite, BytecodeFailure, BytecodeOps, FieldInitSite, FixIn, FixOut, JvmExcept, given}
import sturdy.values.{Abstractly, Finite, Topped, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.objects.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.arrays.{Array, ArrayOps, LiftedArrayOps, given}
import sturdy.values.references.{AllocationSiteAddr, given}

import java.net.URL

object ConstantAnalysis extends Interpreter, Numbers, ConstantObjects, Exceptions:
  override type J[A] = WithJoin[A]
  type Mth = Method
  type MthName = String
  type MthSig = MethodDescriptor
  type Idx = I32
  override type ExcV = JvmExceptAbstract[Value]


  //  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
//    override def apply(c: ConcreteInterpreter.Value): Value = c match
//      case ConcreteInterpreter.Value.TopValue => Value.TopValue
//      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Topped.Actual(i))
//      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(Topped.Actual(l))
//      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(Topped.Actual(f))
//      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(Topped.Actual(d))
//      //case ConcreteInterpreter.Value.Obj(o) => Value.Obj(Topped.Actual(o))
//      //case ConcreteInterpreter.Value.Array(a) => Value.Array(Topped.Actual(a))

  class Instance(files: Project[URL], path: String) extends GenericInstance:

    private given Instance = this

    override val fixpoint: fix.Fixpoint[FixIn, FixOut] = ???
      //fix.notContextSensitive(fix.iter.innermost(StackedStates())).fixpoint

    override val fixpointSuper = fixpoint

    val joinUnit: WithJoin[Unit] = implicitly
    val jvV: WithJoin[ConstantAnalysis.Value] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objAlloc = new AAllocationFromContext(site => ObjAddr(site))
    override val objFieldAlloc: Allocation[FieldAddr, FieldInitSite] = new AAllocationFromContext(fieldSite => FieldAddr(fieldSite.s, fieldSite.name, fieldSite.cls))
    override val arrayAlloc = new AAllocationFromContext(site => ArrayAddr(site))
    override val arrayValAlloc = new AAllocationFromContext(elemSite => ArrayElemAddr(elemSite.s, elemSite.ix))
    override val objFieldStore = new TopStore()
    override val arrayValStore = new TopStore()
    override val staticVarStore = new TopStore()
    override val frame = new JoinableDecidableCallFrame(0, List())
    override val project: Project[URL] = files
    override val projectSource: String = path

    given Project[URL] = project
    private given Failure = failure
    import ConcreteInterpreter.given

    given objectTypeOps: TypeOps[ObjRep, TypeRep, Bool] with
      override def instanceOf(v: ObjRep, target: ReferenceType): Topped[Boolean] = v match
        case Topped.Top => Topped.Top
        case Topped.Actual(o) =>
          if (o.cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy))
            Topped.Top // because `null <= o` and `instanceOf(null, target) == false`
          else
            Topped.Actual(false)

    given arrayTypeOps: TypeOps[ArrayRep, TypeRep, Bool] with
      override def instanceOf(v: ArrayRep, target: ReferenceType): Topped[Boolean] = v match
        case Topped.Top => Topped.Top
        case Topped.Actual(o) =>
          if (o.arrayType == target.asArrayType)
            Topped.Top // because `null <= o` and `instanceOf(null, target) == false`
          else
            Topped.Actual(false)

    given nullTypeOps: TypeOps[NullVal, TypeRep, Bool] with
      override def instanceOf(v: NullVal, target: ReferenceType): Topped[Boolean] =
        if (target == null)
          Topped.Actual(true)
        else
          Topped.Actual(false)

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

    given arraySizeOps[AID, Addr, ArrayType]: SizeOps[Array[AID, Addr, ArrayType, Value], Boolean] with
      override def is32Bit(v: Array[AID, Addr, ArrayType, Value]): Boolean = true


    override val bytecodeOps: BytecodeOps[Topped[FrameData], Value, ReferenceType] = implicitly

    override val objectOps: ObjectOps[(ObjectType, String), ObjAddr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, ConstantAnalysis.Value, WithJoin] =
      new LiftedObjectOps[(ObjectType, String), ObjAddr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, ConstantAnalysis.Value, WithJoin, ObjRep, NullVal](_.asObj, Value.Obj.apply, _.asNull, Value.Null.apply)(
        using new constObjOps(using objFieldAlloc, objFieldStore, project, failure, effectStack)
      )
      //???
    override val arrayOps: ArrayOps[ArrayAddr, Value, Value, Value, ArrayType, ArrayElemInitSite, WithJoin] =
      new LiftedArrayOps[ArrayAddr, Value, Value, Value, ArrayType, ArrayElemInitSite, WithJoin, ArrayRep, I32](_.asArray, Value.Array.apply, _.asInt32, Value.Int32.apply)(
        using new constArrayOps(using arrayValAlloc, arrayValStore, jvV)
      )
      //???


