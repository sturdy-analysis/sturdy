package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.{AnalysisState, Effectful}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.Serialize
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.callframe.CCallFrameInt
import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.JoinedOperandStack
import sturdy.effect.operandstack.JoinedOperandStack.OperandState
import sturdy.effect.symboltable.{SymbolTable, ToppedSymbolTable}
import sturdy.fix
import sturdy.fix
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.ConstantValues
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.analyses.Fix.{*, given}
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import swam.syntax.*
import sturdy.values.convert.ToppedConvert
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

object ConstantAnalysis extends Interpreter, ConstantValues:

  type Addr = Topped[Int]
  type Bytes = IndexedSeqView[Topped[Byte]]
  type Size = Topped[Int]
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = Topped[Int]
  type FunV = Topped[Powerset[FunctionInstance[Value]]]

  type Symbol = Topped[SymbolUntopped]

  enum SymbolUntopped:
    case Function(ix: FuncIx)
    case Global(ix: GlobalAddr)

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

  given ConstantWasmOperations(using Failure): WasmOperations[Value, Addr, Size, FuncIx, FunV, Symbol, Entry] with
    override type WasmOpsJoin[A] = WithJoin[A]

    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def funcIxToSymbol(funcIx: FuncIx): Symbol = Topped.Actual(SymbolUntopped.Function(funcIx))
    override def globIxToSymbol(globalIdx: GlobalAddr): Symbol = Topped.Actual(SymbolUntopped.Global(globalIdx))

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

  trait ASerialize extends Serialize[Value, Bytes, MemoryInst, MemoryInst], Failure:
    val cSerialize = new ConcreteInterpreter.CSerialize with Failure {
      override def fail(kind: FailureKind, msg: String): Nothing = ASerialize.this.fail(kind, msg)
    }
    override def decode(dat: IndexedSeqView[Topped[Byte]], decInfo: MemoryInst): Value =
      val bytes = dat.map {
        case Topped.Top => return decInfo match
          case _: (i32.Load | i32.Load8S | i32.Load8U | i32.Load16S | i32.Load16U) => Value.Int32(Topped.Top)
          case _: (i64.Load | i64.Load8S | i64.Load8U | i64.Load16S | i64.Load16U | i64.Load32S | i64.Load32U ) => Value.Int64(Topped.Top)
          case _: f32.Load => Value.Float32(Topped.Top)
          case _: f64.Load => Value.Float64(Topped.Top)
          case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $decInfo.")
        case Topped.Actual(b) => b
      }.toArray
      liftConcreteValue(cSerialize.decode(ByteBuffer.wrap(bytes), decInfo))

    override def encode(v: Value, encInfo: MemoryInst): IndexedSeqView[Topped[Byte]] = v match
      case Value.TopValue | Value.Int32(Topped.Top) | Value.Int64(Topped.Top) | Value.Float32(Topped.Top) | Value.Float64(Topped.Top) => encInfo match
        case _: i32.Store => Array.fill(4)(Topped.Top).view
        case _: i32.Store8 => Array.fill(1)(Topped.Top).view
        case _: i32.Store16 => Array.fill(2)(Topped.Top).view
        case _: i64.Store => Array.fill(8)(Topped.Top).view
        case _: i64.Store8 => Array.fill(1)(Topped.Top).view
        case _: i64.Store16 => Array.fill(2)(Topped.Top).view
        case _: i64.Store32 => Array.fill(4)(Topped.Top).view
        case _: f32.Store => Array.fill(4)(Topped.Top).view
        case _: f64.Store => Array.fill(8)(Topped.Top).view
        case _ => throw new IllegalArgumentException(s"Expected store instruction, but got $encInfo.")
      case Value.Int32(Topped.Actual(i)) => cSerialize.encode(ConcreteInterpreter.Value.Int32(i), encInfo).array().view.map(Topped.Actual.apply)
      case Value.Int64(Topped.Actual(l)) => cSerialize.encode(ConcreteInterpreter.Value.Int64(l), encInfo).array().view.map(Topped.Actual.apply)
      case Value.Float32(Topped.Actual(f)) => cSerialize.encode(ConcreteInterpreter.Value.Float32(f), encInfo).array().view.map(Topped.Actual.apply)
      case Value.Float64(Topped.Actual(d)) => cSerialize.encode(ConcreteInterpreter.Value.Float64(d), encInfo).array().view.map(Topped.Actual.apply)

  type InState =
    (CCallFrameInt.Vars[Value],
      ConstantAddressMemory.Memories[MemoryAddr, Topped[Byte]],
      ToppedSymbolTable.Tables[TableAddr, SymbolUntopped, Entry])
  type OutState =
    (ConstantAddressMemory.Memories[MemoryAddr, Topped[Byte]],
      ToppedSymbolTable.Tables[TableAddr, SymbolUntopped, Entry])
  type AllState = InState

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends JoinedOperandStack[Value]
      with ConstantAddressMemory[MemoryAddr, Topped[Byte]](Topped.Actual(0))
      with ASerialize
      with ToppedSymbolTable[TableAddr, SymbolUntopped, Entry]
      with CMutableCallFrameInt[FrameData[Value], Value] with CCallFrameInt(rootFrameData, rootFrameValues)
      with ABoolBranching[Value]
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

  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value]): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    given Effects = effects
    new Instance(effects)

  class Instance(effects: Effects)(using Failure, Effectful)
    extends GenericInstance[Effects] with GenericInterpreter(effects) :

    given Effects = effects

    def i32Ops: IntOps[I32] = implicitly
    def i64Ops: LongOps[I64] = implicitly
    def f32Ops: FloatOps[F32] = implicitly
    def f64Ops: DoubleOps[F64] = implicitly
    def i32EqOps: EqOps[I32, Bool] = implicitly
    def i64EqOps: EqOps[I64, Bool] = implicitly
    def f32EqOps: EqOps[F32, Bool] = implicitly
    def f64EqOps: EqOps[F64, Bool] = implicitly
    def i32CompareOps: CompareOps[I32, Bool] = implicitly
    def i64CompareOps: CompareOps[I64, Bool] = implicitly
    def f32CompareOps: CompareOps[F32, Bool] = implicitly
    def f64CompareOps: CompareOps[F64, Bool] = implicitly
    def i32UnsignedCompareOps: UnsignedCompareOps[I32, Bool] = implicitly
    def i64UnsignedCompareOps: UnsignedCompareOps[I64, Bool] = implicitly
    def convertI32I64: ConvertIntLong[I32, I64] = implicitly
    def convertI32F32: ConvertIntFloat[I32, F32] = implicitly
    def convertI32F64: ConvertIntDouble[I32, F64] = implicitly
    def convertI64I32: ConvertLongInt[I64, I32] = implicitly
    def convertI64F32: ConvertLongFloat[I64, F32] = implicitly
    def convertI64F64: ConvertLongDouble[I64, F64] = implicitly
    def convertF32I32: ConvertFloatInt[F32, I32] = implicitly
    def convertF32I64: ConvertFloatLong[F32, I64] = implicitly
    def convertF32F64: ConvertFloatDouble[F32, F64] = implicitly
    def convertF64I32: ConvertDoubleInt[F64, I32] = implicitly
    def convertF64I64: ConvertDoubleLong[F64, I64] = implicitly
    def convertF64F32: ConvertDoubleFloat[F64, F32] = implicitly

    implicit def topFunctionCall(args: Seq[Nothing], invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit =
      val invokeAllFuns = module.functions.map(fun => () => invoke(fun, args))
      effects.joinComputationsIterable(invokeAllFuns)
    val functionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] = implicitly

    val phi: fix.Combinator[FixIn[Value], FixOut[Value]] =
      fix.contextSensitive[FrameData[Value], FixIn[Value], FixOut[Value]](frameSensitive,
        fix.filter(isFunOrWhile,
          fix.iter.topmost
        )
      )
