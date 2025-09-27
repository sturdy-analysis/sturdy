package sturdy.language.bytecode.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.bytecode.Interpreter
import sturdy.values.Topped

trait Numbers extends Interpreter:
  final type I8 = Topped[Byte]
  final type I16 = Topped[Short]
  final type U16 = Topped[Char]
  final type I32 = Topped[Int]
  final type I64 = Topped[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type Bool = Topped[Boolean]

  final def topI32: I32 = Topped.Top

  final def topI64: I64 = Topped.Top

  final def topF32: F32 = Topped.Top

  final def topF64: F64 = Topped.Top

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32 match
    case Topped.Top => Topped.Top
    case Topped.Actual(i) => Topped.Actual(i != 0)

  final def boolean(b: Bool): Value = b match
    case Topped.Top => Value.Int32(topI32)
    case Topped.Actual(true) => Value.Int32(Topped.Actual(1))
    case Topped.Actual(false) => Value.Int32(Topped.Actual(0))
