package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.{AnalysisState, Effectful}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinedCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.JoinedOperandStack
import sturdy.effect.symboltable.{JoinedSymbolTable, ToppedSymbolTable}
import sturdy.effect.symboltable.ToppedSymbolTable.CombineTable
import sturdy.fix
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import swam.syntax.*
import swam.FuncType
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.doubles.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floats.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.longs.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.taint.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView

object ConstantTaintAnalysis extends Interpreter, ConstantTaintValues, ToppedFunctionValue, Fix, ControlFlow:
  type MayJoin[A] = WithJoin[A]
  override type Ctx = CallString
  type Addr = Topped[Int]
  type AByte = TaintProduct[Topped[Byte]]
  type Bytes = Seq[AByte]
  type Size = Topped[Int]
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = Topped[Int]
  type FunV = Topped[Powerset[FunctionInstance]]

  given ConstantSpecialWasmOperations(using f: Failure, eff: Effectful): SpecialWasmOperations[Value, Addr, Size, FuncIx, FunV, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32.value
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32.value
    override def valToSize(v: Value): Size = v.asInt32.value
    override def sizeToVal(sz: Size): Value = Value.Int32(untainted(sz))

    override def indexLookup[A](ix: Value, vec: Vector[A]): OptionA[A] =
      ix.asInt32.value match
        case Topped.Actual(i) =>
          if (i >= 0 && i < vec.size)
            OptionA.Some(Iterable.single(vec(i)))
          else
            OptionA.None()
        case Topped.Top =>
          OptionA.NoneSome(vec)


    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantTaintAnalysis.Value]): List[ConstantTaintAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      case HostFunction.fd_close => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_read => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_seek => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_write => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))
      case HostFunction.fd_fdstat_get => eff.joinWithFailure(List(Value.Int32(tainted(Topped.Top))))(f.fail(FileError, s"in ${hostFunc.name}"))

  type InState =
    (ConcreteCallFrame.Vars[Value],
      ConstantAddressMemory.Memories[MemoryAddr, AByte],
      Globals.Values[Value],
      JoinedOperandStack.Operands[Value])
  type OutState =
    (ConstantAddressMemory.Memories[MemoryAddr, AByte],
      Globals.Values[Value],
      JoinedOperandStack.Operands[Value])
  type AllState = InState

  class Effects(rootFrameData: FrameData, rootFrameValues: Iterable[Value])
    extends JoinedOperandStack[Value]
      with ConstantAddressMemory[MemoryAddr, AByte](untainted(Topped.Actual(0)))
      with Globals[Value]
      with ToppedSymbolTable[TableAddr, Int, FunV]
      with JoinedCallFrame[FrameData, Int, Value]
      with JoinedExcept[WasmException[Value], ExcV]
      with AFailureCollect
      with AnalysisState[InState, OutState, AllState] {

    override def initialCallFrameData = rootFrameData
    override def initialCallFrameVars = rootFrameValues.view.zipWithIndex.map(_.swap)
    override protected def makeGlobalsTable = new JoinedSymbolTable[Unit, GlobalAddr, Value] {}

    override def getInState() = (getFrameVars, getMemories, getGlobalValues, getOperandFrame)
    override def getOutState() = (getMemories, getGlobalValues, getOperandFrame)
    override def getAllState() = getInState()
    def setInState(in: InState) =
      setFrameVars(in._1)
      setMemories(in._2)
      setGlobalValues(in._3)
      setOperandFrame(in._4)
    def setOutState(out: OutState) =
      setMemories(out._1)
      setGlobalValues(out._2)
      setOperandFrame(out._3)
    def setAllState(all: AllState) = setInState(all)
  }

  def apply(rootFrameData: FrameData, rootFrameValues: Iterable[Value]): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    given Effects = effects
    new Instance(effects)

  class Instance(effects: Effects)(using Failure, Effectful)
    extends GenericInstance(effects) :

    private given Effects = effects
    private given Instance = this

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    val callSites = callSitesLogger()

    protected override def context = callSites.callString(0)
    protected override def contextFree = fix.log(callSites, _)
    protected override def contextSensitive = fix.filter(isFunOrWhile, fix.iter.innermost)
