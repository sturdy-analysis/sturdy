package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.{AnalysisState, Effectful}
import sturdy.effect.bytememory.TopMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinedDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.JoinedDecidableOperandStack
import sturdy.effect.symboltable.{TopSymbolTable, JoinedSymbolTable}
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

object TypeAnalysis extends Interpreter, TypeValues, ToppedFunctionValue, ControlFlow:
  type MayJoin[A] = WithJoin[A]
  type Addr = I32
  type Bytes = BaseType[Seq[Byte]]
  type Size = I32
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = I32
  type FunV = Unit

  given TypeSpecialWasmOperations(using f: Failure, eff: Effectful): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): OptionPowerset[A] =
      if (vec.isEmpty)
        OptionPowerset.None()
      else
        OptionPowerset.NoneSome(Powerset(vec.toSet))

    override def invokeHostFunction(hostFunc: HostFunction, args: List[TypeAnalysis.Value]): List[TypeAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      case HostFunction.fd_close => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_read => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_seek => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_write => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_fdstat_get => eff.joinWithFailure(List(Value.Int32(topI32)))(f.fail(FileError, s"in ${hostFunc.name}"))

  type InState =
    (ConcreteCallFrame.Vars[Value],
      Globals.Values[Value],
      JoinedDecidableOperandStack.Operands[Value])
  type OutState =
    (Globals.Values[Value],
      JoinedDecidableOperandStack.Operands[Value])
  type AllState = InState

  class Effects(rootFrameData: FrameData, rootFrameValues: Iterable[Value])
    extends JoinedDecidableOperandStack[Value]
      with TopMemory[MemoryAddr, Addr, Bytes, Size]
      with Globals[Value]
      with TopSymbolTable[TableAddr, FuncIx, FunV]
      with JoinedDecidableCallFrame[FrameData, Int, Value]
      with JoinedExcept[WasmException[Value], ExcV]
      with AFailureCollect
      with AnalysisState[InState, OutState, AllState] {

    override def initialCallFrameData = rootFrameData
    override def initialCallFrameVars = rootFrameValues.view.zipWithIndex.map(_.swap)
    override protected def makeGlobalsTable = new JoinedSymbolTable[Unit, GlobalAddr, Value] {}

    override def getInState() = (getFrameVars, getGlobalValues, getOperandFrame)
    override def getOutState() = (getGlobalValues, getOperandFrame)
    override def getAllState() = getInState()
    def setInState(in: InState) =
      setFrameVars(in._1)
      setGlobalValues(in._2)
      setOperandFrame(in._3)
    def setOutState(out: OutState) =
      setGlobalValues(out._1)
      setOperandFrame(out._2)
    def setAllState(all: AllState) = setInState(all)
  }

  class Instance(conf: WasmConfig)(using effects: Effects) extends
      GenericInstance(effects),
      WasmFixpoint[Value, InState, OutState, AllState](conf):
    private given Instance = this
    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    override def toString: String = s"constant $config"

  def apply(rootFrameData: FrameData, rootFrameValues: Iterable[Value]): WasmConfig => Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    new Instance(_)(using effects)
