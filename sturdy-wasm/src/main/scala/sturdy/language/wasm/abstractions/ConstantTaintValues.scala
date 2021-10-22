package sturdy.language.wasm.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Interpreter
import sturdy.values.Finite
import sturdy.values.Topped
import sturdy.values.taint.*

trait ConstantTaintValues extends Interpreter:
  final type I32 = TaintProduct[Topped[Int]]
  final type I64 = TaintProduct[Topped[Long]]
  final type F32 = TaintProduct[Topped[Float]]
  final type F64 = TaintProduct[Topped[Double]]
  final type Bool = TaintProduct[Topped[Boolean]]

  final def topI32: I32 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topI64: I64 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topF32: F32 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topF64: F64 = TaintProduct(Taint.TopTaint, Topped.Top)

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32.map {
    case Topped.Top => Topped.Top
    case Topped.Actual(i) => Topped.Actual(i != 0)
  }
  final def boolean(b: Bool): Value = Value.Int32(b.map {
    case Topped.Top => Topped.Top
    case Topped.Actual(true) => Topped.Actual(1)
    case Topped.Actual(false) => Topped.Actual(0)
  })

  def liftConcreteValue(cv: ConcreteInterpreter.Value): Value = cv match
    case ConcreteInterpreter.Value.TopValue => Value.TopValue
    case ConcreteInterpreter.Value.Int32(i) => Value.Int32(untainted(Topped.Actual(i)))
    case ConcreteInterpreter.Value.Int64(l) => Value.Int64(untainted(Topped.Actual(l)))
    case ConcreteInterpreter.Value.Float32(f) => Value.Float32(untainted(Topped.Actual(f)))
    case ConcreteInterpreter.Value.Float64(d) => Value.Float64(untainted(Topped.Actual(d)))
