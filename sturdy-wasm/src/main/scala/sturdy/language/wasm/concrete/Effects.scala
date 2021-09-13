package sturdy.language.wasm.concrete

import sturdy.effect.{CMayCompute, MayCompute, NoJoin}
import sturdy.effect.binarymemory.{MemSize, Serialize}
import sturdy.language.wasm.concrete.Interpreter.{Value, allocByteBuffer}
import sturdy.language.wasm.concrete.Interpreter.Value.*
import sturdy.language.wasm.generic.WasmOperations
import swam.syntax.*

import java.nio.{ByteBuffer, ByteOrder}

trait CMemSize extends MemSize[Value,Int]:
  override def valToSize(v: Value): Int =
    v.asInt32

  override def sizeToVal(s: Int): Value =
    Int32(s)

trait CSerialize extends Serialize[Value, ByteBuffer, MemoryInst, MemoryInst]:
  override def decode(dat: ByteBuffer, decInfo: MemoryInst): Value =
    decInfo match
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

  override def encode(v: Value, encInfo: MemoryInst): ByteBuffer =
    encInfo match
      case _: i32.Store =>
        val buf = allocByteBuffer(4)
        buf.putInt(0,v.asInt32)
      case _: i32.Store8 =>
        val buf = allocByteBuffer(1)
        val b = (v.asInt32 % (1 << 8)).toByte
        buf.put(0,b)
      case _: i32.Store16 =>
        val buf = allocByteBuffer(2)
        val s = (v.asInt32 % (1 << 16)).toShort
        buf.putShort(0,s)
      case _: i64.Store =>
        val buf = allocByteBuffer(8)
        buf.putLong(0,v.asInt64)
      case _: i64.Store8 =>
        val buf = allocByteBuffer(1)
        val b = (v.asInt64 % (1L << 8)).toByte
        buf.put(0,b)
      case _: i64.Store16 =>
        val buf = allocByteBuffer(2)
        val s = (v.asInt64 % (1L << 16)).toShort
        buf.putShort(0,s)
      case _: i64.Store32 =>
        val buf = allocByteBuffer(4)
        val i = (v.asInt64 % (1L << 32)).toInt
        buf.putInt(0,i)
      case _: f32.Store =>
        val buf = allocByteBuffer(4)
        buf.putFloat(0,v.asFloat32)
      case _: f64.Store =>
        val buf = allocByteBuffer(8)
        buf.putDouble(0,v.asFloat64)
      case _ => throw new IllegalArgumentException(s"Expected store instruction, but got $encInfo.")
      
trait CWasmOperations extends WasmOperations[Value]:
  override type WasmOpsJoin[A] = NoJoin[A]
  override type WasmOpsJoinComp = Unit

  override def indexLookup[A](ix: Value, list: Vector[A]): MayCompute[A, NoJoin, Unit] =
    val i = ix.asInt32
    if (i < list.size)
      CMayCompute.Computes(list(i))
    else
      CMayCompute.ComputesNot()