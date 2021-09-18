package sturdy.language.wasm.analyses

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.Serialize
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.callframe.CCallFrameInt
import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.AFailureCollect
import sturdy.effect.failure.CFailure
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.COperandStack
import sturdy.effect.symboltable.{ToppedSymbolTable, SymbolTable}
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.abstractions.ConstantValues
import sturdy.language.wasm.generic.FunctionInstance
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.language.wasm.generic.GenericInterpreter.FrameData
import sturdy.language.wasm.generic.GenericInterpreter.WasmException
import sturdy.language.wasm.generic.WasmOperations
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import swam.syntax.*
import sturdy.values.doubles.{*, given}
import sturdy.values.exceptions.{*, given}
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
  type FunV = Topped[FunctionInstance[Value]]

  given WasmOperations[Value, Addr, Size, FuncIx] with
    override type WasmOpsJoin[A] = Join[A]

    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): OptionA[A] =
      ix.asInt32 match
        case Topped.Actual(i) =>
          if (i < vec.size)
            OptionA.Some(Iterable.single(vec(i)))
          else
            OptionA.None()
        case Topped.Top =>
          OptionA.NoneSome(vec)

  trait ASerialize extends Serialize[Value, Bytes, MemoryInst, MemoryInst]:
    override def decode(dat: IndexedSeqView[Topped[Byte]], decInfo: MemoryInst): ConstantAnalysis.Value = ???
    override def encode(v: Value, encInfo: MemoryInst): IndexedSeqView[Topped[Byte]] = ???

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends COperandStack[Value]
      with ConstantAddressMemory[Int, Topped[Byte]]
      with ASerialize
      with ToppedSymbolTable[Int, Int, FunV]
      with CMutableCallFrameInt[FrameData[Value], Value] with CCallFrameInt(rootFrameData, rootFrameValues)
      with ABoolBranching[Value]
      with JoinedExcept[WasmException[Value], ExcV]
      with AFailureCollect

//  class Instance(effects: Effects)
//    extends GenericInstance with GenericInterpreter(effects) :
//    given Failure = effects
//
//    def i32Ops: IntOps[I32] = implicitly
//    def i64Ops: LongOps[I64] = implicitly
//    def f32Ops: FloatOps[F32] = implicitly
//    def f64Ops: DoubleOps[F64] = implicitly
//    def i32EqOps: EqOps[I32, Bool] = implicitly
//    def i64EqOps: EqOps[I64, Bool] = implicitly
//    def f32EqOps: EqOps[F32, Bool] = implicitly
//    def f64EqOps: EqOps[F64, Bool] = implicitly
//    def i32CompareOps: CompareOps[I32, Bool] = implicitly
//    def i64CompareOps: CompareOps[I64, Bool] = implicitly
//    def f32CompareOps: CompareOps[F32, Bool] = implicitly
//    def f64CompareOps: CompareOps[F64, Bool] = implicitly
//    def i32IntCompareOps: IntegerCompareOps[I32, Bool] = implicitly
//    def i64IntCompareOps: IntegerCompareOps[I64, Bool] = implicitly
//    def convertI32I64: ConvertIntLong[I32, I64] = implicitly
//    def convertI32F32: ConvertIntFloat[I32, F32] = implicitly
//    def convertI32F64: ConvertIntDouble[I32, F64] = implicitly
//    def convertI64I32: ConvertLongInt[I64, I32] = implicitly
//    def convertI64F32: ConvertLongFloat[I64, F32] = implicitly
//    def convertI64F64: ConvertLongDouble[I64, F64] = implicitly
//    def convertF32I32: ConvertFloatInt[F32, I32] = implicitly
//    def convertF32I64: ConvertFloatLong[F32, I64] = implicitly
//    def convertF32F64: ConvertFloatDouble[F32, F64] = implicitly
//    def convertF64I32: ConvertDoubleInt[F64, I32] = implicitly
//    def convertF64I64: ConvertDoubleLong[F64, I64] = implicitly
//    def convertF64F32: ConvertDoubleFloat[F64, F32] = implicitly
//
//  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value]): Instance =
//    val effects = new Effects(rootFrameData, rootFrameValues)
//    new Instance(effects)
