package sturdy.language.wasm.concrete

import sturdy.effect.binarymemory.{MemSize, Serialize}
import swam.runtime.Value
import swam.syntax.*

import java.nio.ByteBuffer

trait CMemSize extends MemSize[Value,Int]:
  override def valToSize(v: Value): Int =
    v.asInt

  override def sizeToVal(s: Int): Value =
    Value.Int32(s)

trait CSerialize extends Serialize[Value, ByteBuffer, MemoryInst, MemoryInst]:
  override def decode(dat: ByteBuffer, decInfo: MemoryInst): Value =
    decInfo match
      case _: i32.Load => ???
      case _: i32.Load8S => ???
      case _: i32.Load8U => ???
      case _: i32.Load16S => ???
      case _: i32.Load16U => ???
      case _: i64.Load => ???
      case _: i64.Load8S => ???
      case _: i64.Load8U => ???
      case _: i64.Load16S => ???
      case _: i64.Load16U => ???
      case _: i64.Load32S => ???
      case _: i64.Load32U => ???
      case _: f32.Load => ???
      case _: f64.Load => ???
      case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $decInfo.")

  override def encode(v: Value, encInfo: MemoryInst): ByteBuffer =
    encInfo match
      case _: i32.Store => ???
      case _: i32.Store8 => ???
      case _: i32.Store16 => ???
      case _: i64.Store => ???
      case _: i64.Store8 => ???
      case _: i64.Store16 => ???
      case _: i64.Store32 => ???
      case _: f32.Store => ???
      case _: f64.Store => ???
      case _ => throw new IllegalArgumentException(s"Expected store instruction, but got $encInfo.")