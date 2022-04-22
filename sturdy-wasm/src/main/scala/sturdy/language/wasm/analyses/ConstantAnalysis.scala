package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, AnalysisState}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinedDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinedDecidableOperandStack, given}
import sturdy.effect.symboltable.{JoinedSymbolTable, ConstantSymbolTable}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix
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
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView

object ConstantAnalysis extends Interpreter, ConstantValues, ControlFlow:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = Seq[Topped[Byte]]
  type Size = I32
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = I32
  type FunV = Powerset[FunctionInstance]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      ix.asInt32 match
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

    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantAnalysis.Value]): List[ConstantAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      case HostFunction.fd_close => eff.joinWithFailure(List(Value.Int32(Topped.Top)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_read => eff.joinWithFailure(List(Value.Int32(Topped.Top)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_seek => eff.joinWithFailure(List(Value.Int32(Topped.Top)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_write => eff.joinWithFailure(List(Value.Int32(Topped.Top)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_fdstat_get => eff.joinWithFailure(List(Value.Int32(Topped.Top)))(f.fail(FileError, s"in ${hostFunc.name}"))

  abstract class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value]) extends
      GenericInstance
//      , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this
    
    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly

    
    val stack: JoinedDecidableOperandStack[Value] = new JoinedDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, Topped[Byte]] = new ConstantAddressMemory(Topped.Actual(0))
    val globals: JoinedSymbolTable[Unit, GlobalAddr, Value] = new JoinedSymbolTable
    val funTable: ConstantSymbolTable[TableAddr, Int, Powerset[FunctionInstance]] = new ConstantSymbolTable
    val callFrame: JoinedDecidableCallFrame[FrameData, Int, Value] = new JoinedDecidableCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], Powerset[WasmException[Value]]] = new JoinedExcept
    val failure: AFailureCollect = new AFailureCollect
    private given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly
