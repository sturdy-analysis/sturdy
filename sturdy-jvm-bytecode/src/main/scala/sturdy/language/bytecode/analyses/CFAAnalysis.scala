package sturdy.language.bytecode.analyses

import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, ClassFile, ClassType, Method, MethodDescriptor, ReferenceType}
import sturdy.data.{WithJoin, given}
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.{DecidableMutableCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.except.{Except, JoinedExcept}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.operandstack.{DecidableOperandStack, JoinableDecidableOperandStack}
import sturdy.effect.store.Store
import sturdy.effect.symboltable.{DecidableSymbolTable, JoinableDecidableSymbolTable}
import sturdy.fix.Fixpoint
import sturdy.language.bytecode.Interpreter
import sturdy.language.bytecode.abstractions.{AddrSet, FieldIdent, InvokeType, Site, given}
import sturdy.language.bytecode.generic.{BytecodeFailure, BytecodeOps, FixIn, FixOut, JvmExcept, given_Finite_BytecodeFailure, given}
import sturdy.values.arrays.ArrayOps
import sturdy.values.{Combine, MaybeChanged, Powerset, Topped, Widen, Widening, finitely, given}
import sturdy.values.exceptions.PowersetExceptional
import sturdy.values.objects.ObjectOps
import sturdy.values.references.given

import java.net.URL

// TODO: proper implementation
case class PointsToRefValue()

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
  override type MthName = String
  override type MthSig = MethodDescriptor
  override type FieldName = FieldIdent
  // TODO: where exactly are these used?
  override type TypeRep = ReferenceType
  override type ObjType = ClassType
  override type AType = ArrayType

  override type Addr = AddrSet
  override type RefValue = Topped[PointsToRefValue]

  private type Exc = JvmExcept[Value]
  override type ExcV = Powerset[Exc]
  override implicit val except: Except[Exc, ExcV, J] = JoinedExcept[Exc, ExcV]()

  given Widen[ExcV] with
    override def apply(v1: ExcV, v2: ExcV): MaybeChanged[ExcV] =
      MaybeChanged(v1 ++ v2, v1)

  override def topI32: I32 = ()

  override def topI64: I64 = ()

  override def topF32: F32 = ()

  override def topF64: F64 = ()

  override def topRef: RefValue = Topped.Top

  override def asBoolean(v: Value)(using Failure): Bool = ()

  override def boolean(b: Bool): Value = Value.Int32(b)

  given CombineRefValue[W <: Widening]: Combine[RefValue, W] with
    override def apply(v1: RefValue, v2: RefValue): MaybeChanged[RefValue] =
      ???

  class Instance(implicit val project: Project[URL]) extends GenericInstance:
    override val fixpoint: Fixpoint[FixIn, FixOut] = ???
    override val fixpointSuper: Fixpoint[FixIn, FixOut] = fixpoint

    override val bytecodeOps: BytecodeOps[CFAAnalysis.Value, ReferenceType] = ???
    override val objectOps: ObjectOps[FieldIdent, AddrSet, CFAAnalysis.Value, ClassFile, CFAAnalysis.Value, Site, Method, String, MethodDescriptor, CFAAnalysis.Value, (InvokeType, ClassFile), J] = ???
    override val arrayOps: ArrayOps[AddrSet, CFAAnalysis.Value, CFAAnalysis.Value, CFAAnalysis.Value, ArrayType, Site, J] = ???

    override val joinUnit: WithJoin[Unit] = implicitly
    override val jvV: WithJoin[Value] = implicitly
    override val joinAddr: WithJoin[AddrSet] = implicitly
    override val failure: Failure = CollectedFailures[BytecodeFailure]
    override val except: Except[Exc, ExcV, J] = implicitly
    override val stack: DecidableOperandStack[Value] = JoinableDecidableOperandStack[Value]()

    override val objAlloc: Allocator[AddrSet, Site] = ???
    override val objFieldAlloc: Allocator[AddrSet, Site] = ???
    override val arrayAlloc: Allocator[AddrSet, Site] = ???
    override val arrayValAlloc: Allocator[AddrSet, Site] = ???
    override val staticAlloc: Allocator[AddrSet, Site] = ???

    override val store: Store[AddrSet, CFAAnalysis.Value, J] = ???
    override val frame: DecidableMutableCallFrame[FrameData, FrameData, CFAAnalysis.Value, Site] = JoinableDecidableCallFrame(0, List())
    override val classInitializationState: DecidableSymbolTable[Unit, ClassType, InitializationResult] = JoinableDecidableSymbolTable[Unit, ClassType, InitializationResult]()
    override val staticFieldTable: DecidableSymbolTable[Unit, FieldIdent, AddrSet] = JoinableDecidableSymbolTable[Unit, FieldName, AddrSet]()
