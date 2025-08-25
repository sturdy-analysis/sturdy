package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.TopMemory
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{FiniteSymbolTableWithDrop, JoinableDecidableSymbolTable, SizedSymbolTable, SizedUpperBoundSymbolTable, SymbolTableWithDrop, UpperBoundSymbolTable}
import sturdy.fix
import sturdy.fix.Combinator
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.floating.FloatOps
import swam.syntax.*
import swam.{FuncType, ReferenceType}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.simd.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.types.{*, given}
import sturdy.values.{*, given}
import sturdy.values.references.{ReferenceOps, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.control.{ControlObservable, RecordingControlObserver}
import sturdy.language.wasm.analyses.TypeAnalysis.typedTop
import sturdy.values.addresses.AddressOffset

object TypeAnalysis extends Interpreter, TypeValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = BaseType[Seq[Byte]]
  type Size = I32
  type Index = I32

  given TypeSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, WithJoin] with
    override def valToAddr(v: Value): Addr = v.asInt32
    override def valToIdx(v: Value): Index = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Num(NumValue.Int32(sz))

    // TODO: implement this for the TypeAnalysis
    override def valToRef(v: TypeAnalysis.Value, funcs: Vector[FunctionInstance]): RefV = ???
    override def refToVal(r: RefV): TypeAnalysis.Value = ???
    override def liftBytes(b: Seq[Byte]): BaseType[Seq[Byte]] = ???
    override def funcInstToRefV(f: FunctionInstance): RefV = ???
    override def isNullRef(r: Value): TypeAnalysis.Value = ???

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      if (vec.isEmpty)
        JOptionPowerset.None()
      else
        JOptionPowerset.NoneSome(Powerset(vec.toSet))

  given TypeAddressOffset(using f: Failure, effectStack: EffectStack): AddressOffset[Addr] with
    override def addOffsetToAddr(offset: Int, addr: Addr): Addr =
      effectStack.joinWithFailure {
        addr
      } {
        f.fail(MemoryAccessOutOfBounds, s"$addr + $offset")
      }

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]
//    , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))
    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[Value]] = new fix.ContextualFixpoint {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
    }

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvBytes: WithJoin[Bytes] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    override def jvRefV: WithJoin[RefV] = implicitly
    override def jvElem: WithJoin[Elem] = implicitly

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: TopMemory[MemoryAddr, Addr, Bytes, Size] = new TopMemory
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val elems: SymbolTableWithDrop[Unit, ElemAddr, Elem, J] = FiniteSymbolTableWithDrop[Unit, ElemAddr, Elem](Seq.empty)(using CombineEquiSeq, CombineEquiSeq, implicitly, implicitly)
    val tables: SizedUpperBoundSymbolTable[TableAddr, Index, RefV] = new SizedUpperBoundSymbolTable(Powerset())
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value, InstLoc] = new JoinableDecidableCallFrame(FrameData.empty, Iterable.empty)
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, WithJoin] = implicitly

    override def invokeHostFunction(hostFunc: HostFunction, args: List[TypeAnalysis.Value]): List[TypeAnalysis.Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        failure.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        effectStack.joinWithFailure(result)(failure.fail(FileError, s"in ${hostFunc.name}"))

    override def toString: String = s"type $config"
