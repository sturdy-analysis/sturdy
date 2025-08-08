package sturdy.values.config

import sturdy.values.convert.ConvertConfig

enum Overflow extends ConvertConfig[Overflow]:
  case Allow
  case Fail
  case JumpToBounds
  def canFail: Boolean = this == Fail

enum Bits extends ConvertConfig[Bits]:
  case Signed
  case Unsigned
  case Raw
  def canFail: Boolean = false

enum BytesSize(val bytes: Int) extends ConvertConfig[BytesSize]:
  case Byte extends BytesSize(1)
  case Short extends BytesSize(2)
  case Int extends BytesSize(4)
  case Long extends BytesSize(8)
  
  def bits: Int = this.bytes * 8
  def canFail: Boolean = false

enum Padding(val zeroBytes: Int, val wrappingBytes: Int) extends ConvertConfig[Padding]:
  case ZeroShort extends Padding(2, 1)
  case ZeroInt extends Padding(4, 2)
  case ZeroLong extends Padding(8, 4)
  case Raw extends Padding(0, 0)
  def canFail: Boolean = false

object BytesSize:
  val Float: BytesSize = Int
  val Double: BytesSize = Long
