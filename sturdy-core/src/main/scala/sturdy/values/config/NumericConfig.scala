package sturdy.values.config

enum Overflow:
  case Allow
  case Fail
  case JumpToBounds

enum Bits:
  case Signed
  case Unsigned
  case Raw

enum BytesSize(val bytes: Int):
  case Byte extends BytesSize(1)
  case Short extends BytesSize(2)
  case Int extends BytesSize(4)
  case Long extends BytesSize(8)
  
  def bits: Int = this.bytes * 8
