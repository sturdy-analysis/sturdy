package sturdy.language.wasm


import sturdy.effect.noJoin
import sturdy.effect.CMayCompute
import sturdy.effect.MayCompute
import sturdy.effect.NoJoin
import sturdy.effect.binarymemory.CMemory
import sturdy.effect.binarymemory.Serialize
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.callframe.{CMutableCallFrameInt, CCallFrameInt}
import sturdy.effect.except.CExcept
import sturdy.effect.failure.{CFailure, Failure}
import sturdy.effect.operandstack.COperandStack
import sturdy.effect.table.CTable
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.FunctionInstance
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.language.wasm.generic.GenericInterpreter.FrameData
import sturdy.language.wasm.generic.GenericInterpreter.WasmException
import sturdy.language.wasm.generic.WasmOperations
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import swam.syntax.*
import sturdy.values.doubles.{*, given}
import sturdy.values.floats.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.longs.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.unit

import java.nio.ByteBuffer
import java.nio.ByteOrder

object ConcreteInterpreter extends Interpreter :
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean

  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException

  override def asBoolean(v: Value): Boolean = v.asInt32 != 0
  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

  override type Addr = Int
  override type Bytes = ByteBuffer
  override type Size = Int

  trait CSerialize extends Serialize[Value, ByteBuffer, MemoryInst, MemoryInst] :
    import Value.*
    override def decode(dat: ByteBuffer, decInfo: MemoryInst): Value = decInfo match
      case _: i32.Load => Int32(dat.getInt(0))
      case _: i32.Load8S => Int32(dat.get(0))
      case _: i32.Load8U => Int32(dat.get(0) & 0xFF)
      case _: i32.Load16S => Int32(dat.getShort(0))
      case _: i32.Load16U => Int32(dat.getShort(0) & 0xFFFF)
      case _: i64.Load => Int64(dat.getLong(0))
      case _: i64.Load8S => Int64(dat.get(0))
      case _: i64.Load8U => Int64(dat.get(0) & 0xFFL)
      case _: i64.Load16S => Int64(dat.getShort(0))
      case _: i64.Load16U => Int64(dat.getShort(0) & 0xFFFFL)
      case _: i64.Load32S => Int64(dat.getInt(0))
      case _: i64.Load32U => Int64(dat.getInt(0) & 0xFFFFFFFFFL)
      case _: f32.Load => Float32(dat.getFloat(0))
      case _: f64.Load => Float64(dat.getDouble(0))
      case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $decInfo.")

    private def newByteBuffer(cap: Int): ByteBuffer =
      val buf = ByteBuffer.allocate(cap)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      buf

    override def encode(v: Value, encInfo: MemoryInst): ByteBuffer =
      encInfo match
        case _: i32.Store =>
          val buf = newByteBuffer(4)
          buf.putInt(0, v.asInt32)
        case _: i32.Store8 =>
          val buf = newByteBuffer(1)
          val b = (v.asInt32 % (1 << 8)).toByte
          buf.put(0, b)
        case _: i32.Store16 =>
          val buf = newByteBuffer(2)
          val s = (v.asInt32 % (1 << 16)).toShort
          buf.putShort(0, s)
        case _: i64.Store =>
          val buf = newByteBuffer(8)
          buf.putLong(0, v.asInt64)
        case _: i64.Store8 =>
          val buf = newByteBuffer(1)
          val b = (v.asInt64 % (1L << 8)).toByte
          buf.put(0, b)
        case _: i64.Store16 =>
          val buf = newByteBuffer(2)
          val s = (v.asInt64 % (1L << 16)).toShort
          buf.putShort(0, s)
        case _: i64.Store32 =>
          val buf = newByteBuffer(4)
          val i = (v.asInt64 % (1L << 32)).toInt
          buf.putInt(0, i)
        case _: f32.Store =>
          val buf = newByteBuffer(4)
          buf.putFloat(0, v.asFloat32)
        case _: f64.Store =>
          val buf = newByteBuffer(8)
          buf.putDouble(0, v.asFloat64)
        case _ => throw new IllegalArgumentException(s"Expected store instruction, but got $encInfo.")

  given WasmOperations[Value, Addr, Size] with
    override type WasmOpsJoin[A] = NoJoin[A]
    override type WasmOpsJoinComp = Unit

    override def valueToAddr(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): MayCompute[A, NoJoin, Unit] =
      val i = ix.asInt32
      if (i < vec.size)
        CMayCompute.Computes(vec(i))
      else
        CMayCompute.ComputesNot()

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends COperandStack[Value]
      with CMemory
      with CSerialize
      with CTable[Value, FunctionInstance[Value]](_.asInt32)
      with CMutableCallFrameInt[FrameData[Value], Value] with CCallFrameInt(rootFrameData, rootFrameValues)
      with CBoolBranching[Value]
      with CExcept[WasmException[Value]]
      with CFailure

  class Instance(effects: Effects)
    extends GenericInstance with GenericInterpreter(effects) :
    given Failure = effects

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
    def i32IntCompareOps: IntegerCompareOps[I32, Bool] = implicitly
    def i64IntCompareOps: IntegerCompareOps[I64, Bool] = implicitly
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

  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value]): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    new Instance(effects)
