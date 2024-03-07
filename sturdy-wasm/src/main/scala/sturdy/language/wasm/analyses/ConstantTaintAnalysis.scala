package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.symboltable.{ConstantSymbolTable, JoinableDecidableSymbolTable, given}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.floating.FloatOps
import swam.syntax.*
import swam.FuncType
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.taint.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.control.{ControlObservable, RecordingControlObserver}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.operandstack.JoinableDecidableOperandStack

object ConstantTaintAnalysis extends Interpreter, ConstantTaintValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = Topped[Int]
  type AByte = TaintProduct[Topped[Byte]]
  type Bytes = Seq[AByte]
  type Size = Topped[Int]
  type FuncIx = Topped[Int]
  type FunV = Powerset[FunctionInstance]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32.value
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32.value
    override def valToSize(v: Value): Size = v.asInt32.value
    override def sizeToVal(sz: Size): Value = Value.Int32(untainted(sz))

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


    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantTaintAnalysis.Value]): List[ConstantTaintAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], val config: WasmConfig) extends
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
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, TaintProduct[Topped[Byte]]] = new ConstantAddressMemory(untainted(Topped.Actual(0)))
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val funTable: ConstantSymbolTable[TableAddr, Int, Powerset[FunctionInstance]] = new ConstantSymbolTable
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value] = new JoinableDecidableCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures
    given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    override def toString: String = s"constant-taint $config"
