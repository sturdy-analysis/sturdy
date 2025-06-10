package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut
import sturdy.language.wasm.generic.InstLoc
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Interpreter
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.Topped
import sturdy.values.booleans
import sturdy.values.floating
import sturdy.values.integer
import sturdy.values
import sturdy.values.types.BaseType
import swam.OpCode
import swam.syntax
import swam.syntax.LoadInst
import swam.syntax.LoadNInst
import swam.syntax.StoreInst
import swam.syntax.StoreNInst

import scala.collection.MapView

trait TypeValues extends Interpreter:
  final type I32 = BaseType[Int]
  final type I64 = BaseType[Long]
  final type F32 = BaseType[Float]
  final type F64 = BaseType[Double]
  final type Bool = BaseType[Boolean]

  final def topI32: I32 = BaseType[Int]
  final def topI64: I64 = BaseType[Long]
  final def topF32: F32 = BaseType[Float]
  final def topF64: F64 = BaseType[Double]

  final def asBoolean(v: Value)(using Failure): Bool =
    val _ = v.asInt32
    BaseType[Boolean]
  final def boolean(b: Bool): Value =
    Value.Int32(topI32)

  def liftConcreteValue(cv: ConcreteInterpreter.Value): Value = cv match
    case ConcreteInterpreter.Value.TopValue => Value.TopValue
    case ConcreteInterpreter.Value.Int32(i) => Value.Int32(topI32)
    case ConcreteInterpreter.Value.Int64(l) => Value.Int64(topI64)
    case ConcreteInterpreter.Value.Float32(f) => Value.Float32(topF32)
    case ConcreteInterpreter.Value.Float64(d) => Value.Float64(topF64)

  def recursiveCallLogger(analysis: Instance): RecursiveCallLogger[Value] =
    val recCall = new RecursiveCallLogger[Value]
    analysis.fixpoint.addContextFreeLogger(recCall)
    recCall