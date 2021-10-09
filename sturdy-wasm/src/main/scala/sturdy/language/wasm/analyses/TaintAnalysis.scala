//package sturdy.language.wasm.analyses
//
//import sturdy.data.*
//import sturdy.data.{CombineUnit, MakeJoined}
//import sturdy.effect.branching.ABoolBranching
//import sturdy.effect.bytememory.{ConstantAddressMemory, Memory, Serialize}
//import sturdy.effect.callframe.{CCallFrameInt, CMutableCallFrameInt}
//import sturdy.effect.except.JoinedExcept
//import sturdy.effect.failure.*
//import sturdy.effect.operandstack.{JoinedOperandStack, OperandStack}
//import sturdy.effect.operandstack.JoinedOperandStack.OperandState
//import sturdy.effect.symboltable.{SymbolTable, ToppedSymbolTable}
//import sturdy.effect.{AnalysisState, Effectful}
//import sturdy.fix
//import sturdy.language.wasm.analyses.ConstantAnalysis.{Effects, FuncIx, Instance, Symbol, SymbolUntopped}
//import sturdy.language.wasm.abstractions.Fix
//import sturdy.language.wasm.generic.*
//import sturdy.language.wasm.generic.{GlobalAddr, GlobalInstance, MemoryAddr, TableAddr, FunctionInstance}
//import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
//import sturdy.values.*
//import sturdy.values.convert.ToppedConvert
//import sturdy.values.doubles.*
//import sturdy.values.exceptions.{*, given}
//import sturdy.values.floats.*
//import sturdy.values.functions.*
//import sturdy.values.ints.*
//import sturdy.values.longs.*
//import sturdy.values.relational.*
//import sturdy.values.taint.{*, given}
//import sturdy.values.TopTopped
//import sturdy.values.JoinPowerset
//import swam.syntax.*
//
//import java.nio.{ByteBuffer, ByteOrder}
//import scala.collection.IndexedSeqView
//
//
//class TaintAnalysis[BaseV, BaseFunV]
//  (using ops: WasmOps[BaseV, BaseFunV]):
//
//  type Value = TaintProduct[BaseV]
//  type FunV = TaintProduct[BaseFunV]
//
//  given TaintWasmOps(using Failure): WasmOps[Value, FunV] with
//    val intOps: IntOps[Value] = new TaintIntOps(using ops.intOps)
//    val longOps: LongOps[Value] = new TaintLongOps(using ops.longOps)
//    val floatOps: FloatOps[Value] = new TaintFloatOps(using ops.floatOps)
//    val doubleOps: DoubleOps[Value] = new TaintDoubleOps(using ops.doubleOps)
//    val eqOps: EqOps[Value, Value] = new TaintEqOps(using ops.eqOps)
//    val compareOps: CompareOps[Value, Value] = new TaintCompareOps(using ops.compareOps)
//    val unsignedCompareOps: UnsignedCompareOps[Value, Value] = new TaintUnsignedCompareOps(using ops.unsignedCompareOps)
//    val convertIntLong: ConvertIntLong[Value, Value] = new TaintConvert(using ops.convertIntLong)
//    val convertIntFloat: ConvertIntFloat[Value, Value] = new TaintConvert(using ops.convertIntFloat)
//    val convertIntDouble: ConvertIntDouble[Value, Value] = new TaintConvert(using ops.convertIntDouble)
//    val convertLongInt: ConvertLongInt[Value, Value] = new TaintConvert(using ops.convertLongInt)
//    val convertLongFloat: ConvertLongFloat[Value, Value] = new TaintConvert(using ops.convertLongFloat)
//    val convertLongDouble: ConvertLongDouble[Value, Value] = new TaintConvert(using ops.convertLongDouble)
//    val convertFloatInt: ConvertFloatInt[Value, Value] = new TaintConvert(using ops.convertFloatInt)
//    val convertFloatLong: ConvertFloatLong[Value, Value] = new TaintConvert(using ops.convertFloatLong)
//    val convertFloatDouble: ConvertFloatDouble[Value, Value] = new TaintConvert(using ops.convertFloatDouble)
//    val convertDoubleInt: ConvertDoubleInt[Value, Value] = new TaintConvert(using ops.convertDoubleInt)
//    val convertDoubleLong: ConvertDoubleLong[Value, Value] = new TaintConvert(using ops.convertDoubleLong)
//    val convertDoubleFloat: ConvertDoubleFloat[Value, Value] = new TaintConvert(using ops.convertDoubleFloat)
//    val functionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] = new FunctionOps {
//      override def funValue(fun: FunctionInstance[Value]): FunV =
//        untainted(ops.functionOps.funValue(fun.asInstanceOf[FunctionInstance[BaseV]]))
//      override def invokeFun(fun: FunV, args: Seq[Nothing])(invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit =
//        fun.unary(funV => ops.functionOps.invokeFun(funV, args)((fun, args) => invoke(fun.asInstanceOf[FunctionInstance[Value]], args)))
//    }
//
////  class Instance extends GenericInterpreter[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, Symbol, Entry, Effects]
//
////class TaintAnalysis
////  (val base: Interpreter)
////  (baseBoolAsBoolean: base.Bool => Topped[Boolean])
////  (using val ops: WasmOperations[base.Value, Topped[Int], Topped[Int], base.FuncIx, base.FunV, base.Symbol, base.Entry],
////   baseOps: base.ValueWasmOps, serialize: Serialize[base.Value, IndexedSeqView[Topped[Byte]], MemoryInst, MemoryInst]
////  )
////  (using Join[base.I32], Join[base.I64], Join[base.F32], Join[base.F64], Join[base.Entry], Top[base.Entry], ops.WasmOpsJoin[Unit])
////  extends Interpreter:
////
////  type I32 = TaintProduct[base.I32]
////  type I64 = TaintProduct[base.I64]
////  type F32 = TaintProduct[base.F32]
////  type F64 = TaintProduct[base.F64]
////  type Bool = TaintProduct[base.Bool]
////  type Addr = Topped[Int]
////  type Size = Topped[Int]
////  type Bytes = IndexedSeqView[TaintProduct[Topped[Byte]]]
////  type ExcV = Powerset[WasmException[Value]]
////  type FuncIx = base.FuncIx
////  type FunV = TaintProduct[base.FunV]
////  type Entry = TaintProduct[base.Entry]
////  type Symbol = base.Symbol
////
////  enum SymbolUntopped:
////    case Function(ix: FuncIx)
////    case Global(ix: GlobalAddr)
////
////  final def topI32: I32 = untainted(base.topI32)
////  final def topI64: I64 = untainted(base.topI64)
////  final def topF32: F32 = untainted(base.topF32)
////  final def topF64: F64 = untainted(base.topF64)
////
////  final def asBoolean(v: Value)(using Failure): Bool =
////    v.asInt32.unary(x => base.asBoolean(base.Value.Int32(x)))
////
////  override final def boolean(b: TaintProduct[base.Bool]): Value = base.boolean(b.value) match {
////    case base.Value.Int32(x) => this.Value.Int32(TaintProduct(b.taint, x))
////    case base.Value.Int64(x) => this.Value.Int64(TaintProduct(b.taint, x))
////    case base.Value.Float32(x) => this.Value.Float32(TaintProduct(b.taint, x))
////    case base.Value.Float64(x) => this.Value.Float64(TaintProduct(b.taint, x))
////    case base.Value.TopValue => this.Value.TopValue
////  }
////
////  def unary[B](f: base.Value => B)(v: Value): TaintProduct[B] = v match {
////    case Value.TopValue => TaintProduct(Taint.TopTaint, f(base.Value.TopValue))
////    case Value.Int32(x) => TaintProduct(x.taint, f(base.Value.Int32(x.value)))
////    case Value.Int64(x) => TaintProduct(x.taint, f(base.Value.Int64(x.value)))
////    case Value.Float32(x) => TaintProduct(x.taint, f(base.Value.Float32(x.value)))
////    case Value.Float64(x) => TaintProduct(x.taint, f(base.Value.Float64(x.value)))
////  }
////
////  def valueToProduct: Value => TaintProduct[base.Value] = unary(identity)
////
////  def taintValue(taint: Taint, v: base.Value): Value = v match {
////    case base.Value.TopValue => Value.TopValue
////    case base.Value.Int32(x) => Value.Int32(TaintProduct(taint, x))
////    case base.Value.Int64(x) => Value.Int64(TaintProduct(taint, x))
////    case base.Value.Float32(x) => Value.Float32(TaintProduct(taint, x))
////    case base.Value.Float64(x) => Value.Float64(TaintProduct(taint, x))
////  }
////
////  given TaintWasmOperations(using Failure): WasmOperations[Value, Addr, Size, FuncIx, FunV, Symbol, Entry] with
////    override type WasmOpsJoin[A] = ops.WasmOpsJoin[A]
////
////    override def valueToAddr(v: Value): Addr = unary(ops.valueToAddr)(v).value
////    override def valueToFuncIx(v: Value): FuncIx = unary(ops.valueToFuncIx)(v).value
////    override def valToSize(v: Value): Size = unary(ops.valToSize)(v).value
////    override def sizeToVal(sz: Size): Value = taintValue(Taint.Untainted, ops.sizeToVal(sz))
////
////    override def funcIxToSymbol(funcIx: FuncIx): Symbol = ops.funcIxToSymbol(funcIx)
////    override def globIxToSymbol(globalIdx: GlobalAddr): Symbol = ops.globIxToSymbol(globalIdx)
////
////    override def funVToEntry(funV: FunV): Entry = funV.unary(ops.funVToEntry)
////    override def globIToEntry(globI: GlobalInstance[Value]): Entry = {
////      val product: TaintProduct[base.Value] = valueToProduct(globI.value)
////      val entry: base.Entry = ops.globIToEntry(GlobalInstance(globI.tpe, product.value))
////      product.copyTaint(entry)
////    }
////    override def entryToFuncV(entry: Entry): FunV = entry.unary(ops.entryToFuncV)
////
////    override def entryToGlobI(entry: Entry): GlobalInstance[Value] = {
////      val instance: GlobalInstance[base.Value] = ops.entryToGlobI(entry.value)
////      GlobalInstance(instance.tpe, taintValue(entry.taint,instance.value))
////    }
////    override def indexLookup[A](ix: Value, vec: Vector[A]) =
////      unary(ops.indexLookup(_, vec))(ix).value
////
////
////  trait ASerialize extends
////    Serialize[Value, Bytes, MemoryInst, MemoryInst], Failure:
////    override def encode(v: Value, encInfo: MemoryInst): Bytes = {
////      val product: TaintProduct[base.Value] = valueToProduct(v)
////      val bytes: IndexedSeqView[Topped[Byte]] = serialize.encode(product.value, encInfo)
////      bytes.map(byte => TaintProduct(product.taint, byte))
////    }
////    override def decode(dat: IndexedSeqView[TaintProduct[Topped[Byte]]], decInfo: MemoryInst): Value = {
////      val raw: IndexedSeqView[Topped[Byte]] = dat.map(_.value)
////      val taint: Taint = dat.map(_.taint).foldLeft(Taint.Untainted)(Join(_, _))
////      taintValue(taint, serialize.decode(raw, decInfo))
////    }
////
////  type Mem = ConstantAddressMemory.Memories[MemoryAddr, TaintProduct[Topped[Byte]]]
////  type SymTable = ToppedSymbolTable.Tables[TableAddr, SymbolUntopped, Entry]
////
////  type InState  = (CCallFrameInt.Vars[Value], Mem, SymTable)
////  type OutState = (Mem, SymTable)
////  type AllState = InState
////
////  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
////    extends JoinedOperandStack[Value]
////      with ConstantAddressMemory[MemoryAddr, TaintProduct[Topped[Byte]]](untainted(Topped.Actual(0)))
////      with ASerialize
////      with ToppedSymbolTable[TableAddr, Symbol, Entry]
////      with CMutableCallFrameInt[FrameData[Value], Value] with CCallFrameInt(rootFrameData, rootFrameValues)
////      with ABoolBranching[Value](using v => baseBoolAsBoolean(v.asBoolean.value))
////      with JoinedExcept[WasmException[Value], ExcV]
////      with AFailureCollect
////      with AnalysisState[InState, OutState, AllState] {
////    override def getInState() = (getFrameVars, getMemories, getSymbolTables)
////    override def getOutState() = (getMemories, getSymbolTables)
////    override def getAllState() = getInState()
////    def setInState(in: InState) =
////      setFrameVars(in._1)
////      setMemories(in._2)
////      setSymbolTables(in._3)
////    def setOutState(out: OutState) =
////      setMemories(out._1)
////      setSymbolTables(out._2)
////    def setAllState(all: AllState) = setInState(all)
////  }
////
//////  trait MyFailure extends AFailureCollect
//////  class MyEffects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
//////    extends Effects(rootFrameData, rootFrameValues) with MyFailure
////
//////  class Instance(effects: Effects)(using Failure, Effectful) extends GenericInstance with GenericInterpreter(effects) :
//////
//////    implicit def topFunctionCall(args: Seq[Nothing], invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit =
//////      val invokeAllFuns = module.functions.map(fun => () => invoke(fun, args))
//////      effects.joinComputationsIterable(invokeAllFuns)
//////
//////    given Effects = effects
//////
//////    val functionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] = new FunctionOps {
//////      override def funValue(fun: FunctionInstance[Value]): FunV =
//////        untainted(baseOps.functionOps.funValue(fun.asInstanceOf[FunctionInstance[base.Value]]))
//////      override def invokeFun(fun: FunV, args: Seq[Nothing])(invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit =
//////        fun.unary(funV => baseOps.functionOps.invokeFun(funV, args)((fun, args) => invoke(fun.asInstanceOf[FunctionInstance[Value]], args)))
//////    }
//////
//////    import baseOps.{*, given}
//////    given IntOps[base.I32] = baseOps.ai32Ops
//////    summon[WasmOps[Value, FunV]]
////
//////    val wasmOps: WasmOps[Value, FunV] = implicitly
//////
//////    val phi: fix.Combinator[FixIn[Value], FixOut[Value]] =
//////      fix.contextSensitive[FrameData[Value], FixIn[Value], FixOut[Value]](frameSensitive,
//////        fix.filter(isFunOrWhile,
//////          fix.iter.topmost
//////        )
//////      )
////
////
//////
//////  // We need these implicits for the GenericInstance below, because Scala cannot infer them.
//////  implicit val taintLongOps: LongOps[I64] = implicitly
//////  implicit val taintFloatOps: FloatOps[F32] = implicitly
//////  implicit val taintDoubleOps: DoubleOps[F64] = implicitly
//////  implicit val taintIntEqOps: EqOps[I32, Bool] = implicitly
//////  implicit val taintLongEqOps: EqOps[I64, Bool] = implicitly
//////  implicit val taintFloatEqOps: EqOps[F32, Bool] = implicitly
//////  implicit val taintDoubleEqOps: EqOps[F64, Bool] = implicitly
//////  implicit val taintIntCompareOps: CompareOps[I32, Bool] = implicitly
//////  implicit val taintLongCompareOps: CompareOps[I64, Bool] = implicitly
//////  implicit val taintFloatCompareOps: CompareOps[F32, Bool] = implicitly
//////  implicit val taintDoubleCompareOps: CompareOps[F64, Bool] = implicitly
//////  implicit val taintIntUnsignedCompareOps: UnsignedCompareOps[I32, Bool] = implicitly
//////  implicit val taintLongUnsignedCompareOps: UnsignedCompareOps[I64, Bool] = implicitly
//////  implicit val taintConvertIntLong: ConvertIntLong[I32, I64] = implicitly
//////  implicit val taintConvertIntFloat: ConvertIntFloat[I32, F32] = implicitly
//////  implicit val taintConvertIntDouble: ConvertIntDouble[I32, F64] = implicitly
//////  implicit val taintConvertLongInt: ConvertLongInt[I64, I32] = implicitly
//////  implicit val taintConvertLongFloat: ConvertLongFloat[I64, F32] = implicitly
//////  implicit val taintConvertLongouble: ConvertLongDouble[I64, F64] = implicitly
//////  implicit val taintConvertFloatInt: ConvertFloatInt[F32, I32] = implicitly
//////  implicit val taintConvertFloatLong: ConvertFloatLong[F32, I64] = implicitly
//////  implicit val taintConvertFloatDOuble: ConvertFloatDouble[F32, F64] = implicitly
//////  implicit val taintConvertDoubleInt: ConvertDoubleInt[F64, I32] = implicitly
//////  implicit val taintConvertDoubleLong: ConvertDoubleLong[F64, I64] = implicitly
//////  implicit val taintConvertDoubleFloat: ConvertDoubleFloat[F64, F32] = implicitly
//////
//////  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value]): Instance =
//////    val effects = new Effects(rootFrameData, rootFrameValues)
//////    given Effects = effects
//////    new Instance(effects)
//////
//////  class Instance(effects: Effects)(using Failure, Effectful)
//////    extends GenericInstance with GenericInterpreter(effects):
//////
//////    given Effects = effects
//////
//////    def i32Ops: IntOps[I32] = implicitly
//////    def i64Ops: LongOps[I64] = implicitly
//////    def f32Ops: FloatOps[F32] = implicitly
//////    def f64Ops: DoubleOps[F64] = implicitly
//////    def i32EqOps: EqOps[I32, Bool] = implicitly
//////    def i64EqOps: EqOps[I64, Bool] = implicitly
//////    def f32EqOps: EqOps[F32, Bool] = implicitly
//////    def f64EqOps: EqOps[F64, Bool] = implicitly
//////    def i32CompareOps: CompareOps[I32, Bool] = implicitly
//////    def i64CompareOps: CompareOps[I64, Bool] = implicitly
//////    def f32CompareOps: CompareOps[F32, Bool] = implicitly
//////    def f64CompareOps: CompareOps[F64, Bool] = implicitly
//////    def i32UnsignedCompareOps: UnsignedCompareOps[I32, Bool] = implicitly
//////    def i64UnsignedCompareOps: UnsignedCompareOps[I64, Bool] = implicitly
//////    def convertI32I64: ConvertIntLong[I32, I64] = implicitly
//////    def convertI32F32: ConvertIntFloat[I32, F32] = implicitly
//////    def convertI32F64: ConvertIntDouble[I32, F64] = implicitly
//////    def convertI64I32: ConvertLongInt[I64, I32] = implicitly
//////    def convertI64F32: ConvertLongFloat[I64, F32] = implicitly
//////    def convertI64F64: ConvertLongDouble[I64, F64] = implicitly
//////    def convertF32I32: ConvertFloatInt[F32, I32] = implicitly
//////    def convertF32I64: ConvertFloatLong[F32, I64] = implicitly
//////    def convertF32F64: ConvertFloatDouble[F32, F64] = implicitly
//////    def convertF64I32: ConvertDoubleInt[F64, I32] = implicitly
//////    def convertF64I64: ConvertDoubleLong[F64, I64] = implicitly
//////    def convertF64F32: ConvertDoubleFloat[F64, F32] = implicitly
//////
//////    given TaintFunctionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] with
//////      override def funValue(fun: FunctionInstance[Value]): FunV =
//////        untainted(funops.funValue(fun.asInstanceOf[FunctionInstance[base.Value]]))
//////      override def invokeFun(fun: TaintProduct[base.FunV], args: Seq[Nothing])(invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit =
//////        fun.unary(funV => funops.invokeFun(funV, args)((fun, args) => invoke(fun.asInstanceOf[FunctionInstance[Value]], args)))
//////
//////    implicit def topFunctionCall(args: Seq[Nothing], invoke: (FunctionInstance[Value], Seq[Nothing]) => Unit): Unit =
//////      val invokeAllFuns = module.functions.map(fun => () => invoke(fun, args))
//////      effects.joinComputationsIterable(invokeAllFuns)
//////
//////    val functionOps: FunctionOps[FunctionInstance[Value], Nothing, Unit, FunV] = implicitly
//////
//////    val phi: fix.Combinator[FixIn[Value], FixOut[Value]] =
//////      fix.contextSensitive[FrameData[Value], FixIn[Value], FixOut[Value]](frameSensitive,
//////        fix.filter(isFunOrWhile,
//////          fix.iter.topmost
//////        )
//////      )
