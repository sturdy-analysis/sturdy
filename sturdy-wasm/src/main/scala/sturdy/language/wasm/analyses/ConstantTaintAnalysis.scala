package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.symboltable.{FiniteSymbolTableWithDrop, JoinableDecidableSymbolTable, SizedConstantTable, SizedSymbolTable, SymbolTableWithDrop, given}
import sturdy.effect.symboltable.SizedConstantTable.CombineTable
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.analyses.ConstantAnalysis.{given}
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
import sturdy.values.references.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.simd.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.taint.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.control.{ControlObservable, RecordingControlObserver}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.operandstack.JoinableDecidableOperandStack
import sturdy.values.addresses.AddressOffset
import sturdy.values.references.ReferenceOps

object ConstantTaintAnalysis extends Interpreter, ConstantTaintValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = Topped[Int]
  type AByte = TaintProduct[Topped[Byte]]
  type Bytes = Seq[AByte]
  type Size = Topped[Int]
  type Index = Topped[Int]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, WithJoin] with
    override def valToAddr(v: Value): Addr = v.asInt32.value
    override def valToIdx(v: Value): Index = v.asInt32.value
    override def valToSize(v: Value): Size = v.asInt32.value
    override def sizeToVal(sz: Size): Value = Value.Num(NumValue.Int32(untainted(sz)))
    // TODO: implement this for the ConstantTaintAnalysis
    override def funcInstToRefV(f: FunctionInstance): RefV = ???
    override def valToRef(v: Value, funcs: Vector[FunctionInstance]): RefV = ???
    override def refToVal(r: RefV): Value = ???
    override def liftBytes(b: Seq[Byte]): Bytes = ???
    override def isNullRef(r: Value): ConstantTaintAnalysis.Value = ???
    override def wrapExnRef(tag: TagInstance, fields: List[Value]): Value = Value.ExnRef(tag, fields)
    override def unwrapExnRef(v: Value): (TagInstance, List[Value]) = v match
      case Value.ExnRef(tag, fields) => (tag, fields)
      case _ => f.fail(TypeError, s"Expected exnref but got $v")

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      ix.asInt32.value match
        case Topped.Actual(i) =>
          if (i >= 0 && i < vec.size)
            JOptionPowerset.Some(Powerset(vec(i)))
          else
            JOptionPowerset.None()
        case Topped.Top =>
          if (vec.isEmpty)
            JOptionPowerset.None()
          else
            JOptionPowerset.NoneSome(Powerset(vec.toSet))

  given ConstantTaintAddressOffset(using f: Failure, effectStack: EffectStack): AddressOffset[Addr] = ConstantAddressOffset

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], val config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]
//    , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this

    override protected def handleTailCallInFunction(ex: WasmException[Value])(using Fixed): Unit = ex match
      case WasmException(JumpTarget.TailCall(nextFunc, nextLoc), tailArgs, _) =>
        stack.pushN(tailArgs)
        invoke(nextFunc, nextLoc)

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
//    override def widenState: Widen[State] = implicitly

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, TaintProduct[Topped[Byte]]] = new ConstantAddressMemory(untainted(Topped.Actual(0)))
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val elems: SymbolTableWithDrop[Unit, ElemAddr, Elem, J] = FiniteSymbolTableWithDrop[Unit, ElemAddr, Elem](Seq.empty)
    val tables: SizedConstantTable[TableAddr, RefV] = new SizedConstantTable
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value, InstLoc] = new JoinableDecidableCallFrame(FrameData.empty, Iterable.empty)
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    given Failure = failure
    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, WithJoin] = implicitly

    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantTaintAnalysis.Value]): List[ConstantTaintAnalysis.Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        failure.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        effectStack.joinWithFailure(result)(failure.fail(FileError, s"in ${hostFunc.name}"))

    override def toString: String = s"constant-taint $config"
