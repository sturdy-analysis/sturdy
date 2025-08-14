package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, Method, MethodDescriptor, ClassType, ReferenceType}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.TrySturdy
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.JoinableDecidableOperandStack
import sturdy.effect.store.AStoreThreaded
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.{Fixpoint, Logger}
import sturdy.language.bytecode.{ConcreteInterpreter, Interpreter}
import sturdy.language.bytecode.abstractions.{AbstractReferenceValue, Addr, AddrSet, ConstantObjects, Exceptions, Numbers, Site, given}
import sturdy.language.bytecode.generic.{BytecodeFailure, BytecodeOps, FixIn, FixOut, given}
import sturdy.values.{Topped, given}
import sturdy.values.booleans.given
import sturdy.values.convert.given
import sturdy.values.floating.given
import sturdy.values.integer.given
import sturdy.values.objects.*
import sturdy.values.ordering.given
import sturdy.values.arrays.{Array, ArrayOps, LiftedArrayOps}
import sturdy.values.references.{finitePowersetAddr, PowersetAddr}

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

  type singleAddr = sturdy.language.bytecode.abstractions.Addr
  type InitialStore = Map[singleAddr, Value]

  class Instance(files: Project[URL], path: String, initStore: InitialStore) extends GenericInstance:

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
    
    override val fixpointSuper: Fixpoint[FixIn, FixOut] = fixpoint
    Fixpoint.DEBUG = false
    
    val joinUnit: WithJoin[Unit] = implicitly
    val jvV: WithJoin[ConstantAnalysis.Value] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext(site => PowersetAddr(Addr.Object(site)))
    override val objFieldAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.FieldInitialization(s, name, cls) => PowersetAddr(Addr.Field(s, name, cls))
      case _ => ??? // TODO
    override val arrayAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext(site => PowersetAddr(Addr.Array(site)))
    override val arrayValAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.ArrayElementInitialization(s, ix) => PowersetAddr(Addr.ArrayElement(s, ix))
      case _ => ??? // TODO
    override val staticAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.StaticInitialization(obj, name) => PowersetAddr(Addr.Static(obj, name))
      case _ => ??? // TODO
    override val store: AStoreThreaded[singleAddr, AddrSet, Value] = new AStoreThreaded(initStore)
    override val frame = new JoinableDecidableCallFrame(0, List())
    override val project: Project[URL] = files
    override val projectSource: String = path

    override val staticAddrMap: mutable.Map[(ClassType, String), Addr] = mutable.Map()

    given Project[URL] = project
    private given Failure = failure
    import ConcreteInterpreter.given

    given constantTypeOps[OID, AID] (using project: Project[URL]): TypeOps[RefValue, TypeRep, Bool] with
      override def instanceOf(v: RefValue, target: TypeRep): Bool =
        if (v.isActual)
          val tmp = v.get
          tmp match
            case tmp: AbstractReferenceValue.maybeNullObject[constantArray, constantObj] =>
              val obj: Object[Addr, ClassFile, Addr, FieldName] = ??? // tmp.obj
              if (target == null)
                Topped.Actual(false)
              else
                Topped.Actual(obj.cls.thisType.isSubtypeOf(target.mostPreciseClassType)(project.classHierarchy))
            case tmp: AbstractReferenceValue.maybeNullArray[constantArray, constantObj] =>
              val array: Array[Addr, Addr, AType, Value] = tmp.array
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

    override val bytecodeOps: BytecodeOps[Topped[FrameData], Value, ReferenceType] = implicitly

    override val objectOps: ObjectOps[(ClassType, String), Addr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, Site, Method, String, MethodDescriptor, ConstantAnalysis.Value, WithJoin] =
      new LiftedObjectOps[(ClassType, String), Addr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, Site, Method, String, MethodDescriptor, ConstantAnalysis.Value, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using objOps(using objFieldAlloc, store, project, failure, effectStack)
      )

    override val arrayOps: ArrayOps[Addr, Value, Value, Value, ArrayType, Site, WithJoin] =
      new LiftedArrayOps[Addr, Value, Value, Value, ArrayType, Site, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new constArrayOps(using arrayValAlloc, store, jvV)
      )
