package sturdy.language.bytecode.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.bytecode.Interpreter
import sturdy.values.Topped
import sturdy.values.integer.NumericInterval

trait Numbers extends Interpreter:
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

trait IntervalNumbers extends Interpreter:
  final type I32 = NumericInterval[Int]
  final type I64 = NumericInterval[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type Bool = Topped[Boolean]

  final def topI32: NumericInterval[Int] = NumericInterval(Int.MinValue, Int.MaxValue)

  final def topI64: NumericInterval[Long] = NumericInterval(Long.MinValue, Long.MaxValue)

  final def topF32: F32 = Topped.Top

  final def topF64: F64 = Topped.Top

  final def asBoolean(v: Value)(using failure: Failure): Bool = v match
    case Value.Int32(i) => i.toBoolean
    case Value.TopValue => Topped.Top
    case _ => ??? // TODO: not implemented

  final def boolean(b: Bool): Value = Value.Int32(b match
    case Topped.Top => NumericInterval(0, 1)
    case Topped.Actual(true) => NumericInterval(1, 1)
    case Topped.Actual(false) => NumericInterval(0, 0)
  )
