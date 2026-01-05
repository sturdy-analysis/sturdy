package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, ClassType, Method, MethodDescriptor, ReferenceType}
import sturdy.data.{WithJoin, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.{DecidableMutableCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.except.{Except, JoinedExcept}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack}
import sturdy.effect.store.{AStoreThreaded, Store}
import sturdy.effect.symboltable.{DecidableSymbolTable, JoinableDecidableSymbolTable}
import sturdy.fix.Fixpoint
import sturdy.language.bytecode.Interpreter
import sturdy.language.bytecode.abstractions
import sturdy.language.bytecode.abstractions.{Addr, AddrSet, ArrayOpContext, FieldAccessContext, FieldIdent, InvokeContext, Site, StaticMethodDeclaration, given}
import sturdy.language.bytecode.analyses.ConstantAnalysis.JvmExceptAbstract
import sturdy.language.bytecode.generic.{BytecodeFailure, BytecodeOps, FixIn, FixOut, JvmExcept, given}
import sturdy.language.bytecode.util.given
import sturdy.values.arrays.ArrayOps
import sturdy.values.{Combine, MaybeChanged, Powerset, Topped, Widening, finitely}
import sturdy.values.objects.ObjectOps
import sturdy.values.references.{PowersetAddr, given}

import java.net.URL

case class ObjectValue(addr: Addr, ty: ClassType, fields: Map[FieldIdent, CFAAnalysis.Addr])

case class ArrayValue(addr: Addr, ty: ArrayType, values: Seq[CFAAnalysis.Addr], length: CFAAnalysis.I32)

enum NullState:
  case Null, NotNull, PotentiallyNull

given CombineNullState[W <: Widening]: Combine[NullState, W] with
  override def apply(v1: NullState, v2: NullState): MaybeChanged[NullState] = (v1, v2) match
    case (v1, v2) if v1 == v2 => MaybeChanged.Unchanged(v1)
    case (NullState.PotentiallyNull, _) => MaybeChanged.Unchanged(NullState.PotentiallyNull)
    case (NullState.NotNull, _) => MaybeChanged.Changed(NullState.PotentiallyNull)
    case (NullState.Null, _) => MaybeChanged.Changed(NullState.PotentiallyNull)

case class PointsToRefValue(objects: Powerset[ObjectValue], arrays: Powerset[ArrayValue], nullState: NullState)

// attempted 1-cfa points-to analysis implementation
object CFAAnalysis extends Interpreter:
  override type J[A] = WithJoin[A]

  // primitive types (currently unit)
  override type I8 = Unit
  override type I16 = Unit
  override type U16 = Unit
  override type I32 = Unit
  override type I64 = Unit
  override type F32 = Unit
  override type F64 = Unit
  override type Bool = Unit

  // representations
  override type Mth = Method
  override type StaticMth = StaticMethodDeclaration
  override type FieldName = FieldIdent
  // TODO: where exactly are these used?
  override type TypeRep = ReferenceType
  override type ObjType = ClassType
  override type AType = ArrayType

  override type Addr = AddrSet
  // TODO: does topped make sense here?
  // currently topped.top is never introduced, but topRef should probably refer to something?
  override type RefValue = Topped[PointsToRefValue]

  private type Exc = JvmExcept[Value]
  override type ExcV = JvmExceptAbstract[Value]
  override implicit val except: Except[Exc, ExcV, J] = JoinedExcept[Exc, ExcV]()

  override def topI32: I32 = ()

  override def topI64: I64 = ()

  override def topF32: F32 = ()

  override def topF64: F64 = ()

  override def topRef: RefValue = Topped.Top

  override def asBoolean(v: Value)(using Failure): Bool = ()

  override def boolean(b: Bool): Value = Value.Int32(b)

  given CombineRefValue[W <: Widening]: Combine[RefValue, W] with
    override def apply(v1: RefValue, v2: RefValue): MaybeChanged[RefValue] = (v1, v2) match
      case (Topped.Top, _) => MaybeChanged.Unchanged(Topped.Top)
      case (_, Topped.Top) => MaybeChanged.Changed(Topped.Top)
      case (Topped.Actual(v1), Topped.Actual(v2)) => for {
        objects <- MaybeChanged(v1.objects ++ v2.objects, v1.objects)
        arrays <- MaybeChanged(v1.arrays ++ v2.arrays, v1.arrays)
        nullState <- CombineNullState[W](v1.nullState, v1.nullState)
      } yield Topped.Actual(PointsToRefValue(objects, arrays, nullState))

  class Instance(implicit val project: Project[URL]) extends GenericInstance:
    override val fixpoint: Fixpoint[FixIn, FixOut] = ???

    override val bytecodeOps: BytecodeOps[Value, ReferenceType] = ???
    override val objectOps: ObjectOps[FieldIdent, AddrSet, Value, ClassFile, Value, Site, Method, StaticMth, Value, InvokeContext, FieldAccessContext, J] = ???
    override val arrayOps: ArrayOps[AddrSet, Value, Value, Value, ArrayType, Site, ArrayOpContext, J] = ???

    override val joinUnit: WithJoin[Unit] = implicitly
    override val jvV: WithJoin[Value] = implicitly
    override val joinAddr: WithJoin[AddrSet] = implicitly
    override val failure: Failure = CollectedFailures[BytecodeFailure]
    override val except: Except[Exc, ExcV, J] = implicitly
    override val stack: DecidableOperandStack[Value] = JoinableDecidableOperandStack[Value]()

    override val objAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext(site => PowersetAddr(Addr.Object(site)))
    override val objFieldAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.FieldInitialization(s, ident) => PowersetAddr(Addr.Field(s, ident))
      case site => failure.fail(BytecodeFailure.IncorrectSiteVariant(site), "expected Site.FieldInitialization")
    override val arrayAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext(site => PowersetAddr(Addr.Array(site)))
    override val arrayValAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.ArrayElementInitialization(s, ix) => PowersetAddr(Addr.ArrayElement(s, ix))
      case site => failure.fail(BytecodeFailure.IncorrectSiteVariant(site), "expected Site.ArrayElementInitialization")
    override val staticAlloc: Allocator[AddrSet, Site] = AAllocatorFromContext:
      case Site.StaticInitialization(ident) => PowersetAddr(Addr.Static(ident))
      case site => failure.fail(BytecodeFailure.IncorrectSiteVariant(site), "expected Site.StaticInitialization")
    override val store: Store[AddrSet, Value, J] = AStoreThreaded[abstractions.Addr, Addr, Value](Map())
    override val frame: DecidableMutableCallFrame[FrameData, FrameData, Value, Site] = JoinableDecidableCallFrame(0, List())
    override val classInitializationState: DecidableSymbolTable[Unit, ClassType, InitializationResult] = JoinableDecidableSymbolTable[Unit, ClassType, InitializationResult]()
    override val staticFieldTable: DecidableSymbolTable[Unit, FieldIdent, AddrSet] = JoinableDecidableSymbolTable[Unit, FieldName, AddrSet]()

    override def exceptionHandler(mth: Method)(using Fixed): Exc => Unit =
      case JvmExcept.Jump(targetPC) =>
        enterMethod(targetPC, mth)
      case JvmExcept.Ret(_) =>
        throw UnsupportedOperationException("ret exceptions are not handled") // TODO
      case JvmExcept.Return(_) =>
        ()
      case JvmExcept.ThrowObject(exception) =>
        ??? // TODO
