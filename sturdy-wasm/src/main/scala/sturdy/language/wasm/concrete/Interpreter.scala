package sturdy.language.wasm.concrete

import java.nio.{ByteBuffer, ByteOrder}


object Interpreter:
  enum Value:
    case Int32(i: Int)
    case Int64(l: Long)
    case Float32(f: Float)
    case Float64(d: Double)

    def asInt32: Int = this match
      case Int32(i) => i
      case _ => throw new IllegalArgumentException(s"Expected Int32 but got $this")

    def asInt64: Long = this match
      case Int64(l) => l
      case _ => throw new IllegalArgumentException(s"Expected Int64 but got $this")

    def asFloat32: Float = this match
      case Float32(f) => f
      case _ => throw new IllegalArgumentException(s"Expected Float32 but got $this")

    def asFloat64: Double = this match
      case Float64(d) => d
      case _ => throw new IllegalArgumentException(s"Expected Float64 but got $this")

  def allocByteBuffer(size: Int): ByteBuffer =
    val buf = ByteBuffer.allocate(size)
    buf.order(ByteOrder.LITTLE_ENDIAN)
    buf
