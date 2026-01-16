package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, BooleanType, ByteType, CharType, ClassFile, ClassHierarchy, ClassType, DoubleType, FloatType, IntegerType, LongType, Method, ReferenceType, ShortType, Type}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.{EffectStack, TrySturdy}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.{Except, JoinedExcept}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.JoinableDecidableOperandStack
import sturdy.effect.store.{AStoreThreaded, Store}
import sturdy.effect.symboltable.JoinableDecidableSymbolTable
import sturdy.{fix, values}
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.{Fixpoint, Logger}
import sturdy.language.bytecode.{Interpreter, abstractions}
import sturdy.language.bytecode.abstractions.{Addr, AddrSet, ArrayOpContext, Exceptions, FieldAccessContext, FieldIdent, InvokeContext, Numbers, Site, StaticMethodDeclaration, given}
import sturdy.language.bytecode.generic.{BytecodeFailure, BytecodeOps, FixIn, FixOut, JvmExcept, given}
import sturdy.language.bytecode.util.given
import sturdy.values.{Combine, MaybeChanged, Structural, Topped, Widening, given}
import sturdy.values.booleans.given
import sturdy.values.convert.given
import sturdy.values.floating.given
import sturdy.values.integer.given
import sturdy.values.objects.*
import sturdy.values.ordering.given
import sturdy.values.arrays.{Array, ArrayOps, LiftedArrayOps}
import sturdy.values.references.{PowersetAddr, given}

import java.net.URL

enum AbstractReferenceValue[A, O]:
  case maybeNullObject(obj: O, maybeNull: Boolean)
  case maybeNullArray(array: A, maybeNull: Boolean)
  case NullValue()

