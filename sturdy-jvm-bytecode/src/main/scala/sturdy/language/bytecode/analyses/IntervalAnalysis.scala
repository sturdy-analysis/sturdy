package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ObjectType, ReferenceType}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.{EffectStack, TrySturdy}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.{DecidableMutableCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.except.{Except, JoinedExcept}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack}
import sturdy.effect.store.{AStoreThreaded, Store, TopStore}
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.context.Sensitivity
import sturdy.fix.{ContextualFixpoint, Fixpoint, Logger}
import sturdy.language.bytecode.ConcreteInterpreter.{Bool, TypeRep}
import sturdy.language.bytecode.{ConcreteInterpreter, Interpreter}
import sturdy.language.bytecode.abstractions.{AbstractReferenceValue, Addr, AddrSet, ConstantObjects, Exceptions, IntervalNumbers, IntervalObjects, Numbers, Site, given}
import sturdy.language.bytecode.generic.{ArrayElemInitSite, BytecodeFailure, BytecodeOps, FieldInitSite, FixIn, FixOut, JvmExcept, given}
import sturdy.values.{Abstractly, Finite, Topped, Widen, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.objects.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.arrays.{Array, ArrayOps, LiftedArrayOps, given}
import sturdy.values.references.{AllocationSiteAddr, PowersetAddr, given}

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

  type arrayVarStore = Map[Addr, Value]
  type fieldStore = Map[Addr, Value]
  type staticStore = Map[Addr, Value]

  class Instance(files: Project[URL], path: String, initArrayVarStore: arrayVarStore, initFieldStore: fieldStore, initStaticStore: staticStore) extends GenericInstance:

    private given Instance = this

    override val fixpoint: fix.Fixpoint[FixIn, FixOut] =
      fix.log(new Logger[FixIn, FixOut] {
        override def enter(dom: FixIn): Unit = ()
          //if (dom.isInstanceOf[FixIn.Eval]) println(s"enter $dom")
        override def exit(dom: FixIn, codom: TrySturdy[FixOut]): Unit = ()
      },
        fix.notContextSensitive(
          fix.filter[FixIn, FixOut](_.isInstanceOf[FixIn.Jump],
            fix.iter.innermost[FixIn, FixOut, Unit](StackedStates())
          )
        )).fixpoint


    override val fixpointSuper = fixpoint
    Fixpoint.DEBUG = false

    val joinUnit: WithJoin[Unit] = implicitly
    val jvV: WithJoin[IntervalAnalysis.Value] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objAlloc: Allocator[AddrSet, Site] = ??? // new AAllocatorFromContext(site => ObjAddr(site))
    override val objFieldAlloc: Allocator[AddrSet, Site] = ??? // new AAllocatorFromContext(fieldSite => FieldAddr(fieldSite.s, fieldSite.name, fieldSite.cls))
    override val arrayAlloc: Allocator[AddrSet, Site] = ??? // new AAllocatorFromContext(site => ArrayAddr(site))
    override val arrayValAlloc: Allocator[AddrSet, Site] = ??? // new AAllocatorFromContext(elemSite => ArrayElemAddr(elemSite.s, elemSite.ix))
    override val staticAlloc: Allocator[AddrSet, Site] = ??? // new AAllocatorFromContext(site => StaticAddr(site.obj, site.name))
    override val objFieldStore: AStoreThreaded[Addr, AddrSet, Value] = new AStoreThreaded(initFieldStore)
    override val arrayValStore: AStoreThreaded[Addr, AddrSet, Value] = new AStoreThreaded(initArrayVarStore)
    override val staticVarStore: AStoreThreaded[Addr, AddrSet, Value] = new AStoreThreaded(initStaticStore)
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

/*    given objectTypeOps: TypeOps[ObjRep, TypeRep, Bool] with
      override def instanceOf(v: ObjRep, target: ReferenceType): Topped[Boolean] =
        if(target == null)
          Topped.Actual(false)
        else{
          v match
            case Topped.Top => Topped.Top
            case Topped.Actual(o) =>
              if (o.cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy))
                Topped.Actual(true) // because `null <= o` and `instanceOf(null, target) == false`
              else
                Topped.Actual(false)
        }
        

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
          Topped.Actual(false)*/
    given intervalTypeOps[OID, AID] (using project: Project[URL]): TypeOps[RefValue, TypeRep, Bool] with
      override def instanceOf(v: RefValue, target: TypeRep): Bool =
        if (v.isActual)
          val tmp = v.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullObject[constantArray, constantObj] =>
              val obj: Object[ObjAddr, ClassFile, FieldAddr, FieldName] = tmp.obj
              if (target == null)
                Topped.Actual(false)
              else
                Topped.Actual(obj.cls.thisType.isSubtypeOf(target.mostPreciseObjectType)(project.classHierarchy))
            case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
              val array: Array[ArrayAddr, ArrayElemAddr, AType, Value] = tmp.array
              if (target == null)
                Topped.Actual(false)
              else
                Topped.Actual(array.arrayType == target.asArrayType)
            case tmp: AbstractReferenceValue.NullValue[constantArray, constantObj] =>
              if (target == null)
                Topped.Actual(true)
              else
                Topped.Actual(false)
        else
          ???

    given intSizeOps: SizeOps[I32, Bool] with
      override def is32Bit(v: I32): Bool = Topped.Actual(true)

    given floatSizeOps: SizeOps[F32, Bool] with
      override def is32Bit(v: F32): Bool = Topped.Actual(true)

    given longSizeOps: SizeOps[I64, Bool] with
      override def is32Bit(v: I64): Bool = Topped.Actual(false)

    given doubleSizeOps: SizeOps[F64, Bool] with
      override def is32Bit(v: F64): Bool = Topped.Actual(false)

    given refSizeOps: SizeOps[RefValue, Bool] with
      override def is32Bit(v: RefValue): Bool = Topped.Actual(true)

    //given objectSizeOps[OID, Addr, FieldName]: SizeOps[Object[OID, ClassFile, Addr, FieldName], Boolean] with
    //  override def is32Bit(v: Object[OID, ClassFile, Addr, FieldName]): Boolean = true

    //given arraySizeOps[AID, Addr, ArrayType]: SizeOps[Array[AID, Addr, ArrayType, Value], Boolean] with
    //  override def is32Bit(v: Array[AID, Addr, ArrayType, Value]): Boolean = true


    override val bytecodeOps: BytecodeOps[Idx, Value, ReferenceType] = implicitly

    override val objectOps: ObjectOps[(ObjectType, String), ObjAddr, IntervalAnalysis.Value, ClassFile, IntervalAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, IntervalAnalysis.Value, WithJoin] =
      new LiftedObjectOps[(ObjectType, String), ObjAddr, IntervalAnalysis.Value, ClassFile, IntervalAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, IntervalAnalysis.Value, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new constObjOps(using objFieldAlloc, objFieldStore, project, failure, effectStack)
      )
    //???
    override val arrayOps: ArrayOps[ArrayAddr, Value, Value, Value, ArrayType, Site, WithJoin] =
      new LiftedArrayOps[ArrayAddr, Value, Value, Value, ArrayType, Site, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new constArrayOps(using arrayValAlloc, arrayValStore, jvV)
      )
//???



