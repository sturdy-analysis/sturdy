package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.{AnalysisState, Effectful}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.callframe.CCallFrameNumbered
import sturdy.effect.callframe.CMutableCallFrameNumbered
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.JoinedOperandStack
import sturdy.effect.operandstack.JoinedOperandStack.OperandState
import sturdy.effect.symboltable.{ToppedSymbolTable, SymbolTable}
import sturdy.fix
import sturdy.fix
import sturdy.language.wasm.{Interpreter, ConcreteInterpreter}
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
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView

object ConstantAnalysis extends Interpreter, ConstantValues, ToppedFunctionValue, Fix:
  type MayJoin[A] = WithJoin[A]
  type Addr = Topped[Int]
  type Bytes = Seq[Topped[Byte]]
  type Size = Topped[Int]
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = Topped[Int]
  type FunV = Topped[Powerset[FunctionInstance[Value]]]

  type Symbol = Topped[SymbolUntopped]
  type SymbolUntopped = FuncIx | GlobalAddr

  enum Entry:
    case Function(fun: FunV)
    case Global(glob: GlobalInstance[Value])
    case Top

  given JoinAnEntry(using jF: Join[FunV], jG: Join[GlobalInstance[Value]]): Join[Entry] with
    override def apply(v1: Entry, v2: Entry): Entry = (v1, v2) match
      case (Entry.Function(f1), Entry.Function(f2)) => Entry.Function(jF(f1, f2))
      case (Entry.Global(g1), Entry.Global(g2)) => Entry.Global(jG(g1, g2))
      case _ => Entry.Top

  given EntryTopped: Top[Entry] with
    override def top: Entry = Entry.Top

  given ConstantSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Size, FuncIx, FunV, Symbol, Entry, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def funcIxToSymbol(funcIx: FuncIx): Symbol = Topped.Actual(funcIx)
    override def globIxToSymbol(globalIdx: GlobalAddr): Symbol = Topped.Actual(globalIdx)

    override def funVToEntry(funV: FunV): Entry = Entry.Function(funV)
    override def globIToEntry(globI: GlobalInstance[Value]): Entry = Entry.Global(globI)
    override def entryToFuncV(entry: Entry): FunV = entry match
      case Entry.Function(funV) => funV
      case _ => throw new IllegalArgumentException(s"Expected a function, but got $entry.")
    override def entryToGlobI(entry: Entry): GlobalInstance[Value] = entry match
      case Entry.Global(globI) => globI
      case _ => throw new IllegalArgumentException(s"Expected a global, but got $entry.")

    override def indexLookup[A](ix: Value, vec: Vector[A]): OptionA[A] =
      ix.asInt32 match
        case Topped.Actual(i) =>
          if (i < vec.size)
            OptionA.Some(Iterable.single(vec(i)))
          else
            OptionA.None()
        case Topped.Top =>
          OptionA.NoneSome(vec)

    val runtime: Map[HostFunction, List[Value] => List[Value]] = Map(
      HostFunction.Exit() -> { args =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      }
    )

    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantAnalysis.Value]): List[ConstantAnalysis.Value] =
      runtime(hostFunc)(args)

  type InState =
    (CCallFrameNumbered.Vars[Value],
      ConstantAddressMemory.Memories[MemoryAddr, Topped[Byte]],
      ToppedSymbolTable.Tables[TableAddr, SymbolUntopped, Entry])
  type OutState =
    (ConstantAddressMemory.Memories[MemoryAddr, Topped[Byte]],
      ToppedSymbolTable.Tables[TableAddr, SymbolUntopped, Entry])
  type AllState = InState

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends JoinedOperandStack[Value]
      with ConstantAddressMemory[MemoryAddr, Topped[Byte]](Topped.Actual(0))
      with ToppedSymbolTable[TableAddr, SymbolUntopped, Entry]
      with CMutableCallFrameNumbered[FrameData[Value], Value] with CCallFrameNumbered(rootFrameData, rootFrameValues)
      with JoinedExcept[WasmException[Value], ExcV]
      with AFailureCollect
      with AnalysisState[InState, OutState, AllState] {
    override def getInState() = (getFrameVars, getMemories, getSymbolTables)
    override def getOutState() = (getMemories, getSymbolTables)
    override def getAllState() = getInState()
    def setInState(in: InState) =
      setFrameVars(in._1)
      setMemories(in._2)
      setSymbolTables(in._3)
    def setOutState(out: OutState) =
      setMemories(out._1)
      setSymbolTables(out._2)
    def setAllState(all: AllState) = setInState(all)
  }

  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value], cfgOnlyCalls: Boolean): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    given Effects = effects
    new Instance(effects, cfgOnlyCalls)

  class Instance(effects: Effects, cfgOnlyCalls: Boolean)(using Failure, Effectful)
    extends GenericInstance(effects) :

    private given Effects = effects
    private given Instance = this

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, Symbol, Entry, WithJoin] = implicitly

    val cfg = control[FrameData[Value]](sensitive = false, cfgOnlyCalls)

    val phi: fix.Combinator[FixIn[Value], FixOut[Value]] =
      fix.contextSensitive(frameSensitive,
        fix.log(cfg.logger,
          fix.filter(isFunOrWhile,
            fix.iter.topmost
          )
        )
      )

    def initializeModule(module: Module): ModuleInstance[Value] =
      val modInst = super.initializeModule(module)
      modInst
