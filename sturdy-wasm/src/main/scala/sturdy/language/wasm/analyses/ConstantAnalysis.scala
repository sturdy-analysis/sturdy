package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.Effectful
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.Serialize
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.callframe.CCallFrameInt
import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.*
import sturdy.effect.operandstack.JoinedOperandStack
import sturdy.effect.symboltable.{ToppedSymbolTable, SymbolTable}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.ConstantValues
import sturdy.language.wasm.generic.FunctionInstance
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.language.wasm.generic.GenericInterpreter.FrameData
import sturdy.language.wasm.generic.GenericInterpreter.WasmException
import sturdy.language.wasm.generic.WasmOperations
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

  given ConstantWasmOperations(using Failure): WasmOperations[Value, Addr, Size, FuncIx] with
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

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends JoinedOperandStack[Value]
      with ConstantAddressMemory[Int, Topped[Byte]](Topped.Actual(0))
      with ASerialize
      with ToppedSymbolTable[Int, Int, FunV]
      with CMutableCallFrameInt[FrameData[Value], Value] with CCallFrameInt(rootFrameData, rootFrameValues)
      with ABoolBranching[Value]
      with JoinedExcept[WasmException[Value], ExcV]
      with AFailureCollect

  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value]): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    given Effects = effects
    new Instance(effects)

  class Instance(effects: Effects)(using Failure, Effectful)
    extends GenericInstance[Effects] with GenericInterpreter(effects) :

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
