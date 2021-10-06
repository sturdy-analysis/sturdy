package sturdy.language.wasm.analyses

import sturdy.data.*
import sturdy.data.{CombineUnit, MakeJoined}
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.bytememory.{ConstantAddressMemory, Memory, Serialize}
import sturdy.effect.callframe.{CCallFrameInt, CMutableCallFrameInt}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.*
import sturdy.effect.operandstack.{JoinedOperandStack, OperandStack}
import sturdy.effect.operandstack.JoinedOperandStack.OperandState
import sturdy.effect.symboltable.{SymbolTable, ToppedSymbolTable}
import sturdy.effect.{AnalysisState, Effectful}
import sturdy.fix
import sturdy.language.wasm.analyses.ConstantAnalysis.{Effects, FuncIx, Instance, Symbol, SymbolUntopped}
import sturdy.language.wasm.analyses.Fix.*
import sturdy.language.wasm.generic.*
import sturdy.language.wasm.generic.{GlobalAddr, GlobalInstance, MemoryAddr, TableAddr, FunctionInstance}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.*
import sturdy.values.convert.ToppedConvert
import sturdy.values.doubles.*
import sturdy.values.exceptions.*
import sturdy.values.floats.*
import sturdy.values.functions.*
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.*
import sturdy.values.taint.*
import sturdy.values.taint.{CombineTaintProduct, TaintDoubleOps, TaintFloatOps, TaintIntOps, TaintLongOps, TaintProduct}
import sturdy.values.TopTopped
import sturdy.values.JoinPowerset
import swam.syntax.*

import java.nio.{ByteBuffer, ByteOrder}
import scala.collection.IndexedSeqView