object ConstantAnalysis extends Interpreter, Numbers, Exceptions:
  override type J[A] = WithJoin[A]
  override type Mth = Method
  override type StaticMth = StaticMethodDeclaration
  private type Exc = JvmExcept[Value]
  override type ExcV = JvmExceptAbstract[Value]

  override type Addr = AddrSet

  override type ObjType = ClassFile
  override type FieldName = FieldIdent

  type Obj = Object[Addr, ObjType, Addr, FieldName]
  type Arr = Array[Addr, Addr, AType, I32]
  override type RefValue = Topped[AbstractReferenceValue[Arr, Obj]]
  override type TypeRep = ReferenceType
  override type AType = ArrayType

  override final def topRef: RefValue = Topped.Top

  override implicit val except: Except[Exc, ExcV, J] = JoinedExcept[Exc, ExcV]()

  given combineRef[W <: Widening]: Combine[RefValue, W] with
    override def apply(v1: RefValue, v2: RefValue): MaybeChanged[RefValue] = (v1, v2) match
      case (Topped.Actual(v1), Topped.Actual(v2)) => (v1, v2) match
        case (AbstractReferenceValue.maybeNullObject(obj, _), AbstractReferenceValue.NullValue()) =>
          MaybeChanged.Changed(Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, true)))
        case (AbstractReferenceValue.NullValue(), AbstractReferenceValue.maybeNullObject(obj, _)) =>
          MaybeChanged.Changed(Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, true)))
        case (AbstractReferenceValue.maybeNullObject(_, _), AbstractReferenceValue.maybeNullObject(_, _)) =>
          MaybeChanged.Changed(topRef)
        case (AbstractReferenceValue.maybeNullArray(_, _), AbstractReferenceValue.maybeNullArray(_, _)) =>
          MaybeChanged.Changed(topRef)
        case (AbstractReferenceValue.maybeNullArray(array, _), AbstractReferenceValue.NullValue()) =>
          MaybeChanged.Changed(Topped.Actual(AbstractReferenceValue.maybeNullArray(array, true)))
        case (AbstractReferenceValue.NullValue(), AbstractReferenceValue.maybeNullArray(array, _)) =>
          MaybeChanged.Changed(Topped.Actual(AbstractReferenceValue.maybeNullArray(array, true)))
        case (AbstractReferenceValue.NullValue(), AbstractReferenceValue.NullValue()) =>
          MaybeChanged.Changed(topRef)
        case _ => ???
      case _ => MaybeChanged.Changed(topRef)

  given structuralRef[A, O]: Structural[AbstractReferenceValue[A, O]] with {}

  given objOps(using alloc: Allocator[Addr, Site], store: Store[Addr, Value, WithJoin], project: Project[URL], f: Failure, eff: EffectStack): ObjectOps[FieldName, Addr, Value, ClassFile, RefValue, Site, Method, StaticMth, I32, InvokeContext, FieldAccessContext, WithJoin] =
    new ObjectOps[FieldName, Addr, Value, ClassFile, RefValue, Site, Method, StaticMth, I32, InvokeContext, FieldAccessContext, WithJoin]:
      given hierachy: ClassHierarchy = project.classHierarchy

      override def makeObject(oid: Addr, c: ClassFile, fields: Seq[(Value, Site, FieldName)]): RefValue =
        val fieldAddrs = fields.map: (v, site, name) =>
          val addr = alloc(site)
          store.write(addr, v)
          (name, addr)
        .toMap
        Topped.Actual(AbstractReferenceValue.maybeNullObject(Object(oid, c, fieldAddrs), false))

      override def getField(context: FieldAccessContext)(ref: RefValue, identifier: FieldName): Value = ref match
        // TODO: fix
        case Topped.Top => ??? // getFieldNonActual
        case Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, _)) => ???
        // store.read(obj.fields.getOrElse(name, failure.fail(BytecodeFailure.FieldNotFound, s"field $name not found"))).getOrElse(failure.fail(BytecodeFailure.UnboundField, s"$name not bound"))
        case Topped.Actual(AbstractReferenceValue.NullValue()) => throw NullPointerException()
        case Topped.Actual(_) => ???

      override def setField(context: FieldAccessContext)(ref: RefValue, identifier: FieldName, v: Value): JOption[WithJoin, Unit] = ref match
        case Topped.Top => JOptionA.some(Value.TopValue)
        case Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, _)) =>
          if (!obj.fields.contains(identifier))
            JOptionA.none
          else
            store.write(obj.fields(identifier), v)
            JOptionA.some(())
        case Topped.Actual(_) => ???

      override def invokeMethod(context: InvokeContext)(staticMethodDeclaration: StaticMth, ref: RefValue, args: Seq[Value])(invoke: (RefValue, Method, Seq[Value]) => Value): Value = ref match
        case Topped.Top => mkTopVal(staticMethodDeclaration.descriptor.returnType)
        case Topped.Actual(AbstractReferenceValue.maybeNullObject(obj, _)) =>
          // TODO: test, add errors/exceptions
          // val resolvedMethod = resolveMethod(context._3.thisType, staticClass.thisType, mthName, sig)
          // val selectedMethod = selectMethod(obj.cls.thisType, resolvedMethod)
          invoke(ref, /* selectedMethod */ ???, args)
        case Topped.Actual(_) => ???

      override def makeNull(): RefValue = Topped.Actual(AbstractReferenceValue.NullValue())

      override def isNull(ref: RefValue): I32 = ref match
        case Topped.Top => topI32
        case Topped.Actual(AbstractReferenceValue.maybeNullObject(_, false)) => Topped.Actual(0)
        case Topped.Actual(AbstractReferenceValue.NullValue()) => Topped.Actual(1)
        case Topped.Actual(_) => ???

  given arrayOps(using alloc: Allocator[Addr, Site], store: Store[Addr, Value, WithJoin], jvV: WithJoin[Value]): ArrayOps[Addr, I32, Value, RefValue, ArrayType, ArrayOpContext, WithJoin] with
    override def makeArray(ctx: ArrayOpContext)(aid: Addr, defaultValue: => Value, arrayType: AType, arraySize: I32): RefValue = arraySize match
      case Topped.Actual(size) =>
        val values = Range.Int(0, size, 1).map: index =>
          val addr = alloc(ctx)
          store.write(addr, defaultValue)
          addr
        Topped.Actual(AbstractReferenceValue.maybeNullArray(Array(aid, values, arrayType, arraySize), false))
      case Topped.Top => ??? // TODO

    override def get(ctx: ArrayOpContext)(ref: RefValue, idx: I32): JOption[WithJoin, Value] = (ref, idx) match
      case (Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)), Topped.Actual(idx)) =>
        if (idx >= array.vals.size)
          JOptionA.none
        else
          store.read(array.vals(idx))
      case (Topped.Actual(_), Topped.Actual(_)) => ???
      case _ => JOptionA.some(Value.TopValue)

    override def set(ctx: ArrayOpContext)(ref: RefValue, idx: I32, v: Value): JOption[WithJoin, Unit] =
      (ref, idx) match
        case (Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)), Topped.Actual(idx)) =>
          if (idx >= array.vals.size)
            JOptionA.none
          else
            store.write(array.vals(idx), v)
            JOptionA.some(())
        case (Topped.Actual(_), Topped.Actual(_)) => ???
        case _ => JOptionA.none

    override def length(ctx: ArrayOpContext)(ref: RefValue): Value = ref match
      case Topped.Top => Value.Int32(topI32)
      case Topped.Actual(AbstractReferenceValue.maybeNullArray(array, _)) => Value.Int32(array.arraySize)
      case Topped.Actual(_) => ???

  private type singleAddr = abstractions.Addr
  type InitialStore = Map[singleAddr, Value]

  class Instance(files: Project[URL], path: String, initStore: InitialStore) extends GenericInstance:
    override val fixpoint: fix.Fixpoint[FixIn, FixOut] =
      fix.log(new Logger[FixIn, FixOut]:
        override def enter(dom: FixIn): Unit = ()

        override def exit(dom: FixIn, codom: TrySturdy[FixOut]): Unit = ()

        ,
        fix.notContextSensitive(
          fix.filter[FixIn, FixOut](_.isInstanceOf[FixIn.Jump],
            fix.iter.innermost[FixIn, FixOut, Unit](StackedStates())
          )
        )
      ).fixpoint

    Fixpoint.DEBUG = false

    override val joinUnit: WithJoin[Unit] = implicitly
    override val jvV: WithJoin[ConstantAnalysis.Value] = implicitly
    override val joinAddr: WithJoin[Addr] = implicitly

    override val stack = new JoinableDecidableOperandStack
    override val failure = new CollectedFailures[BytecodeFailure]
    override val except = new JoinedExcept()
    override val objAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext(site => PowersetAddr(Addr.Object(site)))
    override val objFieldAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.FieldInitialization(s, ident) => PowersetAddr(Addr.Field(s, ident))
      case _ => ??? // TODO
    override val arrayAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext(site => PowersetAddr(Addr.Array(site)))
    override val arrayValAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.ArrayElementInitialization(s, ix) => PowersetAddr(Addr.ArrayElement(s, ix))
      case _ => ??? // TODO
    override val staticAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.StaticInitialization(ident) => PowersetAddr(Addr.Static(ident))
      case _ => ??? // TODO
    override val store: AStoreThreaded[singleAddr, AddrSet, Value] = new AStoreThreaded(initStore)
    override val frame = JoinableDecidableCallFrame(0, List())
    override val project: Project[URL] = files

    override val classInitializationState: JoinableDecidableSymbolTable[Unit, ClassType, InitializationResult] = JoinableDecidableSymbolTable[Unit, ClassType, InitializationResult]()
    override val staticFieldTable: JoinableDecidableSymbolTable[Unit, FieldName, AddrSet] = JoinableDecidableSymbolTable[Unit, FieldName, AddrSet]()

    given Project[URL] = project

    private given Failure = failure

    given constantTypeOps[OID, AID](using project: Project[URL]): TypeOps[RefValue, TypeRep] with
      override def typeOf(v: RefValue): ReferenceType =
        if v.isActual then
          val refVal = v.get
          refVal match
            case AbstractReferenceValue.maybeNullObject(obj, _) => obj.cls.thisType
            case AbstractReferenceValue.maybeNullArray(array, _) => array.arrayType
            case AbstractReferenceValue.NullValue() => ??? // TODO
        else
          ??? // TODO

      override def ifInstanceOf[A](v: RefValue, ty: ReferenceType)(ifTrue: => A)(ifFalse: => A): A =
        ??? // TODO

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

    override val bytecodeOps: BytecodeOps[Value, ReferenceType] = implicitly

    override val objectOps: ObjectOps[FieldName, Addr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, Site, Method, StaticMth, ConstantAnalysis.Value, InvokeContext, FieldAccessContext, WithJoin] =
      new LiftedObjectOps[FieldName, Addr, ConstantAnalysis.Value, ClassFile, ConstantAnalysis.Value, Site, Method, StaticMth, ConstantAnalysis.Value, InvokeContext, FieldAccessContext, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using objOps(using objFieldAlloc, store, project, failure, effectStack)
      )

    override val arrayOps: ArrayOps[Addr, Value, Value, Value, ArrayType, ArrayOpContext, WithJoin] =
      new LiftedArrayOps[Addr, Value, Value, Value, ArrayType, ArrayOpContext, WithJoin, RefValue, I32](_.asRef, Value.ReferenceValue.apply, _.asInt32, Value.Int32.apply)(
        using new arrayOps(using arrayValAlloc, store, jvV)
      )

    override def exceptionHandler(mth: Method)(using Fixed): Exc => Unit =
      ??? // TODO

  private def mkTopVal(ty: Type): Value = ty match
    case ByteType => Value.Int32(topI32)
    case ShortType => Value.Int32(topI32)
    case IntegerType => Value.Int32(topI32)
    case FloatType => Value.Float32(topF32)
    case LongType => Value.Int64(topI64)
    case DoubleType => Value.Float64(topF64)
    case BooleanType => Value.Int32(topI32)
    case CharType => Value.Int32(topI32)
    case _: ClassType => Value.ReferenceValue(topRef)
    case _: ArrayType => Value.ReferenceValue(topRef)
    case _ => ??? // TODO: not implemented
