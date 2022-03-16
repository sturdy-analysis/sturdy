package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.{Effectful, AnalysisState}
import sturdy.effect.bytememory.TopMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinedDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinedDecidableOperandStack, given}
import sturdy.effect.symboltable.{JoinedSymbolTable, UpperBoundSymbolTable}
import sturdy.fix
import sturdy.fix.Combinator
import sturdy.fix.context.Sensitivity
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
import sturdy.values.types.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView

object TypeAnalysis extends Interpreter, TypeValues, ControlFlow:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = BaseType[Seq[Byte]]
  type Size = I32
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = I32
  type FunV = Powerset[FunctionInstance]

  given TypeSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      if (vec.isEmpty)
        JOptionPowerset.None()
      else
        JOptionPowerset.NoneSome(Powerset(vec.toSet))

    override def invokeHostFunction(hostFunc: HostFunction, args: List[TypeAnalysis.Value]): List[TypeAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      case HostFunction.fd_close => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_read => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_seek => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_write => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_fdstat_get => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
    GenericInstance
//    , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[Value]] = new fix.ContextualFixpoint {
      override type Ctx = config.ctx.Ctx
      val (contextPreparation, sensitivity) = config.ctx.make[Value]
      import config.ctx.finiteCtx
      override protected def contextFree = contextPreparation
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = config.fix.get(using analysisState, effectStack)
    }

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly

    val stack: JoinedDecidableOperandStack[Value] = new JoinedDecidableOperandStack
    val memory: TopMemory[MemoryAddr, Addr, Bytes, Size] = new TopMemory
    val globals: JoinedSymbolTable[Unit, GlobalAddr, Value] = new JoinedSymbolTable
    val funTable: UpperBoundSymbolTable[TableAddr, FuncIx, FunV] = new UpperBoundSymbolTable(Powerset())
    val callFrame: JoinedDecidableCallFrame[FrameData, Int, Value] = new JoinedDecidableCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], Powerset[WasmException[Value]]] = new JoinedExcept
    val failure: AFailureCollect = new AFailureCollect
    given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    override def toString: String = s"type $config"