trait TaintAnalysis(val interp: Interpreter,
                    implicit val ops: WasmOperations[interp.Value, Topped[Int], Topped[Int], interp.FuncIx, interp.FunV, interp.Symbol, interp.Entry],
                    implicit val serialize: Serialize[interp.Value, IndexedSeqView[Topped[Byte]], MemoryInst, MemoryInst],
                    implicit val funops: FunctionOps[FunctionInstance[interp.Value], Nothing, Unit, interp.FunV],
                    implicit val intOps: IntOps[interp.I32],
                    implicit val longOps: LongOps[interp.I64],
                    implicit val floatOps: FloatOps[interp.F32],
                    implicit val doubleOps: DoubleOps[interp.F64],
                    implicit val intEqOps: EqOps[interp.I32, interp.Bool],
                    implicit val longEqOps: EqOps[interp.I64, interp.Bool],
                    implicit val floatEqOps: EqOps[interp.F32, interp.Bool],
                    implicit val doubleEqOps: EqOps[interp.F64, interp.Bool],
                    implicit val intCompareOps: CompareOps[interp.I32, interp.Bool],
                    implicit val longCompareOps: CompareOps[interp.I64, interp.Bool],
                    implicit val floatCompareOps: CompareOps[interp.F32, interp.Bool],
                    implicit val doubleCompareOps: CompareOps[interp.F64, interp.Bool],
                    implicit val intUnsignedCompareOps: UnsignedCompareOps[interp.I32, interp.Bool],
                    implicit val longUnsignedCompareOps: UnsignedCompareOps[interp.I64, interp.Bool],
                    implicit val joinVal: Join[interp.Value],
                    implicit val joinI32: Join[interp.I32],
                    implicit val joinI64: Join[interp.I64],
                    implicit val joinF32: Join[interp.F32],
                    implicit val joinF64: Join[interp.F64],
                    implicit val joinEntry: Join[interp.Entry],
                    implicit val unitJoin: ops.WasmOpsJoin[Unit],
                    implicit val topEntry: Top[interp.Entry],
                    ) extends Interpreter:
  type I32 = TaintProduct[interp.I32]
  type I64 = TaintProduct[interp.I64]
  type F32 = TaintProduct[interp.F32]
  type F64 = TaintProduct[interp.F64]
  type Bool = TaintProduct[interp.Bool]
  type Addr = Topped[Int]
  type Size = Topped[Int]
  type Bytes = IndexedSeqView[TaintProduct[Topped[Byte]]]
  type ExcV = Powerset[WasmException[Value]]
  type FuncIx = TaintProduct[interp.FuncIx]
  type FunV = TaintProduct[interp.FunV]
  type Entry = TaintProduct[interp.Entry]

  type Symbol = Topped[SymbolUntopped]

  enum SymbolUntopped:
    case Function(ix: FuncIx)
    case Global(ix: GlobalAddr)

  final def topI32: I32 = untainted(interp.topI32)
  final def topI64: I64 = untainted(interp.topI64)
  final def topF32: F32 = untainted(interp.topF32)
  final def topF64: F64 = untainted(interp.topF64)

  final def asBoolean(v: Value)(using Failure): Bool =
    v.asInt32.unary(x => interp.asBoolean(interp.Value.Int32(x)))

  override final def boolean(b: TaintProduct[interp.Bool]): Value = interp.boolean(b.value) match {
    case interp.Value.Int32(x) => this.Value.Int32(TaintProduct(b.taint, x))
    case interp.Value.Int64(x) => this.Value.Int64(TaintProduct(b.taint, x))
    case interp.Value.Float32(x) => this.Value.Float32(TaintProduct(b.taint, x))
    case interp.Value.Float64(x) => this.Value.Float64(TaintProduct(b.taint, x))
    case interp.Value.TopValue => this.Value.TopValue
  }

  def unary[B](f: interp.Value => B)(v: Value): TaintProduct[B] = v match {
    case Value.TopValue => TaintProduct(Taint.TopTaint, f(interp.Value.TopValue))
    case Value.Int32(x) => TaintProduct(x.taint, f(interp.Value.Int32(x.value)))
    case Value.Int64(x) => TaintProduct(x.taint, f(interp.Value.Int64(x.value)))
    case Value.Float32(x) => TaintProduct(x.taint, f(interp.Value.Float32(x.value)))
    case Value.Float64(x) => TaintProduct(x.taint, f(interp.Value.Float64(x.value)))
  }

  def valueToProduct: Value => TaintProduct[interp.Value] = unary(identity)

  def taintValue(taint: Taint, v: interp.Value): Value = v match {
    case interp.Value.TopValue => Value.TopValue
    case interp.Value.Int32(x) => Value.Int32(TaintProduct(taint, x))
    case interp.Value.Int64(x) => Value.Int64(TaintProduct(taint, x))
    case interp.Value.Float32(x) => Value.Float32(TaintProduct(taint, x))
    case interp.Value.Float64(x) => Value.Float64(TaintProduct(taint, x))
  }

  given TaintWasmOperations(using Failure): WasmOperations[Value, Addr, Size, FuncIx, FunV, Symbol, Entry] with
    override type WasmOpsJoin[A] = ops.WasmOpsJoin[A]

    override def valueToAddr(v: Value): Addr = unary(ops.valueToAddr)(v).value
    override def valueToFuncIx: Value => FuncIx = unary(ops.valueToFuncIx)
    override def valToSize(v: Value): Size = unary(ops.valToSize)(v).value
    override def sizeToVal(sz: Size): Value = taintValue(Taint.Untainted, ops.sizeToVal(sz))

    override def funcIxToSymbol(funcIx: FuncIx): Symbol = Topped.Actual(SymbolUntopped.Function(funcIx))
    override def globIxToSymbol(globalIdx: GlobalAddr): Symbol = Topped.Actual(SymbolUntopped.Global(globalIdx))

    override def funVToEntry(funV: FunV): Entry = funV.unary(ops.funVToEntry)
    override def globIToEntry(globI: GlobalInstance[Value]): Entry = {
      val product: TaintProduct[interp.Value] = valueToProduct(globI.value)
      val entry: interp.Entry = ops.globIToEntry(GlobalInstance(globI.tpe, product.value))
      product.replace(entry)
    }
    override def entryToFuncV(entry: Entry): FunV = entry.unary(ops.entryToFuncV)

    override def entryToGlobI(entry: Entry): GlobalInstance[Value] = {
      val instance: GlobalInstance[interp.Value] = ops.entryToGlobI(entry.value)
      GlobalInstance(instance.tpe, taintValue(entry.taint,instance.value))
    }
    override def indexLookup[A](ix: Value, vec: Vector[A]) =
      unary(ops.indexLookup(_, vec))(ix).value

  trait ASerialize extends
    Serialize[Value, Bytes, MemoryInst, MemoryInst], Failure:
    override def encode(v: Value, encInfo: MemoryInst): Bytes = {
      val product: TaintProduct[interp.Value] = valueToProduct(v)
      val bytes: IndexedSeqView[Topped[Byte]] = serialize.encode(product.value, encInfo)
      bytes.map(byte => TaintProduct(product.taint, byte))
    }
    override def decode(dat: IndexedSeqView[TaintProduct[Topped[Byte]]], decInfo: MemoryInst): Value = {
      val noTaint: IndexedSeqView[Topped[Byte]] = dat.map(_.value)
      val taint: Taint = ??? // (data.map(_.taint)) TODO: Join taint values
      taintValue(taint, serialize.decode(noTaint, decInfo))
    }
  object ASerialize extends ASerialize

  type Mem = ConstantAddressMemory.Memories[MemoryAddr, TaintProduct[Topped[Byte]]]
  type SymTable = ToppedSymbolTable.Tables[TableAddr, SymbolUntopped, Entry]

  type InState  = (CCallFrameInt.Vars[Value], Mem, SymTable)
  type OutState = (Mem, SymTable)
  type AllState = InState

  implicit val joinValue2: Join[Value] = implicitly
  implicit val topEntry2: Top[Entry] = implicitly
  implicit val topByte: Top[ TaintProduct[Topped[Byte]]] = implicitly
  implicit val exc: Exceptional[WasmException[Value], ExcV, WithJoin] = implicitly

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends JoinedOperandStack[Value]
      with ConstantAddressMemory[MemoryAddr, TaintProduct[Topped[Byte]]](untainted(Topped.Actual(0)))
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
//
//  val testStack: OperandStack[Value] = new JoinedOperandStack[Value] {}
//  val testMemory: Memory[MemoryAddr, Addr, Bytes, Size] = new ConstantAddressMemory[MemoryAddr, TaintProduct[Topped[Byte]]](untainted(Topped.Actual(0))) {}
//  val testSerialize: Serialize[Value, Bytes, MemoryInst, MemoryInst] = new ASerialize {}
//  val testToppedSymbolTable: SymbolTable[TableAddr, Symbol, Entry] = new ToppedSymbolTable[TableAddr, SymbolUntopped, Entry] {}
  val test: GenericEffects[Value, Addr, Bytes, Size, ExcV, Symbol, Entry] = Effects(null, null)


  // We need these implicits for the GenericInstance below, because Scala cannot infer them.
  implicit val taintLongOps: LongOps[I64] = implicitly
  implicit val taintFloatOps: FloatOps[F32] = implicitly
  implicit val taintDoubleOps: DoubleOps[F64] = implicitly
  implicit val taintIntEqOps: EqOps[I32, Bool] = implicitly
  implicit val taintLongEqOps: EqOps[I64, Bool] = implicitly
  implicit val taintFloatEqOps: EqOps[F32, Bool] = implicitly
  implicit val taintDoubleEqOps: EqOps[F64, Bool] = implicitly
  implicit val taintIntCompareOps: CompareOps[I32, Bool] = implicitly
  implicit val taintLongCompareOps: CompareOps[I64, Bool] = implicitly
  implicit val taintFloatCompareOps: CompareOps[F32, Bool] = implicitly
  implicit val taintDoubleCompareOps: CompareOps[F64, Bool] = implicitly
  implicit val taintIntUnsignedCompareOps: UnsignedCompareOps[I32, Bool] = implicitly
  implicit val taintLongUnsignedCompareOps: UnsignedCompareOps[I64, Bool] = implicitly
  implicit val taintConvertIntLong: ConvertIntLong[I32, I64] = implicitly
  implicit val taintConvertIntFloat: ConvertIntFloat[I32, F32] = implicitly
  implicit val taintConvertIntDouble: ConvertIntDouble[I32, F64] = implicitly
  implicit val taintConvertLongInt: ConvertLongInt[I64, I32] = implicitly
  implicit val taintConvertLongFloat: ConvertLongFloat[I64, F32] = implicitly
  implicit val taintConvertLongouble: ConvertLongDouble[I64, F64] = implicitly
  implicit val taintConvertFloatInt: ConvertFloatInt[F32, I32] = implicitly
  implicit val taintConvertFloatLong: ConvertFloatLong[F32, I64] = implicitly
  implicit val taintConvertFloatDOuble: ConvertFloatDouble[F32, F64] = implicitly
  implicit val taintConvertDoubleInt: ConvertDoubleInt[F64, I32] = implicitly
  implicit val taintConvertDoubleLong: ConvertDoubleLong[F64, I64] = implicitly
  implicit val taintConvertDoubleFloat: ConvertDoubleFloat[F64, F32] = implicitly

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

  given TaintFunctionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] with
    override def funValue(fun: FunctionInstance[Value]): FunV = untainted(funops.funValue(fun))
    override def invokeFun(fun: TaintProduct[interp.FunV], args: Seq[Nothing])(invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit = ???

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
