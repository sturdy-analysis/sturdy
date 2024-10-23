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
import sturdy.language.bytecode.abstractions.{ConstantObjects, Exceptions, IntervalNumbers, IntervalObjects, Numbers}
import sturdy.language.bytecode.generic.{ArrayElemInitSite, BytecodeFailure, BytecodeOps, FieldInitSite, FixIn, FixOut, JvmExcept, given}
import sturdy.values.{Abstractly, Finite, Topped, Widen, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.objects.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.arrays.{Array, ArrayOps, LiftedArrayOps, given}
import sturdy.values.references.{AllocationSiteAddr, given}

import java.net.URL
import scala.collection.mutable

object IntervalAnalysis extends Interpreter, IntervalNumbers, IntervalObjects, Exceptions:
  override type J[A] = WithJoin[A]
  type Mth = Method
  type MthName = String
  type MthSig = MethodDescriptor
  type Idx = I32
  override type ExcV = JvmExceptAbstract[Value]

  var intIntervalBounds: Set[Int] = Set(-1, 0, 1)
  var longIntervalBounds: Set[Long] = Set(-1, 0, 1)
  given Widen[I32] = new NumericIntervalWiden[Int](intIntervalBounds, Int.MinValue, Int.MaxValue)
  given Widen[I64] = new NumericIntervalWiden[Long](longIntervalBounds, Long.MinValue, Long.MaxValue)

  //  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
  //    override def apply(c: ConcreteInterpreter.Value): Value = c match
  //      case ConcreteInterpreter.Value.TopValue => Value.TopValue
  //      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Topped.Actual(i))
  //      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(Topped.Actual(l))
  //      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(Topped.Actual(f))
  //      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(Topped.Actual(d))
  //      //case ConcreteInterpreter.Value.Obj(o) => Value.Obj(Topped.Actual(o))
  //      //case ConcreteInterpreter.Value.Array(a) => Value.Array(Topped.Actual(a))

  type arrayVarStore = Map[ArrayElemAddr, Value]
  type fieldStore = Map[FieldAddr, Value]
  type staticStore = Map[StaticAddr, Value]

  class Instance(files: Project[URL], path: String, initArrayVarStore: arrayVarStore, initFieldStore: fieldStore, initStaticStore: staticStore) extends GenericInstance:

    private given Instance = this

    override val fixpoint: fix.Fixpoint[FixIn, FixOut] =
      fix.notContextSensitive(
        fix.iter.innermost[FixIn, FixOut, Unit](StackedStates())
      ).fixpoint


    override val fixpointSuper = fixpoint

    val joinUnit: WithJoin[Unit] = implicitly
    val jvV: WithJoin[IntervalAnalysis.Value] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objAlloc = new AAllocationFromContext(site => ObjAddr(site))
    override val objFieldAlloc: Allocation[FieldAddr, FieldInitSite] = new AAllocationFromContext(fieldSite => FieldAddr(fieldSite.s, fieldSite.name, fieldSite.cls))
    override val arrayAlloc = new AAllocationFromContext(site => ArrayAddr(site))
    override val arrayValAlloc = new AAllocationFromContext(elemSite => ArrayElemAddr(elemSite.s, elemSite.ix))
    override val staticAlloc = new AAllocationFromContext(site => StaticAddr(site.obj, site.name))
    override val objFieldStore: AStoreSingleAddrThreadded[FieldAddr, Value] = new AStoreSingleAddrThreadded(initFieldStore)
    override val arrayValStore: AStoreSingleAddrThreadded[ArrayElemAddr, Value] = new AStoreSingleAddrThreadded(initArrayVarStore)
    override val staticVarStore: AStoreSingleAddrThreadded[StaticAddr, Value] = new AStoreSingleAddrThreadded(initStaticStore)
    override val frame = new JoinableDecidableCallFrame(0, List())
    override val project: Project[URL] = files
    override val projectSource: String = path

    override val staticAddrMap: mutable.Map[(ObjectType, String), IntervalAnalysis.StaticAddr] = mutable.Map()
    
    given Project[URL] = project
    private given Failure = failure
    import ConcreteInterpreter.given

    given ConvertIntFloat[I32, F32] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertIntFloat[Topped[Int], F32]]).adaptConfig(NilCC && _)

    given ConvertIntDouble[I32, F64] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertIntDouble[Topped[Int], F64]]).adaptConfig(NilCC && _)

    given ConvertLongFloat[I64, F32] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertLongFloat[Topped[Long], F32]]).adaptConfig(NilCC && _)

    given ConvertLongDouble[I64, F64] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertLongDouble[Topped[Long], F64]]).adaptConfig(NilCC && _)

    given ConvertFloatInt[F32, I32] =
      new TransitiveConvert(using summon[ConvertFloatInt[F32, Topped[Int]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)

    given ConvertFloatLong[F32, I64] =
      new TransitiveConvert(using summon[ConvertFloatLong[F32, Topped[Long]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)

    given ConvertDoubleInt[F64, I32] =
      new TransitiveConvert(using summon[ConvertDoubleInt[F64, Topped[Int]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)

    given ConvertDoubleLong[F64, I64] =
      new TransitiveConvert(using summon[ConvertDoubleLong[F64, Topped[Long]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)

    given objectTypeOps: TypeOps[ObjRep, TypeRep, Bool] with
      override def instanceOf(v: ObjRep, target: ReferenceType): Topped[Boolean] =
        if(target == null)
          Topped.Actual(false)
        v match
          case Topped.Top => Topped.Top
          case Topped.Actual(o) =>
            if (o.cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy))
              Topped.Actual(true) // because `null <= o` and `instanceOf(null, target) == false`
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


    override val bytecodeOps: BytecodeOps[Idx, Value, ReferenceType] = implicitly

    override val objectOps: ObjectOps[(ObjectType, String), ObjAddr, IntervalAnalysis.Value, ClassFile, IntervalAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, IntervalAnalysis.Value, WithJoin] =
      new LiftedObjectOps[(ObjectType, String), ObjAddr, IntervalAnalysis.Value, ClassFile, IntervalAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, IntervalAnalysis.Value, WithJoin, ObjRep, NullVal](_.asObj, Value.Obj.apply, _.asNull, Value.Null.apply)(
        using new constObjOps(using objFieldAlloc, objFieldStore, project, failure, effectStack)
      )
    //???
    override val arrayOps: ArrayOps[ArrayAddr, Value, Value, Value, ArrayType, ArrayElemInitSite, WithJoin] =
      new LiftedArrayOps[ArrayAddr, Value, Value, Value, ArrayType, ArrayElemInitSite, WithJoin, ArrayRep, I32](_.asArray, Value.Array.apply, _.asInt32, Value.Int32.apply)(
        using new constArrayOps(using arrayValAlloc, arrayValStore, jvV)
      )
//???


