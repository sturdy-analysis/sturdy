package sturdy.language.bytecode.abstractions

import org.opalj.br.{ArrayType, ClassFile}
import sturdy.language.bytecode.{ConcreteInterpreter, Interpreter}
import sturdy.values.{Combine, MaybeChanged, Topped, Widening}
import sturdy.values.objects.Object
import sturdy.values.arrays.Array
import sturdy.effect.failure.Failure

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

