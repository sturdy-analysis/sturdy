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
import sturdy.language.bytecode.abstractions.{AbstractReferenceValue, ConstantObjects, Exceptions, Numbers}
import sturdy.language.bytecode.generic.{ArrayElemInitSite, BytecodeFailure, BytecodeOps, FieldInitSite, FixIn, FixOut, JvmExcept, given}
import sturdy.util.{Lazy, lazily}
import sturdy.values.{Abstractly, Combine, Finite, MaybeChanged, Topped, Widen, Widening, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.objects.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.arrays.{Array, ArrayOps, LiftedArrayOps, given}
import sturdy.values.references.{AllocationSiteAddr, given}

import java.net.URL
import scala.collection.mutable

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

  type arrayVarStore = Map[ArrayElemAddr, Value]
  type fieldStore = Map[FieldAddr, Value]
  type staticStore = Map[StaticAddr, Value]

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
    val jvV: WithJoin[ConstantAnalysis.Value] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objAlloc = new AAllocatorFromContext(site => ObjAddr(site))
    override val objFieldAlloc: Allocator[FieldAddr, FieldInitSite] = new AAllocatorFromContext(fieldSite => FieldAddr(fieldSite.s, fieldSite.name, fieldSite.cls))
    override val arrayAlloc = new AAllocatorFromContext(site => ArrayAddr(site))
    override val arrayValAlloc = new AAllocatorFromContext(elemSite => ArrayElemAddr(elemSite.s, elemSite.ix))
    override val staticAlloc = new AAllocatorFromContext(site => StaticAddr(site.obj, site.name))
    override val objFieldStore: AStoreThreaded[FieldAddr, Value] = new AStoreThreaded(initFieldStore)
    override val arrayValStore: AStoreThreaded[ArrayElemAddr, Value] = new AStoreThreaded(initArrayVarStore)
    override val staticVarStore: AStoreThreaded[StaticAddr, Value] = new AStoreThreaded(initStaticStore)
    override val frame = new JoinableDecidableCallFrame(0, List())
    override val project: Project[URL] = files
    override val projectSource: String = path

    override val staticAddrMap: mutable.Map[(ObjectType, String), StaticAddr] = mutable.Map()

    given Project[URL] = project
    private given Failure = failure
    import ConcreteInterpreter.given

/*
    given objectTypeOps: TypeOps[ObjRep, TypeRep, Bool] with
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
          Topped.Actual(false)
*/
    given constantTypeOps[OID, AID] (using project: Project[URL]): TypeOps[RefValue, TypeRep, Bool] with
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

    /*given objectSizeOps[OID, Addr, FieldName]: SizeOps[Object[OID, ClassFile, Addr, FieldName], Boolean] with
      override def is32Bit(v: Object[OID, ClassFile, Addr, FieldName]): Boolean = true

    given arraySizeOps[AID, Addr, ArrayType]: SizeOps[Array[AID, Addr, ArrayType, Value], Boolean] with
      override def is32Bit(v: Array[AID, Addr, ArrayType, Value]): Boolean = true*/

    given refSizeOps: SizeOps[RefValue, Bool] with
      override def is32Bit(v: RefValue): Bool = Topped.Actual(true)


    override val bytecodeOps: BytecodeOps[Topped[FrameData], Value, ReferenceType] = implicitly

    override val objectOps: ObjectOps[(ObjectType, String), ObjAddr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, ConstantAnalysis.Value, WithJoin] =
      new LiftedObjectOps[(ObjectType, String), ObjAddr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, FieldInitSite, Method, String, MethodDescriptor, ConstantAnalysis.Value, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new constObjOps(using objFieldAlloc, objFieldStore, project, failure, effectStack)
      )
      //???
    override val arrayOps: ArrayOps[ArrayAddr, Value, Value, Value, ArrayType, ArrayElemInitSite, WithJoin] =
      new LiftedArrayOps[ArrayAddr, Value, Value, Value, ArrayType, ArrayElemInitSite, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new constArrayOps(using arrayValAlloc, arrayValStore, jvV)
      )
      //???

