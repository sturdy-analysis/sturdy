package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.{Effectful, AnalysisState}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinedDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.JoinedDecidableOperandStack
import sturdy.effect.symboltable.{JoinedSymbolTable, ConstantSymbolTable}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix
import sturdy.language.wasm.{Interpreter, ConcreteInterpreter}
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

object ConstantTaintAnalysis extends Interpreter, ConstantTaintValues, ControlFlow:
  type J[A] = WithJoin[A]
  type Addr = Topped[Int]
  type AByte = TaintProduct[Topped[Byte]]
  type Bytes = Seq[AByte]
  type Size = Topped[Int]
  type ExcV = Powerset[WasmException[Value]]
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
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      case HostFunction.fd_close => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_read => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_seek => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_write => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_fdstat_get => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))


  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], val config: WasmConfig) extends
    GenericInstance
//    , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this

    override type Ctx = config.ctx.Ctx
    val (contextPreparation, sensitivity) = config.ctx.make[Value]
    import config.ctx.finiteCtx
    override protected def contextFree = contextPreparation
    override protected def context: fix.context.Sensitivity[FixIn, Ctx] = sensitivity
    override protected def contextSensitive = config.fix.get(using analysisState, effectStack)

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    val stack: JoinedDecidableOperandStack[Value] = new JoinedDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, TaintProduct[Topped[Byte]]] = new ConstantAddressMemory(untainted(Topped.Actual(0)))
    val globals: JoinedSymbolTable[Unit, GlobalAddr, Value] = new JoinedSymbolTable
    val funTable: ConstantSymbolTable[TableAddr, Int, Powerset[FunctionInstance]] = new ConstantSymbolTable
    val callFrame: JoinedDecidableCallFrame[FrameData, Int, Value] = new JoinedDecidableCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], Powerset[WasmException[Value]]] = new JoinedExcept
    
    override def toString: String = s"constant-taint $config"
