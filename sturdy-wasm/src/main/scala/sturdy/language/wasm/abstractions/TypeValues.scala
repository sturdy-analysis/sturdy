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
  final type V128 = BaseType[Array[Byte]]
  final type Bool = BaseType[Boolean]
  final type FuncReference = BaseType[Int]
  final type ExternReference = BaseType[Int]

  final def topI32: I32 = BaseType[Int]
  final def topI64: I64 = BaseType[Long]
  final def topF32: F32 = BaseType[Float]
  final def topF64: F64 = BaseType[Double]
  final def topV128: V128 = BaseType[Array[Byte]]
  final def topFuncRef: FuncReference = BaseType[Int]
  final def topExternRef: ExternReference = BaseType[Int]

  final def asBoolean(v: Value)(using Failure): Bool =
    val _ = v.asInt32
    BaseType[Boolean]
  final def boolean(b: Bool): Value =
    Value.Num(NumValue.Int32(topI32))

  def liftConcreteValue(cv: ConcreteInterpreter.Value): Value = cv match
    case ConcreteInterpreter.Value.TopValue => Value.TopValue
    case ConcreteInterpreter.Value.Num(NumValue.Int32(i)) => Value.Num(NumValue.Int32(topI32))
    case ConcreteInterpreter.Value.Num(NumValue.Int64(l)) => Value.Num(NumValue.Int64(topI64))
    case ConcreteInterpreter.Value.Num(NumValue.Float32(f)) => Value.Num(NumValue.Float32(topF32))
    case ConcreteInterpreter.Value.Num(NumValue.Float64(d)) => Value.Num(NumValue.Float64(topF64))
    //case ConcreteInterpreter.Value.FuncRef(f) => Value.FuncRef(topFuncRef)


